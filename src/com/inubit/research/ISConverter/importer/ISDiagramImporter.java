/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.converter.Importer;
import net.frapu.code.converter.UnsupportedFileTypeException;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.domainModel.DomainModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class converts an IS 5.1 Diagram into a ProcessModel.
 * 
 * @author fpu
 */
public class ISDiagramImporter implements Importer {

    private Map<String, ProcessNode> id2FlowObjects = new HashMap<String, ProcessNode>();
    private Map<String, ProcessEdge> id2ProcessEdge = new HashMap<String, ProcessEdge>();
    private List<EdgeHolder> workflowConnections = new LinkedList<EdgeHolder>();
    private List<ProcessModel> models;
    //public static final String DEFAULT_WORKFLOW_XPATH = "/IBISWorkflow/Workflows/WorkflowGroup/Workflow";
    // Hack by fpu to allow import of single workflow instances
    public static final String DEFAULT_WORKFLOW_XPATH = "//Workflow";
    public final static String SEMANTICANNOTATION_MODEL_PROPERTY = "SemanticAnnotations_Model";
    public final static String SEMANTICANNOTATION_PROPERTY = "SemanticAnnotation";
    public final static String PROP_GROUP = "is_group";
    private ISDrawElementExtactor f_ext;

    /**
     * This constructor requires user interaction to select the file!!!
     * @throws java.lang.Exception
     */
    public ISDiagramImporter() {
        // Create new BPMNModel
        models = new ArrayList<ProcessModel>();
    }

    /**
     * Converts an IS 5.1 BPD workflow.
     * @param workflow
     */
    public ISDiagramImporter(Document document) throws Exception {
        this();
        scanDocument(document);
    }

    /**
     * parses directly from the "Workflow" element
     * @throws Exception 
     * @throws java.lang.Exception
     */
    public ISDiagramImporter(Element workflowElement) throws Exception {
        this();
        models.add(this.convert(workflowElement));
    }

    /**
     * parses directly from the given Element, but you have to decide if it is the "Workflow"
     * Element or the document root
     * @throws Exception 
     * @throws java.lang.Exception
     */
    public ISDiagramImporter(Element xmlNode, boolean isDocRoot) throws Exception {
        this();
        if (isDocRoot) {
            scanDocument(xmlNode);
        } else {
            models.add(this.convert(xmlNode));
        }
    }

    public List<ProcessModel> getModels() {
        return models;
    }

    /**
     * Opens a model from the file system. This methods shows a file chooser
     * for the user!
     * 
     * @throws java.lang.Exception
     */
    public void openModel(File file) throws Exception {
        // Get file name
        if (file == null) {
            return;
        }
        boolean importOk = false;

        try {
            // Open zipfile and find workflow.xml
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = null;
            // Search all entries until workflow.xml is found
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().equals("workflow/workflow.xml")) {
                    // This is our entry
                    DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
                    xmlFactory.setNamespaceAware(true); // never forget this!
                    DocumentBuilder builder = xmlFactory.newDocumentBuilder();
                    Document d = builder.parse(zipIn);
                    scanDocument(d);
                    importOk = true;
                    break;
                }
            }
            zipIn.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
      if (!importOk) {
            // Try to parse directly (added by fpu)
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
            Document d = builder.parse(file);
            scanDocument(d);
            importOk = true;
        }

