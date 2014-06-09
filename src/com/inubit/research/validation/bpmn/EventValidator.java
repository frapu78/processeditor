/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import net.frapu.code.visualization.bpmn.SequenceFlow;
import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;

/**
 *
 * @author tmi
 */
class EventValidator {

    private EventAdaptor event;
    private ModelAdaptor model;
    private BPMNValidator validator;

    public EventValidator(EventAdaptor event, ModelAdaptor model,
            BPMNValidator validator) {
        this.event = event;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        if (event.isCancelEvent()) {
            validateCancelEvent();
        }
        if (event.isMessageEvent()) {
            new MessageEventValidator(event, model, validator).doValidation();
        }
        if (event.isStartEvent()) {
            validateStartEvent();
        }
        if (event.isIntermediateEvent()) {
            validateIntermediateEvent();
        }
        if (event.isEndEvent()) {
            validateEndEvent();
        }
    }

    private void validateCancelEvent() {
        if (event.isCancelEndEvent()) {
            validateCancelEndEvent();
        } else {
            validateCancelIntermediateEvent();
        }
    }

    private void validateCancelEndEvent() {
        ClusterAdaptor cluster = model.getClusterForNode(event);
        while (!cluster.isNull()) {
            if (cluster.isTransaction()) {
                return;
            }
            cluster = model.getClusterForNode(cluster);
        }
        validator.addMessage("cancelEndEventOutsideTransaction", event);
    }

    private void validateCancelIntermediateEvent() {
        NodeAdaptor parent = event.getParentNode();
        if (parent.isNull() || !parent.isCluster()
                || !((ClusterAdaptor) parent).isTransaction()) {
            validator.addMessage("cancelIntermediateEventNotAttachedToTransaction", event);
        }
    }

    private void validateStartEvent() {
        if (model.getOutgoingEdges(SequenceFlow.class, event).isEmpty()) {
            validator.addMessage("startEventWithoutOutgoingSequenceFlow", event);
        }
        if (event.isErrorStartEvent() ||
                event.isCompensationStartEvent() ||
                event.isEscalationStartEvent()) {
            validateStartEventOnlyAllowedInEventSubProcess();
        }
        if (event.isErrorStartEvent() || event.isCompensationStartEvent()) {
            validateStartEventOnlyInterrupting();
        }
        if (!event.isInterrupting()) {
            validateNonInterruptingStartEvent();
        }
    }

    private void validateStartEventOnlyAllowedInEventSubProcess() {
        if (!model.getClusterForNode(event).isEventSubProcess()) {
            validator.addMessage("startEventTypeOnlyInEventSubProcess", event);
        }
    }

    private void validateStartEventOnlyInterrupting() {
        if (!event.isInterrupting()) {
            validator.addMessage("startEventTypeOnlyInterrupting", event);
        }
    }

    private void validateNonInterruptingStartEvent() {
        if (!model.getClusterForNode(event).isEventSubProcess()) {
            validator.addMessage("nonInterruptingStartEventOutsideEventSubProcess", event);

        }
    }

    private void validateIntermediateEvent() {
        if (event.isAttached() &&
                !(model.hasChoreography() && event.isRootNode())) {
            validateAttachedEvent();
        } else {
            validateIntermediateEventInNormalFlow();
        }
        if (event.isErrorIntermediateEvent()) {
            validateErrorIntermediateEvent();
        }
        if (event.isThrowing()) {
            validateThrowingIntermediateEvent();
        } else {
            validateCatchingIntermediateEvent();
        }
    }

    private void validateAttachedEvent() {
        if (event.isThrowing()) {
            validator.addMessage("attachedThrowingEvent", event);
        }
        if (event.isNoneIntermediateEvent() || event.isLinkIntermediateEvent()) {
            validator.addMessage("unallowedAttachedEvent", event);
        }
    }

    private void validateIntermediateEventInNormalFlow() {
        if (!event.isInterrupting()) {
            validator.addMessage("nonInterruptingInNormalFlow", event);
        }
        if (event.isEscalationIntermediateEvent() && event.isCatching()) {
            validator.addMessage("catchingEscalationIntermediateInNormalFlow", event);
        }
    }

    private void validateEndEvent() {
        if (model.getIncomingEdges(SequenceFlow.class, event).isEmpty()) {
            validator.addMessage("endEventWithoutSequenceFlow", event);
        }
    }

    private void validateErrorIntermediateEvent() {
        if (! (event.isAttached() && event.isInterrupting())) {
            validator.addMessage("wrongErrorIntermediateEvent", event);
        }
    }

    private void validateThrowingIntermediateEvent() {
        if (event.isParallelMultipleIntermediateEvent() ||
                event.isConditionalIntermediateEvent() ||
                event.isTimerIntermediateEvent()) {
            validator.addMessage("onlyCatchingAllowed", event);
        }
    }

    private void validateCatchingIntermediateEvent() {
        if (event.isCompensationIntermediateEvent() &&
                !(event.isAttached() &&
                  event.isInterrupting())) {
            validator.addMessage("illegalCatchingCompensationIntermediateEvent", event);
        }
    }
}
