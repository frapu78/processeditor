/**
 *
 * Process Editor - inubit IS Converter
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.exporter;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.frapu.code.converter.Exporter;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.IntermediateEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.inubit.research.ISConverter.importer.ISDiagramImporter;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.domainModel.DomainModel;

/**
 * @author ff
 *
 */
public class ISBPDExporter implements Exporter {

    /**
     *
     */
    private static final String VERSION = "5.3";
    private ISDrawElementExporter f_exp = null;
    private ProcessModel f_model;

    @Override
    public String getDisplayName() {
        return "IS 5.3 diagram";
    }

    @Override
    public String[] getFileTypes() {
        return new String[]{"diagram.zip"};
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        HashSet<Class<? extends ProcessModel>> _result = new HashSet<Class<? extends ProcessModel>>();
        _result.add(BPMNModel.class);
        _result.add(DomainModel.class);
        return _result;
    }

    public Document serializeAsDocument(ProcessModel m) throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        if (m instanceof BPMNModel) {
            f_exp = new BPMNExporter();
        } else if (m instanceof DomainModel) {
            f_exp = new DomainDiagramExporter();
        } else {
            throw new Exception("Model type not supported");
        }
        f_model = m;
        Document document = documentBuilder.newDocument();

        Element _root = createStandardTags(document, m);
        writeMainDiagramAttributes(document, _root, m);

        createIDs();

        for (ProcessNode node : m.getNodes()) {
            Element el = document.createElement("WorkflowModule");
            writeWorkflowModule(node, el, document);
            _root.appendChild(el);
        }

