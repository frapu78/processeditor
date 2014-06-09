package com.inubit.research.validation;

import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;
import org.junit.Test;

/**
 *
 * @author tmi
 */
public class InstantiationValidatorTest extends BPMNValidationTestCommons {

    private EventBasedGateway createInstantiatingGateway() {
        EventBasedGateway gateway = new EventBasedGateway();
        gateway.setProperty(EventBasedGateway.PROP_INSTANTIATE,
                EventBasedGateway.TYPE_INSTANTIATE_EXCLUSIVE);
        model.addNode(gateway);
        return gateway;
    }

    private TimerIntermediateEvent addTimerTo(EventBasedGateway gateway) {
        TimerIntermediateEvent timer = new TimerIntermediateEvent();
        timer.setText("timer");
        model.addNode(timer);
        if (model.getClusterForNode(gateway) != null) {
            model.getClusterForNode(gateway).addProcessNode(timer);
        }
        model.addEdge(new SequenceFlow(gateway, timer));
        return timer;
    }

    private Task createTask() {
        Task task = new Task();
        task.setText("task");
        model.addNode(task);
        return task;
    }

     @Test
     public void testMultipleNoneStartEventsInOnePool() {
         StartEvent start1 = createNoneStartEvent(),
                    start2 = createNoneStartEvent();
         Task task1 = createTask(),
              task2 = createTask();
         addSequenceFlow(start1, task1);
         addSequenceFlow(start2, task2);
         EndEvent end1 = createEndEvent(),
                  end2 = createEndEvent();
         addSequenceFlow(task1, end1);
         addSequenceFlow(task2, end2);
         assertOneWarning(texts.getLongText("multipleNoneStartEventsInOnePool"),
                 listOf(start1, start2), true);
     }

     @Test
     public void testNoneStartEventsInMultiplePools() {
         StartEvent start1 = createNoneStartEvent(),
                    start2 = createNoneStartEvent();
         getPoolA().addProcessNode(start1);
         getPoolB().addProcessNode(start2);
         addSequenceFlow(start1, getTask1InA(false, true));
         addSequenceFlow(start2, getTask1InB(false, true));
         assertOneInfo(texts.getLongText("noneStartEventsInMultiplePools"),
                 listOf(start1,start2), true);
     }

     @Test
     public void testMultipleTimerStartEvents() {
         TimerStartEvent timer1 = createTimerStartEvent(),
                         timer2 = createTimerStartEvent();
         Task task1 = createTask(),
              task2 = createTask();
         addSequenceFlow(timer1, task1);
         addSequenceFlow(timer2, task2);
         EndEvent end1 = createEndEvent(),
                  end2 = createEndEvent();
         addSequenceFlow(task1, end1);
         addSequenceFlow(task2, end2);
         assertOneInfo(texts.getLongText("multipleTriggeredStartEventsInOnePool"),
                 listOf(timer1, timer2), true);
     }

     @Test
     public void testMultipleInstantiatingGatewaysInOnePool() {
         EventBasedGateway gateway1 = createInstantiatingGateway(),
                           gateway2 = createInstantiatingGateway();
         TimerIntermediateEvent timer1 = addTimerTo(gateway1),
                                timer2 = addTimerTo(gateway1),
                                timer3 = addTimerTo(gateway2),
                                timer4 = addTimerTo(gateway2);
         ExclusiveGateway exclusiveGateway1 = new ExclusiveGateway(),
                          exclusiveGateway2 = new ExclusiveGateway();
         model.addNode(exclusiveGateway1);
         model.addNode(exclusiveGateway2);
         addSequenceFlow(timer1, exclusiveGateway1);
         addSequenceFlow(timer2, exclusiveGateway1);
         addSequenceFlow(timer3, exclusiveGateway2);
         addSequenceFlow(timer4, exclusiveGateway2);
         ParallelGateway parallelGateway = new ParallelGateway();
         model.addNode(parallelGateway);
         addSequenceFlow(exclusiveGateway1, parallelGateway);
         addSequenceFlow(exclusiveGateway2, parallelGateway);
         EndEvent end = createEndEvent();
         addSequenceFlow(parallelGateway, end);
         assertOneInfo(texts.getLongText("multipleExclusiveInstantiatingGatewaysInOnePool"),
                 listOf(gateway1, gateway2), true);
     }

