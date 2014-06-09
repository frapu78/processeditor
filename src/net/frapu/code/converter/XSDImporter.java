/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.uml.Aggregation;
import net.frapu.code.visualization.uml.Association;
import net.frapu.code.visualization.uml.ClassModel;
import net.frapu.code.visualization.uml.Inheritance;
import net.frapu.code.visualization.uml.UMLClass;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.inubit.research.layouter.WorkBenchSpecific.WorkbenchHandler;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.util.ElementUtil;

/**
 *
 * @author frank
 */
public class XSDImporter implements Importer {

    public final static String PROP_XPATH = "xpath";

    public final static String STEREOTYPE_ELEMENT = "Element";
    public final static String STEREOTYPE_SIMPLETYPE = "SimpleType";
    public final static String STEREOTYPE_COMPLEXTYPE = "ComplexType";
    public final static String STEREOTYPE_GROUP = "Group";
    public final static String STEREOTYPE_EXTERNAL = "External";
    public final static String STEREOTYPE_ENUMERATION = "Enumeration";
    public final static String STEREOTYPE_CHOICE = "Choice";

    public final static String TEXT_DUMMY = "DUMMY_";

    public final static String CONF_SHOW_SIMPLE_CLASSES = "xsd_show_simple_classes";
    public final static String CONF_SHOW_ENUMERATIONS = "xsd_show_enumerations";
    public final static String CONF_SHOW_EXTERNAL_BASES_TYPES = "xsd_show_external_base_types";
    public final static String CONF_COLOR_CODE_STEREOTYPES = "xsd_color_code_stereotypes";

    public final static String ATTR_XMLNS = "xmlns";
    public final static String ATTR_BASE = "base";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_REF = "ref";
    public final static String ATTR_VALUE = "value";
    public final static String ATTR_MINOCCURS = "minOccurs";
    public final static String ATTR_MAXOCCURS = "maxOccurs";
    public final static String ATTR_ABSTRACT = "abstract";
    public final static String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();

    Configuration config = Configuration.getInstance();

    private int dummyCounter = 0;
    private boolean debug = false;

     @Override
    public List<ProcessModel> parseSource(File f) throws Exception {
        return parseSource(f.toURI());
    }

    public List<ProcessModel> parseSource(URI uri) throws Exception {

        //FileInputStream in = new FileInputStream(f);
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(true);
        xpath.setNamespaceContext(new XSDImporter.MyNamespaceContext());
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        InputStream in = uri.toURL().openStream();
        Document xmlDoc = builder.parse(in);

        /** Map of externalId, ProcessObject */
        Map<String, ProcessObject> idMap = new HashMap<String, ProcessObject>();
        //List<ProcessModel> result = new LinkedList<ProcessModel>();

        // Check if XML-Schema
        String query = "/xsd:schema";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList rootNodes = (NodeList) res;
        if (rootNodes.getLength() != 1) {
            throw new UnsupportedFileTypeException("No XML-Schema file");
        }

        // Create Model
        ClassModel model = new ClassModel();
        model.setProperty(ClassModel.PROP_SOURCE, uri.toASCIIString());
        List<ProcessModel> modelList = new LinkedList<ProcessModel>();
        modelList.add(model);

        // Collect all contained top-level Elements and show them as classes
        query = "/xsd:schema/xsd:element";
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList elementNodes = (NodeList) res;
        if(debug)
        	System.out.println("Root Element Count: " + elementNodes.getLength());
        for (int elementCounter = 0; elementCounter < elementNodes.getLength(); elementCounter++) {
            Element elementNode = (Element) elementNodes.item(elementCounter);
            String name = elementNode.getAttribute("name");

            UMLClass umlClass = new UMLClass();
            umlClass.setSize(150, 100);
            umlClass.setText(name);
            umlClass.setStereotype(STEREOTYPE_ELEMENT);
            umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(elementNode));
            model.addNode(umlClass);
            idMap.put(name, umlClass);
        }

