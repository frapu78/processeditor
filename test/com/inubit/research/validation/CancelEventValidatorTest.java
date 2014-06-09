package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.CancelEndEvent;
import net.frapu.code.visualization.bpmn.CancelIntermediateEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class CancelEventValidatorTest extends BPMNValidationTestCommons {

    private CancelIntermediateEvent createIntermediateEvent() {
        CancelIntermediateEvent event = new CancelIntermediateEvent();
        event.setText("cancel");
        model.addNode(event);
        return event;
    }

    protected CancelEndEvent createCancelEndEvent() {
        CancelEndEvent event = new CancelEndEvent();
        event.setText("cancel");
        model.addNode(event);
        return event;
    }

    @Test
    public void testCancelEndEventInTopLevelProcess() {
        CancelEndEvent end = createCancelEndEvent();
        addSequenceFlow(getNonPoolTask1(true, false),end);
        assertOneError(texts.getLongText("cancelEndEventOutsideTransaction"),
                end, true);
    }

    @Test
    public void testCancelIntermediateEventInNormalFlow() {
        CancelIntermediateEvent event = createIntermediateEvent();
        addSequenceFlow(getNonPoolTask1(true, false),event);
        addSequenceFlow(event, getNonPoolTask2(false, true));
        assertOneError(texts.getLongText(
                "cancelIntermediateEventNotAttachedToTransaction"),
                event, true);
    }

    @Test
    public void testCancelIntermediateEventAttachedToTask() {
        CancelIntermediateEvent event = createIntermediateEvent();
        event.setParentNode(getNonPoolTask1());
        model.addEdge(new SequenceFlow(event, gatewayBefore(getGlobalEnd())));
        assertOneError(texts.getLongText(
                "cancelIntermediateEventNotAttachedToTransaction"),
                event, true);
    }

    @Test
    public void testCancelEndEventInAndIntermediateEventAttachedToTransaction() {
        SubProcess transaction = new SubProcess();
        transaction.setTransaction();
        transaction.setText("Transaction");
        model.addNode(transaction);
        CancelIntermediateEvent intermediate = createIntermediateEvent();
        intermediate.setParentNode(transaction);

        CancelEndEvent end = createCancelEndEvent();
        transaction.addProcessNode(end);
        transaction.addProcessNode(getNonPoolTask1(false, false));
        model.addEdge(new SequenceFlow(getNonPoolTask1(),end));
        model.addEdge(new SequenceFlow(intermediate, getNonPoolTask2(false, true)));

        StartEvent start = createNoneStartEvent();
        transaction.addProcessNode(start);
        addSequenceFlow(start, getNonPoolTask1());

        addSequenceFlow(getGlobalStart(), transaction);
        addSequenceFlow(transaction, gatewayBefore(getNonPoolTask2()));

        assertNoMessages(true);
    }
}
