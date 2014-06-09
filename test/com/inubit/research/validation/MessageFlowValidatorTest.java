package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MultipleEndEvent;
import net.frapu.code.visualization.bpmn.MultipleIntermediateEvent;
import net.frapu.code.visualization.bpmn.MultipleStartEvent;
import net.frapu.code.visualization.bpmn.ParallelMultipleIntermediateEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class MessageFlowValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testMessageFlowInPool() {
        MessageFlow flow = new MessageFlow(
                getTask1InA(true, false), getTask2InA(false, true));
        makeSendTask(getTask1InA());
        makeReceiveTask(getTask2InA());
        model.addEdge(flow);
        addSequenceFlow(getTask1InA(), getTask2InA());
        assertOneError(texts.getLongText("messageFlowInPool"), flow, true);
    }

    @Test
    public void testCorrectMessageFlow() {
        MessageFlow flow = new MessageFlow(getTask1InA(), getPoolB());
        makeSendTask(getTask1InA());
        model.addEdge(flow);
        assertNoMessages(true);
    }

    @Test
    public void testMessageFlowToDataStore() {
        DataStore store = new DataStore();
        store.setText("myStore");
        model.addNode(store);
        getPoolB().addProcessNode(store);
        MessageFlow flow = new MessageFlow(getTask1InA(),store);
        makeSendTask(getTask1InA());
        model.addEdge(flow);
        layoutModel();
        store.setPos(getPoolB().getPos().x + 10, getPoolB().getPos().y + 10);
        assertOneError(texts.getLongText("illegalMessageFlowTarget"), flow, false);
    }

    @Test
    public void testMessageFlowFromDataStore() {
        DataStore store = new DataStore();
        store.setText("myStore");
        model.addNode(store);
        getPoolB().addProcessNode(store);
        MessageFlow flow = new MessageFlow(store, getTask1InA());
        makeReceiveTask(getTask1InA());
        model.addEdge(flow);
        layoutModel();
        store.setPos(getPoolB().getPos().x + 10, getPoolB().getPos().y + 10);
        assertOneError(texts.getLongText("illegalMessageFlowSource"), flow, false);
    }

    @Test
    public void testMessageFlowToMultipleStartEvent() {
        MultipleStartEvent start = new MultipleStartEvent();
        start.setText("multiple");
        model.addNode(start);
        getPoolA().addProcessNode(start);
        addSequenceFlow(start, getTask1InA(false, false));
        makeSendTask(getTask1InB());
        addMessageFlow(getTask1InB(),start);
        EndEvent end = createEndEvent();
        getPoolA().addProcessNode(end);
        addSequenceFlow(getTask1InA(),end);
        assertNoMessages(true);
    }

    @Test
    public void testMessageFlowFromMultipleEndEvent() {
        MultipleEndEvent end = new MultipleEndEvent();
        end.setText("multiple");
        model.addNode(end);
        getPoolA().addProcessNode(end);
        makeReceiveTask(getTask1InB());
        addMessageFlow(end, getTask1InB());
        addSequenceFlow(getTask1InA(true, false), end);
        assertNoMessages(true);
    }

    @Test
    public void testMessageFlowFromThrowingMultipleIntermediateEvent() {
        MultipleIntermediateEvent event = new MultipleIntermediateEvent();
        event.setText("multiple");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_THROWING);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        makeReceiveTask(getTask1InB());
        addMessageFlow(event, getTask1InB());
        assertNoMessages(true);
    }

    @Test
    public void testMessageFlowToThrowingMultipleIntermediateEvent() {
        MultipleIntermediateEvent event = new MultipleIntermediateEvent();
        event.setText("multiple");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_THROWING);
        makeSendTask(getTask1InB());
        MessageFlow flow = addMessageFlow(getTask1InB(),event);
        assertOneError(texts.getLongText("illegalMessageFlowTarget"), flow, true);
    }

    @Test
    public void testMessageFlowToCatchingMultipleIntermediateEvent() {
        MultipleIntermediateEvent event = new MultipleIntermediateEvent();
        event.setText("multiple");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        makeSendTask(getTask1InB());
        addMessageFlow(getTask1InB(),event);
        assertNoMessages(true);
    }

    @Test
    public void testMessageFlowToCatchingParallelMultipleIntermediateEvent() {
        ParallelMultipleIntermediateEvent event =
                new ParallelMultipleIntermediateEvent();
        event.setText("multiple");
        model.addNode(event);
        getPoolA().addProcessNode(event);
        addSequenceFlow(getStartInA(), event);
        addSequenceFlow(event, getEndInA());
        makeSendTask(getTask1InB());
        addMessageFlow(getTask1InB(),event);
        assertNoMessages(true);
    }

}
