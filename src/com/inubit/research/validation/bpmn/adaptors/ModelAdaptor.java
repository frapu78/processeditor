/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * Adapts a BPMNModel for the specific needs of Validation.
 * @author tmi
 */
public class ModelAdaptor extends AbstractAdaptor {

    private BPMNModel adaptee;

    public ModelAdaptor(BPMNModel adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }

    public List<NodeAdaptor> getNodes() {
        return adaptNodeList(adaptee.getNodes(), this);
    }

    public List<EdgeAdaptor> getEdges() {
        return adaptEdgeList(adaptee.getEdges(), this);
    }

    public List<ProcessObjectAdaptor> getObjects() {
        List<ProcessObjectAdaptor> objects =
                new LinkedList<ProcessObjectAdaptor>(getNodes());
        objects.addAll(getEdges());
        return objects;
    }

    public boolean hasNode(NodeAdaptor node) {
        return adaptee.getNodes().contains(node.getAdaptee());
    }

    public boolean hasEdge(EdgeAdaptor edge) {
        return adaptee.getEdges().contains(edge.getAdaptee());
    }

    public ClusterAdaptor getClusterForNode(NodeAdaptor node) {
        return ClusterAdaptor.adapt(
                adaptee.getClusterForNode(node.getAdaptee()),
                this);
    }

    public ClusterAdaptor getGraphicalClusterForNode(NodeAdaptor node) {
        //getClusters() will not be used here, because it seems, that the list
        //of clusters is not always in the same order as the list of nodes.
        ClusterAdaptor containingCluster = ClusterAdaptor.adapt(null, this);
        //(the global pool definitely contains this node)
        for (NodeAdaptor currentNode : getNodes()) {
            if (currentNode.equals(node)) {
                return containingCluster;
            }
            if (currentNode.isCluster()) {
                if (((ClusterAdaptor) currentNode).graphicallyContains(node)) {
                    containingCluster = (ClusterAdaptor) currentNode;
                }
            }
        }

        return containingCluster;
    }

    public LaneableClusterAdaptor getPoolForNode(NodeAdaptor node) {
        return node.isPool() ? (LaneableClusterAdaptor) node
                : new LaneableClusterAdaptor(
                adaptee.getPoolForNode(node.getAdaptee()), this);
    }

    public List<EdgeAdaptor> getIncomingEdges(
            Class<? extends ProcessEdge> type, NodeAdaptor node) {
        return adaptEdgeList(
                adaptee.getIncomingEdges(type, node.getAdaptee()),
                this);
    }

    public List<EdgeAdaptor> getOutgoingEdges(
            Class<? extends ProcessEdge> type, NodeAdaptor node) {
        return adaptEdgeList(
                adaptee.getOutgoingEdges(type, node.getAdaptee()),
                this);
    }

    public List<NodeAdaptor> getSucceedingNodes(
            Class<? extends ProcessEdge> edgeType, NodeAdaptor node) {
        return adaptNodeList(
                adaptee.getSucceedingNodes(edgeType, node.getAdaptee()),
                this);
    }

    public List<NodeAdaptor> getPrecedingNodes(
            Class<? extends ProcessEdge> edgeType, NodeAdaptor node) {
        return adaptNodeList(
                adaptee.getPrecedingNodes(edgeType, node.getAdaptee()),
                this);
    }

    public List<NodeAdaptor> getNeighborNodes(
            Class<? extends ProcessEdge> edgeType, NodeAdaptor node) {
        return adaptNodeList(
                adaptee.getNeighbourNodes(edgeType, node.getAdaptee()), this);
    }

    /**
     * Find an edge of the specified type with the supplied source and target.
     * This does not create an edge.
     * @return one edge of the specified type from source to target or null,
     * if there is no such edge in this model.
     */
    public EdgeAdaptor getEdgeFromTo(Class<? extends ProcessEdge> type,
            NodeAdaptor source, NodeAdaptor target) {
        for (EdgeAdaptor edge : getOutgoingEdges(type, source)) {
            if (edge.getTarget().equals(target)) {
                return edge;
            }
        }
        return null;
    }

    public BPMNModel getAdaptee() {
        return adaptee;
    }

    public List<ClusterAdaptor> getClusters() {
        return adaptNodeList(adaptee.getClusters(), this);
    }

