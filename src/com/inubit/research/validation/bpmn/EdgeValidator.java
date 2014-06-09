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
 * An EdgeValidator checks the syntactic correctness of an edge of a BPMN BPD.
 * @author tmi
 */
public abstract class EdgeValidator {

    public static EdgeValidator validatorFor(EdgeAdaptor edge,
            ModelAdaptor model, BPMNValidator validator)
            throws UnsupportedEdgeException {
        if (edge.isSequenceFlow()) {
            return new SequenceFlowValidator(edge, model, validator);
        }
        if (edge.isMessageFlow()) {
            return new MessageFlowValidator(edge, model, validator);
        }
        if (edge.isAssociation()) {
            return new AssociationValidator(edge, model, validator);
        }
        if (edge.isConversationLink()) {
            return new ConversationLinkValidator(edge, model, validator);
        }
        throw new UnsupportedEdgeException();
    }

    protected EdgeAdaptor edge;
    protected ModelAdaptor model;
    protected BPMNValidator validator;

    protected EdgeValidator(EdgeAdaptor edge, ModelAdaptor model,
            BPMNValidator validator) {
        this.edge = edge;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        checkHasSourceAndTarget();
    }

    private void checkHasSourceAndTarget() {
        if (edge.getTarget().getAdaptee() == null ||
                !model.hasNode(edge.getTarget())) {
            validator.addMessage("edgeWithoutTarget", edge);
        }
        if (edge.getSource().getAdaptee() == null ||
                !model.hasNode(edge.getSource())) {
            validator.addMessage("edgeWithoutSource", edge);
        }
    }
}
