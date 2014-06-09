package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.ParallelGateway;
import com.inubit.research.validation.bpmn.BPMNValidator;
import java.util.List;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.LinkIntermediateEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tmi
 */
public class SequenceFlowValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testSequenceFlowCrossingPool() {
        SequenceFlow flow = addSequenceFlow(gatewayAfter(getTask1InA()),
                gatewayAfter(getTask1InB()));
        assertOneError(texts.getLongText("sequenceFlowCrossingPool"), flow, true);
    }

    @Test
    public void testSequenceFlowCrossingSubProcess() {
        SubProcess subProcess = new SubProcess();
        subProcess.setText("subProcess");
        model.addNode(subProcess);
        subProcess.addProcessNode(getNonPoolTask1(false, false));
        SequenceFlow flow = addSequenceFlow(
                getNonPoolTask1(), getNonPoolTask2(false, true));
        addSequenceFlow(getGlobalStart(), subProcess);
        addSequenceFlow(subProcess, gatewayAfter(getNonPoolTask2()));

        //manual layouting (layouter complains about the flow, that crosses the
        //SubProcess-border
        getGlobalStart().setPos(0,50);
        subProcess.setPos(200, 50);
        getNonPoolTask1().setPos(210, 60);
        getNonPoolTask2().setPos(1000, 50);
        getGlobalEnd().setPos(1300, 50);
        assertOneError(texts.getLongText("sequenceFlowCrossingSubProcess"), flow, false);
    }

    @Test
    public void testSequenceFlowFromPool() {
        SequenceFlow flow = addSequenceFlow(getPoolA(),
                gatewayAfter(getNonPoolTask1()));
        assertOneError(texts.getLongText("illegalSequenceFlowSource"), flow, true);
    }

    @Test
    public void testSequenceFlowToPool() {
        SequenceFlow flow = addSequenceFlow(gatewayAfter(getNonPoolTask1()), getPoolA());
        assertOneError(texts.getLongText("illegalSequenceFlowTarget"), flow, true);
    }

    @Test
    public void testSequenceFlowToInstantiatingGateway() {
        EventBasedGateway gateway = new EventBasedGateway();
        gateway.setProperty(EventBasedGateway.PROP_INSTANTIATE,
                EventBasedGateway.TYPE_INSTANTIATE_EXCLUSIVE);
        model.addNode(gateway);
        TimerIntermediateEvent timer1 = new TimerIntermediateEvent();
        TimerIntermediateEvent timer2 = new TimerIntermediateEvent();
        timer1.setText("Timer1");
        timer2.setText("Timer2");
        model.addNode(timer1);
        model.addNode(timer2);
        addSequenceFlow(gateway, timer1);
        addSequenceFlow(gateway, timer2);
        SequenceFlow bad = addSequenceFlow(getNonPoolTask1(false, false),gateway);
        addSequenceFlow(timer1, getGlobalEnd());
        addSequenceFlow(timer2, getNonPoolTask1());
        assertOneError(texts.getLongText("illegalSequenceFlowTarget"), bad, true);
    }

    @Test
    public void testSequenceFlowToStartEvent() {
        SequenceFlow flow = addSequenceFlow(
                gatewayAfter(getTask1InA()), getStartInA());
        //addSequenceFlow(start, getTask2InA(false, true));
        assertOneError(texts.getLongText("illegalSequenceFlowTarget"), flow, true);
    }

    @Test
    public void testSequenceFlowFromEndEvent() {
        EndEvent end = createEndEvent();
        getPoolA().addProcessNode(end);
        addSequenceFlow(getTask1InA(true, false), end);
        SequenceFlow flow = addSequenceFlow(end, getTask2InA(false, true));
        assertOneError(texts.getLongText("illegalSequenceFlowSource"), flow, true);
    }

    @Test
    public void testSequenceFlowFromEventSubProcess() {
        SubProcess sub = new SubProcess();
        sub.setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        sub.setText("subProcess");
        model.addNode(sub);
        TimerStartEvent start = createTimerStartEvent();
        sub.addProcessNode(start);
        start.setText("myTimerStartInEventSubProcess");
        sub.addProcessNode(getNonPoolTask2(false, false));
        addSequenceFlow(start, getNonPoolTask2());
        EndEvent end = createEndEvent();
        end.setText("myEnd");
        sub.addProcessNode(end);
        addSequenceFlow(getNonPoolTask2(), end);

        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        SequenceFlow flow = addSequenceFlow(sub, gateway);
        addSequenceFlow(getGlobalStart(), gateway);
        addSequenceFlow(gateway, getNonPoolTask1(false, true));
        assertOneError(texts.getLongText("illegalSequenceFlowSource"), flow, true);
    }

    @Test
    public void testUnlabeledConditionalFlow() {
        Task task3InA = new Task();
        task3InA.setText("task3InA");
        model.addNode(task3InA);
        getPoolA().addProcessNode(task3InA);
        addSequenceFlow(getTask1InA(true, false), getTask2InA(false, false));
        SequenceFlow conditionalFlow = addSequenceFlow(getTask1InA(),task3InA);
        conditionalFlow.setProperty(SequenceFlow.PROP_SEQUENCETYPE,
                SequenceFlow.TYPE_CONDITIONAL);
        conditionalFlow.setLabel("");
        Gateway gateway = new ParallelGateway();
        gateway.setText("gw");
        model.addNode(gateway);
        getPoolA().addProcessNode(gateway);
        addSequenceFlow(getTask2InA(), gateway);
        addSequenceFlow(task3InA, gateway);
        addSequenceFlow(gateway, getEndInA());
        layoutModel();
        List<ValidationMessage> messages = new BPMNValidator().doValidation(model);
        assertEquals(2, messages.size());
        ValidationMessage message;
        if (messages.get(0).getShortDescription().
                equals(texts.getShortText("unlabeledConditionalFlow"))) {
            message = messages.get(0);
        } else {
            message = messages.get(1);
        }
        assertEquals(texts.getLongText("unlabeledConditionalFlow"), message.getDescription());
        assertEquals(ValidationMessage.TYPE_INFO, message.getType());
        assertEquals(conditionalFlow, message.getPrimaryObject());
        assertTrue(message.getInvolvedObjects().isEmpty());
    }

    @Test
    public void testUnlabeledFlowFromDecisionGateway() {
        Gateway gateway = new Gateway();
        model.addNode(gateway);
        getPoolA().addProcessNode(gateway);
        addSequenceFlow(gateway, getTask1InA(false, true));
        SequenceFlow unlabeledFlow = addSequenceFlow(gateway, getTask2InA(false, false));
        unlabeledFlow.setLabel("");
        addSequenceFlow(getTask2InA(), gateway);
        addSequenceFlow(getStartInA(), gateway);
        assertOneInfo(texts.getLongText("unlabeledFlowFromDecisionGateway"), unlabeledFlow, true);
    }

    @Test
    public void testUnlabeledFlowFromExclusiveJoin() {
        Gateway gateway1 = new Gateway();
        model.addNode(gateway1);
        getPoolA().addProcessNode(gateway1);
        addSequenceFlow(getTask1InA(false, false),gateway1);
        addSequenceFlow(getTask2InA(false, false),gateway1);
        Task task3InA = new Task();
        task3InA.setText("task3InA");
        model.addNode(task3InA);
        getPoolA().addProcessNode(task3InA);
        addSequenceFlow(gateway1, task3InA);
        ExclusiveGateway gateway2 = new ExclusiveGateway();
        model.addNode(gateway2);
        getPoolA().addProcessNode(gateway2);
        addSequenceFlow(getStartInA(), gateway2);
        addSequenceFlow(gateway2, getTask1InA());
        addSequenceFlow(gateway2, getTask2InA());
        addSequenceFlow(task3InA, getEndInA());
        assertNoMessages(true);
    }

    @Test
    public void testUnlabeledDefaultFlowFromDecisionGateway() {
        Gateway gateway = new Gateway();
        model.addNode(gateway);
        getPoolA().addProcessNode(gateway);
        addSequenceFlow(gateway, getTask1InA(false, true));
        SequenceFlow defaultFlow = addSequenceFlow(gateway, getTask2InA(false, false));
        defaultFlow.setProperty(SequenceFlow.PROP_SEQUENCETYPE,
                SequenceFlow.TYPE_DEFAULT);
        addSequenceFlow(getTask2InA(), gateway);
        addSequenceFlow(getStartInA(), gateway);
        assertNoMessages(true);
    }

    @Test
    public void testFlowToAttachedEvent() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("time");
        model.addNode(timer);
        timer.setParentNode(getNonPoolTask2());
        getNonPoolTask2().setPos(50, 50);//layouting has to be done manually here,
        //because the layouter would remove the SequenceFlow to the attached Event
        timer.setPos(50 - timer.getBounds().width / 2,
                50 - timer.getBounds().height / 2);
        getNonPoolTask1(false, false).setPos(100, 100);
        getGlobalStart().setPos(0,0);
        getGlobalEnd().setPos(100, 0);
        addSequenceFlow(timer, getNonPoolTask1(false, false));
        SequenceFlow badFlow = addSequenceFlow(getNonPoolTask1(), timer);
        assertOneError(texts.getLongText("sequenceFlowToAttachedEvent"), badFlow, false);
    }

    @Test
    public void testFlowToCatchingLinkIntermediateEvent() {
        LinkIntermediateEvent throwingLink = new LinkIntermediateEvent(),
                              catchingLink = new LinkIntermediateEvent();
        model.addNode(throwingLink);
        model.addNode(catchingLink);
        throwingLink.setText("link0");
        throwingLink.setProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE,
                LinkIntermediateEvent.EVENT_SUBTYPE_THROWING);
        catchingLink.setText("link0");
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        addSequenceFlow(getNonPoolTask1(true, false), gateway);
        addSequenceFlow(gateway, throwingLink);
        addSequenceFlow(catchingLink, getNonPoolTask2(false, true));
        SequenceFlow badFlow = addSequenceFlow(gateway, catchingLink);
        assertOneError(texts.getLongText("illegalSequenceFlowTarget"), badFlow, true);
    }

    @Test
    public void testFlowFromThrowingLinkIntermediateEvent() {
        LinkIntermediateEvent throwingLink = new LinkIntermediateEvent(),
                              catchingLink = new LinkIntermediateEvent();
        model.addNode(throwingLink);
        model.addNode(catchingLink);
        throwingLink.setText("link0");
        throwingLink.setProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE,
                LinkIntermediateEvent.EVENT_SUBTYPE_THROWING);
        catchingLink.setText("link0");
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        addSequenceFlow(getNonPoolTask1(true, false), throwingLink);
        addSequenceFlow(catchingLink, gateway);
        addSequenceFlow(gateway, getNonPoolTask2(false, true));
        SequenceFlow badFlow = addSequenceFlow(throwingLink, gateway);
        assertOneError(texts.getLongText("illegalSequenceFlowSource"), badFlow, true);
    }

    @Test
    public void testValidLinkUsage() {
        LinkIntermediateEvent throwingLink = new LinkIntermediateEvent(),
                              catchingLink = new LinkIntermediateEvent();
        model.addNode(throwingLink);
        model.addNode(catchingLink);
        throwingLink.setText("link0");
        throwingLink.setProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE,
                LinkIntermediateEvent.EVENT_SUBTYPE_THROWING);
        catchingLink.setText("link0");
        addSequenceFlow(getNonPoolTask1(true, false), throwingLink);
        addSequenceFlow(catchingLink, getNonPoolTask2(false, true));
        assertNoMessages(true);
    }

    @Test
    public void testFlowFromCompensationTask() {
        CompensationIntermediateEvent event = new CompensationIntermediateEvent();
        model.addNode(event);
        event.setParentNode(getNonPoolTask1());
        model.addEdge(new Association(event,
                getNonPoolTask2(false, false)));
        getNonPoolTask2().setProperty(Task.PROP_COMPENSATION, Task.TRUE);
        SequenceFlow badFlow = addSequenceFlow(getNonPoolTask2(),
                gatewayAfter(getNonPoolTask1()));
        assertOneError(texts.getLongText("illegalSequenceFlowSource"), badFlow, true);
    }
}
