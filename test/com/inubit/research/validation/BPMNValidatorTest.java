package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.InclusiveGateway;
import com.inubit.research.validation.bpmn.BPMNValidator;
import java.util.List;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Does miscalaneous tests for BPMN-Validation (more exactly, test, which are
 * not worse getting their own Subclass)
 * @author tmi
 */
public class BPMNValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testCorrectSequenceAndMessageFlow() {
        SubProcess subProcessInA = new SubProcess();
        subProcessInA.setText("subProcess");
        model.addNode(subProcessInA);
        getPoolA().addProcessNode(subProcessInA);
        Task task1InSubProcess = new Task();
        Task task2InSubProcess = new Task();
        task1InSubProcess.setText("task1...");
        task2InSubProcess.setText("task2...");
        model.addNode(task1InSubProcess);
        model.addNode(task2InSubProcess);
        subProcessInA.addProcessNode(task1InSubProcess);
        subProcessInA.addProcessNode(task2InSubProcess);
        model.addEdge(new SequenceFlow(getTask1InA(true, false),subProcessInA));
        model.addEdge(new SequenceFlow(task1InSubProcess, task2InSubProcess));
        model.addEdge(new MessageFlow(getTask1InB(), getTask1InA()));
        model.addEdge(new MessageFlow(task1InSubProcess, getPoolB()));
        makeSendTask(getTask1InB());
        makeReceiveTask(getTask1InA());
        makeSendTask(task1InSubProcess);
        addSequenceFlow(subProcessInA, getEndInA());
        assertNoMessages(true);
    }

    @Test
    public void testPoolContainingLanesAndOtherNodes() {
        Lane lane = new Lane();
        lane.setText("LaneInA");
        getPoolA().addLane(lane);
        model.addNode(lane);
        Task someTask = new Task();
        someTask.setText("someTask");
        lane.addProcessNode(someTask);
        model.addNode(someTask);
        addSequenceFlow(getTask1InA(true, false), someTask);
        addSequenceFlow(someTask, getTask2InA(false, true));
        List<ProcessObject> related =
                listOf(lane, getTask1InA(), getTask2InA());
        related.add(getStartInA());
        related.add(getEndInA());
        layoutModel();
        List<ValidationMessage> validationMessages =
                new BPMNValidator().doValidation(model);
        assertEquals(2, validationMessages.size());//one Message, that tells, that someTask
        //is not laying on the cluster, it is contained in and the texts, that
        //tells, that the pool contains lanes and other nodes.
        ValidationMessage message;
        if (validationMessages.get(0).getDescription().equals(
                texts.getLongText("poolContainingLanesAndOtherNodes"))) {
            message = validationMessages.get(0);
        } else {
            message = validationMessages.get(1);
        }
        assertEquals(texts.getLongText("poolContainingLanesAndOtherNodes"),
                message.getDescription());
        assertEquals(ValidationMessage.TYPE_ERROR, message.getType());
        assertEquals(getPoolA(), message.getPrimaryObject());
        assertEquals(related.size(), message.getInvolvedObjects().size());
        assertTrue(message.getInvolvedObjects().containsAll(related));
    }

    @Test
    public void testPoolContainingLanes() {
        Lane lane1 = new Lane();
        Lane lane2 = new Lane();
        lane1.setText("Lane1InA");
        lane2.setText("Lane2InA");
        model.addNode(lane1);
        model.addNode(lane2);
        getPoolA().addLane(lane1);
        getPoolA().addLane(lane2);
        getPoolA().removeProcessNode(getTask1InA(true, false));
        lane1.addProcessNode(getTask1InA());
        getPoolA().removeProcessNode(getTask2InA(false, true));
        lane2.addProcessNode(getTask2InA());
        getPoolA().removeProcessNode(getStartInA());
        lane1.addProcessNode(getStartInA());
        getPoolA().removeProcessNode(getEndInA());
        lane2.addProcessNode(getEndInA());
        addSequenceFlow(getTask1InA(), getTask2InA());
        assertNoMessages(true);
    }

    @Test
    public void testUnnecessaryGateway() {
        Gateway gateway = new Gateway();
        gateway.setText("gateway");
        model.addNode(gateway);
        model.addEdge(new SequenceFlow(getNonPoolTask1(true, false),gateway));
        model.addEdge(new SequenceFlow(gateway, getNonPoolTask2(false, true)));
        assertOneInfo(texts.getLongText("uselessGateway"), gateway, true);
    }

    @Test
    public void testChoreographyTaskInPool() {
        ChoreographyTask choreoTask = new ChoreographyTask();
        choreoTask.setText("task");
        model.addNode(choreoTask);
        getPoolA().addProcessNode(choreoTask);
        addSequenceFlow(getStartInA(), choreoTask);
        addSequenceFlow(choreoTask, getEndInA());
        assertOneError(texts.getLongText("choreographyActivityInPool"),
                choreoTask, true);
    }

    @Test
    public void testChoreographySubProcessInPool() {
        ChoreographySubProcess choreoSubProcess = new ChoreographySubProcess();
        choreoSubProcess.setText("sub");
        model.addNode(choreoSubProcess);
        getPoolA().addProcessNode(choreoSubProcess);
        addSequenceFlow(getStartInA(), choreoSubProcess);
        addSequenceFlow(choreoSubProcess, getEndInA());
        assertOneError(texts.getLongText("choreographyActivityInPool"),
                choreoSubProcess, true);
    }
    
    @Test
    public void testMultipleDefaultFlows() {
        Task task3 = new Task();
        task3.setText("task3");
        model.addNode(task3);
        Gateway split = new InclusiveGateway();
        model.addNode(split);
        addSequenceFlow(getGlobalStart(), split);
        SequenceFlow default1 = addSequenceFlow(split, getNonPoolTask1(false, false)),
                     default2 = addSequenceFlow(split, getNonPoolTask2(false, false));
        default1.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_DEFAULT);
        default2.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_DEFAULT);
        addSequenceFlow(split, task3);
        Gateway join = new InclusiveGateway();
        model.addNode(join);
        addSequenceFlow(getNonPoolTask1(), join);
        addSequenceFlow(getNonPoolTask2(), join);
        addSequenceFlow(task3, join);
        addSequenceFlow(join, getGlobalEnd());
        assertOneError(texts.getLongText("multipleDefaultFlows"), split,
                listOf(default1, default2), true);
    }

    @Test
    public void testOnlyDefaultFlows() {
        SequenceFlow defaultFlow = addSequenceFlow(getNonPoolTask1(true, false),
                getNonPoolTask2(false, true));
        defaultFlow.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_DEFAULT);
        assertOneError(texts.getLongText("defaultFlowButNoConditionalFlow"),
                getNonPoolTask1(), listOf(defaultFlow), true);
    }
}
