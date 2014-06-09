/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tmi
 */
abstract class StraightFlowNode extends BranchingTree {

    protected BranchingTree next;

    public StraightFlowNode(BranchingTree parent) {
        super(parent);
    }

    public StraightFlowNode(BranchingTree parent, NodeAdaptor node) {
        super(parent, node);
    }

    @Override
    public void setNext(BranchingTree next) {
        this.next = next;
    }

    @Override
    public boolean allPathesContainOneOf(Collection<NodeAdaptor> nodes) {
        if (nodes.contains(getNode())) {
            return true;
        } else {
            return next.allPathesContainOneOf(nodes);
        }
    }

    @Override
    public boolean allParallelPathesSynchronizeBefore(NodeAdaptor node) {
        if (getNode().equals(node)) {
            return false;
        } else {
            return next.allParallelPathesSynchronizeBefore(node);
        }
    }

    @Override
    public void closePath(BranchingTree from) {
        getParent().closePath(this);
    }

    @Override
    protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
        Map<String, Collection<BranchingTree>> occurences = next.processNodeOccurenceMap();
        if (!occurences.containsKey(getNode().getId())) {
            occurences.put(getNode().getId(), new HashSet<BranchingTree>());
        }
        occurences.get(getNode().getId()).add(this);
        return occurences;
    }

    @Override
    public boolean removeClosedPathes() {
        return next.removeClosedPathes();
    }

    @Override
    public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
            Collection<BranchingTree> branches,
            Map<String, Collection<BranchingTree>> nodeOccurences,
            NodeAdaptor before) {
        if (getNode().equals(before)) {
            return branches.isEmpty();
        } else {
            return next.synchronizesWithAllBeforeAndKeepsSynchronized(
                    branches, nodeOccurences, before);
        }
    }

    @Override
    protected boolean synchronizesWithOneOf(
            Collection<BranchingTree> pathes,
            Map<String, Collection<BranchingTree>> processNodeOccurences) {
        return next.synchronizesWithOneOf(pathes, processNodeOccurences);
    }

    @Override
    public boolean trimAndEliminateToEndingAtNode(NodeAdaptor node) {
        if (getNode().equals(node)) {
            next = new EmptyBranchingTree(this);
            return false;
        } else {
            return next.trimAndEliminateToEndingAtNode(node);
        }
    }

    @Override
    public boolean contains(BranchingTree tree) {
        if (equals(tree)) {
            return true;
        } else {
            return next.contains(tree);
        }
    }

    @Override
    public boolean contains(NodeAdaptor node) {
        if (node.equals(getNode())) {
            return true;
        } else {
            return next.contains(node);
        }
    }

    protected Set<NodeAdaptor> nodeInNewSet() {
        Set<NodeAdaptor> result = new HashSet<NodeAdaptor>();
        result.add(getNode());
        return result;
    }
}