    public List<LaneableClusterAdaptor> getPools() {
        List<LaneableClusterAdaptor> pools =
                new LinkedList<LaneableClusterAdaptor>();
        for (NodeAdaptor node : getClusters()) {
            if (node.isPool()) {
                pools.add((LaneableClusterAdaptor) node);
            }
        }
        if (hasGlobalPool()) {
            pools.add(new LaneableClusterAdaptor(null, this));
        }
        return pools;
    }

    public List<ClusterAdaptor> getProcessClusters() {
        List<ClusterAdaptor> processes = getClusters();
        ListIterator<ClusterAdaptor> iter = processes.listIterator();
        while (iter.hasNext()) {
            if (iter.next().isLane()) {
                iter.remove();
            }
        }
        if (hasGlobalPool()) {
            processes.add(new LaneableClusterAdaptor(null, this));
        }
        return processes;
    }

    /**
     * returns models representing the contained Processes without their Sub-Processes,
     * i.e. there will be a model for each pool and every SubProcess.
     */
    public List<ModelAdaptor> getSubmodelsForNonAdHocProcesses() {
        List<ModelAdaptor> models = new LinkedList<ModelAdaptor>();
        for (ClusterAdaptor cluster : getProcessClusters()) {
            if (!cluster.isAdHocSubProcess()) {
                models.add(subModelFor(cluster));
            }
        }
        return models;
    }

    private ModelAdaptor subModelFor(ClusterAdaptor cluster) {
        ModelAdaptor model = new ModelAdaptor(new BPMNModel(cluster.getText()));
        List<NodeAdaptor> processNodes = cluster.getNodesOfContainedProcess();
        for (NodeAdaptor node : processNodes) {
            model.addNode(node);
        }
        for (EdgeAdaptor edge : getEdges()) {
            if (processNodes.contains(edge.getSource())
                    && processNodes.contains(edge.getTarget())) {
                model.addEdge(edge);
            }
        }
        return model;
    }

