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
import java.util.HashSet;
import java.util.Set;

/**
 * represents any ProcessNode, that is no join or split and no ChoreographyActivity
 * @author tmi
 */
class SilentNode extends StraightFlowNode {

  public SilentNode(BranchingTree parent) {
    super(parent);
    next = new EmptyBranchingTree(this);
  }

  public SilentNode(BranchingTree parent, NodeAdaptor node) {
    super(parent, node);
    next = new EmptyBranchingTree(this);
  }

  @Override
  public boolean allAlternativesInvolve(String participant) {
    return next.allAlternativesInvolve(participant);
  }

  @Override
  public boolean noAlternativesInvolve(String participant) {
    return next.noAlternativesInvolve(participant);
  }

  @Override
  public Collection<NodeAdaptor> activitiesWithParticipant(String participant) {
    return next.activitiesWithParticipant(participant);
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    return next.allAlternativesContainChoreographyActivities();
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    return next.allAlternativesContainMultipleChoreographyActivities();
  }

  @Override
  public boolean noPathesContainTriggeredStartEvent() {
    if (getNode().isStartEvent() && !getNode().isNoneStartEvent()) {
      return false;
    }
    return next.noPathesContainTriggeredStartEvent();
  }

  @Override
  public Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    if (participants.isEmpty()) {
      return new HashSet<NodeAdaptor>();
    } else {
      return next.parallelGatewaysBeforeFirstParticipationOf(participants);
    }
  }

  @Override
  public Collection<String> getParticipants() {
    return next.getParticipants();
  }

  @Override
  public Set<NodeAdaptor> firstNodesOf(String participant) {
    return nodeInNewSet();
  }
}
