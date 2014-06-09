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
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * represents a ChoreographyActivity
 * @author tmi
 */
class ActivityNode extends StraightFlowNode {
  public ActivityNode(BranchingTree parent, ProcessNode activity) {
    super(parent, activity);
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
  public Collection<ProcessNode> activitiesWithParticipant(String participant) {
    Collection<ProcessNode> activities =
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
  public BranchingTree eraseFirstChoreographyActivity() {
    next.setParent(getParent());
    return next;
  }

  @Override
  public boolean noPathesContainNonEmptyStartEvent() {
    return next.noPathesContainNonEmptyStartEvent();
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    if(isParticipant(participant) 
            || Utils.isNonEmptyStartEvent(getNode())
            || Utils.isNonEmptyIntermediateEvent(getNode())) {
      return true;
    }
    return next.allAlternativesContainNonEmptyNonEndEventOrInvolve(participant);
  }

  @Override
  public Collection<String> getParticipants() {
    Collection<String> participants = next.getParticipants();
    participants.addAll(Utils.participantsOf(getNode()));
    return participants;
  }

  @Override
  public Collection<ProcessNode> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    Collection<String> participantsWithoutMyself =
            new HashSet<String>(participants);
    participantsWithoutMyself.removeAll(Utils.participantsOf(getNode()));
    if(participantsWithoutMyself.isEmpty()) {
      return new HashSet<ProcessNode>();
    } else {
      return next.parallelGatewaysBeforeFirstParticipationOf(
              participantsWithoutMyself);
    }
  }

  @Override
  public ProcessNode createInstantiatingGateways(
          String participant, BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    if(isParticipant(participant)) {
      return getStartEvent(participant, model, startEvents, firstNode, pools);
    } else {
      return next.createInstantiatingGateways(
              participant, model, startEvents, firstNode, pools);
    }
  }

  @Override
  public Set<ProcessNode> firstNodesOf(String participant) {
    if(isParticipant(participant)) {
      return nodeInNewSet();
    } else {
      return next.firstNodesOf(participant);
    }
  }

  @Override
  protected StartEvent correspondingStartEvent() {
    return new StartEvent();
  }
}