    public List<NodeAdaptor> getRootNodes() {
        List<NodeAdaptor> roots = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : getNodes()) {
            if (getClusterForNode(node).isNull()) {
                roots.add(node);
            }
        }
        return roots;
    }

    public List<NodeAdaptor> getRootNodesExceptPools() {
        List<NodeAdaptor> roots = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : getRootNodes()) {
            if (!node.isPool()) {
                roots.add(node);
            }
        }
        return roots;
    }

    public List<NodeAdaptor> getRootProcessNodes() {
        if (hasChoreography() || hasConversation()) {
            return new LinkedList<NodeAdaptor>();
        } else {
            return getRootNodesExceptPools();
        }
    }

    public boolean isColaboration() {
        return getPools().size() > 1;
    }

    public boolean isPureChoreography() {
        return hasChoreography() && getPools().isEmpty();
    }

    public boolean hasChoreography() {
        for (NodeAdaptor node : getNodes()) {
            if (node.isChoreographyActivity()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasConversation() {
        for (NodeAdaptor node : getNodes()) {
            if (node.isConversation()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGlobalPool() {
        boolean hasNonConvesationNodes = false;
        for (NodeAdaptor node : getRootNodesExceptPools()) {
            if (!node.isConversation()) {
                if (!node.isAllowedInChoreography()) {
                    return true;
                } else {
                    hasNonConvesationNodes = true;
                }
            }
        }
        if (hasChoreography()) {
            return false;
        }
        if (hasConversation()) {
            return hasNonConvesationNodes;
        }
        return !getRootNodesExceptPools().isEmpty();
    }

    /*The following methods are used for preprocessing of the model for
    Soundness-checking.*/
    public ModelAdaptor copy() {
        ModelAdaptor copy = new ModelAdaptor(new BPMNModel(adaptee.getProcessName()));
        for (ProcessObjectAdaptor object : getObjects()) {
            copy.addObject(object);
        }
        return copy;
    }

    public void addObject(ProcessObjectAdaptor object) {
        adaptee.addObject(object.getAdaptee());
    }

    public Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> reduceToOneStartEvent() {
        Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedNodesMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
        List<NodeAdaptor> instantiatingGateways = new LinkedList<NodeAdaptor>(),
                startEvents = new LinkedList<NodeAdaptor>(),
                otherInitialNodes = new LinkedList<NodeAdaptor>();
        findNodesWithoutIncomingSequenceFlow(
                startEvents, instantiatingGateways, otherInitialNodes);
        if ((startEvents.size() > 1)
                || !instantiatingGateways.isEmpty()
                || !otherInitialNodes.isEmpty()) {
            NodeAdaptor startNode = new EventAdaptor(new StartEvent(), this);
            addNode(startNode);
            addedNodesMap.put(startNode, new HashSet<ProcessObjectAdaptor>());
            addedNodesMap.get(startNode).addAll(instantiatingGateways);
            addedNodesMap.get(startNode).addAll(startEvents);
            addedNodesMap.get(startNode).addAll(otherInitialNodes);
            if (!otherInitialNodes.isEmpty()) {
                startNode = addCombiningGateway(addedNodesMap,
                        otherInitialNodes, startNode,
                        new GatewayAdaptor(new ParallelGateway(), this));
            }
            if (!startEvents.isEmpty()) {
                startNode = addCombiningGateway(addedNodesMap,
                        startEvents, startNode,
                        new GatewayAdaptor(new ExclusiveGateway(), this));
            }
            if (!instantiatingGateways.isEmpty()) {
                startNode = addCombiningGateway(addedNodesMap,
                        instantiatingGateways, startNode,
                        new GatewayAdaptor(new ParallelGateway(), this));
            }

        }
        return addedNodesMap;
    }

    /**
     * adds a gateway, that has outgoing SequenceFlow to all the nodes in
     * intialNodes and incomming SequenceFlow from the startNode. The added node
     * will be reported in the addedNodesMap.
     */
    private NodeAdaptor addCombiningGateway(
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap,
            List<NodeAdaptor> initialNodes,
            NodeAdaptor startNode,
            GatewayAdaptor combiningGateway) {
        addNode(combiningGateway);
        addedObjectsMap.put(combiningGateway,
                new HashSet<ProcessObjectAdaptor>(initialNodes));
        addSequenceFlow(startNode, combiningGateway);
        for (NodeAdaptor node : initialNodes) {
            if (node.isStartEvent() || node.isEndEvent()) {
                node = replaceByIntermediateEvent((EventAdaptor) node, addedObjectsMap);
            }
            EdgeAdaptor added = addSequenceFlow(combiningGateway, node);
            addedObjectsMap.put(added, new HashSet<ProcessObjectAdaptor>());
            addedObjectsMap.get(added).add(node);
        }
        return combiningGateway;
    }

    /**
     * Scans the nodes of a process in order to find all the nodes, that have
     * no incomming SequenceFlow. They will be added to the appropriate collection
     * @param process the process, which´s initial nodes should be found
     * @param addedNodesMap a mapping, where the processNodes, which have
     * been added are reported. ProcessNodes are only added by this method, when
     * SubProcesses are found and therefore reduceProcessToOneStartEvent is
     * called.
     * @param startEvents a list, to which the StartEvents of this Process will
     * be added
     * @param instantiatingGateways a list, to which the instantiating Gateways
     * of this process will be added
     * @param otherInitialNodes a list, to which other nodes without incomming
     * SequenceFlow will be added.
     */
    private void findNodesWithoutIncomingSequenceFlow(
            List<NodeAdaptor> startEvents,
            List<NodeAdaptor> instantiatingGateways,
            List<NodeAdaptor> otherInitialNodes) {
        for (NodeAdaptor node : getNodes()) {
            if (node.getIncomingSequenceFlow().isEmpty()) {
                if (node.isStartEvent()) {
                    startEvents.add(node);
                } else if (node.isInstantiatingGateway()) {
                    instantiatingGateways.add(node);
                } else if (node.shouldHaveIncommingSequenceFlow()
                        && !(node.isEventSubProcess()
                        || (node.isTask()
                        && ((ActivityAdaptor) node).isCompensationTask())
                        || (node.isSubProcess()
                        && ((ClusterAdaptor) node).isCompensationSubProcess()))) {
                    otherInitialNodes.add(node);
                }
            }
        }
    }

    public Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> transformAttachmentsToGateways() {
        Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
        for (NodeAdaptor node : getNodes()) {
            if (node.isEvent()) {
                EventAdaptor event = (EventAdaptor) node;
                if (event.isAttached()
                        && !event.isCompensationIntermediateEvent()) {
                    NodeAdaptor parent = event.getParentNode();
                    GatewayAdaptor insertedGW = null;
                    if (event.isInterrupting()) {
                        insertedGW = 
                                insertGatewayBefore(parent, addedObjectsMap);
                    }
                    EventAdaptor copy = (EventAdaptor)event.copy();
                    ((AttachedNode) copy.getAdaptee()).setParentNode(null);
                    replaceNode(event, copy, addedObjectsMap);
                    EdgeAdaptor added = null;
                    if (event.isInterrupting()) {
                        added = addSequenceFlow(insertedGW, copy);
                    } else {
                        added = addSequenceFlow(parent, copy);
                        added.getAdaptee().setProperty(SequenceFlow.PROP_SEQUENCETYPE,
                                SequenceFlow.TYPE_CONDITIONAL);
                    }
                    addedObjectsMap.put(added, new HashSet<ProcessObjectAdaptor>());
                    addedObjectsMap.get(added).add(event);
                }
            }
        }
        return addedObjectsMap;
    }

    private GatewayAdaptor insertGatewayBefore(NodeAdaptor node,
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap) {
        GatewayAdaptor insertedGW =
                new GatewayAdaptor(new ExclusiveGateway(), this);
        addNode(insertedGW);
        addedObjectsMap.put(insertedGW, new HashSet<ProcessObjectAdaptor>());
        addedObjectsMap.get(insertedGW).add(node);
        ClusterAdaptor cluster = getClusterForNode(node);
        if (!cluster.isNull()) {
            cluster.getAdaptee().addProcessNode(insertedGW.getAdaptee());
        }
        redicrectEdges(node, insertedGW, addedObjectsMap, EdgeSelection.incoming);
        EdgeAdaptor added = addSequenceFlow(insertedGW, node);
        addedObjectsMap.put(added, new HashSet<ProcessObjectAdaptor>());
        addedObjectsMap.get(added).add(node);
        return insertedGW;
    }

    public Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> reduceToOneEndEventPerProcess() {
        Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
        List<NodeAdaptor> finalNodes = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : getNodes()) {
            if (node.getOutgoingSequenceFlow().isEmpty()
                    && (node.shouldHaveOutgoingSequenceFlow()
                    || node.isEndEvent())) {
                finalNodes.add(node);
            }
        }
        if (finalNodes.size() > 1) {
            GatewayAdaptor addedGateway =
                    new GatewayAdaptor(new InclusiveGateway(), this);
            addNode(addedGateway);
            addedObjectsMap.put(addedGateway,
                    new HashSet<ProcessObjectAdaptor>(finalNodes));
            for (NodeAdaptor node : finalNodes) {
                if (node.isEndEvent()) {
                    node = replaceByIntermediateEvent(
                            (EventAdaptor) node, addedObjectsMap);
                }
                EdgeAdaptor addedFlow = addSequenceFlow(node, addedGateway);
                addedObjectsMap.put(addedFlow, new HashSet<ProcessObjectAdaptor>());
                addedObjectsMap.get(addedFlow).add(node);
            }
            EventAdaptor addedEndEvent =
                    new EventAdaptor(new EndEvent(0, 0, "end"), this);
            addNode(addedEndEvent);
            addedObjectsMap.put(addedEndEvent,
                    new HashSet<ProcessObjectAdaptor>(finalNodes));
            EdgeAdaptor addedFlow = addSequenceFlow(addedGateway, addedEndEvent);
            addedObjectsMap.put(addedFlow,
                    new HashSet<ProcessObjectAdaptor>(finalNodes));
        }
        return addedObjectsMap;
    }

    public void removeArtifactsAndMessages() {
        for (NodeAdaptor node : getNodes()) {
            if (node.isArtifact() || node.isMessage()) {
                adaptee.removeNode(node.getAdaptee());
            }
        }
    }

    public void removeEventSubProcessesAndCompenstaionHandlers() {
        for (NodeAdaptor node : getNodes()) {
            if (node.isEventSubProcess() || node.isForCompensation()) {
                adaptee.removeNode(node.getAdaptee());
            }
        }
    }

    public Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>
            connectLinkEventsViaSequenceFlow() {
        Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
        for (NodeAdaptor node : getNodes()) {
            if (node.isEvent()
                    && ((EventAdaptor) node).isThrowingLinkIntermediateEvent()) {
                connectLinkFrom((EventAdaptor) node, addedObjectsMap);
            }
        }
        return addedObjectsMap;
    }

    private void connectLinkFrom(EventAdaptor throwingLink,
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap) {
        for (NodeAdaptor node : getNodes()) {
            if (node.isEvent()
                    && ((EventAdaptor) node).isCatchingLinkIntermediateEvent()
                    && node.getText().equals(throwingLink.getText())) {
                EdgeAdaptor addedFlow = addSequenceFlow(throwingLink, node);
                addedObjectsMap.put(addedFlow, new HashSet<ProcessObjectAdaptor>());
                addedObjectsMap.get(addedFlow).add(throwingLink);
                addedObjectsMap.get(addedFlow).add(node);
                return;
            }
        }
    }

    private void addNode(NodeAdaptor node) {
        adaptee.addNode(node.getAdaptee());
    }

    private EventAdaptor replaceByIntermediateEvent(EventAdaptor startOrEndEvent,
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap) {
        EventAdaptor newEvent = startOrEndEvent.getIntermediateEventWithSameTrigger();
        replaceNode(startOrEndEvent, newEvent, addedObjectsMap);
        return newEvent;
    }

    private void replaceNode(NodeAdaptor oldNode, NodeAdaptor newNode,
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap) {
        addNode(newNode);
        addedObjectsMap.put(newNode, new HashSet<ProcessObjectAdaptor>());
        addedObjectsMap.get(newNode).add(oldNode);
        redicrectEdges(oldNode, newNode, addedObjectsMap, EdgeSelection.incoming);
        redicrectEdges(oldNode, newNode, addedObjectsMap, EdgeSelection.outgoing);
        getAdaptee().removeNode(oldNode.getAdaptee());
    }

    private enum EdgeSelection {
        incoming {
            @Override
            public List<EdgeAdaptor> getForNode(NodeAdaptor node) {
                return node.getIncomingSequenceFlow();
            }
        },
        outgoing {
            @Override
            public List<EdgeAdaptor> getForNode(NodeAdaptor node) {
                return node.getOutgoingSequenceFlow();
            }
        };
        public abstract List<EdgeAdaptor> getForNode(NodeAdaptor node);
    };

    private void redicrectEdges(NodeAdaptor from, NodeAdaptor to,
            Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap,
            EdgeSelection direction) {
        for (EdgeAdaptor edge : direction.getForNode(from)) {
            EdgeAdaptor replacement = addSequenceFlow(
                    direction.equals(EdgeSelection.incoming)? 
                        edge.getSource() : to,
                    direction.equals(EdgeSelection.outgoing)?
                        edge.getTarget() : to);
            addedObjectsMap.put(replacement, new HashSet<ProcessObjectAdaptor>());
            addedObjectsMap.get(replacement).add(edge);
            removeEdge(edge);
        }
    }

    public Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>
            interceptDirectEdgesFromStartToEnd() {
        Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> addedObjectsMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
        for (EdgeAdaptor edge : getEdges()) {
            if (edge.isSequenceFlow()
                    && edge.getSource().isStartEvent()
                    && edge.getTarget().isEndEvent()) {
                IntermediateEvent rawEvent = new IntermediateEvent();
                EventAdaptor event = new EventAdaptor(rawEvent, this);
                addNode(event);
                addedObjectsMap.put(event, new HashSet<ProcessObjectAdaptor>());
                addedObjectsMap.get(event).add(edge);
                SequenceFlow
                        rawFlow1 = new SequenceFlow(edge.getSource().getAdaptee(), rawEvent),
                        rawFlow2 = new SequenceFlow(rawEvent, edge.getTarget().getAdaptee());
                EdgeAdaptor flow1 = new EdgeAdaptor(rawFlow1, this),
                        flow2 = new EdgeAdaptor(rawFlow2, this);
                addEdge(flow1);
                addEdge(flow2);
                addedObjectsMap.put(flow1, new HashSet<ProcessObjectAdaptor>());
                addedObjectsMap.put(flow2, new HashSet<ProcessObjectAdaptor>());
                addedObjectsMap.get(flow1).add(edge);
                addedObjectsMap.get(flow2).add(edge);
                removeEdge(edge);
            }
        }
        return addedObjectsMap;
    }

    private void addEdge(EdgeAdaptor edge) {
        adaptee.addEdge(edge.getAdaptee());
    }

    private void removeEdge(EdgeAdaptor edge) {
        adaptee.removeEdge(edge.getAdaptee());
    }

    private EdgeAdaptor addSequenceFlow(NodeAdaptor source, NodeAdaptor target) {
        ProcessEdge added = new SequenceFlow(source.getAdaptee(), target.getAdaptee());
        adaptee.addEdge(added);
        return new EdgeAdaptor(added, this);
    }

    public void removeAll() {
        for (ProcessObject object: adaptee.getObjects()) {
            adaptee.removeObject(object);
        }
    }
}
