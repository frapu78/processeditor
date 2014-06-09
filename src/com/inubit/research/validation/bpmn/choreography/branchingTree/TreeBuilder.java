/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.ChoreographyNodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * TreeBuilder lets you build a BranchingTree from a BPMNModel starting at a
 * given ProcessNode and following the SequenceFlow in its own direction or
 * in its opposite direction
 * @author tmi
 */
public class TreeBuilder {

    public TreeBuilder() {
    }

    /**
     * enum for specyfying in which direction a Tree should be built.
     */
    public enum FlowDirection {

        /**
         * build the tree in the opposite direction of the SequenceFlow
         */
        flowBefore,
        /**
         * build the tree following the direction of the SequenceFlow
         */
        flowAfter;
    }

    /**
     * builds a BranchingTree
     * @param node the node, from which the tree should emerge
     * @param direction states,
     */
    public BranchingTree buildTreeFor(NodeAdaptor node, FlowDirection direction) {
        BranchingTreeRoot tree = new BranchingTreeRoot(null);
        tree.setNext(newTreeNodeFor(node, tree, direction));
        boolean stillChanging = true;
        HashSet<BranchingTree> activeNodes = new HashSet<BranchingTree>();
        activeNodes.add(tree.getFirst());
        while (stillChanging) {
            stillChanging = false;
            for (BranchingTree curr : (HashSet<BranchingTree>) activeNodes.clone()) {
                stillChanging |= nextNodeForTree(curr, activeNodes, direction);
            }
        }
        tree.removeClosedPathes();
        return tree;
    }

    /**
     * creates the next BranchingTree-node for a given node
     * @param tree the BranchingTree-node for wich to create children
     * @param activeNodes the set of nodes, that are currently active (needed in
     * order to be updated)
     * @param direction the direction, in which the tree is to be built
     * @return true, if new BranchingTree-nodes were generated
     */
    private boolean nextNodeForTree(BranchingTree tree,
            HashSet<BranchingTree> activeNodes, FlowDirection direction) {
        boolean changed = false;
        Collection<NodeAdaptor> nodes =
                neighborNodesOfForDirection(tree.getNode(), direction);
        for (NodeAdaptor node : nodes) {
            BranchingTree addedNode = addAsNewNodeAfter(node, tree, direction);
            if (addedNode != null) {
                activeNodes.add(addedNode);
                changed = true;
            }
        }
        activeNodes.remove(tree);
        return changed;
    }

    /**
     * generates a new BranchingTree-node from a ProcessNode, but does not add it
     * to any tree
     * @param node the ProcessNode that is to be represented by the new
     * BranchingTree-node
     * @param parent the designated parent-node of the node
     * @param direction the direction in which the tree is built
     */
    private BranchingTree newTreeNodeFor(
            NodeAdaptor node, BranchingTree parent, FlowDirection direction) {
        if (isAlternativeSplit(node, direction)) {
            return new AlternativeSplit(parent, node);
        } else if (isParallelSplit(node, direction)) {
            return new ParallelSplit(parent, node);
        } else if (node.isChoreographyActivity()) {
            return new ActivityNode(parent, (ChoreographyNodeAdaptor)node);
        } else if (node.isEvent()) {
            return new SilentNode(parent, node);
        }
        return null;
    }

    private boolean isAlternativeSplit(NodeAdaptor node, FlowDirection direction) {
        if (node.isGateway()) {
            GatewayAdaptor gateway = (GatewayAdaptor) node;
            return gateway.isExclusiveGateway()
                    || gateway.isInclusiveGateway()
                    || gateway.isEventBasedGateway()
                    || gateway.isComplexGateway();
        } else if (direction.equals(FlowDirection.flowAfter)) {
            for (EdgeAdaptor sequenceFlow : node.getOutgoingSequenceFlow() ) {
                if (sequenceFlow.isConditionalSequenceFlow()) return true;
            }
            return false;
        } else {
            return node.getIncomingSequenceFlow().size() > 1;
        }
    }

    private boolean isParallelSplit(NodeAdaptor node, FlowDirection direction) {
        if (node.isGateway()) {
            return ((GatewayAdaptor)node).isParallelGateway();
        } else if (direction.equals(FlowDirection.flowAfter)) {
            List<EdgeAdaptor> outgoingSequenceFlow =
                    node.getOutgoingSequenceFlow();
            for (EdgeAdaptor sequenceFlow :  outgoingSequenceFlow) {
                if (sequenceFlow.isConditionalSequenceFlow()) return false;
            }
            return outgoingSequenceFlow.size() > 1;
        } else {
            return false;
        }
    }

    /**
     * returns the neighbor-nodes of a ProcessNode in the given direction
     */
    private Collection<NodeAdaptor> neighborNodesOfForDirection(
            NodeAdaptor node, FlowDirection direction) {
        if (direction.equals(FlowDirection.flowAfter)) {
            return node.getSucceedingNodes();
        } else {
            return node.getPrecedingNodes();
        }
    }

    /**
     * adds a new BranchingTree-node for a supplied ProcessNode to a BranchingTree
     * @param node the ProcessNode for the new BranchingTree-node
     * @param tree the BranchingTree-node, which the new BranchingTree-node should
     * succeed
     * @param direction the direction in which the tree is built
     * @return the BranchingTree-node, that was added, or null if node is no node
     * from which a BranchingTree-node can be generated.
     */
    private BranchingTree addAsNewNodeAfter(NodeAdaptor node, BranchingTree tree,
            FlowDirection direction) {
        if (tree.pathFromRootCount(node) <= 1) {
            BranchingTree newNode = newTreeNodeFor(node, tree, direction);
            if (newNode != null) {
                tree.setNext(newNode);
                return newNode;
            } else if (direction.equals(FlowDirection.flowBefore)) {
                tree.closePath(null);
            }
        } else {
            tree.closePath(null);
        }
        return null;
    }
}