     @Test
     public void testInstantiatingGatewayAndTimerStartEventInOnePool() {
         EventBasedGateway gateway = createInstantiatingGateway();
         TimerIntermediateEvent timer1 = addTimerTo(gateway),
                                timer2 = addTimerTo(gateway);
         TimerStartEvent start = createTimerStartEvent();
         Gateway join = gatewayBefore(getGlobalEnd());
         addSequenceFlow(timer1, join);
         addSequenceFlow(timer2, join);
         addSequenceFlow(start, join);
         assertOneInfo(texts.getLongText("instantiatingGatewaysAndTriggeredStartEvents"),
                 listOf(gateway, start), true);
     }

     @Test
     public void testStartEventsButNoEndEventsUsed() {
         TimerStartEvent start = createTimerStartEvent();
         Task task = createTask();
         ExclusiveGateway gateway = new ExclusiveGateway();
         model.addNode(gateway);
         addSequenceFlow(start, gateway);
         addSequenceFlow(gateway, task);
         addSequenceFlow(task, gateway);
         assertOneWarning(
                 texts.getLongText("inconsistentUsageOfStartAndEndEventsAtTopLevel"),
                 null, listOf(start), true);
     }

     @Test
     public void testStartEventsButNoEndEventsUsedInPool() {
         Pool pool = new Pool();
         pool.setText("Pool");
         model.addNode(pool);
         TimerStartEvent timer = createTimerStartEvent();
         pool.addProcessNode(timer);
         Task task = createTask();
         pool.addProcessNode(task);
         ExclusiveGateway gateway = new ExclusiveGateway();
         model.addNode(gateway);
         pool.addProcessNode(gateway);
         addSequenceFlow(timer, gateway);
         addSequenceFlow(gateway, task);
         addSequenceFlow(task, gateway);
         assertOneWarning(texts.getLongText("inconsistentUsageOfStartAndEndEvents"),pool, true);
     }

     @Test
     public void testNoStartAndEndEventUsageAndTaskWithoutIncommingSequenceFlow() {
         Task task1 = createTask(),
              task2 = createTask();
         ExclusiveGateway gateway = new ExclusiveGateway();
         model.addNode(gateway);
         addSequenceFlow(task1, gateway);
         addSequenceFlow(gateway, task2);
         addSequenceFlow(task2, gateway);
         assertOneInfo(texts.getLongText("noStartEventUsage"),
                 null, listOf(task1), true);
     }

     @Test
     public void testStartAndEndEventsUsedButTaskWithoutIncommingSequenceFlowExists() {
         Task task1 = createTask(),
              task2 = createTask();
         ParallelGateway gateway = new ParallelGateway();
         model.addNode(gateway);
         StartEvent start = createNoneStartEvent();
         EndEvent end = createEndEvent();
         addSequenceFlow(start, gateway);
         addSequenceFlow(gateway, task2);
         addSequenceFlow(task2, end);
         addSequenceFlow(task1, gateway);
         assertOneWarning(texts.getLongText("sometimesNoStartEventsUsed"), 
                 null, listOf(task1), true);
     }

     @Test
     public void testNoStartAndEndEventUsageAndTaskWithoutOutgoingSequenceFlow() {
         Task task1 = createTask(),
              task2 = createTask();
         ExclusiveGateway gateway = new ExclusiveGateway();
         model.addNode(gateway);
         addSequenceFlow(task1, gateway);
         addSequenceFlow(gateway, task1);
         addSequenceFlow(gateway, task2);
         assertOneInfo(texts.getLongText("noEndEventUsage"),
                 null, listOf(task2), true);
     }

     @Test
     public void testStartAndEndEventsUsedButTaskWithoutOutgoingSequenceFlowExists() {
         StartEvent start = createNoneStartEvent();
         EndEvent end = createEndEvent();
         ExclusiveGateway gateway = new ExclusiveGateway();
         model.addNode(gateway);
         Task task = createTask();
         addSequenceFlow(start, gateway);
         addSequenceFlow(gateway, end);
         addSequenceFlow(gateway, task);
         assertOneWarning(texts.getLongText("sometimesNoEndEventsUsed"),
                 null, listOf(task), true);
     }
}
