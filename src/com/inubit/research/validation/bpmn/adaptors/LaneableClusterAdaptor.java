/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.LaneableCluster;
import net.frapu.code.visualization.bpmn.Pool;

/**
 *
 * @author tmi
 */
public class LaneableClusterAdaptor extends ClusterAdaptor {
    
    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof LaneableCluster;
    }

    protected LaneableClusterAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public LaneableClusterAdaptor(LaneableCluster adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return LaneableClusterAdaptor.canAdapt(node);
    }

    @Override
    public boolean isLaneableCluster() {
        return true;
    }

    @Override
    public boolean isPool() {
        return getAdaptee() == null
                //because a Null-LaneableClusterAdaptor is to be understood as
                //the global pool.
                || getAdaptee() instanceof Pool;
    }

    @Override
    public boolean isLane() {
        return getAdaptee() instanceof Lane;
    }

    public List<NodeAdaptor> recursivelyGetProcessNodesFromLanes() {
        List<NodeAdaptor> contents =
                new LinkedList<NodeAdaptor>(getNodesOfContainedProcess());
        for (LaneableClusterAdaptor lane : getLanes()) {
            contents.addAll(lane.recursivelyGetProcessNodesFromLanes());
        }
        return contents;
    }

    public List<LaneableClusterAdaptor> getLanes() {
        if (isNull()) return new LinkedList<LaneableClusterAdaptor>();
        return adaptNodeList(((LaneableCluster) getAdaptee()).getLanes(), model);
    }

    @Override
    public List<NodeAdaptor> getNodesOfContainedProcess() {
        List<NodeAdaptor> nodes = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : getProcessNodes()) {
            if (!node.isLaneableCluster()) nodes.add(node);
            else {
                nodes.addAll(((LaneableClusterAdaptor)node).
                        getNodesOfContainedProcess());
            }
        }
        return nodes;
    }

    @Override
    public boolean shouldHaveIncommingSequenceFlow() {
        return false;
    }

    @Override
    public boolean shouldHaveOutgoingSequenceFlow() {
        return false;
    }

    @Override
    public boolean mayHavIncommingSequenceFlow() {
        return false;
    }

    @Override
    public boolean mayHaveOutgoingSequenceFlow() {
        return false;
    }

    @Override
    public boolean graphicallyContains(NodeAdaptor node) {
        if (node.isLaneableCluster()) {
            return getLanes().contains((LaneableClusterAdaptor)node);
        } else {
            return super.graphicallyContains(node);
        }
    }

    @Override
    public boolean mayHaveIncommingMessageFlow() {
        return isPool();
    }

    @Override
    public boolean mayHaveOutgoingMessageFlow() {
        return isPool();
    }

    @Override
    public boolean isAllowedInChoreography() {
        return isPool();
    }
}
