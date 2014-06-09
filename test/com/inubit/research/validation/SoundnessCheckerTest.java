/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author tmi
 */
public class SoundnessCheckerTest extends BPMNValidationTestCommons {
    
    private ExclusiveGateway createExclusiveGateway() {
        ExclusiveGateway gateway = new ExclusiveGateway();
        model.addNode(gateway);
        return gateway;
    }
    
    private ParallelGateway createParallelGateway() {
        ParallelGateway gateway = new ParallelGateway();
        model.addNode(gateway);
        return gateway;
    }

    private InclusiveGateway createInclusiveGateway() {
        InclusiveGateway gateway = new InclusiveGateway();
        model.addNode(gateway);
        return gateway;
    }

    private void createBlock(Gateway split, Gateway join) {
        addSequenceFlow(getGlobalStart(), split);
        addSequenceFlow(split, getNonPoolTask1(false, false));
        addSequenceFlow(split, getNonPoolTask2(false, false));
        addSequenceFlow(getNonPoolTask1(false, false), join);
        addSequenceFlow(getNonPoolTask2(false, false), join);
        addSequenceFlow(join, getGlobalEnd());
    }

    @Test
    public void testSoundXORBlock() {
        Gateway split = createExclusiveGateway(),
                join  = createExclusiveGateway();
        createBlock(split, join);
        assertNoMessages(true);
    }

    @Test
    public void testLackOfSynchronization() {
        Gateway split = createParallelGateway(),
                join  = createExclusiveGateway();
        createBlock(split, join);
        assertOneWarning(texts.getLongText("lackOfSynchronization"),
                listOf(split, join), true);
    }

    @Test
    public void testDeadlock() {
        Gateway split = createExclusiveGateway(),
                join  = createParallelGateway();
        createBlock(split, join);
        assertOneError(texts.getLongText("blockStartsExclusiveEndsParallel"),
                listOf(split, join), true);
    }

    @Test
    public void testInclusiveLackOfSynchronization() {
        Gateway split = createInclusiveGateway(),
                join  = createExclusiveGateway();
        createBlock(split, join);
        assertOneWarning(texts.getLongText("blockStartsInclusiveEndsExclusive"),
                listOf(split, join), true);
    }

    @Test
    public void testInclusiveDeadlock() {
        Gateway split = createInclusiveGateway(),
                join  = createParallelGateway();
        createBlock(split, join);
        assertOneError(texts.getLongText("blockStartsInclusiveEndsParallel"),
                listOf(split, join), true);
    }

    private void createLoop(Gateway join, Gateway split) {
        addSequenceFlow(getGlobalStart(), join);
        addSequenceFlow(join, getNonPoolTask1(false, false));
        addSequenceFlow(getNonPoolTask1(), split);
        addSequenceFlow(split, getGlobalEnd());
        addSequenceFlow(split, getNonPoolTask2(false, false));
        addSequenceFlow(getNonPoolTask2(), join);
    }

    @Test
    public void testXORLoop() {
        Gateway join = createExclusiveGateway(),
                split = createExclusiveGateway();
        createLoop(join, split);
        assertNoMessages(true);
    }

    @Test
    public void testLoopWithExclusiveJoinAndParallelSplit() {
        Gateway join = createExclusiveGateway(),
                split = createParallelGateway();
        createLoop(join, split);
        assertOneWarning(texts.getLongText("infiniteLoop"),
                listOf(join, split), true);
    }

    @Test
    public void testLoopWithParallelJoinAndExclusiveSplit() {
        Gateway join = createParallelGateway(),
                split = createExclusiveGateway();
        createLoop(join, split);
        assertOneWarning(texts.getLongText("parallelLoopStart"),
                listOf(join, split), true);
    }

    @Test
    @Ignore
    public void testUnorientedLoopWithParallelGateway() {
        Gateway join = createExclusiveGateway(),
                split = createParallelGateway();
        createLoop(join, split);
        addSequenceFlow(gatewayAfter(getGlobalStart()), split);
        addSequenceFlow(join, gatewayBefore(getGlobalEnd()));

        assertOneWarning(texts.getLongText("loopWithParallelGateway"),
                listOf(join, split), true);
    }
}
