/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package net.frapu.code.converter;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
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
import org.w3c.dom.Text;

import com.inubit.research.server.ProcessEditorServerUtils;
import java.util.List;

/**
 *
 * @author jos
 */
public class XPDLExporter implements Exporter {

    private ProcessModel currentModel;
    // Nodes in the current Workflow, used for Transitions
    private HashMap<String, ProcessNode> workflowNodes;
    //-- Endevents
    public final static String ATTR_NAME = "Name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_ID = "Id";
    private static String startEventClass = StartEvent.class.getCanonicalName();
    private static String MessageStartEventClass = MessageStartEvent.class.getCanonicalName();
    private static String TimerStartEventClass = TimerStartEvent.class.getCanonicalName();
    private static String ConditionalStartEventClass = ConditionalStartEvent.class.getCanonicalName();
    private static String SignalStartEventClass = SignalStartEvent.class.getCanonicalName();
    private static String MultipleStartEventClass = MultipleStartEvent.class.getCanonicalName();
    //-- StartEvents
    private static String endEventClass = EndEvent.class.getCanonicalName();
    private static String ErrorEndEventClass = ErrorEndEvent.class.getCanonicalName();
    private static String MessageEndEventClass = MessageEndEvent.class.getCanonicalName();
    private static String CancelEndEventClass = CancelEndEvent.class.getCanonicalName();
    private static String TerminateEndEventClass = TerminateEndEvent.class.getCanonicalName();
    private static String CompensationEndEventClass = CompensationEndEvent.class.getCanonicalName();
    private static String MultipleEndEventClass = MultipleEndEvent.class.getCanonicalName();
    private static String SignalEndEventClass = SignalEndEvent.class.getCanonicalName();
    //-- IntermediateEventss
    private static String IntermediateEventClass = IntermediateEvent.class.getCanonicalName();
    private static String MessageIntermediateEventClass = MessageIntermediateEvent.class.getCanonicalName();
    private static String ErrorIntermediateEventClass = ErrorIntermediateEvent.class.getCanonicalName();
    private static String CancelIntermediateEventClass = CancelIntermediateEvent.class.getCanonicalName();
    private static String ConditionalIntermediateEventClass = ConditionalIntermediateEvent.class.getCanonicalName();
    private static String CompensationIntermediateEventClass = CompensationIntermediateEvent.class.getCanonicalName();
    private static String TimerIntermediateEventClass = TimerIntermediateEvent.class.getCanonicalName();
    private static String LinkIntermediateEventClass = LinkIntermediateEvent.class.getCanonicalName();
    private static String SignalIntermediateEventClass = SignalIntermediateEvent.class.getCanonicalName();
    private static String MultipleIntermediateEventClass = MultipleIntermediateEvent.class.getCanonicalName();
    //--Task,Gateways,Flows
    private static String TaskCLASS = Task.class.getCanonicalName();
    private static String GATEWAYCLASS = Gateway.class.getCanonicalName();
    private static String EVENTGATEWAYCLASS = EventBasedGateway.class.getCanonicalName();
    private static String EXCLUSIVGATEWAYCLASS = ExclusiveGateway.class.getCanonicalName();
    private static String INCLUSIVGATEWAYCLASS = InclusiveGateway.class.getCanonicalName();
    private static String COMPLEXGATEWAYCLASS = ComplexGateway.class.getCanonicalName();
    private static String PARALLELGATEWAYCLASS = ParallelGateway.class.getCanonicalName();
    private static String MESSAGEFLOWCLASS = MessageFlow.class.getCanonicalName();
    private static String ARTIFACTCLASS = Artifact.class.getCanonicalName();
    private static String SUBPROCESSCLASS = SubProcess.class.getCanonicalName();
    private static String DATAOBJECTCLASS = DataObject.class.getCanonicalName();
    private static String GROUPCLASS = Group.class.getCanonicalName();
    private static String TEXTANNOTATIONCLASS = TextAnnotation.class.getCanonicalName();
    private static String sequenceFlowClass = SequenceFlow.class.getCanonicalName();
    private LinkedList<Element> artifactList;
    //-- Attribute Strings
    private static final String ELEMENT_CONNECTORGRAPHICINFOS = "ConnectorGraphicsInfos";
    private static final String ELEMENT_CONNECTORGRAPHICINFO = "ConnectorGraphicsInfo";
    public String DATABASED_TYPE = "Exclusive";//Exclusive,XOR
    public String EVENTBASED_TYPE = "Exclusive";//Exclusive,EventBasedXOR
    public String OR_TYPE = "Inclusive";//Inclusive,OR
    public String AND_TYPE = "Parallel";//Parallel,AND
    private static String VENDOR_TIBCO = "TIBCO";
    private String vendor = "";
    private Vector<Cluster> laneStorage;
    private Document xmlDoc;
    private ConversiontSettingsDTO transformationSettings;

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {

        if (!(m instanceof BPMNModel)) {
            throw new Exception("XPDL is only eligible for BPMN Models");
        }

        if(transformationSettings==null){
            transformationSettings= new ConversiontSettingsDTO(ConversiontSettingsDTO.BIZAGI);
        }

        if (transformationSettings.isBizAgiGateways()) {
            setDeprecatedGatewayNames();
        }

        currentModel = m.clone();
        workflowNodes = new HashMap<String, ProcessNode>();
        laneStorage = new Vector<Cluster>();
        artifactList = new LinkedList<Element>();
        String name = "";
        if (!f.getName().endsWith(".xpdl")) {
            name = (f + ".xpdl");
        } else {
            name = f.toString();
        }
        FileOutputStream fos = new FileOutputStream(name);
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        Document doc = (getXPDLSerialization());
        ProcessEditorServerUtils.writeXMLtoStream(osw,doc);
        fos.close();
    }

