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
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * represents any ProcessNode, that is no join or split and no ChoreographyActivity
 * @author tmi
 */
class SilentNode extends StraightFlowNode {

  public SilentNode(BranchingTree parent) {
    super(parent);
    next = new EmptyBranchingTree(this);
  }

  public SilentNode(BranchingTree parent, ProcessNode node) {
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
  public Collection<ProcessNode> activitiesWithParticipant(String participant) {
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
  public BranchingTree eraseFirstChoreographyActivity() {
    next = next.eraseFirstChoreographyActivity();
    return this;
  }

  @Override
  public boolean noPathesContainNonEmptyStartEvent() {
    if (Utils.isStartEvent(getNode()) && !Utils.isEmptyStartEvent(getNode())) {
      return false;
    }
    return next.noPathesContainNonEmptyStartEvent();
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    if (Utils.isNonEmptyStartEvent(getNode())
            || Utils.isNonEmptyIntermediateEvent(getNode())) {
      return true;
    }
    return next.allAlternativesContainNonEmptyNonEndEventOrInvolve(participant);
  }

  @Override
  public Collection<ProcessNode> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    if (participants.isEmpty()) {
      return new HashSet<ProcessNode>();
    } else {
      return next.parallelGatewaysBeforeFirstParticipationOf(participants);
    }
  }

  @Override
  public Collection<String> getParticipants() {
    return next.getParticipants();
  }

  @Override
  public ProcessNode createInstantiatingGateways(
          String participant, BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    if (Utils.isIntermediateEvent(getNode())
            || Utils.isNonEmptyStartEvent(getNode())) {
      return getStartEvent(participant, model, startEvents, firstNode, pools);
    } else {
      return next.createInstantiatingGateways(
              participant, model, startEvents, firstNode, pools);
    }
  }

  @Override
  public Set<ProcessNode> firstNodesOf(String participant) {
    return nodeInNewSet();
  }

  @Override
  protected StartEvent correspondingStartEvent() {
    return Utils.correspondingStartEvent((IntermediateEvent) getNode());
  }
}
