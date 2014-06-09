package com.inubit.research.validation;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.ComplexGateway;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class UnlabeledNodesCheckerTest extends BPMNValidationTestCommons {

    @Test
    public void testUnlabeledTask() {
        getTask1InA().setText("");
        assertOneInfo(texts.getLongText("unlabeledActivity"), getTask1InA(), true);
    }

    @Test
    public void testUnlabeledStartEvent() {
        StartEvent start = createNoneStartEvent();
        start.setText("");
        ExclusiveGateway join = new ExclusiveGateway();
        model.addNode(join);
        addSequenceFlow(start, join);
        addSequenceFlow(getGlobalStart(), join);//a second StartEvent is necessary.
        addSequenceFlow(join, getNonPoolTask1(false, true));
        assertOneInfo(texts.getLongText("unlabeledEvent"), start, true);
    }

    @Test
    public void testUnlabeledStartEventWithNoOtherStartEvents() {
        StartEvent start = createNoneStartEvent();
        start.setText("");
        addSequenceFlow(start, getNonPoolTask1(false, true));
        assertNoMessages(true);
    }

    @Test
    public void testUnlabeledEndEventWithNoOtherEndEvents() {
        EndEvent end = createEndEvent();
        end.setText("");
        addSequenceFlow(getNonPoolTask1(true, false), end);
        assertNoMessages(true);
    }

    @Test
    public void testUnlabeledMessageIntermediateEvent() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        model.addNode(event);
        getPoolB().addProcessNode(event);
        event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_CATCHING);
        addSequenceFlow(getStartInB(), event);
        addSequenceFlow(event, getEndInB());
        MessageFlow flow = addMessageFlow(getTask1InA(),event);
        flow.setLabel("");
        makeSendTask(getTask1InA());
        assertOneInfo(texts.getLongText("unlabeledMessageEvent"), event,
                listOf(flow), true);
    }

    @Test
    public void testUnlabeledMessageIntermediateEventWithLabeledMessageFlow() {
        MessageIntermediateEvent event = new MessageIntermediateEvent();
        model.addNode(event);
        getPoolB().addProcessNode(event);
        addSequenceFlow(getStartInB(), event);
        addSequenceFlow(event, getEndInB());
        event.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                IntermediateEvent.EVENT_SUBTYPE_CATCHING);
        MessageFlow flow = new MessageFlow(getTask1InA(),event);
        flow.setLabel("Message");
        model.addEdge(flow);
        getTask1InA().setProperty(Task.PROP_STEREOTYPE, Task.TYPE_SEND);
        List<ProcessObject> related = new LinkedList<ProcessObject>();
        related.add(flow);
        related.add(event);
        assertNoMessages(true);
    }

    @Test
    public void testUnlabeledPool() {
        getPoolA().setText("");
        assertOneInfo(texts.getLongText("unlabeledPool"), getPoolA(), true);
    }

    @Test
    public void testUnlabeledLane() {
        Lane lane = new Lane("", 0, getPoolA());
        model.addNode(lane);
        assertOneInfo(texts.getLongText("unlabeledLane"), lane, true);
    }

    @Test
    public void testUnlabeledComplexGateway() {
        ComplexGateway gateway = new ComplexGateway();
        model.addNode(gateway);
        addSequenceFlow(getGlobalStart(), gateway);
        addSequenceFlow(gateway, getNonPoolTask1(false, false));
        addSequenceFlow(gateway, getNonPoolTask2(false, false));
        InclusiveGateway join = new InclusiveGateway();
        model.addNode(join);
        addSequenceFlow(getNonPoolTask1(), join);
        addSequenceFlow(getNonPoolTask2(), join);
        addSequenceFlow(join, getGlobalEnd());
        assertOneInfo(texts.getLongText("unlabeledComplexGateway"), gateway, true);
    }

    @Test
    public void testUnlabeledDataObject() {
        DataObject data = new DataObject();
        model.addNode(data);
        assertOneInfo(texts.getLongText("unlabeledDataObject"), data, true);
    }

    @Test
    public void testUnlabeledDataStore() {
        DataStore data = new DataStore();
        model.addNode(data);
        assertOneInfo(texts.getLongText("unlabeledDataStore"), data, true);
    }

    @Test
    public void testUnlabeledColapsedSubProcess() {
        SubProcess sub = new SubProcess();
        sub.setProperty(SubProcess.PROP_COLLAPSED, SubProcess.TRUE);
        model.addNode(sub);
        addSequenceFlow(getGlobalStart(), sub);
        addSequenceFlow(sub, getGlobalEnd());
        assertOneInfo(texts.getLongText("unlabeledActivity"), sub, true);
    }

    @Test
    public void testUnlabeledOpenSubProcess() {
        SubProcess sub = new SubProcess();
        model.addNode(sub);
        addSequenceFlow(getGlobalStart(), sub);
        addSequenceFlow(sub, getGlobalEnd());
        sub.addProcessNode(getNonPoolTask1(false, false));
        sub.addProcessNode(getNonPoolTask2(false, false));
        assertNoMessages(true);
    }
}
