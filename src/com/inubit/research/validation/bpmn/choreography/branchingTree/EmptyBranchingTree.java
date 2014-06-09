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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the end of a BranchingTree - just a null-object.
 * @author tmi
 */
class EmptyBranchingTree extends BranchingTree {

  public EmptyBranchingTree(BranchingTree parent) {
    super(parent);
  }

  @Override
  public boolean contains(NodeAdaptor node) {
    return false;
  }

  @Override
  public void closePath(BranchingTree from) {
    getParent().closePath(this);
  }

  @Override
  public boolean allAlternativesInvolve(String participant) {
    return false;
  }

  @Override
  public boolean noAlternativesInvolve(String participant) {
    return true;
  }

  @Override
  public void setNext(BranchingTree next) {
    getParent().setNext(next);
  }

  @Override
  public int pathFromRootCount(NodeAdaptor node) {
    return getParent().pathFromRootCount(node);
  }

  @Override
  public Collection<NodeAdaptor> activitiesWithParticipant(String participant) {
    return new HashSet<NodeAdaptor>();
  }

  @Override
  public boolean removeClosedPathes() {
    return false;
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    return false;
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    return false;
  }

  @Override
  public boolean trimAndEliminateToEndingAtNode(NodeAdaptor node) {
    return true;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<NodeAdaptor> nodes) {
    return false;
  }

  @Override
  protected boolean synchronizesWithOneOf(
          Collection<BranchingTree> pathes,
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    return false;
  }

  @Override
  protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
    return new HashMap<String, Collection<BranchingTree>>();
  }

  @Override
  public boolean noPathesContainTriggeredStartEvent() {
    return true;
  }

  @Override
  public Collection<String> getParticipants() {
    return new HashSet<String>();
  }

  @Override
  public Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    return new HashSet<NodeAdaptor>();
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(NodeAdaptor node) {
    return true;//no parallelism occured
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          NodeAdaptor before) {
    return branches.isEmpty();
  }

  @Override
  public boolean contains(BranchingTree tree) {
    return equals(tree);
  }

  @Override
  public Set<NodeAdaptor> firstNodesOf(String participant) {
    return new HashSet<NodeAdaptor>(0);
  }
}
