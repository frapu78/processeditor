/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

//Also refer to the base-class (BranchingTree) for Javadoc
/**
 * This is an abstract superclass for BranchingTree-nodes, that represent a
 * split.
 * @author tmi
 */
abstract class AbstractSplit extends BranchingTree {

    /**
     * the pathes emerging from this BranchingTree-node
     */
    protected java.util.List<BranchingTree> pathes;
    /**
     * Will be true, if, due to a call to eraseFirstNode or
     * eraseFirstChoreographyActivity, the tree needs to pretend the none-existence
     * of the ProcessNode cotained in this BranchingTree-node.
     */
    protected boolean isErased;

    public AbstractSplit(BranchingTree parent) {
        super(parent);
        pathes = new LinkedList<BranchingTree>();
    }

    public AbstractSplit(BranchingTree parent, NodeAdaptor node) {
        super(parent, node);
        pathes = new LinkedList<BranchingTree>();
    }

    public void addPath(BranchingTree path) {
        pathes.add(path);
    }

    protected abstract boolean isParallel();

    /**
     * sets the property EventBasedGateway.PROP_INSTANTIATE for the supplied gateway
     * according to the split type
     */
    //protected abstract void setInstantiatingProperty(GatewayAdaptor gateway);

    @Override
    public Collection<NodeAdaptor> activitiesWithParticipant(String participant) {
        Collection<NodeAdaptor> activities = new HashSet<NodeAdaptor>();
        if (!isErased && isParticipant(participant)) {
            activities.add(getNode());
        }
        for (BranchingTree next : pathes) {
            activities.addAll(next.activitiesWithParticipant(participant));
        }
        return activities;
    }

    @Override
    public boolean contains(BranchingTree tree) {
        if (equals(tree)) {
            return true;
        }
        for (BranchingTree path : pathes) {
            if (path.contains(tree)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(NodeAdaptor node) {
        if (!isErased && getNode().equals(node)) {
            return true;
        }
        for (BranchingTree tree : pathes) {
            if (tree.contains(node)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<NodeAdaptor> firstNodesOf(String participant) {
        Set<NodeAdaptor> result = new HashSet<NodeAdaptor>();
        if (isParticipant(participant)) {
            result.add(getNode());
        } else {
            for (BranchingTree path : pathes) {
                result.addAll(path.firstNodesOf(participant));
            }
        }
        return result;
    }

    @Override
    public Collection<String> getParticipants() {
        Collection<String> participants = new HashSet<String>();
        for (BranchingTree path : pathes) {
            participants.addAll(path.getParticipants());
        }
        if (!isErased) {
            participants.addAll(getLocalParticipants());
        }
        return participants;
    }

    @Override
    public boolean noPathesContainTriggeredStartEvent() {
        if (!isErased
                && getNode().isStartEvent() //is possible: implicit split-/join-behavior
                && !((EventAdaptor) getNode()).isNoneStartEvent()) {
            return false;
        }
        for (BranchingTree path : pathes) {
            if (!path.noPathesContainTriggeredStartEvent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean noAlternativesInvolve(String participant) {
        if (!isErased && isParticipant(participant)) {
            return false;
        }
        for (BranchingTree tree : pathes) {
            if (!tree.noAlternativesInvolve(participant)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
        Map<String, Collection<BranchingTree>> occurences =
                new HashMap<String, Collection<BranchingTree>>();
        for (BranchingTree path : pathes) {
            occurences.putAll(path.processNodeOccurenceMap());
        }
        if (!isErased) {
            if (!occurences.containsKey(getNode().getId())) {
                occurences.put(getNode().getId(), new HashSet<BranchingTree>());
            }
            occurences.get(getNode().getId()).add(this);
        }
        return occurences;
    }

    /**
     * In a split-node, setNext will add a path to the split.
     */
    @Override
    public void setNext(BranchingTree next) {
        addPath(next);
    }

    @Override
    public boolean trimAndEliminateToEndingAtNode(NodeAdaptor node) {
        if (!isErased && getNode().equals(node)) {
            pathes.clear();
            return false;
        } else {
            for (Iterator<BranchingTree> iter = pathes.iterator(); iter.hasNext();) {
                if (iter.next().trimAndEliminateToEndingAtNode(node)) {
                    iter.remove();
                }
            }
            return pathes.isEmpty();
        }
    }

    @Override
    protected boolean synchronizesWithOneOf(Collection<BranchingTree> otherPathes,
            Map<String, Collection<BranchingTree>> processNodeOccurences) {
        for (BranchingTree path : pathes) {
            if (!path.synchronizesWithOneOf(otherPathes, processNodeOccurences)) {
                return false;
            }
        }
        return !pathes.isEmpty();
    }

    @Override
    public Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
            Collection<String> participants) {
        Collection<String> participantsWithoutMyself = new HashSet<String>(participants);
        participantsWithoutMyself.removeAll(getLocalParticipants());
        Collection<NodeAdaptor> result = new HashSet<NodeAdaptor>();
        if (!participantsWithoutMyself.isEmpty()) {
            for (BranchingTree path : pathes) {
                if (path.getParticipants().containsAll(participants)) {
                    result.addAll(path.parallelGatewaysBeforeFirstParticipationOf(participants));
                }
            }
            if (isParallel()) {
                result.add(getNode());
            }
        }
        return result;
    }
}
