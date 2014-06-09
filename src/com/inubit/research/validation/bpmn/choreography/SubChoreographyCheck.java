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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks, that the initiator of a ChoreographySubProcess is really the initiator
 * of every initial activity of this SubProcess, and that only those participants
 * take part in the SubProcess, who are mentioned in one of the participant bands,
 * and that only Empty StartEvents are used within the SubProcess.
 * @author tmi
 */
public class SubChoreographyCheck extends AbstractChoreographyCheck {

    public SubChoreographyCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isSubChoreography()) {
            checkSubChoreography((SubChoreographyAdaptor) node);
        }
    }

    private void checkSubChoreography(
            SubChoreographyAdaptor subChoreography) {
        checkStartEventTypes(subChoreography);
        if (model.isPureChoreography()) {
            checkParticipants(subChoreography);
            checkInitiators(subChoreography);
        }
    }

    private void checkStartEventTypes(
            SubChoreographyAdaptor subChoreography) {
        for (NodeAdaptor node : subChoreography.getProcessNodes()) {
            if (node.isStartEvent()
                    && !node.isNoneStartEvent()) {
                List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                related.add(subChoreography);
                validator.addMessage("onlyNoneStartEventsInSubChoreography",
                        node, related);
            }
        }
    }

    /**
     * checks, for a ChoreographySubProcess, that only those participants take part
     * in it, who are mentioned in one of the participant bands.
     */
    private void checkParticipants(
            SubChoreographyAdaptor subChoreography) {
        List<String> participants = subChoreography.collectParticipants();
        participants.removeAll(subChoreography.getParticipants());
        if (!participants.isEmpty()) {
            validator.addMessage("illegalSubChoreographyParticipants",
                    subChoreography,
                    activitiesEngagingOneOf(
                        subChoreography.getProcessNodes(), participants));
        }
    }

    /**
     * collects all ChoreographyActivities out of a set of ProcessNodes, that do
     * engage one of the supplied participants.
     */
    private Collection<NodeAdaptor> activitiesEngagingOneOf(
            Collection<NodeAdaptor> nodes, Collection<String> participants) {
        Collection<NodeAdaptor> activities = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : nodes) {
            if (node.isChoreographyActivity()) {
                for (String participant :
                        ((ChoreographyNodeAdaptor)node).getParticipants()) {
                    if (participants.contains(participant)) {
                        activities.add(node);
                        break;
                    }
                }
            }
        }
        return activities;
    }

    /**
     * checks, that all initial activities of the ChoreographySubProcess are initiated
     * by the initiator of the SubProcess.
     */
    private void checkInitiators(
            SubChoreographyAdaptor subChoreography) {
        Collection<NodeAdaptor> nodes = getInitialNodes(subChoreography);
        nodes = nextNeighbourChoreographyActivities(nodes, Direction.Forward);
        checkInitiatorsInSubprocess(nodes, subChoreography);
    }

    private Collection<NodeAdaptor> getInitialNodes(
            SubChoreographyAdaptor subChoreography) {
        Collection<NodeAdaptor> nodes = new HashSet<NodeAdaptor>();
        nodes.addAll(nodesWithSequenceFlowOverBorder(subChoreography, Direction.Backward));
        nodes.addAll(nodesWithoutSequenceFlowInDirection(
                subChoreography, Direction.Backward));
        return nodes;
    }

    /**
     * checks, for the set of initial ChoreographyActivities, that they are initiated
     * by the ChoreographySubProcess´ initiator
     */
    private void checkInitiatorsInSubprocess(
            Collection<NodeAdaptor> firstChoreographyActivities,
            SubChoreographyAdaptor subChoreography) {
        for (NodeAdaptor node : firstChoreographyActivities) {
            if (node.isChoreographyActivity()) {
                if (!((ChoreographyNodeAdaptor)node).getActiveParticipant().
                        equals(subChoreography.getActiveParticipant())) {
                    List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                    related.add(subChoreography);
                    validator.addMessage("illegalSubChoreographyInitiator",
                            node, related);
                }
            }
        }
    }
}
