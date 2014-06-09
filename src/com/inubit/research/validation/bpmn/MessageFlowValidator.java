/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;

/**
 *
 * @author tmi
 */
public class MessageFlowValidator extends EdgeValidator {

    public MessageFlowValidator(EdgeAdaptor edge, ModelAdaptor model,
            BPMNValidator validator) {
        super(edge, model, validator);
    }

    @Override
    public void doValidation() {
        super.doValidation();
        checkClusterCrossingRules();
        checkSourceAndTargetAreAllowed();
    }

    private void checkClusterCrossingRules() {
        if(model.getPoolForNode(edge.getSource()).equals(
                model.getPoolForNode(edge.getTarget()))) {
            validator.addMessage("messageFlowInPool", edge);
        }
    }

    private void checkSourceAndTargetAreAllowed() {
        if (! edge.getSource().mayHaveOutgoingMessageFlow()) {
            validator.addMessage("illegalMessageFlowSource", edge);
        }
        if (! edge.getTarget().mayHaveIncommingMessageFlow()) {
            validator.addMessage("illegalMessageFlowTarget", edge);
        }
    }
}
