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
import java.util.Collection;

/**
 * Checks the requirement, that the initiator of a task has to be a participant
 * of any preceding activity.
 * @author tmi
 */
public class FlowCheck extends AbstractChoreographyCheck {

    public FlowCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    /**
     * checks all incoming SequenceFlow of one node.
     * @param node the node, which´s incoming SequenceFlow is to be checked.
     */
    private void checkSequenceFlowTo(NodeAdaptor node) {
        for (NodeAdaptor source : precedingChoreographyActivities(node)) {
            checkFlowFromTo(source, node);
        }
    }

    /**
     * checks, wheter a SequenceFlow-connection between two nodes would be ok.
     */
    private void checkFlowFromTo(
            NodeAdaptor source, NodeAdaptor target) {
        if (source.isChoreographyActivity() && target.isChoreographyActivity()) {
            Collection<NodeAdaptor> nodesWithoutParticipation =
                    getFinalTasksWithoutParticipant(source,
                    ((ChoreographyNodeAdaptor) target).getActiveParticipant());
            if (!nodesWithoutParticipation.isEmpty()) {
                validator.addMessage(
                        "initiatorOfChoreographyActivityNotParticipantInPriorActivity",
                        target, nodesWithoutParticipation);
            }
        }
    }

    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isChoreographyActivity()) {
            checkSequenceFlowTo(node);
        }
    }
}