        // Collect all contained Groups and show them as classes
        query = "//xsd:group";
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList groupNodes = (NodeList) res;
        if(debug)
        	System.out.println("Group Count: " + groupNodes.getLength());
        for (int groupCounter = 0; groupCounter < groupNodes.getLength(); groupCounter++) {
            Element groupNode = (Element) groupNodes.item(groupCounter);
            String name = groupNode.getAttribute(ATTR_NAME);
            if (name.isEmpty()) {
                continue; // Can not be processed yet (inline definition)
            }
            UMLClass umlClass = new UMLClass();
            umlClass.setSize(150, 100);
            umlClass.setText(name);
            umlClass.setStereotype(STEREOTYPE_GROUP);
            umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(groupNode));
            //node has to be added too...
            model.addNode(umlClass);
            idMap.put(name, umlClass);
        }

        // Collect all contained SimpleTypes and show them as classes
        if (config.getProperty(CONF_SHOW_SIMPLE_CLASSES,"1").equals("1")) {
            query = "//xsd:simpleType";
            res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
            NodeList simpleTypeNodes = (NodeList) res;
            if(debug)
            	System.out.println("SimpleType Count: "+simpleTypeNodes.getLength());
            for (int simpleTypeCounter = 0; simpleTypeCounter < simpleTypeNodes.getLength(); simpleTypeCounter++) {
                Element simpleTypeElement = (Element)simpleTypeNodes.item(simpleTypeCounter);
                String name = simpleTypeElement.getAttribute(ATTR_NAME);
                if (name.isEmpty()) {
                    continue; // Can not be processed yet (inline definition)
                }
                UMLClass umlClass = new UMLClass();
                umlClass.setSize(150, 100);
                umlClass.setText(name);
                umlClass.setStereotype(STEREOTYPE_SIMPLETYPE);
                umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(simpleTypeElement));
                model.addNode(umlClass);
                idMap.put(name, umlClass);
            }
        }

        // Collect all contained ComplexTypes and show them as classes
        query = "//xsd:complexType";
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList complexTypeNodes = (NodeList) res;
        if(debug)
        	System.out.println("ComplexType Count: " + complexTypeNodes.getLength());
        for (int complexTypecounter = 0; complexTypecounter < complexTypeNodes.getLength(); complexTypecounter++) {
            Element complexTypeNode = (Element) complexTypeNodes.item(complexTypecounter);
            String name = complexTypeNode.getAttribute(ATTR_NAME);
            if (name.isEmpty()) {
                continue; // Can not be processed yet (inline definition)
            }
            // Get abstract attribute
            String isAbstract = complexTypeNode.getAttribute(ATTR_ABSTRACT);
            if (isAbstract == null) {
                isAbstract = "false";
            }

            UMLClass umlClass = new UMLClass();
            umlClass.setSize(150, 100);
            umlClass.setText(name);
            umlClass.setStereotype(STEREOTYPE_COMPLEXTYPE);
            if (isAbstract.equals("true")) {
                umlClass.setProperty(UMLClass.PROP_ABSTRACT, UMLClass.TRUE);
            }
            umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(complexTypeNode));
            model.addNode(umlClass);
            idMap.put(name, umlClass);
        }

        processElements(rootNodes.item(0), idMap, model);
        processSimpleTypes(rootNodes.item(0), idMap, model);
        processComplexTypes(rootNodes.item(0), idMap, model);
        processGroupNodeTypes(rootNodes.item(0), idMap, model, null);
                
        // Optimize size of all nodes
        for (ProcessNode node : model.getNodes()) {
            if (node instanceof UMLClass) {
                UMLClass umlClass = (UMLClass) node;
                umlClass.pack();
            }
        }
        // Color code classes if required
        if (config.getProperty(CONF_COLOR_CODE_STEREOTYPES).equals("1")) {
             for (ProcessNode node : model.getNodes()) {
                if (node.getStereotype().equals(STEREOTYPE_ELEMENT)) node.setBackground(new Color(204,204,255));
                if (node.getStereotype().equals(STEREOTYPE_COMPLEXTYPE)) node.setBackground(new Color(255,255,204));
                if (node.getStereotype().equals(STEREOTYPE_SIMPLETYPE)) node.setBackground(new Color(236,250,214));
                if (node.getStereotype().equals(STEREOTYPE_CHOICE)) node.setBackground(new Color(252,226,187));
                if (node.getStereotype().equals(STEREOTYPE_ENUMERATION)) node.setBackground(new Color(254,230,230));
            }
        }

        // Add source to property
        StringWriter sw = new StringWriter();
        Document doc = (xmlDoc);
        ProcessEditorServerUtils.writeXMLtoStream(sw,doc);
        sw.close();
        String docStr = sw.toString();
        model.setProperty(ClassModel.PROP_DATA, docStr);
        
        // Layout model with UMLLayouter
        System.out.println("Layouting model...");
        SugiyamaLayoutAlgorithm layouter = new SugiyamaLayoutAlgorithm(Configuration.getProperties());
        layouter.layoutModel(LayoutUtils.getAdapter(model), 50, 100, 0);
        WorkbenchHandler.postProcess(layouter, model);

        // Add name
        model.setProcessName("Imported Schema");

        return modelList;
    }

    private boolean createNodeFromElement(Element el, Map<String, ProcessObject> idMap, ProcessObject currentNode, ProcessModel model)
            throws Exception {
        String name = el.getAttribute(ATTR_NAME);
        String type = el.getAttribute(ATTR_TYPE);
        String ref = el.getAttribute(ATTR_REF);
        String minOccurs = el.getAttribute(ATTR_MINOCCURS);
        String maxOccurs = el.getAttribute(ATTR_MAXOCCURS);
        if (type.isEmpty() && ref.isEmpty()) {
            // Look up if complex type is defined inside
            type = processComplexTypes(el, idMap, model);
            // Look up internal simple type
            if (type==null) type = processSimpleTypes(el, idMap, model);
            if(debug)
            	System.out.println(currentNode+"-->"+type);
            if (type==null) {
                return false;
            }
        }
        if (type.isEmpty() && !ref.isEmpty()) {
            type = ref;
        }
        if (minOccurs.isEmpty()) {
            minOccurs = "1";
        }
        if (maxOccurs.isEmpty()) {
            maxOccurs = "1";
        }
        if (maxOccurs.equals("unbounded")) {
            maxOccurs = "n";
        }
        String occurrence = "";
        if (!minOccurs.equals("1") || !maxOccurs.equals("1")) {
            occurrence = " [" + minOccurs + ".." + maxOccurs + "] ";
        }
        // Hack for ns
        String type_hack = type.substring(type.indexOf(":") + 1, type.length());
        // Get ProcessObject
        ProcessObject targetObj = idMap.get(type_hack);
        if(debug)
        	System.out.println("ASS to " + type_hack + " : " + targetObj);
        if (!(currentNode instanceof UMLClass)) {
            return false;
        }
        UMLClass sourceNode = (UMLClass) currentNode;
        if (targetObj == null) {
            // Create Attribute
            if (sourceNode.getStereotype().equals("Element")) {
                name = "type";
            }
            sourceNode.addAttribute(name + occurrence + " : " + type);
        } else {
            // Create Association
            UMLClass targetNode = (UMLClass) targetObj;
            if (sourceNode.getStereotype().equals("Element")) {
                Association ass = new Association();
                ass.setSource(sourceNode);
                ass.setTarget(targetNode);
                ass.setLabel("<<type>>");
                ass.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_TARGET);
                model.addEdge(ass);
            } else {
                if (!ref.isEmpty()) name = "<<ref>>";
                Aggregation aggr = new Aggregation();
                aggr.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
                aggr.setSource(sourceNode);
                aggr.setTarget(targetNode);
                aggr.setLabel(name+occurrence);
                model.addEdge(aggr);
            }
        }
        return true;
    }

    private void createNodesFromNodeList(NodeList elementeNodes, Map<String, ProcessObject> idMap, ProcessObject currentNode, ProcessModel model)
        throws Exception {
        for (int elCounter = 0; elCounter < elementeNodes.getLength(); elCounter++) {
            // Iterate over elements
            Element el = (Element) elementeNodes.item(elCounter);
            if (!createNodeFromElement(el, idMap, currentNode, model)) {
                continue;
            }
        }
    }

    private void processElements(Node xmlDoc, Map<String, ProcessObject> idMap, ProcessModel model) throws Exception {
        String query = "/xsd:schema/xsd:element";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList elementNodes = (NodeList) res;
        for (int elementCounter = 0; elementCounter < elementNodes.getLength(); elementCounter++) {
            Element elementNode = (Element) elementNodes.item(elementCounter);
            String name = elementNode.getAttribute(ATTR_NAME);
            ProcessObject currentNode = idMap.get(name);
            if (currentNode instanceof UMLClass) {
                createNodeFromElement(elementNode, idMap, currentNode, model);
            }
        }
    }

    private String processSimpleTypes(Node xmlDoc, Map<String, ProcessObject> idMap, ProcessModel model) throws Exception {
        if (!config.getProperty(CONF_SHOW_SIMPLE_CLASSES,"1").equals("1")) return null;
        String query = "./xsd:simpleType";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList simpleTypeNodes = (NodeList) res;
        if (simpleTypeNodes.getLength()==0) return null; // No ST found
        String name = "";
        for (int stCounter = 0; stCounter < simpleTypeNodes.getLength(); stCounter++) {
            Element simpleTypeNode = (Element) simpleTypeNodes.item(stCounter);
            name = simpleTypeNode.getAttribute(ATTR_NAME);
            if (name.isEmpty()) {
                name = TEXT_DUMMY+(dummyCounter++);
                // Create dummy simple type (internal)
                UMLClass umlClass = new UMLClass();
                umlClass.setSize(150, 100);
                umlClass.setText(name);
                umlClass.setStereotype(STEREOTYPE_SIMPLETYPE);
                umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(simpleTypeNode));
                model.addNode(umlClass);
                idMap.put(name, umlClass);
            }
            if(debug)
            	System.out.println("ST: "+name);

            ProcessObject currentObject = idMap.get(name);
            UMLClass currentClass = null;
            if (currentObject instanceof UMLClass) {
                currentClass = (UMLClass)currentObject;
            }

            
                query = "./xsd:restriction";
                res = xpath.evaluate(query, simpleTypeNode, XPathConstants.NODESET);
                NodeList resNodes = (NodeList) res;
                if (resNodes.getLength()>0) {
                    Element resElement = (Element)resNodes.item(0);
                    String base = resElement.getAttribute(ATTR_BASE);
                    if (!base.isEmpty()) {
                        // Test if base class is defined internally
                        String baseHack = base.substring(base.indexOf(":")+1,base.length());
                        ProcessObject sourceObject = idMap.get(baseHack);
                        if (sourceObject==null) {
                            // Externally, check if should be shown
                            if (config.getProperty(CONF_SHOW_EXTERNAL_BASES_TYPES,"1").equals("1")) {
                                // Create class
                                UMLClass baseClass = new UMLClass();
                                baseClass.setStereotype(STEREOTYPE_EXTERNAL);
                                baseClass.setProperty(PROP_XPATH, ElementUtil.getXPath(resElement));
                                baseClass.setText(base);
                                model.addNode(baseClass);
                                sourceObject = baseClass;
                            }
                        }

                        if (sourceObject instanceof UMLClass) {
                            // Source object defined locally
                            UMLClass baseClass = (UMLClass)sourceObject;
                            // Create inheritance link
                            Inheritance inh = new Inheritance();
                            inh.setSource(currentClass);
                            inh.setTarget(baseClass);
                            model.addEdge(inh);
                        }
                    }
                
            }

            // Possiblity 1:Restriction/Enumeration
            if (config.getProperty(CONF_SHOW_ENUMERATIONS,"1").equals("1")) {
                query = "./xsd:restriction/xsd:enumeration";
                res = xpath.evaluate(query, simpleTypeNode, XPathConstants.NODESET);
                NodeList enumNodes = (NodeList) res;
                for (int enCounter = 0; enCounter < enumNodes.getLength(); enCounter++) {
                    // Process ComplexContent Node
                    Element enumElement = (Element) enumNodes.item(enCounter);
                    String value = enumElement.getAttribute(ATTR_VALUE);

                    if (currentClass != null ) {
                        // Hack to set stereotype to <<enumeration>>
                        currentClass.setStereotype(STEREOTYPE_ENUMERATION);
                        currentClass.addAttribute(value);
                    }
                }
            }
        }

        return name;
    }

    private String processComplexTypes(Node xmlDoc, Map<String, ProcessObject> idMap, ProcessModel model) throws Exception {

        String query = "./xsd:complexType";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList complexTypeNodes = (NodeList) res;
        if (complexTypeNodes.getLength()==0) return null; // No CT found
        String name = "";
        for (int ctCounter = 0; ctCounter < complexTypeNodes.getLength(); ctCounter++) {
            Element complexTypeNode = (Element) complexTypeNodes.item(ctCounter);
            name = complexTypeNode.getAttribute(ATTR_NAME);
            if (name.isEmpty()) {
                name = TEXT_DUMMY+(dummyCounter++);
                // Create dummy complex type (internal)
                UMLClass umlClass = new UMLClass();
                umlClass.setSize(150, 100);
                umlClass.setText(name);
                umlClass.setStereotype(STEREOTYPE_COMPLEXTYPE);
                umlClass.setProperty(PROP_XPATH, ElementUtil.getXPath(complexTypeNode));
                model.addNode(umlClass);
                idMap.put(name, umlClass);
            }
            if(debug)
            	System.out.println("CT: "+name);

            // Process attributes
            query = "./xsd:attribute";
            res = xpath.evaluate(query, complexTypeNode, XPathConstants.NODESET);
            NodeList attributes = (NodeList) res;
            if(debug)
            	System.out.println("Attributes:"+attributes.getLength());
            for (int attrCount=0; attrCount<attributes.getLength(); attrCount++) {
                Element attribute = (Element)attributes.item(attrCount);
                String attrName = attribute.getAttribute(ATTR_NAME);
                String attrType = attribute.getAttribute(ATTR_TYPE);
                // Get current node
                ProcessObject currentNode = idMap.get(name);
                if (currentNode instanceof UMLClass) {
                    UMLClass currentClass = (UMLClass)currentNode;
                    // Look if found in idMap
                    ProcessObject targetObject = idMap.get(attrType);
                    if (targetObject instanceof UMLClass) {
                        UMLClass targetClass = (UMLClass)targetObject;
                        // Create Composition
                        Aggregation aggr = new Aggregation();
                        aggr.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
                        aggr.setSource(currentClass);
                        aggr.setTarget(targetClass);
                        aggr.setLabel("@"+attrName);
                        model.addEdge(aggr);
                    } else {
                        currentClass.addAttribute("@"+attrName+" : "+attrType);
                    }
                }
            }

            // Possiblity 1: Complex Content (Inheritance)
            query = "./xsd:complexContent/xsd:extension";
            res = xpath.evaluate(query, complexTypeNode, XPathConstants.NODESET);
            NodeList complexContentNodes = (NodeList) res;
            for (int ccCounter = 0; ccCounter < complexContentNodes.getLength(); ccCounter++) {
                // Process ComplexContent Node
                Element extensionNode = (Element) complexContentNodes.item(ccCounter);
                // Get base
                String base = extensionNode.getAttribute(ATTR_BASE);
                // Cut prefix (hack)
                base = base.substring(base.indexOf(":") + 1, base.length());

                if(debug)
                	System.out.println("INH: " + name + "->" + base);

                // Look-up in idMap
                ProcessObject parent = idMap.get(base);
                ProcessObject child = idMap.get(name);

                if(debug)
                	System.out.println("  " + child + ", " + parent);

                if (parent != null && child != null) {
                    ProcessNode parentNode = (ProcessNode) parent;
                    ProcessNode childNode = (ProcessNode) child;
                    Inheritance inheritanceLink = new Inheritance();
                    inheritanceLink.setSource(childNode);
                    inheritanceLink.setTarget(parentNode);
                    model.addEdge(inheritanceLink);
                }
                // Process remaining part recursive
                processContainedSequences(extensionNode, idMap, model, child);
                continue;
            }

            // Possibility 2: Sequence
            processContainedSequences(complexTypeNode, idMap, model, idMap.get(name));

            // Possibility 3: Choice
            processContainedChoices(complexTypeNode, idMap, model, idMap.get(name));

            // Posibility 4: Group
            processGroupNodeTypes(complexTypeNode, idMap, model, idMap.get(name));

        }

        return name; // Return last name (there should only be one)
    }

    private String processGroupNodeTypes(Node xmlDoc, Map<String, ProcessObject> idMap, ProcessModel model, ProcessObject currentNode) throws Exception {

        String query = "./xsd:group";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList groupNodeTypes = (NodeList) res;
        if (groupNodeTypes.getLength()==0) return null; // No Group found
        if(debug)
        	System.out.println("Groups found: "+groupNodeTypes.getLength());
        String name = "";
        for (int gCounter = 0; gCounter < groupNodeTypes.getLength(); gCounter++) {
            Element groupTypeNode = (Element) groupNodeTypes.item(gCounter);
            name = groupTypeNode.getAttribute(ATTR_NAME);

            if (name.isEmpty()) {
                // Check if reference
                 String ref = groupTypeNode.getAttribute(ATTR_REF);
                 // Hack away namespace
                 ref = ref.substring(ref.indexOf(":")+1,ref.length());
                 if(debug)
                 	System.out.println("Found reference: "+ref+": "+idMap.get(ref));
                 if (!ref.isEmpty()) {
                     ProcessObject target = idMap.get(ref);
                     if (target instanceof UMLClass) {
                         // Reference found, create link
                         ProcessNode targetNode = (UMLClass)target;
                         Aggregation aggr = new Aggregation();
                         aggr.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
                         aggr.setSource((ProcessNode)currentNode);
                         aggr.setTarget(targetNode);
                         aggr.setLabel("<<ref>>");
                         model.addEdge(aggr);
                     }
                 }

            }

            // Process contained sequence
            processContainedSequences(groupTypeNode, idMap, model, idMap.get(name));
            // Process contained choices
            processContainedChoices(groupTypeNode, idMap, model, idMap.get(name));
        }

        return name;
    }

    private void processContainedSequences(Node node, Map<String, ProcessObject> idMap,
            ProcessModel model, ProcessObject currentNode)
            throws Exception {
        // Try to find sequence inside
        String query = "./xsd:sequence";
        Object res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList seqNodes = (NodeList) res;
        for (int sCounter = 0; sCounter < seqNodes.getLength(); sCounter++) {
            Element seqNode = (Element) seqNodes.item(sCounter);
            processSequence(seqNode, idMap, model, currentNode);
        }
    }

    private void processSequence(Node node, Map<String, ProcessObject> idMap,
            ProcessModel model, ProcessObject currentNode)
            throws Exception {
        String query = "./xsd:element";
        Object res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList elementeNodes = (NodeList) res;
        createNodesFromNodeList(elementeNodes, idMap, currentNode, model);
        processContainedChoices(node, idMap, model, currentNode);
    }

    private void processContainedChoices(Node node, Map<String, ProcessObject> idMap,
            ProcessModel model, ProcessObject currentNode)
            throws Exception {
        // Try to find choice inside
        String query = "./xsd:choice";
        
        Object res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList choiceNodes = (NodeList) res;
        for (int cCounter = 0; cCounter < choiceNodes.getLength(); cCounter++) {
            Element choiceNode = (Element) choiceNodes.item(cCounter);
            processChoice(choiceNode, idMap, model, currentNode);
        }
    }

     private void processChoice(Node node, Map<String, ProcessObject> idMap,
            ProcessModel model, ProcessObject currentNode)
            throws Exception {
         // Check if reference
         Element groupNode = (Element)node;
         String ref = groupNode.getAttribute(ATTR_REF);
         if (!ref.isEmpty()) {
             ProcessObject target = idMap.get(ref);
             if (target instanceof UMLClass) {
                 // Reference found, create link
                 ProcessNode targetNode = (UMLClass)target;
                 Aggregation aggr = new Aggregation();
                 aggr.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
                 aggr.setSource((ProcessNode)currentNode);
                 aggr.setTarget(targetNode);
                 aggr.setLabel("<<ref>>");
                 model.addEdge(aggr);
             }
         }

        String query = "./xsd:element";
        Object res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList elementeNodes = (NodeList) res;
        if (elementeNodes.getLength()>0) {
            // Create dummy choice element
            UMLClass choice = new UMLClass();
            choice.setText(TEXT_DUMMY+(dummyCounter++));
            choice.setStereotype(STEREOTYPE_CHOICE);
            choice.setProperty(PROP_XPATH, ElementUtil.getXPath(node));
            choice.pack();
            Aggregation aggr = new Aggregation();
            aggr.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
            aggr.setSource((ProcessNode)currentNode);
            aggr.setTarget(choice);
            model.addNode(choice);
            model.addEdge(aggr);
            createNodesFromNodeList(elementeNodes, idMap, choice, model);
        }
    }



    @Override
    public String getDisplayName() {
        return "XML-Schema";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"xsd"};
        return types;
    }

    public static void main(String[] args) {
        System.out.println("XML-Schema Test");

        try {
            // Call converter
            XSDImporter conv = new XSDImporter();
            List<ProcessModel> models = conv.parseSource(new File("c:\\users\\fpu\\desktop\\fhg.xsd"));

            // Show first result
            ProcessModel model = models.get(0);

            ProcessEditor editor = new ProcessEditor(model);
            JFrame frame = new JFrame("XML-Schema import");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JScrollPane scrollPane = new JScrollPane(editor);

            frame.add(scrollPane);
            frame.pack();
            frame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class MyNamespaceContext implements javax.xml.namespace.NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (prefix.equals("xsd")) {
                return XMLSCHEMA_NS;
            } 
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespace) {
            if (namespace.equals(XMLSCHEMA_NS)) {
                return "xsd";
            } 
            return null;
            
        }

        public Iterator<String> getPrefixes(String namespace) {
            return null;
        }
    }
}
