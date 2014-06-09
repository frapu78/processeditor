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
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.LinkedList;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import java.util.List;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.MessageFlow;

/**
 * A MessageEventValidator performs specific validation steps for a
 * MessageIntermediateEvent, MessageStartEvent or MessageEndEvent.
 * @author tmi
 */
public class MessageEventValidator {

    private EventAdaptor messageEvent;
    private ModelAdaptor model;
    private BPMNValidator validator;

    public MessageEventValidator(EventAdaptor messageEvent,
            ModelAdaptor model, BPMNValidator validator) {
        this.messageEvent = messageEvent;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        if (!model.isColaboration()) return;
        List<EdgeAdaptor> messages = getMessageFlowList();
        List<EdgeAdaptor> conversationLinks =
                messageEvent.getAdjacentEdges(ConversationLink.class);
        if (messages.isEmpty() && conversationLinks.isEmpty()) {
            reportNoMessages(); // @todo: Validation needs to be refined for main process events!
        } else if (messages.size() + conversationLinks.size() > 1) {
            reportTooManyMessages(messages);
        }
    }

    private List<EdgeAdaptor> getMessageFlowList() {
        if (messageEvent.isThrowingMessageEvent()) {
            return model.getOutgoingEdges(MessageFlow.class, messageEvent);
        } else {
            return model.getIncomingEdges(MessageFlow.class, messageEvent);
        }
    }

    private void reportNoMessages() {
        validator.addMessage(noMessagesTextID(), messageEvent);
    }

    private void reportTooManyMessages(List<EdgeAdaptor> messages) {
        List<ProcessObjectAdaptor> related =
                new LinkedList<ProcessObjectAdaptor>(messages);
        validator.addMessage(tooManyMessagesTextID(), messageEvent, related);
    }

    private String noMessagesTextID() {
        if (messageEvent.isIntermediateEvent()) {
            if (messageEvent.isCatching()) {
                return "messageIntermediateCatchEventWithoutMessageFlow";
            } else {
                return "messageIntermediateThrowEventWithoutMessageFlow";
            }
        } else if (messageEvent.isStartEvent()) {
            return "messageStartEventWithoutMessageFlow";
        } else {
            return "messageEndEventWithoutMessageFlow";
        }
    }

    private String tooManyMessagesTextID() {
        if (messageEvent.isIntermediateEvent()) {
            if (messageEvent.isCatching()) {
                return "multipleIncomingMessageFlowsToIntermediateEvent";
            } else {
                return "multipleOutgoingMessageFlowsFromIntermediateEvent";
            }
        } else if (messageEvent.isStartEvent()) {
            return "multipleIncomingMessageFlowsToMessageStartEvent";
        } else {
            return "multipleOutgoingMessageFlowsFromMessageEndEvent";
        }
    }
}
