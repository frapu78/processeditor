/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.branchingTree;

import com.inubit.research.gui.plugins.choreography.Utils;
import java.util.Collection;
import java.util.HashSet;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 * TreeBuilder lets you build a BranchingTree from a BPMNModel starting at a
 * given ProcessNode and following the SequenceFlow in its own direction or
 * in its opposite direction
 * @author tmi
 */


public class TreeBuilder {
  private BPMNModel model;

  public TreeBuilder(BPMNModel model) {
    this.model = model;
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
     * build the tree in the own direction of the SequenceFlow
     */
    flowAfter;
  }

  /**
   * builds a BranchingTree
   * @param node the node, from which the tree should emerge
   * @param direction states,
   */
  public BranchingTree buildTreeFor(ProcessNode node, FlowDirection direction) {
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
    Collection<ProcessNode> nodes =
            neighborNodesOfForDirection(tree.getNode(), direction);
    for (ProcessNode node : nodes) {
      BranchingTree addedNode = addAsNewNodeAfter(node, tree, direction);
      if(addedNode != null) {
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
          ProcessNode node, BranchingTree parent, FlowDirection direction) {
    if (Utils.isExclusiveGateway(node)
            || Utils.isInclusiveGateway(node)
            || Utils.isEventBasedGateway(node)
            || Utils.isComplexGateway(node)
            || Utils.isImplicitAlternativeSplit(node, direction, model)) {
      return new AlternativeSplit(parent, node);
    } else if (Utils.isParallelGateway(node)
            || Utils.isImplicitParallelSplit(node, direction, model)) {
      return new ParallelSplit(parent, node);
    } else if (Utils.isChoreographyActivity(node)) {
      return new ActivityNode(parent, node);
    } else if (Utils.isEvent(node)) {
      return new SilentNode(parent, node);
    }
    return null;
  }

  /**
   * returns the neighbor-nodes of a ProcessNode in the given direction
   */
  private Collection<ProcessNode> neighborNodesOfForDirection(
          ProcessNode node, FlowDirection direction) {
    if (direction.equals(FlowDirection.flowAfter)) {
      return Utils.getSucceedingNodes(node, model);
    } else {
      return Utils.getPrecedingNodes(node, model);
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
  private BranchingTree addAsNewNodeAfter(ProcessNode node, BranchingTree tree,
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