        return document;

    }

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = serializeAsDocument(m);

        //writing XML Document to ZIP File
        TransformerFactory _transformerFactory = TransformerFactory.newInstance();
        _transformerFactory.setAttribute("indent-number", new Integer(4));
        Transformer _transformer = _transformerFactory.newTransformer();
        _transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource _source = new DOMSource(document);

        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(f));

        writeDummyModuleXML(zipOut, documentBuilder.newDocument(), _transformer);

        zipOut.putNextEntry(new ZipEntry("workflow/workflow.xml"));
        StreamResult _result = new StreamResult(new OutputStreamWriter(zipOut, "utf-8"));
        _transformer.transform(_source, _result);
        zipOut.closeEntry();
        zipOut.close();
    }

    /**
     *
     */
    private void createIDs() {
        int _moduleID = 1;
        for (ProcessNode n : f_model.getNodes()) {
            f_moduleIDMap.put(n, _moduleID++);
        }
        for (ProcessEdge n : f_model.getEdges()) {
            f_moduleIDMap.put(n, _moduleID++);
        }
    }
    private HashMap<ProcessObject, Integer> f_moduleIDMap = new HashMap<ProcessObject, Integer>();

    /**
     * @param node
     * @param el
     */
    private void writeWorkflowModule(ProcessNode node, Element el, Document doc) {

        //writing the generated Module ID
        Element _mid = doc.createElement("ModuleId");
        _mid.setTextContent("" + f_moduleIDMap.get(node));
        el.appendChild(_mid);

        //writing text of the element
        Element _mName = doc.createElement("ModuleName");
        _mName.setTextContent(node.getText());
        el.appendChild(_mName);

        //setting parent if available
        ProcessNode c = null;
        if(node instanceof IntermediateEvent) {
        	c = ((IntermediateEvent)node).getParentNode(f_model);
        }
        if(c == null) {
        	c = f_model.getClusterForNode(node);
        }
        if(c != null) {
	        Element _parentModule =  doc.createElement("ParentModule");
	        _parentModule.setAttribute("moduleId",""+f_moduleIDMap.get(c));   
	        el.appendChild(_parentModule);	
        }

        //writing Position and size
        Element _style = doc.createElement("StyleSheet");
        if (node.getProperty(ProcessNode.PROP_BACKGROUND) != null) {
            String rgb = Integer.toHexString(node.getBackground().getRGB());
            rgb = rgb.substring(2, rgb.length()) + "ff"; // add alpha value to the left
            _style.setAttribute("bgColor", rgb);
        }
        _style.setAttribute("xPos", "" + node.getTopLeftPos().x);
        _style.setAttribute("yPos", "" + node.getTopLeftPos().y);
        _style.setAttribute("width", "" + node.getSize().width);
        _style.setAttribute("height", "" + node.getSize().height);
        el.appendChild(_style);

        Element _propertyElement = null;
        if (f_exp.hasProperties(node)) {
            _propertyElement = createPropertiesBlock(el, doc, node);
        }
        List<ProcessNode> _succs = f_model.getSuccessors(node);
        for (ProcessNode s : _succs) {
            writeConnection(el, s, findEdge(node, s), doc);
        }

        // Setting Module type and specific properties
        String key = "moduleType";
        String value = f_exp.writeProperties(_propertyElement, node, doc);
        
        // comment for module
        // used to reference an existing maskflow id (cab)
        Element _comment = doc.createElement("Comment"); 
        if (node.getProperty(Task.PROP_IMPLEMENTATION) != null) {
            _comment.setTextContent(node.getProperty(Task.PROP_IMPLEMENTATION));
            el.appendChild(_comment);
        }
        
        if (value != null) {
            el.setAttribute(key, value);
        } else { // Do nothing for unknown nodes
            System.out.println("cannot export " + node + "(" + node.getClass().getSimpleName() + ")");
        }
    }

    /**
     *  <Connection moduleOutId="16201509" type="SequenceFlow">
    <ConnectionName>bis 500 km</ConnectionName>

    <ConnectionNames version="4.1"/>
    <Comment>&lt;p style="margin-top: 0"&gt; &lt;/p&gt;</Comment>
    <Comments version="4.1"/>

    <ConnectionId>16201513</ConnectionId>
    <Properties version="4.1">
    <Property name="ModuleSpecific" type="Map">
    <Property name="ConditionType">Expression</Property>
    </Property>
    </Properties>

    <StyleSheet labelPosition="30">
    <Junctures>
    <Point xPos="190" yPos="550"/>
    <Point xPos="320" yPos="550"/>
    <Point xPos="320" yPos="630"/>
    <Point xPos="340" yPos="630"/>
    </Junctures>
    </StyleSheet>
    </Connection>
     * @param el
     * @param s
     * @param findEdge
     */
    private void writeConnection(Element el, ProcessNode s, ProcessEdge edge, Document doc) {
        Element _conn = doc.createElement("Connection");
        if (s instanceof EdgeDocker) {
            EdgeDocker _ed = (EdgeDocker) s;
            _conn.setAttribute("moduleOutId", "" + f_moduleIDMap.get(_ed.getDockedEdge()));
        } else {
            _conn.setAttribute("moduleOutId", "" + f_moduleIDMap.get(s));
        }
        f_exp.setConnectionType(_conn, edge);

        Element _el = doc.createElement("ConnectionName");
        _el.setTextContent(edge.getLabel());
        _conn.appendChild(_el);

        _el = doc.createElement("ConnectionId");
        _el.setTextContent("" + f_moduleIDMap.get(edge));
        _conn.appendChild(_el);

        //Properties
        if (f_exp.hasProperties(edge)) {
            Element _props = createPropertiesBlock(_conn, doc, edge);
            f_exp.writeProperties(_props, edge, doc);
        }
        //Stylesheet
        Element _stylesheet = doc.createElement("StyleSheet");
        if (edge.getRoutingPoints().size() > 2) {
            _stylesheet.setAttribute("labelPosition", "50");
            Element _junc = doc.createElement("Junctures");
            for (Point p : edge.getRoutingPoints()) {
                createPoint(_junc, p, doc);
            }
            _stylesheet.appendChild(_junc);
        }
        _conn.appendChild(_stylesheet);
        el.appendChild(_conn);
    }

    /**
     * @param _junc
     * @param p
     * @param doc
     */
    private void createPoint(Element addTo, Point p, Document doc) {
        Element _point = doc.createElement("Point");
        _point.setAttribute("xPos", "" + p.x);
        _point.setAttribute("yPos", "" + p.y);
        addTo.appendChild(_point);
    }

    /**
     * @param node
     * @param s
     * @return
     */
    private ProcessEdge findEdge(ProcessNode node, ProcessNode s) {
        for (ProcessEdge e : f_model.getEdges()) {
            if (e.getSource().equals(node) && e.getTarget().equals(s)) {
                return e;
            }
        }
        return null;
    }

    /**
     * creates an empty property block, like this one
     *
    <Properties version="4.1">
    <Property name="ModuleSpecific" type="Map">

    </Property>
    </Properties>
     * @param el
     * @param doc
     */
    private Element createPropertiesBlock(Element el, Document doc, ProcessObject node) {
        Element _main = doc.createElement("Properties");
        _main.setAttribute("version", "4.1");
        Element _p = doc.createElement("Property");
        _p.setAttribute("name", f_exp.getPropertyBlockSubElementName(node));
        _p.setAttribute("type", "Map");
        _main.appendChild(_p);
        el.appendChild(_main);
        return _p;
    }

    /**
     *  <?xml version="1.0" encoding="UTF-8"?>
    <IBISWorkflow version="5.1">
    <Modules>
    <ModuleGroup>
    <ModuleGroupName>Data Converter</ModuleGroupName>
    </ModuleGroup>
    </Modules>
    </IBISWorkflow>
     * @param zipOut
     * @param transformer
     * @param document2
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    private void writeDummyModuleXML(ZipOutputStream zipOut, Document doc, Transformer transformer) throws ParserConfigurationException, TransformerException, IOException {
        zipOut.putNextEntry(new ZipEntry("module/module.xml"));

        Element root = doc.createElement("IBISWorkflow");
        root.setAttribute("version", VERSION);
        doc.appendChild(root);

        Element el = doc.createElement("Modules");
        root.appendChild(el);
        root = el;

        el = doc.createElement("ModuleGroup");
        root.appendChild(el);
        root = el;

        el = doc.createElement("ModuleGroupName");
        el.setTextContent("Data Converter");
        root.appendChild(el);
        root = el;

        //writing to entry
        DOMSource _source = new DOMSource(doc);
        StreamResult _result = new StreamResult(zipOut);
        transformer.transform(_source, _result);
        zipOut.closeEntry();
    }

    /**
     * <WorkflowName>myName</WorkflowName>
    <WorkflowUId>52203b56:12304361a11:-7ff4</WorkflowUId>
    <Comment>&lt;p style="margin-top: 0"&gt; &lt;/p&gt;</Comment>
    <CheckinComment>@@@Server: bigfoot.intra.inubit.com@@@Version: 4@@@27.08.2009 08:57:58@@@</CheckinComment>
    <IsActive>true</IsActive>
    <XPathVersion>1.0</XPathVersion>
     * @param document
     * @param _root
     * @param m
     */
    private void writeMainDiagramAttributes(Document document, Element root, ProcessModel m) {
        Element el = document.createElement("WorkflowName");
        if (m.getProcessName() != null && m.getProcessName().length() > 0) {
            el.setTextContent(m.getProcessName());
        } else {
            el.setTextContent(m.getProcessModelURI());
        }
        root.appendChild(el);

        el = document.createElement("WorkflowUId");
        el.setTextContent("" + UUID.randomUUID().toString());
        root.appendChild(el);

        el = document.createElement("Comment");
        el.setTextContent("&lt;p style=\"margin-top: 0\"&gt;" + m.getProperty(ProcessModel.PROP_COMMENT) + "&lt;/p&gt;");
        root.appendChild(el);

        el = document.createElement("CheckinComment");
        if (m.getProperty(ProcessModel.PROP_COMMENT) != null && m.getProperty(ProcessModel.PROP_COMMENT).length() > 0) {
            el.setTextContent(m.getProperty(ProcessModel.PROP_COMMENT));
        } else {
            el.setTextContent("Exported via Inubit Workbench");
        }
        root.appendChild(el);

        el = document.createElement("IsActive");
        el.setTextContent("true");
        root.appendChild(el);

        el = document.createElement("XPathVersion");
        el.setTextContent("1.0");
        root.appendChild(el);

        // Write metadata here (i.e. all model properties)
        el = document.createElement("Metadata");
        el.setAttribute("version", "4.1");
        root.appendChild(el);

        for (String key : m.getPropertyKeys()) {
            if (key.startsWith("#")) {
                continue;
            }
            String prop = m.getProperty(key);
            Element propEl = document.createElement("Property");
            propEl.setAttribute("name", key);
            propEl.setTextContent(prop);
            el.appendChild(propEl);
        }
    }

    /**
     * creates the standard structure required for every IS workflow xml.
     * It will look like this:
    <IBISWorkflow version="5.2">
    <Workflows>
    <WorkflowGroup>
    <WorkflowGroupName>ProcessEditorExport</WorkflowGroupName>
    <Workflow version="head" workflowType="bpd">
    ....
     * @param document
     * @return
     */
    private Element createStandardTags(Document document, ProcessModel m) {
        Element node = document.createElement("IBISWorkflow");
        node.setAttribute("version", VERSION);
        document.appendChild(node);

        Element node2 = document.createElement("Workflows");
        node.appendChild(node2);
        Element node3 = document.createElement("WorkflowGroup");
        node2.appendChild(node3);

        Element node4 = document.createElement("WorkflowGroupName");
        if (m.getProperty(ISDiagramImporter.PROP_GROUP) != null && m.getProperty(ISDiagramImporter.PROP_GROUP).trim().length() > 0) {
            node4.setTextContent(m.getProperty(ISDiagramImporter.PROP_GROUP));
        } else {
            node4.setTextContent("ProcessEditorExport");
        }
        node3.appendChild(node4);
        Element node5 = document.createElement("Workflow");
        node5.setAttribute("version", "head");
        node5.setAttribute("workflowType", f_exp.getWorkflowType());
        node3.appendChild(node5);

        return node5;
    }
}
