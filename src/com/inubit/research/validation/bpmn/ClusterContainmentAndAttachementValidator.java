/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Checks, that cluster-containments and attachement-relations are right. More
 * specifically, it checks the following points:
 * <ul>
 * <li>no cluster contains itself (neither directly nor indirectly)</li>
 * <li>(iff a node is graphically completely contained in a cluster, it is
 * contained in the nodes-list of the cluster)</li>
 * <li>(iff a node is lying on the border of another node, the first node is
 * attached to the second node, if this is allowed. Otherwise it should not be
 * located at the border.)</li>
 * <li>each node is contained in at most one cluster</li>
 * <li>if a cluster node A is attached to a node B, which is contained in
 * cluster C, node A is also contained in C</li>
 * </ul>
 * @author tmi
 */
class ClusterContainmentAndAttachementValidator {

    private ModelAdaptor model;
    private BPMNValidator validator;

    public ClusterContainmentAndAttachementValidator(
            ModelAdaptor model, BPMNValidator validator) {
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() throws RecursiveClusterContainmentException {
        checkForSelfContainingClusters();
        checkSingleNodeContainments();
    }

    private void checkForSelfContainingClusters()
            throws RecursiveClusterContainmentException {
        List<ClusterAdaptor> selfContainingClusters =
                new LinkedList<ClusterAdaptor>();
        for (ClusterAdaptor cluster : model.getClusters()) {
            if (isSelfContaining(cluster)) {
                selfContainingClusters.add(cluster);
            }
        }
        if (!selfContainingClusters.isEmpty()) {
            validator.addMessage("selfContainingCluster", selfContainingClusters);
            throw new RecursiveClusterContainmentException(selfContainingClusters);
        }
    }

    private boolean isSelfContaining(ClusterAdaptor root) {
        Set<ClusterAdaptor> visited = new HashSet<ClusterAdaptor>(),
                current = new HashSet<ClusterAdaptor>();
        current.add(root);
        while (!current.isEmpty()) {
            visited.addAll(current);
            Set<ClusterAdaptor> currentCopy = new HashSet<ClusterAdaptor>(current);
            current.clear();
            for (ClusterAdaptor cluster : currentCopy) {
                for (ClusterAdaptor subCluster : cluster.getClusters()) {
                    if (subCluster.equals(root)) {
                        return true;
                    }
                    if (!visited.contains(subCluster)) {
                        //avoids loops, that do not involve root
                        current.add(subCluster);
                    }
                }
            }
        }
        return false;
    }

    private void checkSingleNodeContainments() {
        for (NodeAdaptor node : model.getNodes()) {
            if (!(node.isEdgeDocker() || node.isArtifact() || node.isMessage())) {
                if (checkNodeIsInOnlyOneCluster(node)) {
                    if (node.isEvent()) {
                        checkEvent((EventAdaptor) node);
                    }
                    if (!node.isEdgeDocker()) {
                        checkGraphicalContainmentEqualsContainment(node);
                    }
                }
            }
        }
    }

    private boolean checkNodeIsInOnlyOneCluster(NodeAdaptor node) {
        List<ClusterAdaptor> containingClusters =
                new LinkedList<ClusterAdaptor>();
        for (ClusterAdaptor cluster : model.getClusters()) {
            if (cluster.contains(node)) {
                containingClusters.add(cluster);
            }
        }
        if (containingClusters.size() > 1) {
            validator.addMessage("nodeInMultipleClusters", node);
            return false;
        }
        return true;
    }

    private void checkEvent(EventAdaptor event) {
        if (event.isAttached()) {
            checkIsContainedInItsParentsCluster(event);
            checkIsLocatedAtParentNodeBoundary(event);
        } else {
            checkIsNotLocatedAtAnyBoundary(event);
        }
    }

    private void checkIsContainedInItsParentsCluster(EventAdaptor event) {
        NodeAdaptor parent = event.getParentNode();
            if (!model.getClusterForNode(parent).
                    equals(model.getClusterForNode(event))) {
                validator.addMessage("attachedIntermediateEventNotInSurroundingCluster",
                        event);
            }
    }

    private void checkIsLocatedAtParentNodeBoundary(EventAdaptor event) {
        if (!intersectsBoundaryOf(event, event.getParentNode())) {
            validator.addMessage("attachedNodeNotLocatedAtParentBorder", event);
        }
    }

    private void checkIsNotLocatedAtAnyBoundary(NodeAdaptor node) {
        for (NodeAdaptor current : model.getNodes()) {
            if (!current.equals(node) &&
                    (current.isActivity() ||
                        current.isCluster() ||
                        current.isEvent()) &&
                    intersectsBoundaryOf(node, current)) {
                validator.addMessage("nodeSeemsAttachedButIsNot", node);
                return;
            }
        }
    }

    /**
     * checks, whether first intersects the boundary of second.
     */
    private boolean intersectsBoundaryOf(NodeAdaptor first, NodeAdaptor second) {
        Rectangle firstBounds = first.getBounds();
        Rectangle secondBounds = second.getBounds();
        int left = secondBounds.x,
                right = secondBounds.x + secondBounds.width,
                top = secondBounds.y,
                bottom = secondBounds.y + secondBounds.height;
        if (firstBounds.intersectsLine(left, top, right, top)) {
            return true;
        }
        if (firstBounds.intersectsLine(left, top, left, bottom)) {
            return true;
        }
        if (firstBounds.intersectsLine(left, bottom, right, bottom)) {
            return true;
        }
        if (firstBounds.intersectsLine(right, bottom, right, top)) {
            return true;
        }
        return false;
    }

    private void checkGraphicalContainmentEqualsContainment(NodeAdaptor node) {
        ClusterAdaptor graphicalCluster = model.getGraphicalClusterForNode(node),
                modelCluster = model.getClusterForNode(node);
        if (graphicalCluster.isNull() && !modelCluster.isNull()) {
            validator.addMessage("containedButNotGraphicallyContained", node);
        } else if (!graphicalCluster.equals(modelCluster)) {
            validator.addMessage("graphicalContainedInAnotherClusterThanReallyContainedIn", node);
        }
    }
}
