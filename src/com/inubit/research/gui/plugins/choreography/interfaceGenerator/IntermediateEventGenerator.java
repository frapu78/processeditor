/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import com.inubit.research.gui.plugins.choreography.Utils;
import com.inubit.research.gui.plugins.choreography.branchingTree.BranchingTree;
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * Generates events, that realize IntermediateEvents from the choreography
 * @author tmi
 */
class IntermediateEventGenerator extends AbstractGenerator{
  private IntermediateEvent event;
  private Map<String, Map<String, StartEvent>> startEvents;

  public IntermediateEventGenerator(BPMNModel choreography,
          BPMNModel colaboration,
          IntermediateEvent event,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools) {
    super(firstNode, lastNode, allNodes, pools, choreography, colaboration);
    this.event = event;
    this.startEvents = startEvents;
  }

  @Override
  public void generate() {
    BranchingTree history = (new TreeBuilder(choreography)).buildTreeFor(
            event, TreeBuilder.FlowDirection.flowBefore);
    for(String participant : pools.keySet()) {
      generateFor(participant, !history.allAlternativesInvolve(participant),
              history.noAlternativesInvolve(participant));
    }
  }

  /**
   * @param mayBeStart true, if this may be the initial node for participant
   * @param isStart true, if this is, in every case, the initial node for
   * participant. May only be true, if mayBeStart is true
   */
  private void generateFor(String participant, boolean mayBeStart, boolean isStart) {
    StartEvent startEvent = null;
    ProcessNode intermediateEvent = null;
    if(mayBeStart) {
      startEvent = Utils.correspondingStartEvent(event);
      addToPool(startEvent, participant);
      registerOnlyNode(participant, startEvent);
      registerStartEvent(participant, startEvent);
    }
    if(!isStart) {
      intermediateEvent = event.copy();
      addToPool(intermediateEvent, participant);
      registerOnlyNode(participant, intermediateEvent);
    }
    if(mayBeStart && !isStart) {
      ProcessNode gateway = new ExclusiveGateway();
      addToPoolRegistered(gateway, participant);
      colaboration.addEdge(new SequenceFlow(startEvent, gateway));
      colaboration.addEdge(new SequenceFlow(intermediateEvent, gateway));
      registerLastNode(event, participant, gateway);
    }
  }

  private void addToPoolRegistered(ProcessNode node, String participant) {
    addToPoolRegistered(node, participant, event);
  }

  private void registerOnlyNode(String participant, ProcessNode node) {
    registerOnlyNode(event, participant, node);
  }

  private void registerStartEvent(String participant, StartEvent start) {
    if(!startEvents.containsKey(event.getId())) {
      startEvents.put(event.getId(), new HashMap<String, StartEvent>());
    }
    startEvents.get(event.getId()).put(participant, start);
  }
}
