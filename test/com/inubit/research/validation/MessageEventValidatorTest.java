package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageEndEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.Pool;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class MessageEventValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testMultipleMessageFlowsToMessageIntermediateEvent() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        event.setText("receive");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        MessageFlow flow1 = addMessageFlow(getTask1InB(true, false),event);
        MessageFlow flow2 = addMessageFlow(getTask2InB(false, true),event);
        makeSendTask(getTask1InB());
        makeSendTask(getTask2InB());
        addSequenceFlow(getTask1InB(), getTask2InB());
        assertOneWarning(
                texts.getLongText("multipleIncomingMessageFlowsToIntermediateEvent"),
                event, listOf(flow1, flow2), true);
    }

    @Test
    public void testMultipleMessageFlowsToMessageStartEvent() {
        MessageStartEvent start = new MessageStartEvent();
        start.setText("receive");
        model.addNode(start);
        getPoolA().addProcessNode(start);
        addSequenceFlow(start, getTask1InA(false, true));
        MessageFlow flow1 = addMessageFlow(getTask1InB(true, false),start);
        MessageFlow flow2 = addMessageFlow(getTask2InB(false, true),start);
        makeSendTask(getTask1InB());
        makeSendTask(getTask2InB());
        addSequenceFlow(getTask1InB(), getTask2InB());
        assertOneWarning(
                texts.getLongText("multipleIncomingMessageFlowsToMessageStartEvent"),
                start, listOf(flow1, flow2), true);
    }

    @Test
    public void testMultipleMessageFlowsFromMessageIntermediateEvent() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        event.setText("receive");
        event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_THROWING);
        model.addNode(event);
        getPoolA().addProcessNode(event);
        MessageFlow flow1 = addMessageFlow(event, getTask1InB(true, false));
        MessageFlow flow2 = addMessageFlow(event, getTask2InB(false, true));
        makeReceiveTask(getTask1InB());
        makeReceiveTask(getTask2InB());
        addSequenceFlow(getTask1InB(), getTask2InB());
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        assertOneWarning(
                texts.getLongText("multipleOutgoingMessageFlowsFromIntermediateEvent"),
                event, listOf(flow1, flow2), true);
    }

    @Test
    public void testMultipleMessageFlowsFromMessageEndEvent() {
        MessageEndEvent end = new MessageEndEvent();
        end.setText("receive");
        model.addNode(end);
        getPoolA().addProcessNode(end);
        MessageFlow flow1 = addMessageFlow(end, getTask1InB(true, false));
        MessageFlow flow2 = addMessageFlow(end, getTask2InB(false, true));
        addSequenceFlow(getTask1InB(), getTask2InB());
        makeReceiveTask(getTask1InB());
        makeReceiveTask(getTask2InB());
        addSequenceFlow(getTask1InA(true, false),end);
        assertOneWarning(
                texts.getLongText("multipleOutgoingMessageFlowsFromMessageEndEvent"),
                end, listOf(flow1, flow2), true);
    }

    @Test
    public void testMessageIntermediateEventWithoutMessageFlow() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        event.setText("event");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        getPoolB().setProperty(Pool.PROP_BLACKBOX_POOL, Pool.TRUE);
        assertOneWarning(
                texts.getLongText("messageIntermediateCatchEventWithoutMessageFlow"),
                event, true);
    }

    @Test
    public void testMessageStartEventWithoutMessageFlow() {
        MessageStartEvent start = new MessageStartEvent();
        start.setText("start");
        model.addNode(start);
        getPoolA().addProcessNode(start);
        addSequenceFlow(start, getTask1InA(false, true));
        getPoolB().setProperty(Pool.PROP_BLACKBOX_POOL, Pool.TRUE);
        assertOneWarning(texts.getLongText("messageStartEventWithoutMessageFlow"), start, true);
    }

    @Test
    public void testMessageEndEventWithoutMessageFlow() {
        MessageEndEvent end = new MessageEndEvent();
        end.setText("end");
        model.addNode(end);
        getPoolA().addProcessNode(end);
        addSequenceFlow(getTask1InA(true, false),end);
        getPoolB().setProperty(Pool.PROP_BLACKBOX_POOL, Pool.TRUE);
        assertOneWarning(texts.getLongText("messageEndEventWithoutMessageFlow"), end, true);
    }

    @Test
    public void testMessageIntermediateEventWithoutMessageFlowInSingleParticipantModel() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        event.setText("my message");
        model.addNode(event);
        addSequenceFlow(getNonPoolTask1(true, false), event);
        addSequenceFlow(event, getNonPoolTask2(false, true));
        assertNoMessages(true);
    }
}
