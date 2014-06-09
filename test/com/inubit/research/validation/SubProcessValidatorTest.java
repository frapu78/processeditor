package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class SubProcessValidatorTest extends BPMNValidationTestCommons {

    private SubProcess subProcess;
    private Task taskInSubProcess;

    private SubProcess getSubProcess() {
        return getSubProcess(true, true);
    }

    private SubProcess getSubProcess(boolean flowFromStart, boolean flowToEnd) {
        if (subProcess == null) {
            subProcess = new SubProcess();
            getSubProcess().setText("SubProcess");
            model.addNode(getSubProcess());
            model.moveToBack(subProcess);
            if (flowFromStart) addSequenceFlow(getGlobalStart(), subProcess);
            if (flowToEnd) addSequenceFlow(subProcess, getGlobalEnd());
        }
        return subProcess;
    }

    private Task getTaskInSubProcess() {
        if (taskInSubProcess == null) {
            taskInSubProcess = new Task();
            taskInSubProcess.setText("task");
            model.addNode(taskInSubProcess);
            getSubProcess().addProcessNode(taskInSubProcess);
        }
        return taskInSubProcess;
    }

    @Test
    public void testAdHocTransactionSubProcess() {
        getSubProcess().setAdHoc();
        getSubProcess().setTransaction();
        assertOneError(texts.getLongText("adHocTransaction"), getSubProcess(), true);
    }

    @Test
    public void testAdHocEventSubProcess() {
        getSubProcess(false, false).setAdHoc();
        getSubProcess().setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        TimerStartEvent start = createTimerStartEvent();
        getSubProcess().addProcessNode(start);
        getSubProcess().addProcessNode(getNonPoolTask1(false, true));
        addSequenceFlow(start, getNonPoolTask1());
        getSubProcess().addProcessNode(getGlobalEnd());
        assertOneError(texts.getLongText("adHocEventSubProcess"), getSubProcess(), true);
    }

    @Test
    public void testSubProcessWithTimerStartEvent() {
        TimerStartEvent timer = createTimerStartEvent();
        getSubProcess().addProcessNode(timer);
        model.addEdge(new SequenceFlow(timer, getTaskInSubProcess()));
        connectTaskToNewEnd();
        assertOneError(texts.getLongText("subProcessWithTriggeredStartEvent"),
                timer, listOf(getSubProcess()), true);
    }

    @Test
    public void testSubProcessWithMultipleNoneStartEvents() {
        StartEvent start1 = createNoneStartEvent(),
                   start2 = createNoneStartEvent();
        getSubProcess().addProcessNode(start1);
        getSubProcess().addProcessNode(start2);
        addSequenceFlow(start1, getTaskInSubProcess());
        addSequenceFlow(start2, gatewayBefore(getTaskInSubProcess()));
        connectTaskToNewEnd();
        assertOneWarning(texts.getLongText("subProcessWithMultipleStartEvents"),
                getSubProcess(), listOf(start1, start2), true);
    }

    @Test
    public void testEventSubProcess() {
        getSubProcess(false, false).
                setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        TimerStartEvent start = createTimerStartEvent();
        start.setProperty(StartEvent.PROP_NON_INTERRUPTING, StartEvent.TRUE);
        getSubProcess().addProcessNode(start);
        model.addEdge(new SequenceFlow(start, getTaskInSubProcess()));
        connectTaskToNewEnd();
        assertNoMessages(true);
    }

    private void connectTaskToNewEnd() {
        EndEvent end = createEndEvent();
        getSubProcess().addProcessNode(end);
        addSequenceFlow(getTaskInSubProcess(),end);
    }

    @Test
    public void testEventSubProcessWithoutStartEvent() {
        getSubProcess(false, false).
                setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        assertOneError(texts.getLongText("eventSubProcessWithoutStartEvent"), getSubProcess(), true);
    }

    @Test
    public void testEventSubProcessWithMultipleStartEvents() {
        getSubProcess(false, false).setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        assertOneError(texts.getLongText("eventSubProcessWithoutStartEvent"), getSubProcess(), true);
        TimerStartEvent start1 = createTimerStartEvent(),
                        start2 = createTimerStartEvent();
        start1.setProperty(StartEvent.PROP_NON_INTERRUPTING, StartEvent.TRUE);
        getSubProcess().addProcessNode(start1);
        getSubProcess().addProcessNode(start2);
        model.addEdge(new SequenceFlow(start1, getTaskInSubProcess()));
        model.addEdge(new SequenceFlow(start2, gatewayBefore(getTaskInSubProcess())));
        connectTaskToNewEnd();
        assertOneError(texts.getLongText("eventSubProcessWithMultipleStartEvents"),
                getSubProcess(), listOf(start1, start2), true);
    }

    @Test
    public void testEventSubProcessWithNoneStartEvent() {
        getSubProcess(false, false).
                setProperty(SubProcess.PROP_EVENT_SUBPROCESS, SubProcess.TRUE);
        StartEvent start = createNoneStartEvent();
        getSubProcess().addProcessNode(start);
        model.addEdge(new SequenceFlow(start, getTaskInSubProcess()));
        connectTaskToNewEnd();
        assertOneError(texts.getLongText("eventSubProcessWithNoneStartEvent"),
                start, listOf(getSubProcess()), true);
    }

    @Test
    public void testAdHocSubProcessContainingStartEvent() {
        getSubProcess().setAdHoc();
        StartEvent start = createNoneStartEvent();
        getSubProcess().addProcessNode(start);
        model.addEdge(new SequenceFlow(start, getTaskInSubProcess()));
        assertOneError(texts.getLongText("adHocSubProcessWithStartEvent"),
                start, listOf(getSubProcess()), true);
    }

    @Test
    public void testAdHocSubProcessContainingEndEvent() {
        getSubProcess().setAdHoc();
        EndEvent end = new EndEvent();
        end.setText("end");
        model.addNode(end);
        getSubProcess().addProcessNode(end);
        model.addEdge(new SequenceFlow(getTaskInSubProcess(),end));
        assertOneError(texts.getLongText("adHocSubProcessWithEndEvent"),
                end, listOf(getSubProcess()), true);
    }
}
