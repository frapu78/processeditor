package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.CompensationStartEvent;
import net.frapu.code.visualization.bpmn.ErrorIntermediateEvent;
import net.frapu.code.visualization.bpmn.ErrorStartEvent;
import net.frapu.code.visualization.bpmn.EscalationIntermediateEvent;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class EventValidatorTest extends BPMNValidationTestCommons {



    @Test
    public void testStartEventWithoutOutgoingSequenceFlow() {
        StartEvent start = createNoneStartEvent();
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        addSequenceFlow(getNonPoolTask1(false, false), gateway);
        addSequenceFlow(gateway, getNonPoolTask1());
        addSequenceFlow(gateway, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("startEventWithoutOutgoingSequenceFlow"), start, true);
    }

    @Test
    public void testEndEventWithoutIncommingSequenceFlow() {
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        addSequenceFlow(getGlobalStart(), gateway);
        addSequenceFlow(gateway, getNonPoolTask1(false, false));
        addSequenceFlow(getNonPoolTask1(), gateway);
        getGlobalEnd();
        assertOneError(texts.getLongText("endEventWithoutSequenceFlow"), getGlobalEnd(), true);
    }

    @Test
    public void testEventInNormalSequenceFlowNonInterrupting() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        timer.setProperty(TimerIntermediateEvent.PROP_NON_INTERRUPTING,
                TimerIntermediateEvent.EVENT_NON_INTERRUPTING_TRUE);
        model.addNode(timer);
        addSequenceFlow(getNonPoolTask1(true, false), timer);
        addSequenceFlow(timer, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("nonInterruptingInNormalFlow"), timer, true);
    }

    @Test
    public void testAttachedThrowingIntermediateEvent() {
        MessageIntermediateEvent message = new MessageIntermediateEvent();
        message.setText("message");
        model.addNode(message);
        message.setParentNode(getNonPoolTask1());
        message.setProperty(TimerIntermediateEvent.PROP_EVENT_SUBTYPE,
                TimerIntermediateEvent.EVENT_SUBTYPE_THROWING);
        addSequenceFlow(message, gatewayBefore(getGlobalEnd()));
        assertOneError(texts.getLongText("attachedThrowingEvent"), message, true);
    }

    @Test
    public void testAttachedNoneEvent() {
        IntermediateEvent event = new IntermediateEvent();
        event.setText("event");
        model.addNode(event);
        event.setParentNode(getNonPoolTask1());
        addSequenceFlow(event, gatewayBefore(getGlobalEnd()));
        assertOneError(texts.getLongText("unallowedAttachedEvent"), event, true);
    }

    @Test
    public void testCatchingEscalationIntermediateEventInNormalFlow() {
        EscalationIntermediateEvent escalation = new EscalationIntermediateEvent();
        escalation.setText("escalate");
        model.addNode(escalation);
        addSequenceFlow(getNonPoolTask1(true, false), escalation);
        addSequenceFlow(escalation, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("catchingEscalationIntermediateInNormalFlow"),
                escalation, true);
    }

    @Test
    public void testErrorIntermediateEventInNormalFlow() {
        ErrorIntermediateEvent error = new ErrorIntermediateEvent();
        error.setText("error");
        model.addNode(error);
        addSequenceFlow(getNonPoolTask1(true, false), error);
        addSequenceFlow(error, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("wrongErrorIntermediateEvent"), error, true);
    }

    @Test
    public void testNonInterruptingAttachedErrorIntermediateEvent() {
        ErrorIntermediateEvent error = new ErrorIntermediateEvent();
        error.setText("error");
        model.addNode(error);
        error.setProperty(ErrorIntermediateEvent.PROP_NON_INTERRUPTING,
                ErrorIntermediateEvent.EVENT_NON_INTERRUPTING_TRUE);
        error.setParentNode(getNonPoolTask1());
        addSequenceFlow(error, gatewayBefore(getGlobalEnd()));
        assertOneError(texts.getLongText("wrongErrorIntermediateEvent"), error, true);
    }

    @Test
    public void testCatchingCompensationIntermediateEventInNormalFlow() {
        CompensationIntermediateEvent compensation =
                new CompensationIntermediateEvent();
        compensation.setText("compensation");
        model.addNode(compensation);
        addSequenceFlow(getNonPoolTask1(true, false), compensation);
        addSequenceFlow(compensation, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText("illegalCatchingCompensationIntermediateEvent"),
                compensation, true);
    }

    @Test
    public void testNonInterruptingAttachedCompensationIntermediateEvent() {
        CompensationIntermediateEvent compensation =
                new CompensationIntermediateEvent();
        compensation.setText("compensation");
        model.addNode(compensation);
        compensation.setParentNode(getNonPoolTask1());
        Association association = 
                new Association(compensation, getNonPoolTask2(false, false));
        model.addEdge(association);
        getNonPoolTask2().setProperty(Task.PROP_COMPENSATION, Task.TRUE);
        compensation.setProperty(CompensationIntermediateEvent.PROP_NON_INTERRUPTING,
                CompensationIntermediateEvent.EVENT_NON_INTERRUPTING_TRUE);
        assertOneError(texts.getLongText("illegalCatchingCompensationIntermediateEvent"),
                compensation, true);
    }

    @Test
    public void testErrorStartEventAtTopLevel() {
        ErrorStartEvent start = new ErrorStartEvent();
        start.setText("error");
        model.addNode(start);
        addSequenceFlow(start, getNonPoolTask1(false, true));
        assertOneError(texts.getLongText("startEventTypeOnlyInEventSubProcess"), start, true);
    }

    @Test
    public void testNonInterruptingCompensationStartEvent() {
        SubProcess sub = new SubProcess();
        model.addNode(sub);
        sub.setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        CompensationStartEvent compensation = new CompensationStartEvent();
        compensation.setText("compensation");
        model.addNode(compensation);
        sub.addProcessNode(compensation);
        compensation.setProperty(CompensationStartEvent.PROP_NON_INTERRUPTING,
                CompensationStartEvent.EVENT_NON_INTERRUPTING_TRUE);
        addSequenceFlow(compensation, getNonPoolTask1(false, true));
        sub.addProcessNode(getNonPoolTask1());
        sub.addProcessNode(getGlobalEnd());
        assertOneError(texts.getLongText("startEventTypeOnlyInterrupting"), compensation, true);
    }

    @Test
    public void testNonInterruptingTimerStartEventOnRootLevel() {
        getNonPoolTask1(true, true);
        getGlobalStart().setProperty(StartEvent.PROP_NON_INTERRUPTING,
                StartEvent.EVENT_NON_INTERRUPTING_TRUE);
        assertOneError(texts.getLongText("nonInterruptingStartEventOutsideEventSubProcess"),
                getGlobalStart(), true);
    }
}
