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
import java.util.List;

/**
 *
 * @author tmi
 */
class ConversationLinkValidator extends EdgeValidator {

    public ConversationLinkValidator(EdgeAdaptor edge, ModelAdaptor model,
            BPMNValidator validator) {
        super(edge, model, validator);
    }

    @Override
    public void doValidation() {
        super.doValidation();
        checkSourceAndTargetAllowed();
        checkIsConnectedToConversation();
        checkIsConnectedToParticipantNode();
    }

    private void checkSourceAndTargetAllowed() {
        if (! (edge.getSource().mayBeConnectedByConversationLink())) {
            List<ProcessObjectAdaptor> related =
                    new LinkedList<ProcessObjectAdaptor>();
            related.add(edge.getSource());
            validator.addMessage("illegalConversationLinkTarget", edge, related);
            //illegal..._Target_ is not a mistake, because the conversation can
            //allways been treated as source and the other node as target
        }
        if (! (edge.getTarget().mayBeConnectedByConversationLink())) {
            List<ProcessObjectAdaptor> related =
                    new LinkedList<ProcessObjectAdaptor>();
            related.add(edge.getTarget());
            validator.addMessage("illegalConversationLinkTarget", edge, related);
        }
    }

    private void checkIsConnectedToConversation() {
        if (! (edge.getSource().isConversation() ||
                edge.getTarget().isConversation())) {
            validator.addMessage("conversationLinkWithoutConversation", edge);
        }
    }

    private void checkIsConnectedToParticipantNode() {
        if (edge.getSource().isConversation() &&
                edge.getTarget().isConversation()) {
            validator.addMessage("conversationLinkWithTwoConversations", edge);
        }
    }
}
