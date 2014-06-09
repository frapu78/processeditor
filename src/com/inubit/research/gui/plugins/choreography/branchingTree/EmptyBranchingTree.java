/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.branchingTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * Represents the end of a BranchingTree - just a null-object.
 * @author tmi
 */
class EmptyBranchingTree extends BranchingTree {

  public EmptyBranchingTree(BranchingTree parent) {
    super(parent);
  }

  @Override
  public boolean contains(ProcessNode node) {
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
  public int pathFromRootCount(ProcessNode node) {
    return getParent().pathFromRootCount(node);
  }

  @Override
  public Collection<ProcessNode> activitiesWithParticipant(String participant) {
    return new HashSet<ProcessNode>();
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
  public boolean trimAndEliminateToEndingAtNode(ProcessNode node) {
    return true;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<ProcessNode> nodes) {
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
  public BranchingTree eraseFirstChoreographyActivity() {
    return this;
  }

  @Override
  public Collection<ProcessNode> nextRealizedNodes(
          Map<String, Map<String, ProcessNode>> realizedNodes, String participant) {
    return new HashSet<ProcessNode>(0);
  }

  @Override
  public boolean noPathesContainNonEmptyStartEvent() {
    return true;
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    return false;
  }

  @Override
  public boolean containsOnIndirectWay(ProcessNode node) {
    return false;
  }

  @Override
  public BranchingTree eraseFirstNode() {
    return this;
  }

  @Override
  public Collection<String> getParticipants() {
    return new HashSet<String>();
  }

  @Override
  public Collection<ProcessNode> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    return new HashSet<ProcessNode>();
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(ProcessNode node) {
    return true;//no parallelism occured
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          ProcessNode before) {
    return branches.isEmpty();
  }

  @Override
  public boolean contains(BranchingTree tree) {
    return equals(tree);
  }

  @Override
  public ProcessNode createInstantiatingGateways(
          String forParticipant, BPMNModel inModel,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    return null;
  }

  @Override
  public Set<ProcessNode> firstNodesOf(String participant) {
    return new HashSet<ProcessNode>(0);
  }

  @Override
  public Collection<EventBasedGateway> getAllEventBasedGateways() {
    return new HashSet<EventBasedGateway>();
  }

  @Override
  public boolean allAlternativesContainMessageReceive() {
    return false;
  }
}
