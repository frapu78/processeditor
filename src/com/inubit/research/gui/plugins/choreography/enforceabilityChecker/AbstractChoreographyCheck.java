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
import com.inubit.research.gui.plugins.choreography.branchingTree.BranchingTree;
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder.FlowDirection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;

/**
 * Abstract superclass for classes that check specific aspects or node-types
 * of choreographies for enforceability.
 * @author tmi
 */
public abstract class AbstractChoreographyCheck {

  protected BPMNModel model;

  /**
   * enum, that is used in methods, that act generically for predecessor and
   * successors, to indicate the interesting direction
   */
  protected enum Direction {

    Forward, Backward;
  }

  public AbstractChoreographyCheck(BPMNModel model) {
    this.model = model;
  }

  public void setModel(BPMNModel model) {
    this.model = model;
  }

  /**
   * specifies, which types of ProcessObjects are of interest for this
   * ChoreographyCheck.
   */
  public abstract Collection<Class<? extends ProcessObject>> getRelevantClasses();

  /**
   * performs the checks necessary for a given object
   * @param object a ProcessObject that should be checked. This can be any
   * ProcessObject. If the object is not of interest for this ChoreographyCheck,
   * it will report no problems
   * @return the Collection of problems that were detected related to this object.
   * If there are no problems, this will be an empty Collection, but not null.
   */
  public abstract Collection<EnforceabilityProblem> checkObject(ProcessObject object);

  /**
   * Checks the realizability of the choreography (no complete enforceability
   * check - each AbstractChoreographyCheck subclass will check only one aspect).
   * @return a Collection of all problems, that were detected in the Choreography
   */
  public Collection<EnforceabilityProblem> checkRealizability() {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    for (ProcessObject object : model.getObjects()) {
      problems.addAll(checkObject(object));
    }
    return problems;
  }

  /*Identifying participants of activities*/
  /**
   * tests, wheter a participant will know when a Choreography-Activity has been
   * finished, i.e. if the node is a ChoreographyTask, he is a participant of the
   * task or if the node is a ChoreographySubProcess, he is a participant of all
   * activities that can occur as last activities in this SubProcess.
   */
  protected boolean noticesCompletion(String participant, ProcessNode node) {
    return isParticipantOfAll(participant, getLastTasks(node));
  }

  /**
   * identifies the ChoreographyTasks in which the specified participant is not
   * involved and that are final for the specified activity (for ChoreographyTasks:
   * the task itself, for ChoreographySubProcesses: the ChoreographyTasks, that
   * can occur last in this SubProcess)
   */
  protected Collection<ProcessNode> getFinalTasksWithoutParticipant(
          ProcessNode activity, String participant) {
    return tasksWithoutParticipant(getLastTasks(activity), participant);
  }

