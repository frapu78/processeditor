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

/**
 * Checks, that the initial participant of a node is never an MI-participant.
 * @author tmi
 */
public class MultipleParticipantsCheck extends AbstractChoreographyCheck {

    public MultipleParticipantsCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isChoreographyActivity()) {
            checkChoreographyActivity((ChoreographyNodeAdaptor) node);
        }
    }

    /**
     * checks for one ChoreographyActivity, that the initiator of this activity
     * is no MI-participant.
     */
    private void checkChoreographyActivity(
            ChoreographyNodeAdaptor activity) {
        if (activity.isMultipleParticipant(activity.getActiveParticipant())) {
            validator.addMessage("miInitiator", activity.asNodeAdaptor());
        }
    }
}