        if (!importOk) {
            throw new UnsupportedFileTypeException("Workflow.xml not found!");
        }
    }

    private void scanDocument(Object d) throws Exception {
        //for each workflow
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        Object res = xpath.evaluate(DEFAULT_WORKFLOW_XPATH, d, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;
        if (nodes.getLength()==0) throw new UnsupportedFileTypeException("Workflow.xml not found!");
        for (int i = 0; i < nodes.getLength(); i++) {
            models.add(convert((Element) nodes.item(i)));
        }

    }

    private ProcessModel convert(Element workflow) throws Exception {

        //clearing all lists
        id2FlowObjects.clear();
        id2ProcessEdge.clear();
        workflowConnections.clear();
        EdgeHolder.getEdgeDockers().clear();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        String _wfType = workflow.getAttribute("workflowType");
        if (_wfType.equals("bpd")) {
            f_ext = new BPMNExtractor();
        } else if (_wfType.equals("organigram")) {
            f_ext = new OrgChartExtractor();
        } else if (_wfType.equals("constraintsdiagram")) {
            f_ext = new DomainModelDiagrammExtractor();
        } else {
            System.out.println("Workflow type \"" + _wfType + "\" is not supported!");
            return null;
        }

        ProcessModel model = f_ext.getEmptyModel();

        // Get workflow name
        String query = "current()/WorkflowName";
        model.setProcessName(getNodeContent(workflow, xpath, query));
        // Get workflow group
        query = "current()/../WorkflowGroupName";
        model.setProperty(PROP_GROUP, getNodeContent(workflow, xpath, query));
        // Get last comment
        query = "current()/CheckinComment";
        model.setProperty(ProcessModel.PROP_COMMENT, getNodeContent(workflow, xpath, query));
        // Get checking time
        query = "current()/LastCheckinDateTime";
        model.setProperty(ProcessModel.PROP_LASTCHECKIN, getNodeContent(workflow, xpath, query));

        if ( model instanceof DomainModel ) {
            query = "current()/Metadata/Property[@name='Namespace-URI']";
            model.setProperty( DomainModel.PROP_NAMESPACE_URI, getNodeContent(workflow, xpath, query) );
            query = "current()/Metadata/Property[@name='Namespace-Prefix']";
            model.setProperty( DomainModel.PROP_NAMESPACE_PREFIX, getNodeContent(workflow, xpath, query) );
        }

        // Get nodes
        query = "current()/WorkflowModule";
        NodeList nodes = (NodeList) xpath.evaluate(query, workflow, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element node = (Element) nodes.item(i);

                String moduleId = null;

                ProcessNode pNode = f_ext.extractNode(node);
                NodeList nodelist;
                if (pNode == null) {
                    System.out.println("Unknown Module: " + node.getAttribute("moduleType"));
                    // Do nothing for unknown nodes
                } else {
                    // Get module name
                    nodelist = node.getElementsByTagName("ModuleName");
                    for (int i2 = 0; i2 < nodelist.getLength(); i2++) {
                        if (nodelist.item(i2) instanceof Element) {
                            Element node2 = (Element) nodelist.item(i2);
                            String text = node2.getTextContent();
                            if (text != null) {
                                pNode.setText(text);
                            }
                        }
                    }
                    // Get module id
                    nodelist = node.getElementsByTagName("ModuleId");
                    for (int i2 = 0; i2 < nodelist.getLength(); i2++) {
                        if (nodelist.item(i2) instanceof Element) {
                            Element node2 = (Element) nodelist.item(i2);
                            moduleId = node2.getTextContent();
                            id2FlowObjects.put(moduleId, pNode);
                        }
                    }

                    pNode.setProperty(ProcessNode.PROP_MODULEID, moduleId);

                    // Get positions
                    nodelist = node.getElementsByTagName("StyleSheet");
                    if (nodelist.item(0) instanceof Element) {
                        Element node2 = (Element) nodelist.item(0);
                        //get bgColor
                        String _bgColor = node2.getAttribute("bgColor");
                        if (_bgColor != null && _bgColor.length() > 0) {
                            //somehow the Alpha channel is in the end -> ignore
                            Color _c = new Color(Integer.parseInt(_bgColor.substring(0, 6), 16));
                            pNode.setBackground(_c);
                        }
                        // Get x/y pos
                        try {
                            int xPos = Integer.parseInt(node2.getAttribute("xPos"));
                            int yPos = Integer.parseInt(node2.getAttribute("yPos"));
                            int width = 0, height = 0;
                            String value = node2.getAttribute("width");
                            if (value.length() > 0) {
                                width = Integer.parseInt(value);
                            }
                            value = node2.getAttribute("height");
                            if (value.length() > 0) {
                                height = Integer.parseInt(value);
                            }
                            if (width > 0) {
                                pNode.setProperty(ProcessNode.PROP_WIDTH, "" + width);
                                pNode.setProperty(ProcessNode.PROP_HEIGHT, "" + height);
                            } else {
                                f_ext.setDefaultSize(pNode);
                            }
                            // Translate position to center
                            xPos += pNode.getSize().width / 2;
                            yPos += pNode.getSize().height / 2;
                            pNode.setProperty(ProcessNode.PROP_XPOS, "" + xPos);
                            pNode.setProperty(ProcessNode.PROP_YPOS, "" + yPos);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    //setting parent if available
                    Element _parentModule = (Element) node.getElementsByTagName("ParentModule").item(0);
                    if (_parentModule != null) {
                        pNode.setProperty("ParentModule", (_parentModule.getAttribute("moduleId")));
                    }
                    f_ext.extractStyleSheet(node, pNode);
                    // Get workflowSequenceFlows
                    nodelist = node.getElementsByTagName("Connection");
                    for (int i2 = 0; i2 < nodelist.getLength(); i2++) {
                        if (nodelist.item(i2) instanceof Element) {
                            Element connectionNode = (Element) nodelist.item(i2);
                            String connType = connectionNode.getAttribute("type");
                            String targetId = connectionNode.getAttribute("moduleOutId");
                            // Save source and target id
                            EdgeHolder _eh = new EdgeHolder(moduleId, targetId, connType);
                            String connectionID = connectionNode.getElementsByTagName("ConnectionId").item(0).getTextContent();
                            _eh.setConnectionID(connectionID);
                            //getting label e.g. <ConnectionName>Über 500 km</ConnectionName>
                            Element label = (Element) connectionNode.getElementsByTagName("ConnectionName").item(0);
                            if (label != null) {
                                _eh.setLabel(label.getTextContent());
                            }
                            f_ext.extractEdgeProperties(connectionNode, _eh);
                            //extracting routing points
                            Element stylehsheet = (Element) connectionNode.getElementsByTagName("StyleSheet").item(0);
                            if (stylehsheet != null) {
                                Element juncture = (Element) stylehsheet.getElementsByTagName("Junctures").item(0);
                                if (juncture != null) {
                                    NodeList junctures = juncture.getElementsByTagName("Point");
                                    //ignore start and end point as they will be set automatically
                                    for (int junc = 0; junc < junctures.getLength(); junc++) {
                                        Node point = junctures.item(junc);
                                        int x = Integer.parseInt(point.getAttributes().getNamedItem("xPos").getNodeValue());
                                        int y = Integer.parseInt(point.getAttributes().getNamedItem("yPos").getNodeValue());
                                        if (junc == 0) {
                                            _eh.setStart(new Point(x, y));
                                        } else if (junc == junctures.getLength() - 1) {
                                            _eh.setEnd(new Point(x, y));
                                        } else {
                                            _eh.addJuncture(x, y);
                                        }
                                    }
                                }
                            }
                            workflowConnections.add(_eh);
                        }
                    }

                    //reading metaData in case any SemanticAnnotations are present
                    String _ann = getFlatProperty(node, SEMANTICANNOTATION_PROPERTY);
                    if (_ann != null && !_ann.isEmpty()) {
                        pNode.setProperty(ProcessNode.PROP_ANNOTATION, _ann);
                    }
                    //maybe the model annotations were saved in this node too?
                    _ann = getFlatProperty(node, SEMANTICANNOTATION_MODEL_PROPERTY);
                    if (_ann != null && !_ann.isEmpty()) {
                        model.setProperty(ProcessNode.PROP_ANNOTATION, _ann);
                    }

                    // Add pNode to model
                    model.addNode(pNode);
                }
            }
        }

        // Create all sequence flows later, so all Model elements were parsed beforehand
        for (EdgeHolder f : workflowConnections) {
            ProcessEdge p = f.toSequenceFlow(this, f_ext);
            id2ProcessEdge.put(f.getConnectionID(), p);
            if(p.getRoutingPoints().size() == 2) { //no routing points defined -> use layouter
            	model.getUtils().getRoutingPointLayouter().optimizeRoutingPoints(p, p.getSource());            	
            }
            model.addEdge(p);
        }
        //now that all processedges are present, edgeDockers can be connected to them
        for (EdgeDocker e : EdgeHolder.getEdgeDockers()) {
            String _target = e.getProperty(EdgeHolder.PROP_EDGEDOCKERTARGET);
            e.removeProperty(EdgeHolder.PROP_EDGEDOCKERTARGET);
            ProcessEdge _edge = id2ProcessEdge.get(_target);
            e.setDockedEdge(_edge);
            model.addNode(e);
        }

        //changes position within the list so nothing overlaps
        ProcessUtils.sortTopologically(model);
        for (ProcessNode p : new ArrayList<ProcessNode>(model.getNodes())) {
            //setting containment relationship if available
            if (p.getProperty("ParentModule") != null) {
                ProcessNode _pn = id2FlowObjects.get(p.getProperty("ParentModule"));
                if (_pn instanceof Cluster) {
                    Cluster c = (Cluster) _pn;
                    if (c != null) {
                        c.addProcessNode(p);
                    }
                } else {
                    f_ext.setParentChildRelationship(p, _pn);
                    model.moveToFront(p);                    
                }
                //so this unnecessary information is not saved in the .model file
                p.removeProperty("ParentModule");
            }
        }
        f_ext.postProcessing(model);
        return model;
    }

    /**
     * gets the text content of the first node found by the given xPath query
     * @param workflow
     * @param xpath
     * @param query
     * @return
     * @throws XPathExpressionException
     */
    private String getNodeContent(Element workflow, XPath xpath, String query) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate(query, workflow, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    /**
     * Something like this right under the Module Element
    <Properties version="4.1">
    <Property name="SemanticAnnotation">http://www.inubit.com#abcdefg</Property>
    </Properties>

     * if the metadata value cannot be found null is returned
     * @param node
     * @param string
     * @return
     */
    private String getFlatProperty(Element node, String xmlName) {
        NodeList nodelist;
        Element node2 = (Element) node.getElementsByTagName("Properties").item(0);
        if (node2 != null) {
            nodelist = node2.getElementsByTagName("Property");
            for (int j = 0; j < nodelist.getLength(); j++) {
                if (nodelist.item(j) instanceof Element) {
                    Element e = (Element) nodelist.item(j);
                    if (e.getAttribute("name").equals(xmlName)) {
                        String prop = e.getTextContent();
                        return prop;
                    }
                }
            }
        }
        return null;
    }

    public ProcessNode getFlowObject(String id) {
        return id2FlowObjects.get(id);
    }

    @Override
    public String getDisplayName() {
        return "IS 5.3 diagrams (BPD,Org,Res)";
    }

    @Override
    public String[] getFileTypes() {
        return new String[]{"zip"};
    }

    @Override
    public List<ProcessModel> parseSource(File f) throws Exception {
        openModel(f);
        return models;
    }
}
