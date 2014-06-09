/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A dedicated root-node is necessary, because all inner nodes must have a parent
 * node. It should not be set to null for them, because sometimes methods of
 * parent are called.
 * @author tmi
 */
class BranchingTreeRoot extends BranchingTree {

  private BranchingTree firstEntry;

  public BranchingTreeRoot(BranchingTree firstEntry) {
    super(null, null);
    this.firstEntry = firstEntry;
  }

  public BranchingTree getFirst() {
    return firstEntry;
  }

  @Override
  public void closePath(BranchingTree from) {
    firstEntry = new EmptyBranchingTree(this);
  }

  @Override
  public boolean contains(NodeAdaptor node) {
    return firstEntry.contains(node);
  }

  @Override
  public void setNext(BranchingTree next) {
    firstEntry = next;
  }

  @Override
  public boolean allAlternativesInvolve(String participant) {
    return firstEntry.allAlternativesInvolve(participant);
  }

  @Override
  public boolean noAlternativesInvolve(String participant) {
    return firstEntry.noAlternativesInvolve(participant);
  }

  @Override
  public int pathFromRootCount(NodeAdaptor node) {
    return 0;
  }

  @Override
  public Collection<NodeAdaptor> activitiesWithParticipant(String participant) {
    return firstEntry.activitiesWithParticipant(participant);
  }

  @Override
  public boolean removeClosedPathes() {
    if(firstEntry.removeClosedPathes()) {
      closePath(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    return firstEntry.allAlternativesContainChoreographyActivities();
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    return firstEntry.allAlternativesContainMultipleChoreographyActivities();
  }

  @Override
  public boolean trimAndEliminateToEndingAtNode(NodeAdaptor node) {
    if(firstEntry.trimAndEliminateToEndingAtNode(node)) {
      closePath(firstEntry);
    }
    return false;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<NodeAdaptor> nodes) {
    return firstEntry.allPathesContainOneOf(nodes);
  }

  @Override
  protected boolean synchronizesWithOneOf(
          Collection<BranchingTree> pathes,
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    return firstEntry.synchronizesWithOneOf(pathes, processNodeOccurences);
  }

  @Override
  protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
    return firstEntry.processNodeOccurenceMap();
  }

  @Override
  public boolean noPathesContainTriggeredStartEvent() {
    return firstEntry.noPathesContainTriggeredStartEvent();
  }

  @Override
  public Collection<String> getParticipants() {
    return firstEntry.getParticipants();
  }

  @Override
  public Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    return firstEntry.parallelGatewaysBeforeFirstParticipationOf(participants);
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(NodeAdaptor node) {
    return firstEntry.allParallelPathesSynchronizeBefore(node);
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          NodeAdaptor before) {
    return firstEntry.synchronizesWithAllBeforeAndKeepsSynchronized(
            branches, nodeOccurences, before);
  }

  @Override
  public boolean contains(BranchingTree tree) {
    if(equals(tree)) return true;
    else return firstEntry.contains(tree);
  }

  @Override
  public Set<NodeAdaptor> firstNodesOf(String participant) {
    return firstEntry.firstNodesOf(participant);
  }
}
