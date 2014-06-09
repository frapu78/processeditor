package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class TaskValidatorTest extends BPMNValidationTestCommons {

    private void setUpForTaskTypeTests() {
        makeSendTask(getTask1InB(true, false));
        makeReceiveTask(getTask2InB(false, true));
        addSequenceFlow(getTask1InB(), getTask2InB());
        //add some MessageFlow, so that these both tasks do not do any harm,
        //if the are not needed
        //add some MessageFlow, so that these both tasks do not do any harm,
        //if the are not needed
        //add some MessageFlow, so that these both tasks do not do any harm,
        //if the are not needed
        Pool poolC = new Pool();
        poolC.setText("poolC");
        model.addNode(poolC);
        Task receiver = new Task();
        Task sender = new Task();
        receiver.setText("receiver");
        sender.setText("sender");
        makeSendTask(sender);
        makeReceiveTask(receiver);
        model.addNode(sender);
        model.addNode(receiver);
        poolC.addProcessNode(sender);
        poolC.addProcessNode(receiver);
        addMessageFlow(getTask1InB(),receiver);
        addMessageFlow(sender, getTask2InB());
        StartEvent startInC = createTimerStartEvent();
        EndEvent endInC = createEndEvent();
        poolC.addProcessNode(startInC);
        poolC.addProcessNode(endInC);
        addSequenceFlow(startInC, receiver);
        addSequenceFlow(receiver, sender);
        addSequenceFlow(sender, endInC);
    }

    @Test
    public void testCorrectSendReceiveCombinations() {
        setUpForTaskTypeTests();
        assertNoMessages(true); //setup does enough
        //setup does enough
        //setup does enough
    }

    @Test
    public void testAbstractTaskWithIncommingMessageFlow() {
        setUpForTaskTypeTests();
        MessageFlow message = addMessageFlow(getTask1InB(), getTask1InA());
        assertOneWarning(texts.getLongText("abstractTaskWithIncommingMessageFlow"),
                getTask1InA(), listOf(message), true);
    }

    @Test
    public void testReceiveTaskWithoutMessageFlow() {
        setUpForTaskTypeTests();
        makeReceiveTask(getTask1InA());
        assertOneWarning(texts.getLongText("receiveTaskWithoutMessageFlow"), getTask1InA(), true);
    }

    @Test
    public void testReceiveTaskWithOutgoingMessageFlow() {
        setUpForTaskTypeTests();
        makeReceiveTask(getTask1InA());
        MessageFlow flow = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("receiveTaskWithOutgoingMessageFlow"),
                getTask1InA(), listOf(flow), true);
    }

    @Test
    public void testReceiveTaskWithIncommingAndOutgoingMessageFlowWithOnePartner() {
        setUpForTaskTypeTests();
        makeReceiveTask(getTask1InA());
        MessageFlow incomming = addMessageFlow(getTask1InB(), getTask1InA());
        MessageFlow outgoing = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("receiveTaskWithIncommingAndOutgoingMessageFlow"),
                getTask1InA(), listOf(incomming, outgoing), true);
    }

    @Test
    public void testAbstractTaskWithOutgoingMessageFlow() {
        setUpForTaskTypeTests();
        MessageFlow outgoing = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("abstractTaskWithOutgoingMessageFlow"),
                getTask1InA(), listOf(outgoing), true);
    }

    @Test
    public void testSendTaskWithoutMessageFlow() {
        setUpForTaskTypeTests();
        makeSendTask(getTask1InA());
        assertOneWarning(texts.getLongText("sendTaskWithoutMessageFlow"), getTask1InA(), true);
    }

    @Test
    public void testSendTaskWithIncommingMessageFlow() {
        setUpForTaskTypeTests();
        MessageFlow message = addMessageFlow(getTask1InB(), getTask1InA());
        makeSendTask(getTask1InA());
        assertOneWarning(texts.getLongText("sendTaskWithIncommingMessageFlow"),
                getTask1InA(), listOf(message), true);
    }

    @Test
    public void testSendTaskWithIncommingAndOutgoingMessageFlowWithOnePartner() {
        setUpForTaskTypeTests();
        makeSendTask(getTask1InA());
        MessageFlow in = addMessageFlow(getTask1InB(), getTask1InA());
        MessageFlow out = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("sendTaskWithIncommingAndOutgoingMessageFlow"),
                getTask1InA(), listOf(in, out), true);
    }

    @Test
    public void testAbstractTaskWithIncommingAndOutgoingMessageFlowWithOnePartner() {
        setUpForTaskTypeTests();
        MessageFlow in = addMessageFlow(getTask1InB(), getTask1InA());
        MessageFlow out = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("abstractTaskWithIncommingAndOutgoingMessageFlow"),
                getTask1InA(), listOf(in, out), true);
    }

    @Test
    public void testServiceTaskWithIncommingAndOutgoingMessageFlowWithOnePartner() {
        setUpForTaskTypeTests();
        makeServiceTask(getTask1InA());
        addMessageFlow(getTask1InB(), getTask1InA());
        addMessageFlow(getTask1InA(), getTask2InB());
        assertNoMessages(true);
    }

    @Test
    public void testServiceTaskWithIncommingMessageFlow() {
        setUpForTaskTypeTests();
        makeServiceTask(getTask1InA());
        MessageFlow in = addMessageFlow(getTask1InB(), getTask1InA());
        assertOneWarning(texts.getLongText("serviceTaskWithOnlyIncommingMessageFlow"),
                getTask1InA(), listOf(in), true);
    }

    @Test
    public void testServiceTaskWithOutgoingMessageFlow() {
        setUpForTaskTypeTests();
        makeServiceTask(getTask1InA());
        MessageFlow out = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("serviceTaskWithOnlyOutgoingMessageFlow"),
                getTask1InA(), listOf(out), true);
    }

    @Test
    public void testTaskWithIncommingAndOutgoingMessageFlowWithMultiplePartners() {
        setUpForTaskTypeTests();
        Pool poolD = new Pool();
        poolD.setText("PoolD");
        model.addNode(poolD);
        Task taskInD = new Task();
        taskInD.setText("TaskInD");
        model.addNode(taskInD);
        poolD.addProcessNode(taskInD);
        makeSendTask(taskInD);
        StartEvent startInD = createTimerStartEvent();
        EndEvent endInD = createEndEvent();
        poolD.addProcessNode(startInD);
        poolD.addProcessNode(endInD);
        addSequenceFlow(startInD, taskInD);
        addSequenceFlow(taskInD, endInD);
        MessageFlow in = addMessageFlow(taskInD, getTask1InA());
        MessageFlow out = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(texts.getLongText("taskWithInAndOutMessageFlowWithMultiplePartners"),
                getTask1InA(), listOf(in, out), true);
    }

    @Test
    public void testSendTaskWithoutMessageFlowInNonColaborationDiagram() {
        getNonPoolTask1().setProperty(Task.PROP_STEREOTYPE, Task.TYPE_SEND);
        assertNoMessages(true);
    }

    @Test
    public void testSendTasWithIncommingMessageFlowAndConversationLink() {
        setUpForTaskTypeTests();
        makeSendTask(getTask1InA());
        Conversation conversation = new Conversation();
        model.addNode(conversation);
        ConversationLink link = new ConversationLink(conversation, getTask1InA());
        model.addEdge(link);
        model.addEdge(new ConversationLink(conversation, getTask2InB()));
        MessageFlow incommingMsg = addMessageFlow(getTask1InB(), getTask1InA());
        assertOneWarning(
                texts.getLongText("sendTaskWithIncommingMessageFlowAndConversationLink"),
                getTask1InA(), listOf(link, incommingMsg), true);
    }

    @Test
    public void testReceiveTaskWithOutgoingMessageFlowAndConversationLink() {
        setUpForTaskTypeTests();
        makeReceiveTask(getTask1InA());
        Conversation conversation = new Conversation();
        model.addNode(conversation);
        ConversationLink link = new ConversationLink(conversation, getTask1InA());
        model.addEdge(link);
        model.addEdge(new ConversationLink(conversation, getTask1InB()));
        MessageFlow outgoingMsg = addMessageFlow(getTask1InA(), getTask2InB());
        assertOneWarning(
                texts.getLongText("receiveTaskWithOutgoingMessageFlowAndConversationLink"),
                getTask1InA(), listOf(link, outgoingMsg), true);
    }

    @Test
    public void testAbstractTaskWithConversationLink() {
        setUpForTaskTypeTests();
        Conversation conversation = new Conversation();
        model.addNode(conversation);
        ConversationLink link = new ConversationLink(conversation,
                getTask1InA(true, false));
        model.addEdge(link);
        model.addEdge(new ConversationLink(conversation, getTask1InB()));
        model.addEdge(new ConversationLink(conversation, getTask2InA(false, true)));
        makeReceiveTask(getTask2InA());
        addSequenceFlow(getTask1InA(), getTask2InA());
        assertOneWarning(texts.getLongText("abstractTaskWithConversationLink"),
                getTask1InA(), listOf(link), true);
    }
}
