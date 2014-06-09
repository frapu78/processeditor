/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ActivityAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.LaneableClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.bpmn.ConversationLink;

/**
 *
 * @author tmi
 */
class ConversationValidator {

    NodeAdaptor conversation;
    ModelAdaptor model;
    BPMNValidator validator;

    public ConversationValidator(NodeAdaptor conversation, ModelAdaptor model,
            BPMNValidator validator) {
        this.conversation = conversation;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        validateNotInPool();
        validatePartnerCount();
        validateHasSendersAndReceivers();
    }

    private void validateNotInPool() {
        if (!model.getPoolForNode(conversation).isNull()) {
            validator.addMessage("conversationInPool", conversation);
        }
    }

    private void validatePartnerCount() {
        Set<LaneableClusterAdaptor> partners =
                new HashSet<LaneableClusterAdaptor>();
        for (EdgeAdaptor conversationLink :
                conversation.getAdjacentEdges(ConversationLink.class)) {
            partners.add(partnerOf(conversationLink));
        }
        if (partners.size() < 2) {
            validator.addMessage("conversationWithTooFewLinks", conversation);
        }
    }

    private LaneableClusterAdaptor partnerOf(EdgeAdaptor conversationLink) {
        NodeAdaptor target;
        if (conversationLink.getSource().equals(conversation)) {
            target = conversationLink.getTarget();
        } else {
            target = conversationLink.getSource();
        }
        return model.getPoolForNode(target);
    }

    private void validateHasSendersAndReceivers() {
        if (!hasSender()) {
            validator.addMessage("conversationOnlyConnectedToReceivers",
                    conversation);
        }
        if (!hasReceiver()) {
            validator.addMessage("conversationOnlyConnectedToSenders",
                    conversation);
        }
    }

    private boolean hasSender() {
        for (EdgeAdaptor link :
                conversation.getAdjacentEdges(ConversationLink.class)) {
            NodeAdaptor otherNode = link.getSource().equals(conversation) ?
                link.getTarget() : link.getSource();
            if (otherNode.isEvent()) {
                EventAdaptor event = (EventAdaptor) otherNode;
                if (event.isThrowing() || event.isStartEvent()) {
                    return true;
                }
            } else if (otherNode.isTask()) {
                if (!((ActivityAdaptor) otherNode).isReceiveTask()) {
                    return true;
                }
            } else {//consider Pools and any nodes, that have not been mentioned
                //to be senders
                return true;
            }
        }
        return false;
    }

    private boolean hasReceiver() {
        for (EdgeAdaptor link :
                conversation.getAdjacentEdges(ConversationLink.class)) {
            NodeAdaptor otherNode = link.getSource().equals(conversation) ?
                link.getTarget() : link.getSource();
            if (otherNode.isEvent()) {
                EventAdaptor event = (EventAdaptor) otherNode;
                if (event.isCatching() || event.isEndEvent()) {
                    return true;
                }
            } else if (otherNode.isTask()) {
                if (!((ActivityAdaptor) otherNode).isSendTask()) {
                    return true;
                }
            } else {//consider Pools and any nodes, that have not been mentioned
                //to be receivers
                return true;
            }
        }
        return false;
    }
}
