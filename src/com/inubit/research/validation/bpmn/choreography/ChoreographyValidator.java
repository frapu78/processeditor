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
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a full enforceability-check for BPMN-Choreography diagrams. It
 * combines the following ChoreographyChecks in order to do so:
 * ForbiddenNodesCheck, EventBasedGatewayCheck, FlowCheck, SubProcessCheck,
 * AssociatedMessagesCheck, AttachedEventsCheck, InclusiveGatewayCheck,
 * MultipleParticipantsCheck.
 * @author tmi
 */
public class ChoreographyValidator {

    ModelAdaptor model;
    private List<AbstractChoreographyCheck> checks;

    public ChoreographyValidator(ModelAdaptor model,
            BPMNValidator validator) {
        this.model = model;
        checks = new LinkedList<AbstractChoreographyCheck>();
        if (!(model.hasGlobalPool() || model.hasConversation())) {
            checks.add(new ForbiddenNodesCheck(model, validator));
        }
        checks.add(new EventBasedGatewayCheck(model, validator));
        checks.add(new FlowCheck(model, validator));
        checks.add(new SubChoreographyCheck(model, validator));
        checks.add(new AssociatedMessagesCheck(model, validator));
        checks.add(new InclusiveGatewayCheck(model, validator));
        checks.add(new MultipleParticipantsCheck(model, validator));
    }

    public void checkModel() {
        for (NodeAdaptor node : model.getRootNodes()) {
            for (AbstractChoreographyCheck check : checks) {
                check.checkNode(node);
            }
        }
    }
}
