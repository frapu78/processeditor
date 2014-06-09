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
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import com.inubit.research.gui.plugins.choreography.branchingTree.BranchingTree;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author tmi
 */
public class EventBasedGatewayCheck extends AbstractChoreographyCheck {

  private static final String DESC_SameParticipants =
          "All the receivers or all the senders of directly following "
          + "activities have to be the same";
  private static final String DESC_AllowedEvents =
          "Only the following Nodes may directly succeed an event-based "
          + "gateway: Choreography Task, Timer Intermediate Event, Signal "
          + "Intermediate Event.";
  private static final String DESC_ExternalSynchronisation =
          "This join preceds the first message of some participant after " +
          "an event-based gateway. Therefore, no enforcement is possible " +
          "in a colaboration diagram.";
  private static final String DESC_OnlyStartEvents =
          "Only Startevents may be connected to an initiating event-based " +
          "gatewas via Sequence Flow.";

  public EventBasedGatewayCheck(BPMNModel model) {
    super(model);
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>();
    classes.add(EventBasedGateway.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if (!(object instanceof EventBasedGateway)) {
      return new HashSet<EnforceabilityProblem>();
    }
    return checkEventBasedGateway((EventBasedGateway) object);
  }

  private Collection<EnforceabilityProblem> checkEventBasedGateway(EventBasedGateway gateway) {
    Collection<EnforceabilityProblem> problems = new HashSet<EnforceabilityProblem>();
    problems.addAll(checkConnectedNodes(gateway));//only allowed nodes connected?
    problems.addAll(checkDirectlySucceedingParticipants(gateway));
        //are all directly following receivers or all directly following senders the same?
    TreeBuilder builder = new TreeBuilder(model);
    BranchingTree branches = builder.buildTreeFor
            (gateway, TreeBuilder.FlowDirection.flowAfter);
    BranchingTree history = builder.buildTreeFor
            (gateway, TreeBuilder.FlowDirection.flowBefore);
    problems.addAll(checkPathParticipation(gateway, branches, history));
        //everyone, who participated before, will participate in every alternative
        //or no alternative (unless he is a sender of a directly succeeding activity)
    problems.addAll(checkForExternalAndJoins(gateway, branches, history));
        //no parallelJoins occur on one path of the gateway, where at least one
        //of the incoming pathes was not forked out of this path
    return problems;
  }

  /**
   * checks, that all the following senders are the same
   */
  private boolean sameFollowingSenders(ProcessNode gateway) {
    String sender = null;
    for (ProcessNode node : Utils.getSucceedingNodes(gateway, model)) {
      if (Utils.isChoreographyActivity(node)) {
        if (sender == null) {
          sender = Utils.initiatorOf(node);
        } else if (!sender.equals(Utils.initiatorOf(node))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * checks, that all the following receivers are the same
   */
  private boolean sameFollowingReceivers(ProcessNode gateway) {
    Set<String> receivers = null;
    for (ProcessNode node : Utils.getSucceedingNodes(gateway, model)) {
      if (receivers == null) {
        receivers = Utils.participantsOf(node);
        receivers.remove(Utils.initiatorOf(node));
      } else {
        Set<String> current = Utils.participantsOf(node);
        current.remove(Utils.initiatorOf(node));
        if (!current.equals(receivers)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * checks, that all the following senders or all the following receivers are
   * the same participant.
   */
  private Collection<EnforceabilityProblem> checkDirectlySucceedingParticipants(
          ProcessNode gateway) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    if (!(sameFollowingSenders(gateway) || sameFollowingReceivers(gateway))) {
      problems.add(new EnforceabilityProblem(DESC_SameParticipants, gateway));
    }
    return problems;
  }

  /**
   * checks, that only the allowed ProcessNodes (ChoreographyTasks,
   * TimerIntermediateEvent or SignalIntermediateEvent) are directly succeeding
   * nodes of the gateway. If the gateway is Instantiating, other nodes are allowed:
   * all StartEvents
   */
  private Collection<EnforceabilityProblem> checkConnectedNodes(
          EventBasedGateway gateway) {
    Collection<EnforceabilityProblem> problems = new HashSet<EnforceabilityProblem>();
    for (ProcessNode node : model.getSucceedingNodes(SequenceFlow.class, gateway)) {
      boolean isInstantiating = ! gateway.getProperty(EventBasedGateway.PROP_INSTANTIATE).
              equals(EventBasedGateway.TYPE_INSTANTIATE_NONE) ;
      if (! isInstantiating
              &&!(Utils.isChoreographyTask(node)
              || Utils.isTimerIntermediateEvent(node)
              || Utils.isSignalIntermediateEvent(node))) {
        problems.add(new EnforceabilityProblem(DESC_AllowedEvents, node, gateway));
      } else if (isInstantiating
              && ! Utils.isStartEvent(node)) {
        problems.add(new EnforceabilityProblem(DESC_OnlyStartEvents, node, gateway));
      }
    }
    return problems;
  }

  /**
   * checks, that all participants, who have been participating before the gateway,
   * will participate in either all or none of the pathes from the gateway.
   */
  private Collection<EnforceabilityProblem> checkPathParticipation(
          EventBasedGateway gateway, BranchingTree branches, BranchingTree history) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    Collection<String> participants = getChoreographyParticipants();
    for (ProcessNode node : Utils.getSucceedingNodes(gateway, model)) {
      participants.remove(Utils.initiatorOf(node));
    }
    for (String participant : participants) {
      if ((!branches.allAlternativesInvolve(participant))
              && (!branches.noAlternativesInvolve(participant))
              && (!history.noAlternativesInvolve(participant))) {
        //TODO: the last condition should be ...
        //!history.noAlternativesInvolveOrContainNonEmptyStartEvent(participant),
        //but there is no time left for me to implement this fuction in the
        //Branching tree
        problems.add(new EnforceabilityProblem(problemDescription(participant),
                gateway,
                branches.activitiesWithParticipant(participant),
                EnforceabilityProblem.ProblemType.TimeoutWarning));
      }
    }
    return problems;
  }

  /**
   * checks for situations in pathes from the gateway, where a parallel join
   * gateway joins the path coming from the gateway with a path, that did not
   * split from this path, before all participants have been participating in
   * the process.
   */
  private Collection<EnforceabilityProblem> checkForExternalAndJoins(
          EventBasedGateway gateway, BranchingTree branches, BranchingTree history) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    Collection<String> participants = history.getParticipants();
    participants.retainAll(branches.getParticipants());
    Collection<ProcessNode> parallelJoins = 
            branches.parallelGatewaysBeforeFirstParticipationOf(participants);
    for(ProcessNode node : parallelJoins) {
      BranchingTree joinHistory = (new TreeBuilder(model)).buildTreeFor
              (node, TreeBuilder.FlowDirection.flowBefore);
      if(!joinHistory.allParallelPathesSynchronizeBefore(gateway)) {
        problems.add(new EnforceabilityProblem(DESC_ExternalSynchronisation, node));
      }
    }
    return problems;
  }

  /**
   * collects all participants of the choreography
   */
  private Collection<String> getChoreographyParticipants() {
    Collection<String> participants = new HashSet<String>();
    for (ProcessNode node : model.getNodes()) {
      participants.addAll(Utils.participantsOf(node));
    }
    return participants;
  }

  /**
   * generates a description for the "timeout needed"-warning
   */
  private String problemDescription(String participant) {
    return "WARNING: Participant " + participant
            + " cannot detect when no more message will be received and therefore"
            + " will wait indefinitely, unless an artificial timeout is set up.";
  }
}
