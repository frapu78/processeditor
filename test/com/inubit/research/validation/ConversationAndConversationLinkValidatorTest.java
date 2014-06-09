/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.validation;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class ConversationAndConversationLinkValidatorTest extends BPMNValidationTestCommons{

    private Conversation createConversation() {
        Conversation conversation = new Conversation();
        model.addNode(conversation);
        conversation.setText("conversation");
        return conversation;
    }

    private ConversationLink addConversationLink(ProcessNode from, ProcessNode to) {
        ConversationLink link = new ConversationLink(from, to);
        model.addEdge(link);
        return link;
    }

    @Test
    public void testConversationWithTwoEmptyPools() {
        Conversation conversation = createConversation();
        addConversationLink(conversation, getPoolA());
        addConversationLink(conversation, getPoolB());
        getPoolB().setProperty(Pool.PROP_BLACKBOX_POOL, Pool.TRUE);
        assertNoMessages(true);
    }

    @Test
    public void testConversationWithOnlyOnePool() {
        Conversation conversation = createConversation();
        addConversationLink(conversation, getPoolA());
        assertOneError(texts.getLongText("conversationWithTooFewLinks"),
                conversation, true);
    }

    @Test
    public void testConversationInAPool() {
        Conversation conversation = createConversation();
        getPoolA().addProcessNode(conversation);
        addConversationLink(conversation, getPoolB());
        Pool poolC = new Pool();
        poolC.setText("pool C");
        model.addNode(poolC);
        addConversationLink(conversation, poolC);
        assertOneError(texts.getLongText("conversationInPool"),
                conversation, true);
    }

    @Test
    public void testConversationLinksToAllowedNodes() {
        Conversation conversation = createConversation();
        makeSendTask(getTask1InA(true, false));
        addConversationLink(conversation, getTask1InA());
        MessageIntermediateEvent messageEvent = new MessageIntermediateEvent();
        addConversationLink(conversation, messageEvent);
        messageEvent.setText("message");
        model.addNode(messageEvent);
        getPoolA().addProcessNode(messageEvent);
        addSequenceFlow(getTask1InA(), messageEvent);
        addSequenceFlow(messageEvent, getEndInA());

        getTask1InB().setStereotype(Task.TYPE_RECEIVE);
        addConversationLink(conversation, getTask1InB());

        assertNoMessages(true);
    }

    @Test
    public void testConversationLinkToTimerStartEvent() {
        Conversation conversation = createConversation();
        ConversationLink link = addConversationLink(conversation, getStartInA());
        makeSendTask(getTask1InB());
        makeReceiveTask(getTask1InA());
        addConversationLink(conversation, getTask1InB());
        addConversationLink(conversation, getTask1InA());
        assertOneError(texts.getLongText("illegalConversationLinkTarget"),
                link, listOf(getStartInA()), true);
    }

    @Test
    public void testConversationLinkBetweenTwoTasks() {
        makeSendTask(getTask1InA(true, false));
        makeReceiveTask(getTask2InA(false, true));
        addSequenceFlow(getTask1InA(), getTask2InA());
        makeReceiveTask(getTask1InB(true, false));
        makeSendTask(getTask2InB(false, true));
        addSequenceFlow(getTask1InB(), getTask2InB());
        addMessageFlow(getTask1InA(), getTask1InB());
        addMessageFlow(getTask2InB(), getTask2InA());

        ConversationLink link = addConversationLink(getTask1InA(), getTask2InB());
        assertOneError(texts.getLongText("conversationLinkWithoutConversation"),
                link, true);
    }

    @Test
    public void testConversationLinkBetweenTwoConversations() {
        Conversation conv1 = createConversation(),
                     conv2 = createConversation();
        makeSendTask(getTask1InA());
        makeReceiveTask(getTask1InB());
        addConversationLink(conv1, getTask1InA());
        addConversationLink(conv1, getTask1InB());
        addConversationLink(conv2, getTask1InA());
        addConversationLink(conv2, getTask1InB());

        ConversationLink badLink = addConversationLink(conv1, conv2);
        assertOneError(texts.getLongText("conversationLinkWithTwoConversations"),
                badLink, true);
    }

    @Test
    public void testConversationOnlyLinkedToSendTasks() {
        Conversation conversation = createConversation();
        makeSendTask(getTask1InA());
        makeSendTask(getTask1InB());
        addConversationLink(conversation, getTask1InA());
        addConversationLink(conversation, getTask1InB());
        assertOneWarning(texts.getLongText("conversationOnlyConnectedToSenders"),
                conversation, true);
    }

    @Test
    public void testConversationOnlyLinkedToReceiveTasks() {
        Conversation conversation = createConversation();
        makeReceiveTask(getTask1InA());
        makeReceiveTask(getTask1InB());
        addConversationLink(conversation, getTask1InA());
        addConversationLink(conversation, getTask1InB());
        assertOneWarning(texts.getLongText("conversationOnlyConnectedToReceivers"),
                conversation, true);
    }
}
