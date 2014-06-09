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

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.Artifact;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.CancelEndEvent;
import net.frapu.code.visualization.bpmn.CancelIntermediateEvent;
import net.frapu.code.visualization.bpmn.CompensationEndEvent;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.ComplexGateway;
import net.frapu.code.visualization.bpmn.ConditionalIntermediateEvent;
import net.frapu.code.visualization.bpmn.ConditionalStartEvent;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.ErrorEndEvent;
import net.frapu.code.visualization.bpmn.ErrorIntermediateEvent;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Group;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.LinkIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageEndEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.MultipleEndEvent;
import net.frapu.code.visualization.bpmn.MultipleIntermediateEvent;
import net.frapu.code.visualization.bpmn.MultipleStartEvent;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SignalEndEvent;
import net.frapu.code.visualization.bpmn.SignalIntermediateEvent;
import net.frapu.code.visualization.bpmn.SignalStartEvent;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TerminateEndEvent;
import net.frapu.code.visualization.bpmn.TextAnnotation;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author frank
 */
public class XPDLImporter implements Importer {

    public final static String ATTR_XMLNS = "xmlns";
    public final static String ATTR_XMLNS_XPDL = "xmlns:xpdl2";
    public final static String ATTR_PROCESS = "Process";
    public final static String ATTR_ID = "Id";
    public final static String ATTR_NAME = "Name";
    public final static String ATTR_WIDTH = "Width";
    public final static String ATTR_Exp_WIDTH = "ExpandedWidth";
    public final static String ATTR_HEIGHT = "Height";
    public final static String ATTR_Exp_HEIGHT = "ExpandedHeight";
    public final static String ATTR_XPOS = "XCoordinate";
    public final static String ATTR_YPOS = "YCoordinate";
    public final static String ATTR_FROM = "From";
    public final static String ATTR_TO = "To";
    public final static String ATTR_TARGET = "Target";
    public final static String ATTR_SOURCE = "Source";
    public final static String ATTR_BOUNDARY_VISIBLE = "BoundaryVisible";
    public final static String ATTR_TRIGGER = "Trigger";
    public final static String ATTR_RESULT = "Result";
    public final static String ATTR_GATEWAYETYPE = "GatewayType";
    public final static String ATTR_EXCLUSIVETYPE = "ExclusiveType";
    public final static String ATTR_LoopType = "LoopType";
    public final static String ATTR_MI_Ordering = "MI_Ordering";
    public final static String ATTR_LANES = "Lanes";
    public final static String ATTR_ARTIFACTTYPE = "ArtifactType";
    public final static String ATTR_TEXTANNOTATION = "TextAnnotation";
    public final static String ATTR_ADHOC = "AdHoc";
    public final static String ATTR_TRANSACTION = "IsATransaction";
    public final static String XPDL_NS_2_1 = "http://www.wfmc.org/2008/XPDL2.1";
    public final static int LANEHEIGHT = 100;
    public final static String COMPLEX_TYPE = "Complex";
    public final static String EVENTBASED_TYPE = "XOR";
    public final static String OR_TYPE = "OR";
    public final static String AND_TYPE = "AND";
    public final static String EXCLUSIVE_TYPE = "Exclusive";
    public final static String VENDOR_TIBCO = "TIBCO";
    public final static String VENDOR_BIZAGI = "BizAgi";
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    private Document xmlDoc;
    /** Map of externalId, ProcessObject */
    private Map<String, ProcessObject> idMap;
    private String vendor;
    private BPMNModel bpmnModel;
    private int currentYCordPool;

    @Override
    public List<ProcessModel> parseSource(File f) throws Exception {
        try {

            idMap = new HashMap<String, ProcessObject>();

            // so you can see the pool
            currentYCordPool = 170;

            FileInputStream in = new FileInputStream(f);
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(true);
            NamespaceContext nsc = new XPDL_NamespaceContext();
            xpath.setNamespaceContext(nsc);
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
            xmlDoc = builder.parse(in);

            checkValidity();

            //Retrieve the Vendor of the Tool that created the xpdl-file
            String query = "/xpdl:Package/xpdl:PackageHeader/xpdl:Vendor";
            Node vendorNode = (Node) xpath.evaluate(query, xmlDoc, XPathConstants.NODE);
            vendor = vendorNode.getTextContent();

            // Create Model
            bpmnModel = new BPMNModel();
            List<ProcessModel> modelList = new LinkedList<ProcessModel>();
            modelList.add(bpmnModel);

            // Retrieve all Pools
            query = "//xpdl:Pool";
            Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
            NodeList poolNodes = (NodeList) res;
            if (poolNodes.getLength() == 0) {
                throw new UnsupportedFileTypeException("No Pools found!");
            }

            // Iterate over all Pools and create contained elements
            for (int poolIndex = 0; poolIndex < poolNodes.getLength(); poolIndex++) {
                Element poolElement = (Element) poolNodes.item(poolIndex);
                processPool(poolElement);
            }


            for (Cluster cluster : bpmnModel.getClusters()) {
                if (cluster instanceof SubProcess) {
                    bpmnModel.moveToBack(cluster);
                }
            }
            for (Cluster cluster : bpmnModel.getClusters()) {
                if (cluster instanceof Pool) {
                    bpmnModel.moveToBack(cluster);
                }
            }


            processArtifacts();
            processMessageFlows();
            processAssociations();


            return modelList;
        } catch (Exception e) {
            throw new UnsupportedFileTypeException("No XPDL 2.1 file!");
        }
    }