    public ConversiontSettingsDTO getDto() {
        return transformationSettings;
    }

    public void setDto(ConversiontSettingsDTO dto) {
        this.transformationSettings = dto;
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
    	Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(BPMNModel.class);
        return result;
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

    public void setDeprecatedGatewayNames() {
        DATABASED_TYPE = "XOR";
        EVENTBASED_TYPE = "EventBasedXOR";
        OR_TYPE = "OR";
        AND_TYPE = "AND";
    }

    /**
     * Returns a serialization of the Process Model in a conform XPDL-Document.
     * @return A Document containing the process model serialized as XPDL.
     * @throws java.lang.Exception
     */
    public Document getXPDLSerialization() throws Exception {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        xmlDoc = builder.newDocument();

        Element rootPackage = xmlDoc.createElement("Package");
        rootPackage.setAttribute("xmlns", "http://www.wfmc.org/2008/XPDL2.1");
        rootPackage.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        if (vendor.equals(VENDOR_TIBCO)) {
            rootPackage.setAttribute("xmlns:xpdExt", "http://www.tibco.com/XPD/xpdExtension1.0.0");
        }
        rootPackage.setAttribute(ATTR_NAME, currentModel.getProcessName());
        rootPackage.setAttribute(ATTR_ID, "" + this.hashCode());

        // Create the PackageHeaderElement
        rootPackage.appendChild(createPackageHeader());

        Element pools = xmlDoc.createElement("Pools");
        Element poolElement = xmlDoc.createElement("Pool");
        if (!vendor.equals(VENDOR_TIBCO)) {
            // Main Pool
            String mainPoolID = UUID.randomUUID().toString();
            poolElement.setAttribute("Id", mainPoolID);
            poolElement.setAttribute("Name", "mainPool");
            poolElement.setAttribute("BoundaryVisible", "false");
            pools.appendChild(poolElement);
            Pool mainpool = new Pool(0, 0, "");
            mainpool.setProperty("width", "0");
            mainpool.setProperty("height", "0");
            setGraphicInfos(null, xmlDoc, mainpool, poolElement);
        }

        // Create the WorklfowProcess Element
        Element workflowProcesses = xmlDoc.createElement("WorkflowProcesses");

        if (!vendor.equals(VENDOR_TIBCO)) {
            for (Cluster cluster : currentModel.getClusters()) {
                if (cluster instanceof Pool) {
                    //TODO jks sinnvolle ProcessId festlegung
                    String processID = UUID.randomUUID().toString();
                    poolElement = createPoolElement(processID, cluster);
                    Element workFlowPrElement = createWorkflowProcess((Pool) cluster, xmlDoc, processID);
                    workflowProcesses.appendChild(workFlowPrElement);
                    pools.appendChild(poolElement);
                }
            }
        }
        // TODO jks
        if (vendor.equals(VENDOR_TIBCO)) {
            HashMap<Integer, Element> poolMap = new HashMap<Integer, Element>();
            String processID = UUID.randomUUID().toString();
            for (Cluster cluster : currentModel.getClusters()) {
                if (cluster instanceof Pool) {
                    //TODO jks sinnvolle ProcessId festlegung
                    poolElement = createPoolElement(processID, cluster);
                    // gibt es schon einen Pool mit diesn y-Koordinaten?
                    Integer key = cluster.getPos().y;
                    while (poolMap.containsKey(key)) {
                        key++;
                    }
                    poolMap.put(key, poolElement);

                }
            }
            Element workFlowPrElement = createWorkflowProcessTibco(xmlDoc, processID);
            workflowProcesses.appendChild(workFlowPrElement);
            //--- Pools sortiert ausgeben
            Object[] key = poolMap.keySet().toArray();
            Arrays.sort(key);

            for (int i = 0; i < key.length; i++) {
                pools.appendChild(poolMap.get(key[i]));
            }
        }
        //--------------------------------------------------------
        Element messageFlows = createMessageFlows(xmlDoc);
        Element associations = createAssociations(xmlDoc);

        rootPackage.appendChild(pools);
        rootPackage.appendChild(messageFlows);
        if (associations != null) {
            rootPackage.appendChild(associations);
        }

        if (artifactList.size() > 0) {
            Element artifacts = xmlDoc.createElement("Artifacts");
            for (Element artifact : artifactList) {
                artifacts.appendChild(artifact);
                rootPackage.appendChild(artifacts);
            }
        }

        rootPackage.appendChild(workflowProcesses);
        rootPackage.appendChild(createExtendedAttributes());
        xmlDoc.appendChild(rootPackage);
        return xmlDoc;
    }

    private Element createPoolElement(String processID, Cluster cluster) {
        Element poolElement = xmlDoc.createElement("Pool");
        String poolName = cluster.getText();
        poolElement = xmlDoc.createElement("Pool");
        poolElement.setAttribute("Id", cluster.getId());
        poolElement.setAttribute("Process", processID);
        poolElement.setAttribute("Name", poolName);
        poolElement.setAttribute("BoundaryVisible", "true");
        if (transformationSettings.isNeedsLanes()) {
            createLaneObjects(cluster);
        }
        poolElement.appendChild(createLaneElements(xmlDoc, (Pool) cluster));
        setGraphicInfos(null, xmlDoc, cluster, poolElement);
        return poolElement;
    }

    private Element createLaneElements(Document xmlDoc, Pool p) {
        Element lanesElement = xmlDoc.createElement("Lanes");
        for (ProcessNode node : p.getLanes()) {
            if (node instanceof Lane) {
                laneStorage.add((Lane) node);
                Element laneElement = xmlDoc.createElement("Lane");
                laneElement.setAttribute("Id", node.getId());
                laneElement.setAttribute("Name", node.getText());
                setGraphicInfos(null, xmlDoc, node, laneElement);
                lanesElement.appendChild(laneElement);
            }
        }
        return lanesElement;
    }

    /**
     * Checks if Cluster has a Lane or not
     * If not a new Lane is created and all ProcessNodes
     * are assigned to it
     * */
    private Lane createLaneObjects(Cluster cluster) {
        boolean hasLane = false;
        if (cluster instanceof Pool) {
            Pool pool = (Pool) cluster;
            for (ProcessNode node : pool.getProcessNodes()) {
                if (node instanceof Lane) {
                    hasLane = true;
                    break;
                }
            }
            if (!hasLane) {
                Lane newLane = new Lane();
                newLane.setProcessNodes(pool.getProcessNodes());
                newLane.setText(pool.getText() + "_Lane");
                Point p = getCoordinates(null, pool);
                newLane.setProperty(Lane.PROP_XPOS, "" + p.x);
                newLane.setProperty(Lane.PROP_YPOS, "" + p.y);
                newLane.setProperty(Lane.PROP_WIDTH, "" + pool.getProperty("width"));
                newLane.setProperty(Lane.PROP_HEIGHT, "" + pool.getProperty("height"));
                pool.setProcessNodes(new LinkedList<ProcessNode>());
                pool.addLane(newLane);
                laneStorage.add((Lane) newLane);
                return newLane;
            }
        }
        return null;
    }

    /**
     * Creates a WorkflowProcess Element
     * @param processID
     * @param xmlDoc
     * @param poolName
     * @return
     */
    private Element createWorkflowProcess(Pool pool, Document xmlDoc, String processID) {
        //Um keine falschen Transitions zu bekommen
        workflowNodes.clear();
        Element workflowProcess = xmlDoc.createElement("WorkflowProcess");
        Element processHeader = xmlDoc.createElement("ProcessHeader");
        workflowProcess.setAttribute("Id", processID);
        workflowProcess.appendChild(processHeader);
        Element transitionsElement = xmlDoc.createElement("Transitions");
        Element activityElements = xmlDoc.createElement("Activities");
        Vector<Element> wfNodes = createElements(transitionsElement, pool, workflowProcess);
        for (int i = 0; i < wfNodes.size(); i++) {
            Element element = wfNodes.elementAt(i);
            activityElements.appendChild(element);
        }
        workflowProcess.appendChild(activityElements);
        transitionsElement = createTransitions(transitionsElement, pool, xmlDoc);
        workflowProcess.appendChild(transitionsElement);
        return workflowProcess;
    }

    /**
     * All Pools in one Process
     * @param xmlDoc
     * @param processID
     * @return
     */
    private Element createWorkflowProcessTibco(Document xmlDoc, String processID) {
        //Um keine falschen Transitions zu bekommen
        workflowNodes.clear();
        Element workflowProcess = xmlDoc.createElement("WorkflowProcess");
        Element processHeader = xmlDoc.createElement("ProcessHeader");
        workflowProcess.setAttribute("Id", processID);
        workflowProcess.appendChild(processHeader);
        Element transitionsElement = xmlDoc.createElement("Transitions");
        Element activityElements = xmlDoc.createElement("Activities");
        for (Cluster cluster : currentModel.getClusters()) {
            if (cluster instanceof Pool) {
                Pool pool = (Pool) cluster;
                for (Lane poolLane : pool.getLanes()) {
                    Vector<Element> wfNodes = createElements(transitionsElement, poolLane, workflowProcess);
                    for (int i = 0; i < wfNodes.size(); i++) {
                        Element element = wfNodes.elementAt(i);
                        activityElements.appendChild(element);
                    }
                    transitionsElement = createTransitions(transitionsElement, poolLane, xmlDoc);
                }
            }
        }
        workflowProcess.appendChild(activityElements);
        workflowProcess.appendChild(transitionsElement);
        return workflowProcess;
    }

    private Element createAssociations(Document xmlDoc) {
        Element associations = null;
        for (ProcessEdge edge : currentModel.getEdges()) {
            // String classTyp = edge.getClass().getName();
            if (edge instanceof Association) {
                if (associations == null) {
                    associations = xmlDoc.createElement("Associations");
                }
                Element association = xmlDoc.createElement("Association");
                association.setAttribute("Id", edge.getId());
                association.setAttribute("AssociationDirection", "From");
                association.setAttribute("Source", edge.getProperty("#sourceNode"));
                association.setAttribute("Target", edge.getProperty("#targetNode"));
                Element connectorGraphicsInfos = convertFlowPointsToConnectorGraphicInfos(edge);
                if (connectorGraphicsInfos != null) {
                    association.appendChild(connectorGraphicsInfos);
                }
                associations.appendChild(association);
            }
        }
        return associations;
    }

    private Element createMessageFlows(Document xmlDoc) {
        Element messageFlows = xmlDoc.createElement("MessageFlows");
        for (ProcessEdge edge : currentModel.getEdges()) {
            String classTyp = edge.getClass().getName();
            if (classTyp.equals(MESSAGEFLOWCLASS)) {
                Element messageFlow = xmlDoc.createElement("MessageFlow");
                messageFlow.setAttribute("Id", edge.getId());
                messageFlow.setAttribute("Source", edge.getProperty("#sourceNode"));
                messageFlow.setAttribute("Target", edge.getProperty("#targetNode"));
                messageFlows.appendChild(messageFlow);
                Element connectorGraphicsInfos = convertFlowPointsToConnectorGraphicInfos(edge);
                if (connectorGraphicsInfos != null) {
                    messageFlow.appendChild(connectorGraphicsInfos);
                }
            }
        }
        return messageFlows;
    }

    /**
     *
     * @param transitionsElement will be created if not null
     * @param cluster
     * @param xmlDoc
     * @return
     */
    private Element createTransitions(Element transitionsElement, Cluster cluster, Document xmlDoc) {
        if (transitionsElement == null) {
            transitionsElement = xmlDoc.createElement("Transitions");
        }
        List<ProcessNode> containedNodes = cluster.getProcessNodes();
        Set<ProcessNode> childNodes = new HashSet<ProcessNode>();
        for (ProcessNode processNode : containedNodes) {
            if (processNode instanceof Cluster && !(processNode instanceof SubProcess)) {
                childNodes.addAll(((Cluster) processNode).getProcessNodes());
            }
        }
        containedNodes.addAll(childNodes);
        for (ProcessEdge edge : currentModel.getEdges()) {
            String classTyp = edge.getClass().getName();
            if (classTyp.equals(sequenceFlowClass)) {
                Element transition = xmlDoc.createElement("Transition");
                // TODO jks eleganter machen
                String from = edge.getProperty("#sourceNode");
                String to = edge.getProperty("#targetNode");
                ProcessNode targetNode = workflowNodes.get(to);
                ProcessNode sourceNode = workflowNodes.get(from);
                if (containedNodes.contains(targetNode) && containedNodes.contains(sourceNode)) {
                    transition.setAttribute("From", from);
                    transition.setAttribute("To", to);
                    transition.setAttribute("Id", edge.getId());
                    transitionsElement.appendChild(transition);
                    Element connectorGraphicsInfos = convertFlowPointsToConnectorGraphicInfos(edge);
                    if (connectorGraphicsInfos != null) {
                        transition.appendChild(connectorGraphicsInfos);
                    }

                }
            }
        }
        return transitionsElement;
    }

    /**
     * Converts the Coordinates of an Edge to 
     * @param edge
     * @return XPDL-Element ConnectorGraphicInfos
     */
    private Element convertFlowPointsToConnectorGraphicInfos(ProcessEdge edge) {
        String bendPoints = edge.getProperty(ProcessEdge.PROP_POINTS);

        String[] points = bendPoints.split("\\+");
        Element connectorGraphicsInfos = xmlDoc.createElement(ELEMENT_CONNECTORGRAPHICINFOS);
        Element connectorGraphicsInfo = xmlDoc.createElement(ELEMENT_CONNECTORGRAPHICINFO);
        Element coordinates = xmlDoc.createElement("Coordinates");
        coordinates.setAttribute("XCoordinate", "" + edge.getSource().getPos().x);
        coordinates.setAttribute("YCoordinate", "" + edge.getSource().getPos().y);
        connectorGraphicsInfo.appendChild(coordinates);

        //bend points
        
        for (int i = 0; i < points.length; i++) {
        String[] point = points[i].split(",");
        try {
        if (point.length > 1) {
        String xPos = point[0];
        String yPos = point[1];
        coordinates = xmlDoc.createElement("Coordinates");
        coordinates.setAttribute("XCoordinate", "" + xPos);
        coordinates.setAttribute("YCoordinate", "" + yPos);
        connectorGraphicsInfo.appendChild(coordinates);
        }
        } catch (Exception e) {
        e.printStackTrace();
        }
        }
         
        coordinates = xmlDoc.createElement("Coordinates");
        coordinates.setAttribute("XCoordinate", "" + edge.getTarget().getPos().x);
        coordinates.setAttribute("YCoordinate", "" + edge.getTarget().getPos().y);
        connectorGraphicsInfo.appendChild(coordinates);
        connectorGraphicsInfos.appendChild(connectorGraphicsInfo);
        return connectorGraphicsInfos;
    }

    /**
     * Creates Elements the XPDL Representation of a Cluster
     * @param pool
     * @param workflowProcess
     * @param xmlDoc
     * @return
     */
    private Vector<Element> createElements(Element transitionsElement, Cluster cluster, Element workflowProcess) {
        List<ProcessNode> clusterNodes = cluster.getProcessNodes();
        Vector<Element> processElements = new Vector<Element>();
        // add lane elements
        if (cluster instanceof Pool) {
            Pool pool = (Pool) cluster;
            for (Lane poolLane : pool.getLanes()) {
                Vector<Element> wfNodes = createElements(null, poolLane, workflowProcess);
                processElements.addAll(wfNodes);
                // transitionsElement = createTransitions(transitionsElement, pool, xmlDoc);
            }
        }

        String classTyp = "";
        String superClassTyp = "";
        Element activitySets = null;
        for (ProcessNode node : clusterNodes) {
            if (node != null) {
                classTyp = node.getClass().getName();
                superClassTyp = node.getClass().getSuperclass().getName();
            }
            Element newElement = null;
            if (classTyp.equals(TaskCLASS)) {
                newElement = createTask(xmlDoc);
            } else if (superClassTyp.equals(startEventClass) || classTyp.equals(startEventClass)) {
                newElement = createStartEvent(xmlDoc, classTyp);
            } else if (superClassTyp.equals(endEventClass) || classTyp.equals(endEventClass)) {
                newElement = createEndEvent(xmlDoc, classTyp);
            } else if (superClassTyp.equals(GATEWAYCLASS) || classTyp.equals(GATEWAYCLASS)) {
                newElement = createGateway(xmlDoc, classTyp);
            } else if (superClassTyp.equals(IntermediateEventClass) || classTyp.equals(IntermediateEventClass)) {
                newElement = createIntermediateEvent(xmlDoc, classTyp, node.getProperty("event_subtype"));
            } else if (classTyp.equals(SUBPROCESSCLASS)) {
                if (activitySets == null) {
                    activitySets = xmlDoc.createElement("ActivitySets");
                    workflowProcess.appendChild(activitySets);
                }
                String setID = UUID.randomUUID().toString();
                Element activitySet = createActivitySet(setID, node, workflowProcess);
                activitySets.appendChild(activitySet);
                newElement = createSubProcess(xmlDoc, setID);
            } else if (superClassTyp.equals(ARTIFACTCLASS)) {
                Element artifact = createArtifact(xmlDoc, classTyp, node);
                setGraphicInfos(cluster, xmlDoc, node, artifact);
                artifact.setAttribute("Id", node.getId());
                artifact.setAttribute("Name", node.getText());
                artifactList.add(artifact);
            }
            // Prüfen ob Element erstellt wurde
            if (newElement != null) {
                setGraphicInfos(cluster, xmlDoc, node, newElement);
                setNameAndID(node, newElement);
                workflowNodes.put(node.getId(), node);
                processElements.add(newElement);
            }
        }
        return processElements;
    }

    private Element createActivitySet(String setID, ProcessNode node, Element workflowProcess) {
        //Create the ActivitySet
        Element activitySet = xmlDoc.createElement("ActivitySet");
        activitySet.setAttribute("Id", setID);
        Vector<Element> subactivities = createElements(null, (Cluster) node, workflowProcess);
        if (subactivities != null) {
            Element activityElements = xmlDoc.createElement("Activities");
            for (int i = 0; i < subactivities.size(); i++) {
                Element element = subactivities.elementAt(i);
                activityElements.appendChild(element);
            }
            activitySet.appendChild(activityElements);
        }
        Element transitions = createTransitions(null, (Cluster) node, xmlDoc);
        activitySet.appendChild(transitions);
        return activitySet;
    }

    private void setNameAndID(ProcessNode node, Element element) {
        element.setAttribute("Id", node.getId());
        element.setAttribute("Name", node.getText());
    }

    private Element createSubProcess(Document xmlDoc, String setID) {
        Element activity = xmlDoc.createElement("Activity");
        Element blockActivity = xmlDoc.createElement("BlockActivity");
        blockActivity.setAttribute("ActivitySetId", setID);
        activity.appendChild(blockActivity);
        return activity;
    }

    /**
     * Creates an Activity
     * @param xmlDoc
     * @return
     */
    private Element createTask(Document xmlDoc) {
        Element activity = xmlDoc.createElement("Activity");
        Element implementation = xmlDoc.createElement("Implementation");
        Element task = xmlDoc.createElement("Task");
        implementation.appendChild(task);
        activity.appendChild(implementation);
        return activity;
    }

    private Element createArtifact(Document xmlDoc, String type, ProcessNode node) {
        Element artifact = xmlDoc.createElement("Artifact");
        if (type.equals(DATAOBJECTCLASS)) {
            artifact.setAttribute("ArtifactType", "DataObject");
        } else if (type.equals(GROUPCLASS)) {
            artifact.setAttribute("ArtifactType", "Group");
        } else if (type.equals(TEXTANNOTATIONCLASS)) {
            artifact.setAttribute("ArtifactType", "Annotation");
            //String text = node.getText();
            artifact.setAttribute("TextAnnotation", node.getText());
        }

        return artifact;
    }

    private Element createGateway(Document xmlDoc, String type) {
        Element activity = xmlDoc.createElement("Activity");
        Element route = xmlDoc.createElement("Route");
        if (type.equals(GATEWAYCLASS)) {
            // do nothing
        } else if (type.equals(EXCLUSIVGATEWAYCLASS)) {
            route.setAttribute("GatewayType", DATABASED_TYPE);
        } else if (type.equals(EVENTGATEWAYCLASS)) {
            route.setAttribute("GatewayType", EVENTBASED_TYPE);
        } else if (type.equals(INCLUSIVGATEWAYCLASS)) {
            route.setAttribute("GatewayType", OR_TYPE);
        } else if (type.equals(COMPLEXGATEWAYCLASS)) {
            route.setAttribute("GatewayType", XPDLImporter.COMPLEX_TYPE);
        } else if (type.equals(PARALLELGATEWAYCLASS)) {
            route.setAttribute("GatewayType", AND_TYPE);
        }

        activity.appendChild(route);
        return activity;
    }

    private Element createStartEvent(Document xmlDoc, String type) {
        Element activity = xmlDoc.createElement("Activity");
        Element event = xmlDoc.createElement("Event");
        Element eventType = null;
        if (type.equals(startEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "None");
        } else if (type.equals(MessageStartEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "Message");
        } else if (type.equals(TimerStartEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "Timer");
        } else if (type.equals(ConditionalStartEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "Conditional");
        } else if (type.equals(SignalStartEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "Signal");
        } else if (type.equals(MultipleStartEventClass)) {
            eventType = xmlDoc.createElement("StartEvent");
            eventType.setAttribute("Trigger", "Multiple");
        }
        event.appendChild(eventType);
        activity.appendChild(event);
        return activity;
    }

    private Element createEndEvent(Document xmlDoc, String type) {
        Element activity = xmlDoc.createElement("Activity");
        Element event = xmlDoc.createElement("Event");
        Element eventType = null;
        if (type.equals(endEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "None");
        } else if (type.equals(ErrorEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Error");
        } else if (type.equals(MessageEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Message");
        } else if (type.equals(CancelEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Cancel");
        } else if (type.equals(CompensationEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Compensation");
        } else if (type.equals(MultipleEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Multiple");
        } else if (type.equals(TerminateEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Terminate");
        } else if (type.equals(SignalEndEventClass)) {
            eventType = xmlDoc.createElement("EndEvent");
            eventType.setAttribute("Result", "Signal");
        }
        event.appendChild(eventType);
        activity.appendChild(event);
        return activity;
    }

    private Element createIntermediateEvent(Document xmlDoc, String type, String subtype) {
        Element activity = xmlDoc.createElement("Activity");
        Element event = xmlDoc.createElement("Event");
        Element eventType = null;
        Element triggerElement = null;
        if (type.equals(IntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            eventType.setAttribute("Trigger", "None");
        } else if (type.equals(MessageIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            triggerElement =
                    xmlDoc.createElement("TriggerResultMessage");
            eventType.appendChild(triggerElement);
            setThrow(triggerElement, subtype);
            eventType.setAttribute("Trigger", "Message");
        } else if (type.equals(TimerIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            eventType.setAttribute("Trigger", "Timer");
        } else if (type.equals(ErrorIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            eventType.setAttribute("Trigger", "Error");
        } else if (type.equals(CancelIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            eventType.setAttribute("Trigger", "Cancel");
        } else if (type.equals(ConditionalIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            eventType.setAttribute("Trigger", "Conditional");
        } else if (type.equals(LinkIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            triggerElement =
                    xmlDoc.createElement("TriggerResultLink");
            eventType.appendChild(triggerElement);
            setThrow(triggerElement, subtype);
            eventType.setAttribute("Trigger", "Link");
        } else if (type.equals(SignalIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            triggerElement =
                    xmlDoc.createElement("TriggerResultSignal");
            eventType.appendChild(triggerElement);
            setThrow(triggerElement, subtype);
            eventType.setAttribute("Trigger", "Signal");
        } else if (type.equals(CompensationIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            triggerElement =
                    xmlDoc.createElement("TriggerResultCompensation");
            eventType.appendChild(triggerElement);
            setThrow(triggerElement, subtype);
            eventType.setAttribute("Trigger", "Compensation");
        } else if (type.equals(MultipleIntermediateEventClass)) {
            eventType = xmlDoc.createElement("IntermediateEvent");
            triggerElement =
                    xmlDoc.createElement("TriggerIntermediateMultiple");
            eventType.appendChild(triggerElement);
            setThrow(triggerElement, subtype);
            eventType.setAttribute("Trigger", "Multiple");
        }

        event.appendChild(eventType);
        activity.appendChild(event);
        return activity;
    }

    private void setThrow(Element elem, String subtype) {
        if (!subtype.equals("Catching")) {
            elem.setAttribute("CatchThrow", "THROW");
        }

    }

    private Element createPackageHeader() {
        Element packageHeader = xmlDoc.createElement("PackageHeader");

        Element xpdlVersion = xmlDoc.createElement("XPDLVersion");
        Text TextNode1 = xmlDoc.createTextNode("2.1");
        xpdlVersion.appendChild(TextNode1);
        Element vendorEl = xmlDoc.createElement("Vendor");
        Text TextNode2 = xmlDoc.createTextNode("inubit AG www.inubit.com");
        vendorEl.appendChild(TextNode2);
        Element created = xmlDoc.createElement("Created");
        Text TextNode3 = xmlDoc.createTextNode(currentModel.getCreationDate());
        created.appendChild(TextNode3);

        packageHeader.appendChild(xpdlVersion);
        packageHeader.appendChild(vendorEl);
        packageHeader.appendChild(created);
        return packageHeader;
    }

    /**
     * Creates the extenden attribute Element, required by Tibco Busines Studio
     * for xpdl import
     * @param xmlDoc
     * @return
     */
    private Element createExtendedAttributes() {
        Element extendedAttributes = xmlDoc.createElement("ExtendedAttributes");

        Element vendorAttr = xmlDoc.createElement("ExtendedAttribute");
        vendorAttr.setAttribute("Name", "CreatedBy");
        vendorAttr.setAttribute("Value", "inubit AG, Berlin (http://inubit.com)");
        extendedAttributes.appendChild(vendorAttr);

        Element formatVersion = xmlDoc.createElement("ExtendedAttribute");
        formatVersion.setAttribute("Name", "FormatVersion");
        formatVersion.setAttribute("Value", "7");
        extendedAttributes.appendChild(formatVersion);

        return extendedAttributes;
    }

    /**
     *
     * @param parentCluster
     * @param xmlDoc
     * @param node
     * @param xmlElement
     */
    public void setGraphicInfos(Cluster parentCluster, Document xmlDoc, ProcessNode node, Element xmlElement) {
        Element nodeGraphicsInfos = xmlDoc.createElement("NodeGraphicsInfos");
        Element nodeGraphicsInfo = xmlDoc.createElement("NodeGraphicsInfo");
        nodeGraphicsInfo.setAttribute("Width", node.getProperty("width"));
        nodeGraphicsInfo.setAttribute("Height", node.getProperty("height"));
        Element coordinates = xmlDoc.createElement("Coordinates");
        Point coordinatesPoint = getCoordinates(parentCluster, node);
        int xPos = coordinatesPoint.x;
        int yPos = coordinatesPoint.y;
        coordinates.setAttribute("XCoordinate", "" + xPos);
        coordinates.setAttribute("YCoordinate", "" + yPos);
        nodeGraphicsInfo.appendChild(coordinates);
        Cluster parentLane = getParentLane(node);
        if (parentLane != null) {
            nodeGraphicsInfo.setAttribute("LaneId", parentLane.getId());
        }
        nodeGraphicsInfos.appendChild(nodeGraphicsInfo);
        xmlElement.appendChild(nodeGraphicsInfos);
    }

    /**
     * Returns the Coordinates of an Element as it would be in the xpdl
     * @param parentCluster
     * @param node
     * @return
     */
    private Point getCoordinates(Cluster parentCluster, ProcessNode node) {
        Point coordinates = new Point();
        int xPos = Integer.parseInt(node.getProperty("x"));
        int yPos = Integer.parseInt(node.getProperty("y"));

        // Translate coordinates to center origin
        xPos -= node.getSize().width / 2;
        yPos -= node.getSize().height / 2;
        /*
        if (!transformationSettings.isUrsprung_Absolut()) {
        if (parentCluster != null) {
        Point parentPos = getCoordinates(null, parentCluster);
        xPos = xPos - parentPos.x;
        yPos = yPos - parentPos.y;
        }
        }*/

        if (yPos < 0) {
            yPos = 0;
        }
        if (xPos < 0) {
            xPos = 0;
        }
        coordinates.x = xPos;
        coordinates.y = yPos;
        return coordinates;
    }

    private Cluster getParentLane(ProcessNode node) {
        for (Cluster cluster : laneStorage) {
            if (cluster.isContained(node) && cluster instanceof Lane) {
                return cluster;
            }
        }
        return null;
    }
}
