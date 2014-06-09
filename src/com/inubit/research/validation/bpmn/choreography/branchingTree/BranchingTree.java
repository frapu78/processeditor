/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.ChoreographyNodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BranchingTree is a structure for representing the pathes of a BPMNDiagram.
 * Because it is a tree, joining nodes cannot be represented and flow after
 * joining of two pathes will be duplicated.
 * <br/><br/>
 * This data-structure was originally intended to analyse Choreography-diagrams
 * and was enriched with some other functionalities. Be aware of the fact, that
 * a BranchingTree can represent the flow of the diagram inverted, if it was built
 * in this way, what is supported by TreeBuilder
 * <br/><br/>
 * From the outside of this class´ package
 * (com.inubit.research.gui.plugins.choreography.branchingTree) only this abstract
 * superclass and the class TreeBuilder can be used, there is no need for knowing
 * the subclasses of BranchingTree.
 * <br/><br/>
 * Note: when the term "ChoreographyActivity" is used, it does not reference the
 * implemented class ChoreographyActivity, but the conceptual class (as defined
 * in the BPMN-specification). This means, it subsumes the classes
 * ChoreographyActivity and ChoreographySubProcess.
 * @author tmi
 */
public abstract class BranchingTree {

    private NodeAdaptor node;

    public BranchingTree(BranchingTree parent) {
        this.parent = parent;
    }

    public BranchingTree(BranchingTree parent, NodeAdaptor node) {
        this(parent);
        this.node = node;
    }
    private BranchingTree parent;

    protected NodeAdaptor getNode() {
        return node;
    }

    protected void setParent(BranchingTree parent) {
        this.parent = parent;
    }

    protected BranchingTree getParent() {
        return parent;
    }

    /**
     * checks, wheter one of the descendant nodes of this BranchingTree-ndoe
     * contains node.
     */
    public abstract boolean contains(NodeAdaptor node);

    /**
     * sets the next node for this BranchingTree-node
     */
    public abstract void setNext(BranchingTree next);

    /**
     * checks, wheter all alternative pathes emerging from this node contain a
     * ChoreographyActivity, that involves participant
     */
    public abstract boolean allAlternativesInvolve(String participant);

    /**
     * checks, wheter none of the descendant nodes of this contains a
     * ChoreographyActivity that involves participant
     */
    public abstract boolean noAlternativesInvolve(String participant);

    /**
     * steps upstairs in the BranchingTree until an AlternativeSplit is reached
     * and then removes the path, where it came from. If the AlternativeSplit has
     * no more pathes left after performing this operation, it will be marked as
     * closed and will be removed when removeClosedPathes is called, unless a path
     * was added to it again. These two phases are necessary in order to avoid
     * removing gateways, wich should be added more pathes to.
     */
    abstract void closePath(BranchingTree from);

    /**
     * This method is necessary to be called, when the tree is completely built
     * and the operation closePath was used while building the tree.
     * @see #closePath(BranchingTree from)
     */
    abstract boolean removeClosedPathes();

    /**
     * gives the number of occurences of a node in the path to (i.e. the ancestors of)
     * this BranchingTree object.
     * @param node the node, whichs occurences to count
     * @return the number of occurences in the path to this node
     */
    public int pathFromRootCount(NodeAdaptor node) {
        if (node.equals(getNode())) {
            return 1 + getParent().pathFromRootCount(node);
        } else {
            return getParent().pathFromRootCount(node);
        }
    }

    /**
     * identifies all choreography activities in this tree (i.e. this node and all
     * of its descendants), that involve participant
     * @param participant the participant, who´s activities should be identified
     */
    public abstract Collection<NodeAdaptor> activitiesWithParticipant(
            String participant);

    public abstract boolean allAlternativesContainChoreographyActivities();

    public abstract boolean allAlternativesContainMultipleChoreographyActivities();

    /**
     * states, whether name is a participant of the ProcessNode represented by this
     * BranchingTree-node
     */
    protected boolean isParticipant(String name) {
        if (node.isChoreographyActivity()) {
            return ((ChoreographyNodeAdaptor) node).hasParticipant(name);
        } else {
            return false;
        }
    }

