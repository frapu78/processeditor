/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography;

import com.inubit.research.validation.bpmn.BPMNValidator;
import com.inubit.research.validation.bpmn.adaptors.ChoreographyNodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.SubChoreographyAdaptor;
import com.inubit.research.validation.bpmn.choreography.branchingTree.BranchingTree;
import com.inubit.research.validation.bpmn.choreography.branchingTree.TreeBuilder;
import com.inubit.research.validation.bpmn.choreography.branchingTree.TreeBuilder.FlowDirection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Abstract superclass for classes that check specific aspects or node-types
 * of choreographies for enforceability.
 * @author tmi
 */
public abstract class AbstractChoreographyCheck {

    protected ModelAdaptor model;
    protected BPMNValidator validator;

    /**
     * enum, that is used in methods, that act generically for predecessor and
     * successors, to indicate the interesting direction
     */
    protected enum Direction {

        Forward, Backward;
    }

    public AbstractChoreographyCheck(ModelAdaptor model, BPMNValidator validator) {
        this.validator = validator;
        this.model = model;
    }

    public void setModel(ModelAdaptor model) {
        this.model = model;
    }

    /**
     * specifies, which types of ProcessObjects are of interest for this
     * ChoreographyCheck.
     */
    //public abstract Collection<Class<? extends ProcessObjectAdaptor>> getRelevantClasses();
    /**
     * performs the checks necessary for a given object
     * @param object a ProcessObject that should be checked. This can be any
     * ProcessObject. If the object is not of interest for this ChoreographyCheck,
     * it will report no problems
     * @return the Collection of problems that were detected related to this object.
     * If there are no problems, this will be an empty Collection, but not null.
     */
    public abstract void checkNode(NodeAdaptor object);

    /*Identifying participants of activities*/
    /**
     * tests, wheter a participant will know when a Choreography-Activity has been
     * finished, i.e. if the node is a ChoreographyTask, he is a participant of the
     * task or if the node is a ChoreographySubProcess, he is a participant of all
     * activities that can occur as last activities in this SubProcess.
     */
    protected boolean noticesCompletion(String participant, NodeAdaptor node) {
        return isParticipantOfAll(participant, getLastTasks(node));
    }

    /**
     * identifies the ChoreographyTasks in which the specified participant is not
     * involved and that are final for the specified activity (for ChoreographyTasks:
     * the task itself, for ChoreographySubProcesses: the ChoreographyTasks, that
     * can occur last in this SubProcess)
     */
    protected Collection<NodeAdaptor> getFinalTasksWithoutParticipant(
            NodeAdaptor activity, String participant) {
        return tasksWithoutParticipant(getLastTasks(activity), participant);
    }

