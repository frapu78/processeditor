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
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ActivityAdaptor;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.MessageFlow;

/**
 *
 * @author tmi
 */
class TaskValidator {

    private ActivityAdaptor task;
    private ModelAdaptor model;
    private BPMNValidator validator;

    private List<EdgeAdaptor> incommingMessages, outgoingMessages,
            conversationLinks;

    public TaskValidator(ActivityAdaptor task, ModelAdaptor model,
            BPMNValidator validator) {
        this.task = task;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        if (!model.isColaboration()) return;
        incommingMessages = task.getIncomingEdges(MessageFlow.class);
        outgoingMessages = task.getOutgoingEdges(MessageFlow.class);
        conversationLinks = task.getAdjacentEdges(ConversationLink.class);
        if (task.isSendTask()) {
            validateSendTask();
        } else if (task.isReceiveTask()) {
            validateReceiveTask();
        } else if (task.isServiceTask()) {
            validateServiceTask();
        } else {
            validateOtherTask();
        }
    }

    private void validateSendTask() {
        if (outgoingMessages.isEmpty() && conversationLinks.isEmpty()) {
            if (incommingMessages.isEmpty()) {
                addMessage("sendTaskWithoutMessageFlow");
            } else {
                addMessage("sendTaskWithIncommingMessageFlow");
            }
        }
        if (! (outgoingMessages.isEmpty() ||
                incommingMessages.isEmpty())) {
            if (hasMultipleCommunicationPartners()) {
                addMessage("taskWithInAndOutMessageFlowWithMultiplePartners");
            } else {
                addMessage("sendTaskWithIncommingAndOutgoingMessageFlow");
            }
        }
        if (outgoingMessages.isEmpty() &&
                !(incommingMessages.isEmpty() || conversationLinks.isEmpty())) {
            addMessage("sendTaskWithIncommingMessageFlowAndConversationLink");
        }
    }
    
    private void validateReceiveTask() {
        if (incommingMessages.isEmpty() && conversationLinks.isEmpty()) {
            if (outgoingMessages.isEmpty()) {
                addMessage("receiveTaskWithoutMessageFlow");
            } else {
                addMessage("receiveTaskWithOutgoingMessageFlow");
            }
        } 
        if (! (outgoingMessages.isEmpty() || incommingMessages.isEmpty())) {
            if (hasMultipleCommunicationPartners()) {
                addMessage("taskWithInAndOutMessageFlowWithMultiplePartners");
            } else {
                addMessage("receiveTaskWithIncommingAndOutgoingMessageFlow");
            }
        }
        if (incommingMessages.isEmpty() &&
                !(outgoingMessages.isEmpty() || conversationLinks.isEmpty())) {
            addMessage("receiveTaskWithOutgoingMessageFlowAndConversationLink");
        }
    }

    private void validateServiceTask() {
        if (conversationLinks.isEmpty()) {
            if (incommingMessages.isEmpty()) {
                if (outgoingMessages.isEmpty()) {
                    addMessage("serviceTaskWithoutMessageFlow");
                } else {
                    addMessage("serviceTaskWithOnlyOutgoingMessageFlow");
                }
            } else {
                if(outgoingMessages.isEmpty()) {
                    addMessage("serviceTaskWithOnlyIncommingMessageFlow");
                } else {
                    if (hasMultipleCommunicationPartners()) {
                        addMessage("taskWithInAndOutMessageFlowWithMultiplePartners");
                    } //else: it is OK
                }
            }
        }
    }

    private void validateOtherTask() {
        if (conversationLinks.isEmpty()) {
            if (incommingMessages.isEmpty()) {
                if (! outgoingMessages.isEmpty()) {
                    addMessage("abstractTaskWithOutgoingMessageFlow");
                } // else: it is OK
            } else {
                if (outgoingMessages.isEmpty()) {
                    addMessage("abstractTaskWithIncommingMessageFlow");
                } else {
                    if (hasMultipleCommunicationPartners()) {
                        addMessage("taskWithInAndOutMessageFlowWithMultiplePartners");
                    } else {
                        addMessage("abstractTaskWithIncommingAndOutgoingMessageFlow");
                    }
                }
            }
        } else {
            addMessage("abstractTaskWithConversationLink");
        }
    }

    private List<ProcessObjectAdaptor> relatedObjects() {
        List<ProcessObjectAdaptor> related =
                new LinkedList<ProcessObjectAdaptor>(incommingMessages);
        related.addAll(outgoingMessages);
        related.addAll(conversationLinks);
        return related;
    }

    private void addMessage(String messageID) {
        validator.addMessage(messageID, task, relatedObjects());
    }

    private boolean hasMultipleCommunicationPartners() {
        NodeAdaptor firstPartner = firstPartner();
        if (firstPartner == null) return false;
        for (EdgeAdaptor edge : incommingMessages) {
            if (!firstPartner.equals(model.getPoolForNode(edge.getSource()))) {
                return true;
            }
        }
        for (EdgeAdaptor edge : outgoingMessages) {
            if (!firstPartner.equals(model.getPoolForNode(edge.getTarget()))) {
                return true;
            }
        }
        return false;
    }

    private NodeAdaptor firstPartner() {
        if (! incommingMessages.isEmpty()) {
            return model.getPoolForNode(
                    incommingMessages.get(0).getSource());
        } else if (! outgoingMessages.isEmpty()) {
            return model.getPoolForNode(
                    outgoingMessages.get(0).getTarget());
        } else {
            return null;
        }
    }

}