    /**
     * Cuts all branches after the given node and eliminates branches,
     * which do not involve this node
     * @return wheter this path is to be eliminated
     */
    public abstract boolean trimAndEliminateToEndingAtNode(
            NodeAdaptor node);

    /**
     * states, wheter all pathes emerging from this node (i.e. all
     * pathes that consist only of decendants of this node) contain at least one
     * of the supplied nodes. A path is also considered containing a node, if does
     * not contain a node itself, but synchronizes with a path containing one of
     * the nodes.
     * @param nodes the nodes, of which at least one should occur
     * @return wheter at least one of the nodes is contained in every alternative path
     */
    public abstract boolean allPathesContainOneOf(
            Collection<NodeAdaptor> nodes);

    /**
     * checks, wheter all alternatives emerging from this BranchingTree-node
     * synchronize with one of the pathes in pathes.
     * @param pathes the pathes, with which to synchronize
     * @param processNodeOccurences a mapping from Node-IDs of all ProcessNodes,
     * that occur in one of the pathes to a Collection of BranchingTrees, that
     * represent the node with the given ID.
     *
     * @return true, if all alternatives emerging from this point synchronize with
     * one of the alternative pathes.
     */
    protected abstract boolean synchronizesWithOneOf(
            Collection<BranchingTree> pathes,
            Map<String, Collection<BranchingTree>> processNodeOccurences);

    /**
     * builds the map needed for checking synchronization of pathes (e.g. the second
     * parameter of synchronizesWithOneOf): it maps each ProcessNode-ID occuring in
     * this tree to the BranchingTee-node, that contains this ProcessNode.
     * @return the generated mapping from ProcessNode-IDs to BranchingTree-nodes
     */
    protected abstract Map<String, Collection<BranchingTree>> processNodeOccurenceMap();
    
    /**
     * checks, wheter the whole tree emerging from this node contains no StartEvents,
     * that are not Empty StartEvents (instances exactly of StartEvent, not of one
     * of its subclasses)
     * @return true, if the whole tree contains no ProcessNode, that is a
     * StartEvent, but not one of its subclasses
     */
    public abstract boolean noPathesContainTriggeredStartEvent();

    /**
     * collects all participants involved in ChoreographyActivities cotained in
     * this tree
     * @return a Collection of participant names
     */
    public abstract Collection<String> getParticipants();

    protected List<String> getLocalParticipants() {
        if (node.isChoreographyActivity()) {
            return ((ChoreographyNodeAdaptor) node).getParticipants();
        } else {
            return new LinkedList<String>();
        }
    }

    /**
     * collects all parallel gateways, that occur in any path emerging from this
     * BranchingTree-Node before all of the supplied participants involved in
     * some ChoreographyActivity
     * @param participants a set of participant names
     * @return the collection of gateways, that satisfy the condition stated above
     */
    public abstract Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
            Collection<String> participants);

    /**
     * checks, wheter all parallel pathes, into which the tree splits from this
     * node on, synchronize again before the occurence of node
     * @param node the node at which to stop looking for synchronization
     */
    public abstract boolean allParallelPathesSynchronizeBefore(NodeAdaptor node);

    /**
     * checks, wheter all pathes emerging from this node synchronize with all
     * of the supplied branches, before the pathes contain the ProcessNode before
     * and wheter the pathes keep synchronized (i.e. if the path splits to parallel
     * pathes again, they also synchronize before the node "before")
     * @param branches the collection of branches, with wich it should synchronize
     * @param nodeOccurences a mapping from ProcessNode-IDs to a Collection of
     * BranchingTree-nodes, that contain the ProcessNode with this ID
     * @param before the node, at which to stop the search for synchronization (by failing)
     */
    public abstract boolean synchronizesWithAllBeforeAndKeepsSynchronized(
            Collection<BranchingTree> branches,
            Map<String, Collection<BranchingTree>> nodeOccurences,
            NodeAdaptor before);

    /**
     * @return true, if tree is a descendant of this BranchingTree-node
     */
    public abstract boolean contains(BranchingTree tree);

    /**
     * Collects all the first nodes, that involve the supplied participant.
     * A ProcessNode is a first node, if it is preceeded by no other nodes in the
     * path from this BranchingTree-node to the BranchingTree-node containing the
     * ProcessNode.
     */
    public abstract Set<NodeAdaptor> firstNodesOf(String participant);
}
