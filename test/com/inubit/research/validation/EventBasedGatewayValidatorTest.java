package com.inubit.research.validation;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SignalIntermediateEvent;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class EventBasedGatewayValidatorTest extends BPMNValidationTestCommons {

    private EventBasedGateway gateway;
    private ExclusiveGateway join;
    private TimerIntermediateEvent timer;
    private Task receiveTask;
    private MessageIntermediateEvent messageEvent;
    private SequenceFlow flowToTimer, flowToTask, flowToMessageEvent;

    private void setUpForEBGWTest(boolean createTimer, boolean createReceiveTask,
            boolean createMessageEvent, boolean connectFromStartEvent) {
        gateway = new EventBasedGateway();
        addToModelAndPoolA(gateway);
        join = new ExclusiveGateway();
        join.setText("joinAfterEBGW");
        addToModelAndPoolA(join);
        addSequenceFlow(join, getEndInA());
        getTask1InB(true, false);
        getTask2InB(false, true);
        addSequenceFlow(getTask1InB(), getTask2InB());
        if (connectFromStartEvent) addSequenceFlow(getStartInA(), gateway);
        if (createTimer) {
            timer = new TimerIntermediateEvent();
            timer.setText("timer");
            addToModelAndPoolA(timer);
            flowToTimer = addSequenceFlow(gateway, timer);
            addSequenceFlow(timer, join);
        }
        if(createReceiveTask) {
            receiveTask = new Task();
            receiveTask.setText("receive");
            makeReceiveTask(receiveTask);
            addToModelAndPoolA(receiveTask);
            makeSendTask(getTask1InB());
            addMessageFlow(getTask1InB(),receiveTask);
            flowToTask = addSequenceFlow(gateway, receiveTask);
            addSequenceFlow(receiveTask, join);
        }
        if(createMessageEvent) {
            messageEvent = new MessageIntermediateEvent();
            messageEvent.setText("event");
            addToModelAndPoolA(messageEvent);
            makeSendTask(getTask2InB());
            addMessageFlow(getTask2InB(),messageEvent);
            flowToMessageEvent = addSequenceFlow(gateway, messageEvent);
            addSequenceFlow(messageEvent, join);
        }
    }

    private void addToModelAndPoolA(ProcessNode node) {
        model.addNode(node);
        getPoolA().addProcessNode(node);
    }

    @Override
    protected Task getTask1InA(boolean fromStart, boolean toEnd) {
        Task task = super.getTask1InA(fromStart, false);
        if (toEnd && model.getOutgoingEdges(SequenceFlow.class, task).isEmpty()) {
            addSequenceFlow(task, join);
        }
        return task;
    }

    @Override
    protected Task getTask2InA(boolean fromStart, boolean toEnd) {
        Task task = super.getTask2InA(fromStart, false);
        if (toEnd && model.getOutgoingEdges(SequenceFlow.class, task).isEmpty()) {
            addSequenceFlow(task, join);
        }
        return task;
    }

    @Test
    public void testNonReceiveTaskFollowingEventBasedGateway() {
        setUpForEBGWTest(true, false, false, true);
        flowToTask = addSequenceFlow(gateway, getTask1InA(false, true));
        assertOneError(texts.getLongText("illegalNodeAfterEventBasedGateway"),
                gateway, listOf(flowToTask, getTask1InA()), true);
    }

    @Test
    public void testReceiveTaskFollowingEventBasedGateway() {
        setUpForEBGWTest(true, true, false, true);
        assertNoMessages(true);
    }

    @Test
    public void testStartEventFollowingEventBasedGateway() {
        setUpForEBGWTest(false, true, false, false);
        TimerStartEvent start = createTimerStartEvent();
        start.setText("timerStart with incoming flow");
        getPoolA().addProcessNode(start);
        SequenceFlow flowToStart = addSequenceFlow(gateway, start);
        addSequenceFlow(start, getTask1InA(false, true));
        addSequenceFlow(join, gateway);
        assertOneError(texts.getLongText("illegalNodeAfterEventBasedGateway"),
                gateway, listOf(flowToStart, start), true);
    }

    @Test
    public void testMessageEventAndReceiveTaskFollowEventBasedGateway() {
        setUpForEBGWTest(false, true, true, true);
        assertOneWarning(
                texts.getLongText("receiveTaskAndMessageEventAfterEBGW"),
                gateway, listOf(messageEvent, receiveTask), true);
    }

    @Test
    public void testNodesFollowingEBGWHavingOtherIncommingSequenceFlow() {
        setUpForEBGWTest(true, true, false, true);
        SequenceFlow badFlow = addSequenceFlow(getTask2InA(false, false),timer);
        addSequenceFlow(join, getTask2InA());
        assertOneError(
                texts.getLongText("nodeAfterEBGWWithOtherIncommingSequenceFlow"),
                timer, listOf(badFlow, gateway, flowToTimer), true);
    }

    @Test
    public void testTaskWithAttachedEventFollowingEventBasedGateway() {
        setUpForEBGWTest(true, true, false, true);
        TimerIntermediateEvent attachedTimer = new TimerIntermediateEvent();
        attachedTimer.setText("too long");
        model.addNode(attachedTimer);
        getPoolA().addProcessNode(attachedTimer);
        attachedTimer.setParentNode(receiveTask);
        addSequenceFlow(attachedTimer, join);
        layoutModel();//must layout twice
        assertOneError(texts.getLongText("taskFollowingEBGWHasAttachedEvent"),
                receiveTask, listOf(attachedTimer), true);
    }

    @Test
    public void testEventBasedGatewayWithOutgoingConditionalFlow() {
        setUpForEBGWTest(true, true, false, true);
        flowToTimer.setProperty(SequenceFlow.PROP_SEQUENCETYPE,
                SequenceFlow.TYPE_CONDITIONAL);
        flowToTimer.setLabel("condition");
        assertOneError(texts.getLongText("conditionalFlowFromEBGW"), flowToTimer, true);
    }

    @Test
    public void testEventBasedGatewayWithOnlyOneOutgoingEdge() {
        setUpForEBGWTest(false, true, false, true);
        model.removeNode(join);
        addSequenceFlow(receiveTask, getEndInA());
        assertOneError(texts.getLongText("onlyOneSequenceFlowFromEBGW"),
                gateway, listOf(flowToTask), true);
    }

    @Test
    public void testThrowingIntermediateEventFollowingEBGW() {
        setUpForEBGWTest(false, true, false, true);
        SignalIntermediateEvent signal = new SignalIntermediateEvent();
        signal.setText("signal");
        signal.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_THROWING);
        addToModelAndPoolA(signal);
        SequenceFlow flowToSignal = addSequenceFlow(gateway, signal);
        addSequenceFlow(signal, join);
        assertOneError(texts.getLongText("illegalNodeAfterEventBasedGateway"),
                gateway, listOf(signal, flowToSignal), true);
    }

}
