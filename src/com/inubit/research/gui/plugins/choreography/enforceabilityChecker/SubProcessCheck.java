/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import com.inubit.research.gui.plugins.choreography.Utils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * Checks, that the initiator of a ChoreographySubProcess is really the initiator
 * of every initial activity of this SubProcess, and that only those participants
 * take part in the SubProcess, who are mentioned in one of the participant bands,
 * and that only Empty StartEvents are used within the SubProcess.
 * @author tmi
 */
public class SubProcessCheck extends AbstractChoreographyCheck {
  private static final String DESC_OnlyNoneStartEvents =
          "The only start events allowed in sub processes are None start events";
  private static final String DESC_AllowedParticipants =
          "All participants of the subprocess must be mentioned in its "+
          "participant bands";
  private static final String DESC_Initiators =
          "The initiator of each first choreography activity within a " +
          "choreography-sub-process must be the same as the initiator of " +
          "the sub-process";

  public SubProcessCheck(BPMNModel model) {
    super(model);
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>(1);
    classes.add(ChoreographySubProcess.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if(! (object instanceof ChoreographySubProcess)) {
      return new HashSet<EnforceabilityProblem>();
    }
    return checkChoreographySubProcess((ChoreographySubProcess) object);
  }

  private Collection<EnforceabilityProblem> checkChoreographySubProcess(
          ChoreographySubProcess subProcess) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    problems.addAll(checkStartEventTypes(subProcess));
    problems.addAll(checkParticipants(subProcess));
    problems.addAll(checkInitiators(subProcess));
    return problems;
  }

  private Collection<EnforceabilityProblem> checkStartEventTypes(
          ChoreographySubProcess subProcess) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    for(ProcessNode node : subProcess.getProcessNodes()) {
      if((node instanceof StartEvent)
              && ! (node.getClass().equals(StartEvent.class))) {
        problems.add(new EnforceabilityProblem(
                DESC_OnlyNoneStartEvents,
                node,
                subProcess));
      }
    }
    return problems;
  }

  /**
   * checks, for a ChoreographySubProcess, that only those participants take part
   * in it, who are mentioned in one of the participant bands.
   */
  private Collection<EnforceabilityProblem> checkParticipants(
          ChoreographySubProcess subProcess) {
    Set<String> participants =
            Utils.participantsOf(subProcess.getProcessNodes());
    participants.removeAll(Utils.participantsOf(subProcess));
    Collection<EnforceabilityProblem> problems = 
            new HashSet<EnforceabilityProblem>();
    if(! participants.isEmpty()) {
      problems.add(new EnforceabilityProblem(
              DESC_AllowedParticipants,
              subProcess,
              activitiesEngagingOneOf(
                subProcess.getProcessNodes(),
                participants)));
    }
     return problems;
  }

  /**
   * collects all ChoreographyActivities out of a set of ProcessNodes, that do
   * engage one of the supplied participants.
   */
  private Collection<ProcessNode> activitiesEngagingOneOf(
          Collection<ProcessNode> nodes, Collection<String> participants) {
    Collection<ProcessNode> activities = new HashSet<ProcessNode>();
    for(ProcessNode node : nodes) {
      for(String participant : Utils.participantsOf(node)) {
        if(participants.contains(participant)) {
          activities.add(node);
          break;
        }
      }
    }
    return activities;
  }

  /**
   * checks, that all initial activities of the ChoreographySubProcess are initiated
   * by the initiator of the SubProcess.
   */
  private Collection<EnforceabilityProblem> checkInitiators(
          ChoreographySubProcess subProcess) {
    Collection<ProcessNode> nodes = getInitialNodes(subProcess);
    nodes = nextNeighbourChoreographyActivities(nodes, Direction.Forward);
    Collection<EnforceabilityProblem> problems =
            checkInitiatorsInSubprocess(nodes, subProcess);
    return problems;
  }

  private Collection<ProcessNode> getInitialNodes(
          ChoreographySubProcess subProcess) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
    nodes.addAll(nodesWithSequenceFlowOverBorder(subProcess, Direction.Backward));
    nodes.addAll(nodesWithoutSequenceFlowInDirection(
            subProcess, Direction.Backward));
    return nodes;
  }

  /**
   * checks, for the set of initial ChoreographyActivities, that they are initiated
   * by the ChoreographySubProcess´ initiator
   */
  private Collection<EnforceabilityProblem> checkInitiatorsInSubprocess(
          Collection<ProcessNode> firstChoreographyActivities,
          ChoreographySubProcess subProcess) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    for(ProcessNode node : firstChoreographyActivities) {
      if(! Utils.initiatorOf(node).equals(Utils.initiatorOf(subProcess))) {
        problems.add(new EnforceabilityProblem(DESC_Initiators, node, subProcess));
      }
    }
    return problems;
  }
}
