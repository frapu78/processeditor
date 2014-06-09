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
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author tmi
 */
class GatewayValidator {

    private GatewayAdaptor gateway;
    private ModelAdaptor model;
    private BPMNValidator validator;

    public GatewayValidator(GatewayAdaptor gateway, ModelAdaptor model,
            BPMNValidator validator) {
        this.gateway = gateway;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        if (gateway.isEventBasedGateway()) return;
        //they are validated by a more specialized validator
        checkForNeedlessness();
        checkForOutgoingConditionalFlows();
        validateOutgoingDefaultFlow();
    }

    private void checkForNeedlessness() {
        if (model.getIncomingEdges(SequenceFlow.class, gateway).size() <= 1 &&
                model.getOutgoingEdges(SequenceFlow.class, gateway).size() <= 1) {
            validator.addMessage("uselessGateway", gateway);
        }
    }

    private void checkForOutgoingConditionalFlows() {
        if (gateway.isEventBasedGateway()) {
            return;
        }
        Set<EdgeAdaptor> conditionalFlows = new HashSet<EdgeAdaptor>();
        for (EdgeAdaptor edge : gateway.getOutgoingSequenceFlow()) {
            if (edge.isConditionalSequenceFlow()) {
                conditionalFlows.add(edge);
            }
        }
        if (!conditionalFlows.isEmpty()) {
            String messageID = gateway.isDecisionGateway()?
                "conditionalFlowFromDecisionGateway" :
                "conditionalFlowFromParallelGateway";
            validator.addMessage(messageID, gateway, conditionalFlows);
        }
    }

    private void validateOutgoingDefaultFlow() {
        Set<EdgeAdaptor> defaultFlows = new HashSet<EdgeAdaptor>(),
                         conditionalFlows = new HashSet<EdgeAdaptor>();
        for (EdgeAdaptor edge : gateway.getOutgoingSequenceFlow()) {
            if (edge.isDefaultSequenceFlow()) {
                defaultFlows.add(edge);
            } else if (gateway.isDecisionGateway()) {
                conditionalFlows.add(edge);
            }
        }
        if (defaultFlows.size() > 1) {
            validator.addMessage("multipleDefaultFlows", gateway, defaultFlows);
        }
        if (!defaultFlows.isEmpty()
                && conditionalFlows.isEmpty()
                && !gateway.isDecisionGateway()) {
            validator.addMessage("defaultFlowFromGatewayWithNoDecision",
                    gateway, defaultFlows);
        }
    }
}
