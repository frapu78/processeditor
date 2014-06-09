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
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.choreography.branchingTree.BranchingTree;
import com.inubit.research.validation.bpmn.choreography.branchingTree.TreeBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tmi
 */
public class EventBasedGatewayCheck extends AbstractChoreographyCheck {

    public EventBasedGatewayCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isEventBasedGateway()) {
            checkEventBasedGateway((GatewayAdaptor) node);
        }
    }

    private void checkEventBasedGateway(
            GatewayAdaptor gateway) {
        checkConnectedNodes(gateway);//only allowed nodes connected?
        checkDirectlySucceedingParticipants(gateway);
        //are all directly following receivers or all directly following senders the same?
        TreeBuilder builder = new TreeBuilder();
        BranchingTree branches = builder.buildTreeFor(
                gateway, TreeBuilder.FlowDirection.flowAfter);
        BranchingTree history = builder.buildTreeFor(
                gateway, TreeBuilder.FlowDirection.flowBefore);
        checkPathParticipation(gateway, branches, history);
        //everyone, who participated before, will participate in every alternative
        //or no alternative (unless he is a sender of a directly succeeding activity)
        checkForExternalAndJoins(gateway, branches, history);
        //no parallelJoins occur on one path of the gateway, where at least one
        //of the incoming pathes was not forked out of this path
    }

    /**
     * checks, that all the following senders are the same
     */
    private boolean sameFollowingSenders(NodeAdaptor gateway) {
        String sender = null;
        for (NodeAdaptor node : gateway.getSucceedingNodes()) {
            if (node.isChoreographyActivity()) {
                ChoreographyNodeAdaptor choreographyActivity =
                        (ChoreographyNodeAdaptor) node;
                if (sender == null) {
                    sender = choreographyActivity.getActiveParticipant();
                } else if (!sender.equals(
                        choreographyActivity.getActiveParticipant())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * checks, that all the following receivers are the same
     */
    private boolean sameFollowingReceivers(NodeAdaptor gateway) {
        Set<String> receivers = null;
        for (NodeAdaptor node : gateway.getSucceedingNodes()) {
            if (node.isChoreographyActivity()) {
                ChoreographyNodeAdaptor choreographyActivity =
                        (ChoreographyNodeAdaptor) node;
                Set<String> currentReceivers = new HashSet<String>(
                        ((ChoreographyNodeAdaptor) node).getPassiveParticipants());
                if (receivers == null) {
                    receivers = currentReceivers;
                } else {
                    if (!currentReceivers.equals(receivers)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * checks, that all the following senders or all the following receivers are
     * the same participant.
     */
    private void checkDirectlySucceedingParticipants(GatewayAdaptor gateway) {
        if (!(sameFollowingSenders(gateway) || sameFollowingReceivers(gateway))) {
            validator.addMessage("differentSendersAndReceiversFollowingEBGW",
                    gateway);
        }
    }

    /**
     * checks, that only the allowed ProcessNodes (ChoreographyTasks,
     * TimerIntermediateEvent or SignalIntermediateEvent) are directly succeeding
     * nodes of the gateway. If the gateway is Instantiating, other nodes are allowed:
     * all StartEvents
     */
    private void checkConnectedNodes(GatewayAdaptor gateway) {
        for (NodeAdaptor node : gateway.getSucceedingNodes()) {
            if (!mayFollowEventBasedGateway(node)) {
                List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                related.add(gateway);
                validator.addMessage("illegalNodeFollowingEBGWInChoreography",
                        node, related);
            }
        }
    }

    private boolean mayFollowEventBasedGateway(NodeAdaptor node) {
        if (node.isChoreographyTask()) {
            return true;
        } else if (node.isEvent()) {
            EventAdaptor event = (EventAdaptor) node;
            return event.isTimerIntermediateEvent()
                    || event.isSignalIntermediateEvent();
        } else {
            return false;
        }
    }

    /**
     * checks, that all participants, who have been participating before the gateway,
     * will participate in either all or none of the pathes from the gateway.
     */
    private void checkPathParticipation(
            GatewayAdaptor gateway, BranchingTree branches, BranchingTree history) {
        Collection<String> participants = getChoreographyParticipants();
        for (NodeAdaptor node : gateway.getSucceedingNodes()) {
            if (node.isChoreographyActivity()) {
                participants.remove(
                        ((ChoreographyNodeAdaptor) node).getActiveParticipant());
            }
        }
        for (String participant : participants) {
            if (!branches.allAlternativesInvolve(participant)
                    && !branches.noAlternativesInvolve(participant)
                    && !history.noAlternativesInvolve(participant)
                    && !history.noPathesContainTriggeredStartEvent()) {
                validator.addMessage("timeoutOnEBGW",
                        gateway, branches.activitiesWithParticipant(participant));
            }
        }
    }

    /**
     * checks for situations in pathes from the gateway, where a parallel join
     * gateway joins the path coming from the gateway with a path, that did not
     * split from this path, before all participants have been participating in
     * the process.
     */
    private void checkForExternalAndJoins(
            GatewayAdaptor gateway, BranchingTree branches, BranchingTree history) {
        Collection<String> participants = history.getParticipants();
        participants.retainAll(branches.getParticipants());
        Collection<NodeAdaptor> parallelJoins =
                branches.parallelGatewaysBeforeFirstParticipationOf(participants);
        for (NodeAdaptor node : parallelJoins) {
            BranchingTree joinHistory = (new TreeBuilder()).buildTreeFor(node,
                    TreeBuilder.FlowDirection.flowBefore);
            if (!joinHistory.allParallelPathesSynchronizeBefore(gateway)) {
                validator.addMessage(
                        "joinAfterEBGWMakesChoreographyUnenforceable", node);
            }
        }
    }

    /**
     * collects all participants of the choreography
     */
    private Collection<String> getChoreographyParticipants() {
        Collection<String> participants = new HashSet<String>();
        for (NodeAdaptor node : model.getNodes()) {
            if (node.isChoreographyActivity()) {
                participants.addAll(
                        ((ChoreographyNodeAdaptor) node).getParticipants());
            }
        }
        return participants;
    }
}
