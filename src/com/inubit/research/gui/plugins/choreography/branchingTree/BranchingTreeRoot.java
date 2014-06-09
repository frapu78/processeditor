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
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;

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
  public boolean contains(ProcessNode node) {
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
  public int pathFromRootCount(ProcessNode node) {
    return 0;
  }

  @Override
  public Collection<ProcessNode> activitiesWithParticipant(String participant) {
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
  public boolean trimAndEliminateToEndingAtNode(ProcessNode node) {
    if(firstEntry.trimAndEliminateToEndingAtNode(node)) {
      closePath(firstEntry);
    }
    return false;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<ProcessNode> nodes) {
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
  public BranchingTree eraseFirstChoreographyActivity() {
    firstEntry = firstEntry.eraseFirstChoreographyActivity();
    return this;
  }

  @Override
  public Collection<ProcessNode> nextRealizedNodes(
          Map<String, Map<String, ProcessNode>> realizedNodes, String participant) {
    return firstEntry.nextRealizedNodes(realizedNodes, participant);
  }

  @Override
  public boolean noPathesContainNonEmptyStartEvent() {
    return firstEntry.noPathesContainNonEmptyStartEvent();
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    return firstEntry.allAlternativesContainNonEmptyNonEndEventOrInvolve(participant);
  }

  @Override
  public boolean containsOnIndirectWay(ProcessNode node) {
    return firstEntry.containsOnIndirectWay(node);
  }

  @Override
  public BranchingTree eraseFirstNode() {
    firstEntry = firstEntry.eraseFirstChoreographyActivity();
    return this;
  }

  @Override
  public Collection<String> getParticipants() {
    return firstEntry.getParticipants();
  }

  @Override
  public Collection<ProcessNode> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    return firstEntry.parallelGatewaysBeforeFirstParticipationOf(participants);
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(ProcessNode node) {
    return firstEntry.allParallelPathesSynchronizeBefore(node);
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          ProcessNode before) {
    return firstEntry.synchronizesWithAllBeforeAndKeepsSynchronized(
            branches, nodeOccurences, before);
  }

  @Override
  public boolean contains(BranchingTree tree) {
    if(equals(tree)) return true;
    else return firstEntry.contains(tree);
  }

  @Override
  public ProcessNode createInstantiatingGateways(
          String forParticipant, BPMNModel inModel,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    return firstEntry.createInstantiatingGateways(
            forParticipant, inModel, startEvents, firstNode, pools);
  }

  @Override
  public Set<ProcessNode> firstNodesOf(String participant) {
    return firstEntry.firstNodesOf(participant);
  }

  @Override
  public Collection<EventBasedGateway> getAllEventBasedGateways() {
    return firstEntry.getAllEventBasedGateways();
  }

  @Override
  public boolean allAlternativesContainMessageReceive() {
    return firstEntry.allAlternativesContainMessageReceive();
  }
}