    /*special sequence-flow situations*/
    /**
     * Identifies the nodes whithin subProcess, that have incoming/outgoing
     * (according to direction) SequenceFlow from/to ProcessNodes, that are not
     * within subProcess.
     */
    protected Collection<NodeAdaptor> nodesWithSequenceFlowOverBorder(
            SubChoreographyAdaptor subProcess, Direction direction) {
        Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : subProcess.getProcessNodes()) {
            for (NodeAdaptor outside : neighbourNodes(node, direction)) {
                if (!subProcess.getProcessNodes().contains(outside)) {
                    nodes.add(node);
                    break;
                }
            }
        }
        return nodes;
    }

    protected Collection<NodeAdaptor> nodesWithoutSequenceFlowInDirection(
            SubChoreographyAdaptor subProcess, Direction direction) {
        Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : subProcess.getProcessNodes()) {
            if (neighbourNodes(node, direction).isEmpty()) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /*Finding out about the neighborhood*/
    protected Collection<NodeAdaptor> directlySucceedingChoreographyActivities(
            NodeAdaptor node) {
        return choreographyActivitiesIn(node.getSucceedingNodes());
    }

    /**
     * finds all ChoreographyActivities, that may directly follow the given node
     * in execution (ProcessNodes other then ChoreographyActivities are ignored).
     */
    protected Collection<NodeAdaptor> succeedingChoreographyActivities(
            NodeAdaptor node) {
        return nextRealNeighbourChoreographyActivities(node, Direction.Forward);
    }

    protected Collection<NodeAdaptor> directlyPrecedingChoreographyActivities(
            NodeAdaptor node) {
        return choreographyActivitiesIn(node.getPrecedingNodes());
    }

    /**
     * finds all ChoreographyActivities, that may directly preceed the given node
     * in execution (ProcessNodes other then ChoreographyActivities are ignored).
     */
    protected Collection<NodeAdaptor> precedingChoreographyActivities(
            NodeAdaptor node) {
        return nextRealNeighbourChoreographyActivities(node, Direction.Backward);
    }

    /**
     * collects all following ChoreographyActities, skipping other nodes than
     * ChoreographyActivities and following LinkEvents.
     */
    protected Collection<NodeAdaptor> nextNeighbourChoreographyActivities(
            Collection<NodeAdaptor> active, Direction direction) {
        Collection<NodeAdaptor> visited = new HashSet<NodeAdaptor>();
        Collection<NodeAdaptor> nextChoreographyActivities = new HashSet<NodeAdaptor>();
        visited.addAll(active);
        for (NodeAdaptor node : visited) {
            if (node.isChoreographyActivity()) {
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

    protected Collection<NodeAdaptor> choreographyActivitiesIn(
            Collection<NodeAdaptor> nodes) {
        Collection<NodeAdaptor> activities = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : nodes) {
            if (node.isChoreographyActivity()) {
                activities.add(node);
            }
        }
        return activities;
    }

    protected Collection<NodeAdaptor> neighbourNodes(NodeAdaptor node, Direction direction) {
        if (direction.equals(Direction.Backward)) {
            return node.getPrecedingNodes();
        } else {
            return node.getSucceedingNodes();
        }
    }

    protected Collection<NodeAdaptor> neighbourChoreographyActivities(
            NodeAdaptor node, Direction direction) {
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
    private Collection<NodeAdaptor> getLastTasks(NodeAdaptor choreographyActivity) {
        if (choreographyActivity.isChoreographyTask()) {
            Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>(1);
            nodes.add(choreographyActivity);
            return nodes;
        } else if (choreographyActivity.isSubChoreography()) {
            return getLastTasksOfSubChoreography(
                    (SubChoreographyAdaptor) choreographyActivity);
        } else {
            return new HashSet<NodeAdaptor>(0);
        }
    }

    private boolean isParticipantOfAll(
            String participant, Collection<NodeAdaptor> nodes) {
        for (NodeAdaptor node : nodes) {
            if (node.isChoreographyActivity()) {
                if (!((ChoreographyNodeAdaptor) node).hasParticipant(participant)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Collection<NodeAdaptor> performStepOfNextChoreographyActivitiesSearch(
            Collection<NodeAdaptor> active,
            Collection<NodeAdaptor> visited,
            Collection<NodeAdaptor> firstChoreographyActivities,
            Direction direction) {
        Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : active) {
            for (NodeAdaptor next : neighbourNodes(node, direction)) {
                if (next.isChoreographyActivity()) {
                    firstChoreographyActivities.add(next);
                } else if (!visited.contains(next)) {
                    nodes.add(next);
                }
                visited.add(next);
            }
        }
        return nodes;
    }

    private Collection<NodeAdaptor> getLastTasksOfSubChoreography(
            SubChoreographyAdaptor subProcess) {
        Collection<NodeAdaptor> finalNodes = nodesWithoutSequenceFlowInDirection(
                subProcess, Direction.Forward);
        finalNodes.addAll(nodesWithSequenceFlowOverBorder(
                subProcess, Direction.Forward));
        finalNodes = nextNeighbourChoreographyActivities(finalNodes, Direction.Backward);
        TreeBuilder builder = new TreeBuilder();
        for (Iterator<NodeAdaptor> iter = finalNodes.iterator(); iter.hasNext();) {
            BranchingTree tree = builder.buildTreeFor(iter.next(),
                    TreeBuilder.FlowDirection.flowAfter);
            if (tree.allAlternativesContainMultipleChoreographyActivities()) {
                iter.remove();
            }
        }
        return getLastTasksForAll(finalNodes);
    }

    /**
     * collects all those NodeAdaptors out of nodes, wich do not involve participant
     */
    private Collection<NodeAdaptor> tasksWithoutParticipant(
            Collection<NodeAdaptor> nodes, String participant) {
        Collection<NodeAdaptor> result = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : nodes) {
            if (node.isChoreographyActivity()) {
                if (!((ChoreographyNodeAdaptor) node).hasParticipant(participant)) {
                    result.add(node);
                }
            }
        }
        return result;
    }

    private Collection<NodeAdaptor> getLastTasksForAll(Collection<NodeAdaptor> nodes) {
        Collection<NodeAdaptor> result = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : nodes) {
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
    private Collection<NodeAdaptor> nextRealNeighbourChoreographyActivities(
            NodeAdaptor node, Direction direction) {
        Collection<NodeAdaptor> neighbours = nextNeighbourChoreographyActivities(
                neighbourNodes(node, direction), direction);
        TreeBuilder builder = new TreeBuilder();
        for (Iterator<NodeAdaptor> iter = neighbours.iterator(); iter.hasNext();) {
            NodeAdaptor neighbour = iter.next();
            BranchingTree tree = builder.buildTreeFor(neighbour,
                    direction.equals(Direction.Backward)
                    ? FlowDirection.flowAfter : FlowDirection.flowBefore);
            tree.trimAndEliminateToEndingAtNode(node);
            Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>(neighbours);
            nodes.remove(neighbour);
            if (tree.allPathesContainOneOf(nodes)) {
                iter.remove();
            }
        }
        return neighbours;
    }
}
