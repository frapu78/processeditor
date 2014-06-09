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
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 *
 * @author tmi
 */
public class ClusterAdaptor extends NodeAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof Cluster;
    }

    public static ClusterAdaptor adapt(Cluster adaptee, ModelAdaptor model) {
        if (LaneableClusterAdaptor.canAdapt(adaptee)) {
            return new LaneableClusterAdaptor(adaptee, model);
        } else if (SubChoreographyAdaptor.canAdapt(adaptee)) {
            return new SubChoreographyAdaptor(adaptee, model);
        }else {
            return new ClusterAdaptor(adaptee, model);
        }
    }

    protected ClusterAdaptor(ProcessNode cluster, ModelAdaptor model) {
        super(cluster, model);
    }

    public ClusterAdaptor(Cluster adaptee, ModelAdaptor model) {
        super (adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return ClusterAdaptor.canAdapt(node);
    }

    @Override
    public Cluster getAdaptee() {
        return (Cluster) super.getAdaptee();
    }

    @Override
    public boolean isAllowedInBPD() {
        return isSubProcess() ||
                isLaneableCluster() ||
                isSubChoreography();
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public boolean isPool() {
        return false;
    }

    @Override
    public boolean isLane() {
        return false;
    }

    public List<NodeAdaptor> getProcessNodes() {
        if (getAdaptee() == null) return model.getRootProcessNodes();
        //Null-Cluster is to be understood as the global Pool.
        return adaptNodeList(getAdaptee().getProcessNodes(), model);
    }

    public boolean contains(NodeAdaptor node) {
        return getAdaptee().isContained(node.getAdaptee());
    }

    public boolean graphicallyContains(NodeAdaptor node) {
        return getAdaptee().isContainedGraphically(
                model.getAdaptee().getNodes(), node.getAdaptee(), false);
    }

    @Override
    public boolean isSubProcess() {
        return getAdaptee() instanceof SubProcess;
    }

    public boolean isWhiteboxSubProcess() {
        return isSubProcess() && getProperty(SubProcess.PROP_COLLAPSED).
                equals(SubProcess.FALSE);
    }

    public boolean isWhiteboxSubChoreography() {
        return false;
    }

    public boolean isAdHocSubProcess() {
        return isSubProcess() &&
                getProperty(SubProcess.PROP_AD_HOC).equals(SubProcess.TRUE);
    }

    @Override
    public boolean isActivity() {
        return isSubProcess();
    }

    @Override
    public boolean isEventSubProcess() {
        return isSubProcess() &&
                getProperty(SubProcess.PROP_EVENT_SUBPROCESS).
                equals(SubProcess.TRUE);
    }

    public boolean isTransaction() {
        return isSubProcess() &&
                getProperty(SubProcess.PROP_TRANSACTION).equals(SubProcess.TRUE);
    }

    public boolean isCompensationSubProcess() {
        return isSubProcess() &&
                getProperty(SubProcess.PROP_COMPENSATION).equals(SubProcess.TRUE);
    }

    /**
     * Returns the nodes, that belong to the Process, which this cluster
     * contains.That means, it returns any nodes directly contained in this
     * cluster for a non-LaneableCluster.
     * For a LaneableCluster (see {@link LaneableClusterAdaptor}) it will not
     * return the lanes, which this cluster contains, but the nodes contained by
     * this lanes.
     */
    public List<NodeAdaptor> getNodesOfContainedProcess() {
        return getProcessNodes();
    }

    public List<ClusterAdaptor> getClusters() {
        List<ClusterAdaptor> clusters = new LinkedList<ClusterAdaptor>();
        for (NodeAdaptor node : getProcessNodes()) {
            if (node.isCluster()) clusters.add((ClusterAdaptor)node);
        }
        return clusters;
    }

    @Override
    public boolean shouldHaveIncommingSequenceFlow() {
        return ! (isEventSubProcess() || isCompensationSubProcess());
    }

    @Override
    public boolean shouldHaveOutgoingSequenceFlow() {
        return ! (isEventSubProcess() || isCompensationSubProcess());
    }

    @Override
    public boolean mayHavIncommingSequenceFlow() {
        return ! isEventSubProcess();
    }

    @Override
    public boolean mayHaveOutgoingSequenceFlow() {
        return ! isEventSubProcess();
    }

    @Override
    public boolean mayHaveIncommingMessageFlow() {
        return isSubProcess();
    }

    @Override
    public boolean mayHaveOutgoingMessageFlow() {
        return isSubProcess();
    }

    @Override
    public boolean isForCompensation() {
        return isCompensationSubProcess();
    }
}
