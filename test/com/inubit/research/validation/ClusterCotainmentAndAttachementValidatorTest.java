package com.inubit.research.validation;

import java.util.List;
import com.inubit.research.validation.bpmn.BPMNValidator;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tmi
 */
public class ClusterCotainmentAndAttachementValidatorTest extends BPMNValidationTestCommons {

    @Test
    public void testSelfCotainingSubProcess() {
        SubProcess sub = new SubProcess();
        sub.setText("sub-process");
        model.addNode(sub);
        sub.addProcessNode(sub);
        assertOneError(texts.getLongText("selfContainingCluster"),
                null, listOf(sub), false);
    }

    @Test
    public void testIndirectlySelfContainingSubProcess() {
        SubProcess sub1 = new SubProcess(),
                   sub2 = new SubProcess();
        sub1.setText("sub1");
        sub2.setText("sub2");
        model.addNode(sub1);
        model.addNode(sub2);
        sub1.addProcessNode(sub2);
        sub2.addProcessNode(sub1);
        assertOneError(texts.getLongText("selfContainingCluster"),
                listOf(sub1, sub2), false);
    }

    @Test
    public void testTaskInMultipleSubProcesses() {
        SubProcess sub1 = new SubProcess(),
                   sub2 = new SubProcess();
        model.addNode(sub1);
        model.addNode(sub2);
        addSequenceFlow(getGlobalStart(), sub1);
        addSequenceFlow(sub1, sub2);
        addSequenceFlow(sub2, getGlobalEnd());
        sub1.addProcessNode(getNonPoolTask1(false, false));
        sub2.addProcessNode(getNonPoolTask1());
        layoutModel();
        getGlobalEnd().setPos(1000, 1000);//layouter puts it at some colliding position
        assertOneError(texts.getLongText("nodeInMultipleClusters"), getNonPoolTask1(), false);
    }

    @Test
    public void testAttachedNodeNotContainedInCluster() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        model.addNode(timer);
        timer.setParentNode(getTask1InA(true, true));
        addSequenceFlow(timer, gatewayBefore(getEndInA()));
        layoutModel();
        timer.setPos(500,500);

        List<ValidationMessage> validationMessages =
                new BPMNValidator().doValidation(model);
        assertEquals(4, validationMessages.size());//1 x SequenceFlow crossing Pool-border,
        //1 x attached IntermediateEvent not located at parent´s border
        //1 x attached IntermediateEvent not in surrounding cluster
        //1 x there were fatal errors
        ValidationMessage message = null;
        for (ValidationMessage oneMessage : validationMessages) {
            if (oneMessage.getDescription().
                equals(texts.getLongText(
                "attachedIntermediateEventNotInSurroundingCluster"))) {
                message = oneMessage;
            }
        }
        assertNotNull(message);
        assertEquals(texts.getLongText(
                "attachedIntermediateEventNotInSurroundingCluster"),
                message.getDescription());
        assertEquals(ValidationMessage.TYPE_ERROR, message.getType());
        assertEquals(0, message.getInvolvedObjects().size());
        assertEquals(timer, message.getPrimaryObject());
    }

    @Test
    public void testEventAttachedToSubProcessInRoot() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        model.addNode(timer);
        SubProcess sub = new SubProcess();
        sub.setText("test");
        model.addNode(sub);
        addSequenceFlow(getGlobalStart(), sub);
        addSequenceFlow(sub, getGlobalEnd());
        timer.setParentNode(sub);
        addSequenceFlow(timer, gatewayBefore(getGlobalEnd()));
        assertNoMessages(true);
    }

    @Test
    public void testNodeGraphicallyContainedInSubProcessButNotInItsNodesList() {
        SubProcess sub = new SubProcess(100, 100, "sub");
        model.addNode(sub);
        addSequenceFlow(getGlobalStart(), sub);
        addSequenceFlow(sub, getNonPoolTask1(false, true));
        getGlobalEnd().setPos(1500,1500);
        getNonPoolTask1().setPos(101, 101);
        assertOneWarning(texts.getLongText("graphicalContainedInAnotherClusterThanReallyContainedIn"),
                getNonPoolTask1(), false);
    }

    @Test
    public void testTaskLyingOnPoolWhichItIsNotContainedBy() {
        getPoolA().setPos(500, 500);
        getNonPoolTask1().setPos(getPoolA().getPos());
        getGlobalStart().setPos(0, 0);
        getGlobalEnd().setPos(100, 0);
        assertOneWarning(texts.getLongText("graphicalContainedInAnotherClusterThanReallyContainedIn"),
                getNonPoolTask1(), false);
    }

    @Test
    public void testAttachedNodeNotLocatedAtParentBorder() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        model.addNode(timer);
        timer.setParentNode(getNonPoolTask1());
        addSequenceFlow(timer, gatewayBefore(getGlobalEnd()));
        layoutModel();
        timer.setPos(500, 500);
        assertOneWarning(texts.getLongText("attachedNodeNotLocatedAtParentBorder"),
                timer, false);
    }

    @Test
    public void testNodeLocatedAtTaskBorderButNotAttached() {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        model.addNode(timer);
        addSequenceFlow(getGlobalStart(), timer);
        addSequenceFlow(timer, getNonPoolTask1(false, true));
        layoutModel();
        timer.setPos(getNonPoolTask1().getTopLeftPos());

        assertOneWarning(texts.getLongText("nodeSeemsAttachedButIsNot"),
                timer, false);
    }
}
