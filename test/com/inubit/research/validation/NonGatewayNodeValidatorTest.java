package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class NonGatewayNodeValidatorTest extends BPMNValidationTestCommons {


    @Test
    public void testOnlyConditionalOutgoingSequenceFlowFromTask() {
        SequenceFlow flow = new SequenceFlow(
                getTask1InA(true, false), getTask2InA(false, true));
        flow.setLabel("condition");
        flow.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_CONDITIONAL);
        model.addEdge(flow);
        assertOneError(texts.getLongText("onlyConditionalOutgoingSequenceFlow"),
                getTask1InA(), listOf(flow), true);
    }

    @Test
    public void testImplicitSplit() {
        Task task3 = new Task();
        task3.setText("task3");
        model.addNode(task3);
        SequenceFlow
                seq1 = addSequenceFlow(
                    getNonPoolTask1(true, false), getNonPoolTask2(false, false)),
                seq2 = addSequenceFlow(getNonPoolTask1(), task3);
        Gateway join = new ParallelGateway();
        join.setText("join");
        model.addNode(join);
        addSequenceFlow(getNonPoolTask2(), join);
        addSequenceFlow(task3, join);
        addSequenceFlow(join, getGlobalEnd());
        assertOneInfo(texts.getLongText("implicitSplit"), getNonPoolTask1(),
                listOf(seq1, seq2), true);
    }

    @Test
    public void testImplicitJoin() {
        Task task3 = new Task();
        task3.setText("task3");
        model.addNode(task3);
        Gateway split = new ExclusiveGateway();
        split.setText("split");
        model.addNode(split);
        addSequenceFlow(getGlobalStart(), split);
        addSequenceFlow(split, getNonPoolTask1(false, false));
        addSequenceFlow(split, getNonPoolTask2(false, false));
        SequenceFlow
                seq1 = addSequenceFlow(getNonPoolTask1(), task3),
                seq2 = addSequenceFlow(getNonPoolTask2(), task3);
        addSequenceFlow(task3, getGlobalEnd());
        assertOneInfo(texts.getLongText("implicitJoin"), task3,
                listOf(seq1, seq2), true);
    }
}