  /*special sequence-flow situations*/
  /**
   * Identifies the nodes whithin subProcess, that have incoming/outgoing
   * (according to direction) SequenceFlow from/to ProcessNodes, that are not
   * within subProcess.
   */
  protected Collection<ProcessNode> nodesWithSequenceFlowOverBorder(
          ChoreographySubProcess subProcess, Direction direction) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
    for (ProcessNode node : subProcess.getProcessNodes()) {
      for (ProcessNode outside : neighbourNodes(node, direction)) {
        if (!subProcess.getProcessNodes().contains(outside)) {
          nodes.add(node);
          break;
        }
      }
    }
    return nodes;
  }

  protected Collection<ProcessNode> nodesWithoutSequenceFlowInDirection(
          ChoreographySubProcess subProcess, Direction direction) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
    for (ProcessNode node : subProcess.getProcessNodes()) {
      if (neighbourNodes(node, direction).isEmpty()) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  /*Finding out about the neighborhood*/

  protected Collection<ProcessNode> directlySucceedingChoreographyActivities(
          ProcessNode node) {
    return choreographyActivitiesIn(
            Utils.getSucceedingNodes(node, model));
  }

  /**
   * finds all ChoreographyActivities, that may directly follow the given node
   * in execution (ProcessNodes other then ChoreographyActivities are ignored).
   */
  protected Collection<ProcessNode> succeedingChoreographyActivities(ProcessNode node) {
    return nextRealNeighbourChoreographyActivities(node, Direction.Forward);
  }

  protected Collection<ProcessNode> directlyPrecedingChoreographyActivities(ProcessNode node) {
    return choreographyActivitiesIn(Utils.getPrecedingNodes(node, model));
  }

  /**
   * finds all ChoreographyActivities, that may directly preceed the given node
   * in execution (ProcessNodes other then ChoreographyActivities are ignored).
   */
  protected Collection<ProcessNode> precedingChoreographyActivities(ProcessNode node) {
    return nextRealNeighbourChoreographyActivities(node, Direction.Backward);
  }

  /**
   * collects all following ChoreographyActities, skipping other nodes than
   * ChoreographyActivities and following LinkEvents.
   */
  protected Collection<ProcessNode> nextNeighbourChoreographyActivities(
          Collection<ProcessNode> active, Direction direction) {
    Collection<ProcessNode> visited = new HashSet<ProcessNode>();
    Collection<ProcessNode> nextChoreographyActivities = new HashSet<ProcessNode>();
    visited.addAll(active);
    for (ProcessNode node : visited) {
      if (Utils.isChoreographyActivity(node)) {
        active.remove(node);
        nextChoreographyActivities.add(node);
      }
    }
    while (!active.isEmpty()) {
      active = performStepOfNextChoreographyActivitiesSearch(
              active, visited, nextChoreographyActivities, direction);
    }
    return nextChoreographyActivities;
  }

  protected Collection<ProcessNode> choreographyActivitiesIn(
          Collection<ProcessNode> nodes) {
    Collection<ProcessNode> activities = new HashSet<ProcessNode>();
    for (ProcessNode node : nodes) {
      if (Utils.isChoreographyActivity(node)) {
        activities.add(node);
      }
    }
    return activities;
  }

  protected Collection<ProcessNode> neighbourNodes(ProcessNode node, Direction direction) {
    if (direction.equals(Direction.Backward)) {
      return Utils.getPrecedingNodes(node, model);
    } else {
      return Utils.getSucceedingNodes(node, model);
    }
  }

  protected Collection<ProcessNode> neighbourChoreographyActivities(
          ProcessNode node, Direction direction) {
    if (direction.equals(Direction.Backward)) {
      return directlyPrecedingChoreographyActivities(node);
    } else {
      return directlySucceedingChoreographyActivities(node);
    }
  }

  /*private helper methods*/
  /**
   * @param choreographyActivity a ChoreographyActivity, which´s last tasks
   * should be collected
   * @return if choreographyActivity is a ChoreographyTask: a Collection cotaining
   * only choreographyActivity; if choreographyActivity is a ChoreographySubProcess:
   * all the ChoreographyTasks, that may be final in the execution of this SubProcess
   */
  private Collection<ProcessNode> getLastTasks(ProcessNode choreographyActivity) {
    if (Utils.isChoreographyTask(choreographyActivity)) {
      Collection<ProcessNode> nodes = new HashSet<ProcessNode>(1);
      nodes.add(choreographyActivity);
      return nodes;
    } else if (Utils.isChoreographySubProcess(choreographyActivity)) {
      return getLastTasksOfChoreographySubProcess(
              (ChoreographySubProcess) choreographyActivity);
    } else {
      return new HashSet<ProcessNode>(0);
    }
  }

  private boolean isParticipantOfAll(String participant, Collection<ProcessNode> nodes) {
    for (ProcessNode node : nodes) {
      if (!Utils.isParticipantOf(participant, node)) {
        return false;
      }
    }
    return true;
  }

  private Collection<ProcessNode> performStepOfNextChoreographyActivitiesSearch(
          Collection<ProcessNode> active,
          Collection<ProcessNode> visited,
          Collection<ProcessNode> firstChoreographyActivities,
          Direction direction) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
    for (ProcessNode node : active) {
      for (ProcessNode next : neighbourNodes(node, direction)) {
        if (Utils.isChoreographyActivity(next)) {
          firstChoreographyActivities.add(next);
        } else if (!visited.contains(next)) {
          nodes.add(next);
        }
        visited.add(next);
      }
    }
    return nodes;
  }

  private Collection<ProcessNode> getLastTasksOfChoreographySubProcess(
          ChoreographySubProcess subProcess) {
    Collection<ProcessNode> finalNodes = nodesWithoutSequenceFlowInDirection(
            subProcess, Direction.Forward);
    finalNodes.addAll(nodesWithSequenceFlowOverBorder(
            subProcess, Direction.Forward));
    finalNodes = nextNeighbourChoreographyActivities(finalNodes, Direction.Backward);
    TreeBuilder builder = new TreeBuilder(model);
    for (Iterator<ProcessNode> iter = finalNodes.iterator(); iter.hasNext();) {
      BranchingTree tree = builder.buildTreeFor(iter.next(),
              TreeBuilder.FlowDirection.flowAfter);
      if (tree.allAlternativesContainMultipleChoreographyActivities()) {
        iter.remove();
      }
    }
    return getLastTasksForAll(finalNodes);
  }

  /**
   * collects all those ProcessNodes out of nodes, wich do not involve participant
   */
  private Collection<ProcessNode> tasksWithoutParticipant(
          Collection<ProcessNode> nodes, String participant) {
    Collection<ProcessNode> result = new HashSet<ProcessNode>();
    for (ProcessNode node : nodes) {
      if (!Utils.isParticipantOf(participant, node)) {
        result.add(node);
      }
    }
    return result;
  }

  private Collection<ProcessNode> getLastTasksForAll(Collection<ProcessNode> nodes) {
    Collection<ProcessNode> result = new HashSet<ProcessNode>();
    for (ProcessNode node : nodes) {
      result.addAll(getLastTasks(node));
    }
    return result;
  }

  /**
   * searches for ChoreographyActivities, that may preceed/succeed (according
   * to direction) the execution of node. (in contrast to normal getNeighbour...-
   * methods, this one leaves out those choreography activities, to which a path
   * without any choreography activities leads, but which are, due to parallel
   * action, never the direct successor of node).
   */
  private Collection<ProcessNode> nextRealNeighbourChoreographyActivities(
          ProcessNode node, Direction direction) {
    Collection<ProcessNode> neighbours = nextNeighbourChoreographyActivities(
            neighbourNodes(node, direction), direction);
    TreeBuilder builder = new TreeBuilder(model);
    for (Iterator<ProcessNode> iter = neighbours.iterator(); iter.hasNext();) {
      ProcessNode neighbour = iter.next();
      BranchingTree tree = builder.buildTreeFor(neighbour,
              direction.equals(Direction.Backward)
              ? FlowDirection.flowAfter : FlowDirection.flowBefore);
      tree.trimAndEliminateToEndingAtNode(node);
      Collection<ProcessNode> nodes = new HashSet<ProcessNode>(neighbours);
      nodes.remove(neighbour);
      if (tree.allPathesContainOneOf(nodes)) {
        iter.remove();
      }
    }
    return neighbours;
  }
}
