/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.FlowObject;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Adapts a BPMN-Node for the specific needs of Validation.
 * @author tmi
 */
public class NodeAdaptor extends AbstractAdaptor implements ProcessObjectAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node == null
                || node instanceof Message
                || node instanceof EdgeDocker
                || node instanceof Conversation;
    }

    public static NodeAdaptor adapt(ProcessNode adaptee, ModelAdaptor model) {
        if (ActivityAdaptor.canAdapt(adaptee)) {
            return new ActivityAdaptor(adaptee, model);
        }
        if (ArtifactAdaptor.canAdapt(adaptee)) {
            return new ArtifactAdaptor(adaptee, model);
        }
        if (LaneableClusterAdaptor.canAdapt(adaptee)) {
            return new LaneableClusterAdaptor(adaptee, model);
        }
        if (SubChoreographyAdaptor.canAdapt(adaptee)) {
            return new SubChoreographyAdaptor(adaptee, model);
        }
        if (ClusterAdaptor.canAdapt(adaptee)) {
            return new ClusterAdaptor(adaptee, model);
        }
        if (EventAdaptor.canAdapt(adaptee)) {
            return new EventAdaptor(adaptee, model);
        }
        if (GatewayAdaptor.canAdapt(adaptee)) {
            return new GatewayAdaptor(adaptee, model);
        }
        if (ChoreographyActivityAdaptor.canAdapt(adaptee)) {
            return new ChoreographyActivityAdaptor(adaptee, model);
        }
        return new NodeAdaptor(adaptee, model);
    }

    private ProcessNode adaptee;
    protected ModelAdaptor model;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected NodeAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        if (isAdaptable(adaptee)) {
            this.adaptee = adaptee;
            this.model = model;
        } else {
            throw new IllegalArgumentException("Cannot adapt " + adaptee);
        }
    }

    public boolean isAdaptable(ProcessNode node) {
        return NodeAdaptor.canAdapt(node);
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isEdge() {
        return false;
    }

    @Override
    public ProcessNode getAdaptee() {
        return adaptee;
    }

    public String getId() {
        return adaptee.getId();
    }

    public boolean isNull() {
        return adaptee == null;
    }

    public boolean isLaneableCluster() {
        return false;
    }

    public boolean isPool() {
        return false;
    }

    public boolean isEvent() {
        return false;
    }

    public boolean isActivity() {
        return false;
    }

    public boolean isTask() {
        return false;
    }

    public boolean isGateway() {
        return false;
    }

    public boolean isDecisionGateway() {
        return false;
    }

    public boolean isExclusiveJoin() {
        return true;
    }

    public boolean isArtifact() {
        return false;
    }

    public boolean isCluster() {
        return false;
    }

    public boolean isSubProcess() {
        return false;
    }

    public boolean isLane() {
        return false;
    }

    public boolean isMessage() {
        return getAdaptee() instanceof Message;
    }

    public boolean isConversation() {
        return adaptee instanceof Conversation;
    }

    public boolean isAllowedInBPD() {
        return isFlowObject() || isEdgeDocker();
    }

    public boolean isAllowedInChoreography() {
        return isMessage() || isEdgeDocker();
    }

    public boolean isEdgeDocker() {
        return adaptee instanceof EdgeDocker;
    }

    public boolean isFlowObject() {
        return adaptee instanceof FlowObject;
    }

    public boolean isChoreographyActivity() {
        return false;
    }

    public boolean isChoreographyTask() {
        return false;
    }

    public boolean isSubChoreography() {
        return false;
    }

    public boolean isRootNode() {
        return model.getClusterForNode(this).isNull();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof NodeAdaptor)) {
            return false;
        }
        if (adaptee == null) {
            return ((NodeAdaptor) otherObject).getAdaptee() == null;
        }
        return adaptee.equals(((NodeAdaptor) otherObject).getAdaptee());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.adaptee != null ? this.adaptee.hashCode() : 0);
        return hash;
    }

    public NodeAdaptor copy() {
        return NodeAdaptor.adapt(adaptee.copy(), model);
    }

    @Override
    public String getProperty(String key) {
        return adaptee.getProperty(key);
    }

    public String getText() {
        if (isNull()) return "";
        return adaptee.getText();
    }

    public List<NodeAdaptor> getAttachedEvents(ModelAdaptor model) {
        List<NodeAdaptor> attachedEvents = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : model.getNodes()) {
            if (node.isEvent()) {
                if (((EventAdaptor) node).getParentNode().equals(this)) {
                    attachedEvents.add(node);
                }
            }
        }
        return attachedEvents;
    }

    public Rectangle getBounds() {
        return adaptee.getBounds();
    }

    public List<EdgeAdaptor> getIncomingSequenceFlow() {
        return getIncomingEdges(SequenceFlow.class);
    }

    public List<EdgeAdaptor> getOutgoingSequenceFlow() {
        return getOutgoingEdges(SequenceFlow.class);
    }

    public List<EdgeAdaptor> getIncomingEdges(Class<? extends ProcessEdge> type) {
        return model.getIncomingEdges(type, this);
    }

    public List<EdgeAdaptor> getOutgoingEdges(Class<? extends ProcessEdge> type) {
        return model.getOutgoingEdges(type, this);
    }

    public List<EdgeAdaptor> getAdjacentEdges(Class<? extends ProcessEdge> type) {
        List<EdgeAdaptor> edges = getIncomingEdges(type);
        edges.addAll(getOutgoingEdges(type));
        return edges;
    }

    public List<NodeAdaptor> getSucceedingNodes() {
        return model.getSucceedingNodes(SequenceFlow.class, this);
    }

    public List<NodeAdaptor> getPrecedingNodes() {
        return model.getPrecedingNodes(SequenceFlow.class, this);
    }

    public List<NodeAdaptor> getNeighborNodes(Class<? extends ProcessEdge> type) {
        return model.getNeighborNodes(type, this);
    }

    public boolean shouldHaveIncommingSequenceFlow() {
        return false;
    }

    public boolean shouldHaveOutgoingSequenceFlow() {
        return false;
    }

    public boolean mayHavIncommingSequenceFlow() {
        return isChoreographyActivity();
    }

    public boolean mayHaveOutgoingSequenceFlow() {
        return isChoreographyActivity();
    }
    
    public boolean mayHaveIncommingMessageFlow() {
        return false;
    }

    public boolean mayBeConnectedByConversationLink() {
        return mayHaveIncommingMessageFlow() ||
                mayHaveOutgoingMessageFlow() ||
                isConversation();
    }

    public boolean mayHaveOutgoingMessageFlow() {
        return false;
    }

    public ClusterAdaptor getContainingProcess() {
        ClusterAdaptor cluster = model.getClusterForNode(this);
        while (! (cluster.isPool() || cluster.isSubProcess())) {
            cluster = model.getClusterForNode(cluster);
        }
        return cluster;
    }

    @Override
    public String toString() {
        if (adaptee == null) {
            return "null";
        }
        return adaptee.toString();
    }

    public boolean isReceiveTask() {
        return false;
    }

    public boolean isServiceTask() {
        return false;
    }

    public boolean isSendTask() {
        return false;
    }

    public boolean isMessageIntermediateEvent() {
        return false;
    }

    public boolean isStartEvent() {
        return false;
    }

    public boolean isNoneStartEvent() {
        return false;
    }

    public boolean isInstantiatingGateway() {
        return false;
    }

    public boolean isExclusiveInstantiatingGateway() {
        return false;
    }

    public boolean isEventBasedGateway() {
        return false;
    }

    public boolean isParallelGateway() {
        return false;
    }

    public boolean isEndEvent() {
        return false;
    }

    public boolean isEventSubProcess() {
        return false;
    }

    public boolean isMessageEvent() {
        return false;
    }

    public boolean isTextAnnotation() {
        return false;
    }

    public boolean isData() {
        return false;
    }

    public boolean isForCompensation() {
        return false;
    }
}
