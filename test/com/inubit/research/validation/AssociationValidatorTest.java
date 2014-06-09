package com.inubit.research.validation;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class AssociationValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testAssociationBetweenTasks() {
        Association assoc = new Association(getTask1InA(true, false), getTask2InA(false, true));
        model.addEdge(assoc);
        addSequenceFlow(getTask1InA(), getTask2InA());
        assertOneError(
                texts.getLongText("illegalAssociationSourceTargetCombination"),
                assoc, true);
    }

    @Test
    public void testAssociationFromTimerEventToTask() {
        TimerIntermediateEvent event = new TimerIntermediateEvent();
        model.addNode(event);
        event.setText("timeEvent");
        getPoolA().addProcessNode(event);
        event.setParentNode(getTask1InA(true, false));
        Association assoc = new Association(event, getTask2InA(false, true));
        addSequenceFlow(getTask1InA(), getTask2InA());
        addSequenceFlow(event, gatewayBefore(getEndInA()));
        model.addEdge(assoc);
        layoutModel();
        event.setPos(getTask1InA().getTopLeftPos());
        assertOneError(
                texts.getLongText("illegalAssociationSourceTargetCombination"),
                assoc, false);
    }

    @Test
    public void testCompensationHandlerAssociation() {
        CompensationIntermediateEvent event = new CompensationIntermediateEvent();
        model.addNode(event);
        event.setText("Event");
        getPoolA().addProcessNode(event);
        event.setParentNode(getTask1InA());
        Association assoc = new Association(event, getTask2InA(false, false));
        model.addEdge(assoc);
        getTask2InA().setProperty(Task.PROP_COMPENSATION, Task.TRUE);
        assertNoMessages(true);
    }

    @Test
    public void testDataAssociationLeavingPool() {
        DataObject data = new DataObject();
        data.setText("myData");
        model.addNode(data);
        getPoolB().addProcessNode(data);
        Association assoc = new Association(getTask1InA(),data);
        model.addEdge(assoc);
        layoutModel();
        data.setPos(data.getPos().x + 10, data.getPos().y);
        getPoolB().setSize(200, getPoolB().getSize().height);

        assertOneError(
                texts.getLongText("associationCrossingPool"), assoc, false);
    }

    @Test
    public void testUndirectedDataAssociation() {
        DataObject data = new DataObject();
        data.setText("myData");
        model.addNode(data);
        getPoolA().addProcessNode(data);
        Association assoc = new Association(getTask1InA(),data);
        assoc.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
        model.addEdge(assoc);
        assertOneWarning(
                texts.getLongText("associationShouldBeDirected"),
                assoc, true);
    }

    @Test
    public void testAssociationFromSequenceFlowToDataObject() {
        SequenceFlow flow = new SequenceFlow(getNonPoolTask1(true, false),
                getNonPoolTask2(false, true));
        model.addEdge(flow);
        EdgeDocker docker = new EdgeDocker(flow);
        model.addNode(docker);
        DataObject data = new DataObject();
        data.setText("Secret data");
        model.addNode(data);
        model.addEdge(new Association(docker, data));
        assertNoMessages(true);
    }

    @Test
    public void testAssociationFromMessageToSequenceFlow() {
        SequenceFlow flow = new SequenceFlow(getNonPoolTask1(true, false),
                getNonPoolTask2(false, true));
        model.addEdge(flow);
        EdgeDocker docker = new EdgeDocker(flow);
        model.addNode(docker);
        Message message = new Message();
        message.setText("Secret message");
        model.addNode(message);
        model.addEdge(new Association(message, docker));
        assertNoMessages(true);
    }
}