    private void checkValidity() throws UnsupportedFileTypeException {
        // Check if XPDL 2.1
        Element rootNode = (Element) xmlDoc.getElementsByTagNameNS(XPDL_NS_2_1, "Package").item(0);
        if (!rootNode.getAttribute(ATTR_XMLNS).equals(XPDL_NS_2_1)) {
            if (!rootNode.getAttribute(ATTR_XMLNS_XPDL).equals(XPDL_NS_2_1)) {
                throw new UnsupportedFileTypeException("No XPDL 2.1 file!");
            }
        }
    }

    /**
     *
     * @param model
     * @throws XPathExpressionException
     */
    private void processAssociations() throws XPathExpressionException {
        // Retrieve all Associations
        String query = "/xpdl:Package/xpdl:Associations/xpdl:Association";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList associations = (NodeList) res;
        for (int counter = 0; counter < associations.getLength(); counter++) {
            Element flow = (Element) associations.item(counter);
            try {
                String source = flow.getAttribute(ATTR_SOURCE);
                String target = flow.getAttribute(ATTR_TARGET);
                ProcessNode sourceNode = (ProcessNode) idMap.get(source);
                ProcessNode targetNode = (ProcessNode) idMap.get(target);
                Association newAssociation = new Association(sourceNode, targetNode);
                bpmnModel.addFlow(newAssociation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param model
     * @throws XPathExpressionException
     */
    private void processMessageFlows() throws XPathExpressionException {
        // Retrieve all MessageFlows
        String query = "/xpdl:Package/xpdl:MessageFlows/xpdl:MessageFlow";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList messageFlows = (NodeList) res;
        for (int counter = 0; counter < messageFlows.getLength(); counter++) {
            Element flow = (Element) messageFlows.item(counter);
            try {
                String source = flow.getAttribute(ATTR_SOURCE);
                String target = flow.getAttribute(ATTR_TARGET);
                MessageFlow newMessageFlow = new MessageFlow((ProcessNode) idMap.get(source), (ProcessNode) idMap.get(target));
                newMessageFlow.setId(flow.getAttribute(ATTR_ID));
                setRoutingPoints(newMessageFlow , flow);
                bpmnModel.addFlow(newMessageFlow);
                idMap.put(newMessageFlow.getId(), newMessageFlow);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Processes an ArtfactElement
     * @param xmlDoc
     * @param model
     * @throws XPathExpressionException
     */
    private void processArtifacts() throws XPathExpressionException {
        // Retrieve all Artifacts
        String query = "/xpdl:Package/xpdl:Artifacts/xpdl:Artifact ";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList artifactsList = (NodeList) res;
        for (int counter = 0; counter < artifactsList.getLength(); counter++) {
            Element artifactElement = (Element) artifactsList.item(counter);
            Artifact artifact = null;
            try {
                String artifactType = artifactElement.getAttribute(ATTR_ARTIFACTTYPE);
                if (artifactType.equals("DataObject")) {
                    artifact = new DataObject();
                    String artifactText = "";
                    if(artifactElement!=null){
                        artifactText = artifactElement.getAttribute(ATTR_NAME);
                    }
                    if(artifactText.equals("")) {
                        Element dataElement = (Element) artifactElement.getElementsByTagNameNS(XPDL_NS_2_1, "DataObject").item(0);
                        if (dataElement != null) {
                            artifactText = dataElement.getAttribute(ATTR_NAME);
                        }
                    }
                    artifact.setText(artifactText);
                    setSizeAndPosition(null, artifactElement, artifact);
                    bpmnModel.addNode(artifact);
                } else if (artifactType.equals("Group")) {
                    artifact = new Group();
                    artifact.setText(artifactElement.getAttribute(ATTR_NAME));
                    setSizeAndPosition(null, artifactElement, artifact);
                    bpmnModel.addNode(artifact);
                } else if (artifactType.equals("Annotation")) {
                    artifact = new TextAnnotation();
                    artifact.setText(artifactElement.getAttribute(ATTR_TEXTANNOTATION));
                    setSizeAndPosition(null, artifactElement, artifact);
                    bpmnModel.addNode(artifact);
                }
                artifact.setId(artifactElement.getAttribute(ATTR_ID));
                idMap.put(artifact.getId(), artifact);
                // hack for getting artifacts into pools
                for (Cluster cluster : bpmnModel.getClusters()) {
                    List<ProcessNode> dummyList = new LinkedList<ProcessNode>();
                    dummyList.add(cluster);
                    Boolean isContained = cluster.isContainedGraphically(dummyList, artifact,true);
                    if (isContained) {
                        cluster.addProcessNode(artifact);
                        break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // Processes all Elements in a Pool ELement
    private void processPool(Element poolElement) throws Exception {

        String processRef = poolElement.getAttribute(ATTR_PROCESS);
        String boundaryVisible = poolElement.getAttribute(ATTR_BOUNDARY_VISIBLE);
        Pool pool = null;
        if (!boundaryVisible.equals("false")) {
            pool = new Pool();
            setSizeAndPosition(null, poolElement, pool);
            pool.setText(poolElement.getAttribute(ATTR_NAME));
            pool.setId(poolElement.getAttribute(ATTR_ID));
            idMap.put(pool.getId(), pool);
            bpmnModel.addNode(pool);
        }

        // get lanes
        String query = "./xpdl:Lanes/xpdl:Lane";
        Object res = xpath.evaluate(query, poolElement, XPathConstants.NODESET);
        NodeList laneList = (NodeList) res;

        int poolheigth = 0;

        if (laneList != null) {
            for (int counter = 0; counter < laneList.getLength(); counter++) {
                Element lane = (Element) laneList.item(counter);
                String lanename = lane.getAttribute(ATTR_NAME);
                String laneId = lane.getAttribute(ATTR_ID);
                Element graphicInfos = (Element) lane.getElementsByTagNameNS(XPDL_NS_2_1, "NodeGraphicsInfos").item(0);
                Element graphicInfo = (Element) graphicInfos.getElementsByTagNameNS(XPDL_NS_2_1, "NodeGraphicsInfo").item(0);
                String laneheight = graphicInfo.getAttribute(ATTR_HEIGHT);
                laneheight = laneheight.replaceAll(",", ".");
                int laneh = new Double(laneheight).intValue();
                poolheigth += laneh;
                Lane newLane = new Lane(lanename, 0, pool);
                newLane.setId(laneId);
                setSizeAndPosition(pool, lane, newLane);
                bpmnModel.addNode(newLane);
                idMap.put(laneId, newLane);
                pool.addLane(newLane);
            }
        }
        if (vendor.equals(VENDOR_TIBCO)) {
            int poolWidth = getPoolWidth(pool.getLanes());
            pool.setSize(poolWidth + 100, poolheigth);
            // Transform Coordinates
            pool.setPos(pool.getSize().width / 2, currentYCordPool + poolheigth / 2);
            processPoolElementsTibco(pool);
            currentYCordPool += poolheigth + 10;
        } else {
            processPoolElements(processRef, pool);
        }

    }

    /**
     * Calculate Poolwidth before processing activities
     * @param laneList
     * @return
     * @throws Exception
     */
    public int getPoolWidth(List<Lane> laneList) throws Exception {
        int maxEndPoint = 0;
        for (Lane lane : laneList) {
            String query = "//xpdl:Activities/xpdl:Activity[xpdl:NodeGraphicsInfos/xpdl:NodeGraphicsInfo/@LaneId='" + lane.getId() + "']";
            Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
            NodeList poolActivities = (NodeList) res;
            for (int counter = 0; counter < poolActivities.getLength(); counter++) {
                Element activity = (Element) poolActivities.item(counter);
                Element graphicInfos = (Element) activity.getElementsByTagNameNS(XPDL_NS_2_1, "NodeGraphicsInfos").item(0);
                Element graphicInfo = (Element) graphicInfos.getElementsByTagNameNS(XPDL_NS_2_1, "NodeGraphicsInfo").item(0);
                Element coordinates = (Element) graphicInfo.getElementsByTagNameNS(XPDL_NS_2_1, "Coordinates").item(0);
                int xPos = new Double(coordinates.getAttribute(ATTR_XPOS)).intValue();
                int width = new Double(graphicInfo.getAttribute(ATTR_HEIGHT)).intValue();
                int endPoint = xPos + width;
                if (endPoint > maxEndPoint) {
                    maxEndPoint = endPoint;
                }
            }
        }
        return maxEndPoint;
    }

    /**
     * Process Elements in the Pool and adds them to the Model
     * @param processRef
     * @param pool
     * @throws XPathExpressionException
     * @throws Exception
     */
    private void processPoolElements(String processRef, Pool pool) throws XPathExpressionException, Exception {
        // Iterate over all elements contained in the pool
        String query = "/xpdl:Package/xpdl:WorkflowProcesses/xpdl:WorkflowProcess[@Id='" + processRef + "']";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList workflowNodes = (NodeList) res;
        for (int wfNodeIndex = 0; wfNodeIndex < workflowNodes.getLength(); wfNodeIndex++) {
            Element wfProcessElement = (Element) workflowNodes.item(wfNodeIndex);
            // Retrieve all Activities in the workflow
            query = "./xpdl:Activities/xpdl:Activity";
            res = xpath.evaluate(query, wfProcessElement, XPathConstants.NODESET);
            NodeList activityNodes = (NodeList) res;
            for (int activityIndex = 0; activityIndex < activityNodes.getLength(); activityIndex++) {
                Element wfActivityElement = (Element) activityNodes.item(activityIndex);
                processActivity(wfActivityElement, pool);
            }
            processTransitions(wfProcessElement);
        }
    }

    /**
     * In Tibco Actitivties are linked with their Pool over the Id of the Lane
     * they are are contained within
     * @param pool
     * @throws XPathExpressionException
     */
    private void processPoolElementsTibco(Pool pool) throws Exception {
        ArrayList<Lane> laneList = (ArrayList<Lane>) pool.getLanes();
        //Retrieve all Lanes of the Pool
        for (Lane lane : laneList) {
            String query = "//xpdl:Activities/xpdl:Activity[xpdl:NodeGraphicsInfos/xpdl:NodeGraphicsInfo/@LaneId='" + lane.getId() + "']";
            Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
            NodeList actNodes = (NodeList) res;
            for (int counter = 0; counter < actNodes.getLength(); counter++) {
                Element actElement = (Element) actNodes.item(counter);
                processActivity(actElement, lane);
            }
        }
        String query = "//xpdl:WorkflowProcess[1]";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODE);
        Element wfProcessElement = (Element) res;
        processTransitions(wfProcessElement);
    }

    /**
     * Processes all transitions in the current Element
     * @param wfProcessElement 
     *        Element which contains the transitions(WorkflowProcess,ActitvitySets)
     * @throws XPathExpressionException
     */
    private void processTransitions(Element wfProcessElement) throws Exception {
        // Retrieve all Transitions
        if (wfProcessElement != null) {
            String query = "./xpdl:Transitions/xpdl:Transition";
            Object res = xpath.evaluate(query, wfProcessElement, XPathConstants.NODESET);
            NodeList transitionNodes = (NodeList) res;
            for (int transitionIndex = 0; transitionIndex < transitionNodes.getLength(); transitionIndex++) {
                Element wfTransitionElement = (Element) transitionNodes.item(transitionIndex);
                String fromId = wfTransitionElement.getAttribute(ATTR_FROM);
                String toId = wfTransitionElement.getAttribute(ATTR_TO);
                SequenceFlow flow = new SequenceFlow((ProcessNode) idMap.get(fromId), (ProcessNode) idMap.get(toId));
                setRoutingPoints(flow, wfTransitionElement);
                bpmnModel.addEdge(flow);
            }

        }
    }

    private void setRoutingPoints(ProcessEdge newEdge, Element transitionElement) throws Exception {
        String query = "./xpdl:ConnectorGraphicsInfos/xpdl:ConnectorGraphicsInfo";
        Object res = xpath.evaluate(query, transitionElement, XPathConstants.NODESET);
        NodeList activityNodes = (NodeList) res;
        if (activityNodes.getLength() > 0) {
            Element graphicsElement = (Element) activityNodes.item(0);
            NodeList bendPoints = graphicsElement.getElementsByTagNameNS(XPDL_NS_2_1, "Coordinates");
            // dont use first and last
            for (int i = 1; i < bendPoints.getLength() - 1; i++) {
                Element bendPoint = (Element) bendPoints.item(i);
                Point routingPoint = getCoordinatesPoint(bendPoint);
                newEdge.addRoutingPoint(i, routingPoint);
            }
        }
    }

    /**
     * Processes an activity element, adds it to the ProcessModel and stores
     * the id reference in the id map.
     * @param activityElement
     * @param model
     * @param idMap
     * @param Cluster
     */
    private void processActivity(Element activityElement, Cluster cluster)
            throws Exception {

        String name = activityElement.getAttribute(ATTR_NAME);
        ProcessNode newNode = null;
        // Process Events
        NodeList eventNodes = activityElement.getElementsByTagNameNS(XPDL_NS_2_1, "Event");
        if (eventNodes.getLength() > 0) {
            for (int eventIndex = 0; eventIndex < eventNodes.getLength(); eventIndex++) {
                Element eventElement = (Element) eventNodes.item(eventIndex);
                newNode = processEvent(eventElement);
            }
        }
        // Process NormalTasks
        NodeList implNodes = activityElement.getElementsByTagNameNS(XPDL_NS_2_1, "Implementation");
        if (implNodes.getLength() > 0) {
            for (int taskIndex = 0; taskIndex < implNodes.getLength(); taskIndex++) {
                Element implElement = (Element) implNodes.item(taskIndex);
                newNode = processTask(implElement, activityElement);
            }
        }
        // Process Subprocess/BlockActivity
        NodeList blockAcNodes = activityElement.getElementsByTagNameNS(XPDL_NS_2_1, "BlockActivity");
        if (blockAcNodes.getLength() > 0) {
            for (int counter = 0; counter < blockAcNodes.getLength(); counter++) {
                // actitvity is subprocess
                newNode = createBlockActivity(cluster, activityElement);
            }
        }
        //Process Gateways
        NodeList gatewayNodes = activityElement.getElementsByTagNameNS(XPDL_NS_2_1, "Route");
        if (gatewayNodes.getLength() > 0) {
            for (int gatewayIndex = 0; gatewayIndex < gatewayNodes.getLength(); gatewayIndex++) {
                Element gatewayElement = (Element) gatewayNodes.item(gatewayIndex);
                newNode = processGateway(gatewayElement);
            }
        }
        if (newNode != null) {
            addNodeToModel(newNode, name, activityElement, cluster);
        }

    }

    /**
     * Adds a Node to the current BPMN Modell
     * @param newNode
     * @param name
     * @param activityElement
     * @param cluster
     * @return
     * @throws Exception
     */
    private ProcessNode addNodeToModel(ProcessNode newNode, String name, Element activityElement, Cluster parentCluster) throws Exception {
        // Set properties of node
        String id = activityElement.getAttribute(ATTR_ID);
        if (newNode != null) {
            newNode.setText(name);
            if (!(newNode instanceof SubProcess)) {
                setSizeAndPosition(parentCluster, activityElement, newNode);
            }
            bpmnModel.addNode(newNode);

            String laneId = getLaneID(activityElement);
            Lane l = (Lane) idMap.get(laneId);
            if (l != null) {
                parentCluster = l;
            }
            
            if (parentCluster != null) {
                parentCluster.addProcessNode(newNode);
            }
            idMap.put(id, newNode);
        }
        return newNode;
    }

    private ProcessNode processEvent(Element eventElement) {
        Event event = null;

        // Check for Start Event
        NodeList eventTypeNode = eventElement.getElementsByTagNameNS(XPDL_NS_2_1, "StartEvent");
        if (eventTypeNode.getLength() > 0) {
            Element startEventElement = (Element) eventTypeNode.item(0);
            String trigger = startEventElement.getAttribute(ATTR_TRIGGER);
            if (trigger.equals("Message")) {
                event = new MessageStartEvent();
            } else if (trigger.equals("Conditional")) {
                event = new ConditionalStartEvent();
            } else if (trigger.equals("Multiple")) {
                event = new MultipleStartEvent();
            } else if (trigger.equals("Timer")) {
                event = new TimerStartEvent();
            } else if (trigger.equals("Signal")) {
                event = new SignalStartEvent();
            } else {
                event = new StartEvent();
            }
        }

        // Check for Intermediate Event
        eventTypeNode = eventElement.getElementsByTagNameNS(XPDL_NS_2_1, "IntermediateEvent");

        if (eventTypeNode.getLength() > 0) {
            Element intermediateEventElement = (Element) eventTypeNode.item(0);
            String trigger = intermediateEventElement.getAttribute(ATTR_TRIGGER);
            if (trigger.equals("Message")) {
                event = new MessageIntermediateEvent();
                setThrowOrCatch(intermediateEventElement, event);
            } else if (trigger.equals("Timer")) {
                event = new TimerIntermediateEvent();
            } else if (trigger.equals("Error")) {
                event = new ErrorIntermediateEvent();
            } else if (trigger.equals("Cancel")) {
                event = new CancelIntermediateEvent();
            } // TODO jks heißt bei BizAGi Rule soll aber Conditional sein
            else if (trigger.equals("Rule") || trigger.equals("Conditional")) {
                event = new ConditionalIntermediateEvent();
            } else if (trigger.equals("Link")) {
                event = new LinkIntermediateEvent();
                setThrowOrCatch(intermediateEventElement, event);
            } else if (trigger.equals("Signal")) {
                event = new SignalIntermediateEvent();
                setThrowOrCatch(intermediateEventElement, event);
            } else if (trigger.equals("Compensation")) {
                event = new CompensationIntermediateEvent();
                setThrowOrCatch(intermediateEventElement, event);
            } else if (trigger.equals("Multiple")) {
                event = new MultipleIntermediateEvent();
                setThrowOrCatch(intermediateEventElement, event);
            } else {
                event = new IntermediateEvent();
            }
        }

        // Check for End Event
        eventTypeNode = eventElement.getElementsByTagNameNS(XPDL_NS_2_1, "EndEvent");
        if (eventTypeNode.getLength() > 0) {
            Element endEventElement = (Element) eventTypeNode.item(0);
            String trigger = endEventElement.getAttribute(ATTR_RESULT);
            if (trigger.equals("Message")) {
                event = new MessageEndEvent();
            } else if (trigger.equals("Error")) {
                event = new ErrorEndEvent();
            } else if (trigger.equals("Cancel")) {
                event = new CancelEndEvent();
            } else if (trigger.equals("Compensation")) {
                event = new CompensationEndEvent();
            } else if (trigger.equals("Signal")) {
                event = new SignalEndEvent();
            } else if (trigger.equals("Terminate")) {
                event = new TerminateEndEvent();
            } else if (trigger.equals("Multiple")) {
                event = new MultipleEndEvent();
            } else {
                event = new EndEvent();
            }
        }
        return event;
    }

    private void setThrowOrCatch(Element eventElement, Event event) {
        try {
            NodeList ndListChilds = eventElement.getChildNodes();
            Element temp = (Element) ndListChilds.item(1);
            String throwtype = temp.getAttribute("CatchThrow");
            if (throwtype.equals("")) {
                throwtype = temp.getAttribute("xpdExt:CatchThrow");
            }
            if (throwtype.equals("THROW")) {
                event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE, IntermediateEvent.EVENT_SUBTYPE_THROWING);
            } else {
                event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE, IntermediateEvent.EVENT_SUBTYPE_CATCHING);
            }
        } catch (Exception e) {
        }
        /* NodeList throwTypeNode = eventElement.getElementsByTagNameNS(XPDL_NS_2_1, TriggerName);
        if (throwTypeNode.getLength() > 0) {
        Element temp = (Element) throwTypeNode.item(0);
        String throwtype = temp.getAttribute("CatchThrow");
         */
    }

    /**
     * 
     * @param parent
     * @param subProcessElement
     * @return
     * @throws Exception
     */
    private ProcessNode createBlockActivity(Cluster parent, Element subProcessElement) throws Exception {
        Element BlockActivityElement = (Element) subProcessElement.getElementsByTagNameNS(XPDL_NS_2_1, "BlockActivity").item(0);
        String setID = BlockActivityElement.getAttribute("ActivitySetId");
        // Retrieve all Activities in the Subprocess
        SubProcess newSubProcess = new SubProcess();
        setSizeAndPosition(parent, subProcessElement, newSubProcess);
        newSubProcess.setId(setID);
        setLoopType(newSubProcess, subProcessElement);
        setAdHoc(newSubProcess);
        String transaction = subProcessElement.getAttribute(ATTR_TRANSACTION);
        if (transaction.equals("true")) {
            newSubProcess.setTransaction();
        }
        processBlockActivity(newSubProcess);
        return newSubProcess;
    }

    /**
     * Process the Activities in Subprocesses
     * @param subProcess
     * @param model
     * @throws Exception
     */
    private void processBlockActivity(Cluster subProcess) throws Exception {
        String setID = subProcess.getId();
        String query = "//xpdl:ActivitySets/xpdl:ActivitySet[@Id='" + setID + "']/xpdl:Activities/xpdl:Activity";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList activityNodes = (NodeList) res;
        for (int activityIndex = 0; activityIndex < activityNodes.getLength(); activityIndex++) {
            Element wfActivityElement = (Element) activityNodes.item(activityIndex);
            processActivity(wfActivityElement, subProcess);
        }
        query = "//xpdl:ActivitySets/xpdl:ActivitySet[@Id='" + setID + "']";
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODE);
        Element activitySetElement = (Element) res;
        processTransitions(activitySetElement);
    }

    private void setAdHoc(SubProcess subProcess) throws XPathExpressionException {
        String setID = subProcess.getId();
        String query = "//xpdl:ActivitySets/xpdl:ActivitySet[@Id='" + setID + "']";

        Element setElement = (Element) xpath.evaluate(query, xmlDoc, XPathConstants.NODE);
        if (setElement != null) {
            String adHoc = setElement.getAttribute(ATTR_ADHOC);
            if (adHoc.equals("true")) {
                subProcess.setAdHoc();
            }
        }
    }

    private ProcessNode processTask(Element implElement, Element activityElement) {
        Task task = new Task();
        setLoopType(task, activityElement);
        setTaskType(implElement, task);
        return task;
    }

    private void setLoopType(ProcessObject task, Element activityElement) {
        Element loopElement = (Element) activityElement.getElementsByTagNameNS(XPDL_NS_2_1, "Loop").item(0);
        String looptype = "";
        try {
            looptype = loopElement.getAttribute(ATTR_LoopType);
            if (looptype.equals("Standard")) {
                task.setProperty(Activity.PROP_LOOP_TYPE, Activity.LOOP_STANDARD);
            } else if (looptype.equals("MultiInstance")) {
                Element LoopMultiInstance = (Element) loopElement.getElementsByTagNameNS(XPDL_NS_2_1, "LoopMultiInstance").item(0);
                if (LoopMultiInstance.getAttribute(ATTR_MI_Ordering).equals("Sequential")) {
                    task.setProperty(Activity.PROP_LOOP_TYPE, Activity.LOOP_MULTI_SEQUENCE);
                } else {
                    task.setProperty(Activity.PROP_LOOP_TYPE, Activity.LOOP_MULTI_PARALLEL);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Sets the Type of the Task
     * @param implElement
     * @param task
     */
    private void setTaskType(Element implElement, Task task) {
        try {
            String tagName = "";
            Element taskElement = (Element) implElement.getElementsByTagNameNS(XPDL_NS_2_1, "Task").item(0);
            if (taskElement != null) {
                NodeList ndListChilds = taskElement.getChildNodes();
                Element temp = (Element) ndListChilds.item(1);
                tagName = temp.getNodeName();
            } else {
                taskElement = (Element) implElement.getElementsByTagNameNS(XPDL_NS_2_1, "Reference").item(0);
                tagName = taskElement.getNodeName();
            }

            if (tagName.contains("TaskUser")) {
                task.setStereotype(Activity.TYPE_USER);
            } else if (tagName.contains("TaskService")) {
                task.setStereotype(Activity.TYPE_SERVICE);
            } else if (tagName.contains("TaskSend")) {
                task.setStereotype(Activity.TYPE_SEND);
            } else if (tagName.contains("TaskReceive")) {
                task.setStereotype(Activity.TYPE_RECEIVE);
            } else if (tagName.contains("TaskManual")) {
                task.setStereotype(Activity.TYPE_MANUAL);
            } else if (tagName.contains("TaskScript")) {
                task.setStereotype(Activity.TYPE_SCRIPT);
            } else if (tagName.contains("Reference")) {
                task.setStereotype(Activity.TYPE_REFERENCE);
            }
        } catch (Exception e) {
        }
    }

    private ProcessNode processGateway(
            Element gatewayElement) {
        String type = gatewayElement.getAttribute(ATTR_GATEWAYETYPE);
        Gateway myGateway = null;
        if (type.equals("Complex")) {
            myGateway = new ComplexGateway();
        } else if (type.equals("EventBasedXOR")) {
            myGateway = new EventBasedGateway();
        } else if (type.equals("Exclusive")) {
            String extype = gatewayElement.getAttribute(ATTR_EXCLUSIVETYPE);
            if (extype.equals("Data")) {
                myGateway = new ExclusiveGateway();
            } else {
                myGateway = new EventBasedGateway();
            }
        } else if (type.equals("OR") || type.equals("Inclusive")) {
            myGateway = new InclusiveGateway();
        } else if (type.equals("AND") || type.equals("Parallel")) {
            myGateway = new ParallelGateway();
        } else {
            myGateway = new Gateway();
        }
        return myGateway;
    }

    /**
     * Sets size and Position of an Processnode
     * @param parentCluster
     * @param activityElement the XPDL Element representing the Node
     * @param node the Node
     * @throws Exception
     */
    private void setSizeAndPosition(Cluster parentCluster, Element activityElement, ProcessNode node) throws Exception {
        setSize(activityElement, node);
        // Check if coordinates exist
        int xPos;
        int yPos;
        if (vendor.equals(VENDOR_TIBCO)) {
            Point p = convertRelativeToAbsolutePosition(parentCluster, activityElement, node);
            xPos = p.x;
            yPos = p.y;
        } else {
            Point coordinates = getNodeCoordinates(activityElement);
            xPos = coordinates.x;
            yPos = coordinates.y;
            xPos += node.getSize().width / 2;
            yPos += node.getSize().height / 2;
        }
        node.setProperty(node.PROP_XPOS, "" + xPos);
        node.setProperty(node.PROP_YPOS, "" + yPos);
    }

    /**
     * Returns the absolute Position of the Node, used if source Tool
     * uses relative Positions
     * @param parentCluster
     * @param activityElement
     * @param node
     * @return Pont with coordinates
     * @throws Exception
     */
    private Point convertRelativeToAbsolutePosition(Cluster parentCluster, Element activityElement, ProcessNode node) throws Exception {
        Point position = new Point();
        Point coordinates = getNodeCoordinates(activityElement);
        int xPos = coordinates.x;
        int yPos = coordinates.y;
        if (node instanceof Artifact) {
            String laneID = getLaneID(activityElement);
            parentCluster = returnParenLane(laneID);
            if (parentCluster == null) {
                parentCluster = bpmnModel.getClusters().get(0);
            }
        }
        if (parentCluster != null) {
            if (node instanceof SubProcess) {
                xPos += parentCluster.getPos().x - parentCluster.getSize().width / 2 + 10;
                //Position des Elements relativ zum Mittelpunkt des Parentclusters
                yPos += (parentCluster.getPos().y - parentCluster.getSize().height / 2);
            } else if (!(parentCluster instanceof SubProcess)) {
                xPos += 30;
                //Position des Elements relativ zum Mittelpunkt des Parentclusters
                yPos += (parentCluster.getPos().y - parentCluster.getSize().height / 2);
            } else if (parentCluster instanceof SubProcess) {
                xPos += parentCluster.getPos().x - parentCluster.getSize().width / 2;
                //Position des Elements ist der Mittelpunkt muss von linke kante mitte umgerechnet werden
                yPos += (parentCluster.getPos().y - parentCluster.getSize().height / 2);
            }
        }
        position.x = xPos;
        position.y = yPos;
        return position;
    }

    private Point getNodeCoordinates(Element activityElement) {
        Point coordinates = new Point();
        try {
            String query = "./xpdl:NodeGraphicsInfos/xpdl:NodeGraphicsInfo";
            Object res = xpath.evaluate(query, activityElement, XPathConstants.NODESET);
            NodeList activityNodes = (NodeList) res;
            if (activityNodes.getLength() > 0) {
                Element graphicsElement = (Element) activityNodes.item(0);
                Element coordinateElement = (Element) graphicsElement.getElementsByTagNameNS(XPDL_NS_2_1, "Coordinates").item(0);
                coordinates = getCoordinatesPoint(coordinateElement);
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XPDLImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return coordinates;
    }

    private Point getCoordinatesPoint(Element coordinateElement) throws NumberFormatException {
        Point coordinates = new Point();
        if (coordinateElement != null) {
            String x = coordinateElement.getAttribute(ATTR_XPOS);
            String y = coordinateElement.getAttribute(ATTR_YPOS);
            // if xpdl has colon as separator replace
            x = x.replaceAll(",", ".");
            y = y.replaceAll(",", ".");
            coordinates.x = new Double(x).intValue();
            coordinates.y = new Double(y).intValue();
        }
        return coordinates;
    }

    private void setSize(Element activityElement, ProcessNode node) {
        try {
            String query = "./xpdl:NodeGraphicsInfos/xpdl:NodeGraphicsInfo";
            Object res = xpath.evaluate(query, activityElement, XPathConstants.NODESET);
            NodeList activityNodes = (NodeList) res;
            if (activityNodes.getLength() > 0) {
                Element graphicsElement = (Element) activityNodes.item(0);
                Double width;
                Double height;
                if (node instanceof SubProcess && (vendor.startsWith(VENDOR_BIZAGI))) {
                    width = new Double(graphicsElement.getAttribute(ATTR_Exp_WIDTH).replaceAll(",", "."));
                    height = new Double(graphicsElement.getAttribute(ATTR_Exp_HEIGHT).replaceAll(",", "."));
                } else {
                    width = new Double(graphicsElement.getAttribute(ATTR_WIDTH).replaceAll(",", "."));
                    height = new Double(graphicsElement.getAttribute(ATTR_HEIGHT).replaceAll(",", "."));
                }
                if (width < 1) {
                    width = 10.0;
                }
                node.setProperty(node.PROP_WIDTH, "" + width.intValue());
                node.setProperty(node.PROP_HEIGHT, "" + height.intValue());
            }
        } catch (Exception e) {
            node.setSize(50, 50);
        }
    }

    private String getLaneID(Element activityElement) {
        String laneId = "";
        try {
            String query = "./xpdl:NodeGraphicsInfos/xpdl:NodeGraphicsInfo";
            Object res = xpath.evaluate(query, activityElement, XPathConstants.NODESET);
            NodeList activityNodes = (NodeList) res;
            if (activityNodes.getLength() > 0) {
                Element graphicsElement = (Element) activityNodes.item(0);
                laneId = graphicsElement.getAttribute("LaneId");
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XPDLImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return laneId;
    }

    private Cluster returnParenLane(String laneID) {
        Lane theLane;
        for (Cluster cluster : bpmnModel.getClusters()) {
            if (cluster instanceof Pool) {
                Pool p = (Pool) cluster;
                List<Lane> lanes = p.getLanes();
                for (int i = 0; i < lanes.size(); i++) {
                    Lane _l = (Lane) lanes.get(i);
                    if (_l.getId().equals(laneID)) {
                        theLane = _l;
                        return theLane;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "XPDL 2.1 (experimental)";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"xpdl"};
        return types;
    }

    public class XPDL_NamespaceContext implements javax.xml.namespace.NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (prefix.equals("xpdl")) {
                return XPDL_NS_2_1;
            }
            return XMLConstants.NULL_NS_URI;

        }

        public String getPrefix(String namespace) {
            if (namespace.equals(XPDL_NS_2_1)) {
                return "xpdl";
            }

            return null;

        }

        public Iterator<String> getPrefixes(String namespace) {
            return null;
        }
    }
}
