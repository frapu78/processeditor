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
import java.util.HashSet;
import java.util.Set;

/**
 * represents a ChoreographyActivity
 * @author tmi
 */
class ActivityNode extends StraightFlowNode {
  public ActivityNode(BranchingTree parent, ChoreographyNodeAdaptor activity) {
    super(parent, activity.asNodeAdaptor());
    next = new EmptyBranchingTree(this);
  }

  @Override
  public boolean allAlternativesInvolve(String participant) {
    if (isParticipant(participant)) {
      return true;
    }
    return next.allAlternativesInvolve(participant);
  }

  @Override
  public boolean noAlternativesInvolve(String participant) {
    if (isParticipant(participant)) {
      return false;
    }
    return next.noAlternativesInvolve(participant);
  }

  @Override
  public Collection<NodeAdaptor> activitiesWithParticipant(String participant) {
    Collection<NodeAdaptor> activities =
            next.activitiesWithParticipant(participant);
    if (isParticipant(participant)) {
      activities.add(getNode());
    }
    return activities;
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    return true;
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    return next.allAlternativesContainChoreographyActivities();
  }

  @Override
  public boolean noPathesContainTriggeredStartEvent() {
    return next.noPathesContainTriggeredStartEvent();
  }

  @Override
  public Collection<String> getParticipants() {
    Collection<String> participants = next.getParticipants();
    participants.addAll(getLocalParticipants());
    return participants;
  }

  @Override
  public Collection<NodeAdaptor> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    Collection<String> participantsWithoutMyself =
            new HashSet<String>(participants);
    participantsWithoutMyself.removeAll(getLocalParticipants());
    if(participantsWithoutMyself.isEmpty()) {
      return new HashSet<NodeAdaptor>();
    } else {
      return next.parallelGatewaysBeforeFirstParticipationOf(
              participantsWithoutMyself);
    }
  }

  @Override
  public Set<NodeAdaptor> firstNodesOf(String participant) {
    if(isParticipant(participant)) {
      return nodeInNewSet();
    } else {
      return next.firstNodesOf(participant);
    }
  }
}
