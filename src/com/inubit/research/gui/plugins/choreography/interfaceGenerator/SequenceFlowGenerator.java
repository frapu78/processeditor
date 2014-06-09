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
import java.util.Map;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * generates SequenceFlow in the colaboration from SequenceFlow in the choreography
 * @author tmi
 */
class SequenceFlowGenerator extends AbstractGenerator {

  SequenceFlow edge;

  public SequenceFlowGenerator(Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools,
          BPMNModel choreography,
          BPMNModel colaboration,
          SequenceFlow edge) {
    super(firstNode, lastNode, allNodes, pools, choreography, colaboration);
    this.edge = edge;
  }

  @Override
  public void generate() {
    for (String participant : pools.keySet()) {
      if (hasKey(lastNode, edge.getSource().getId(), participant)) {
        generateForPool(participant);
      }
    }
  }

  /**
   * generates the SequenceFlow in the pool of participant
   */
  private void generateForPool(String participant) {
    if (hasKey(firstNode, edge.getTarget().getId(), participant)) {
      generateSequenceFlowTo(
              firstNode.get(edge.getTarget().getId()).get(participant),
              participant);
    } else {
      BranchingTree tree = (new TreeBuilder(choreography)).buildTreeFor(
              edge.getTarget(), TreeBuilder.FlowDirection.flowAfter);
      for (ProcessNode node : tree.nextRealizedNodes(firstNode, participant)) {
        generateSequenceFlowTo(node, participant);
      }
    }
  }

  /**
   * generates SequenceFlow from the last node, that realizes the
   * source of edge to target in the pool of participant.
   */
  private void generateSequenceFlowTo(ProcessNode target, String participant) {
    if(target.equals(lastNode.get(edge.getSource().getId()).get(participant))) {
      return;
    }
    SequenceFlow newFlow = new SequenceFlow(
            lastNode.get(edge.getSource().getId()).get(participant),
            target);
    if (!Utils.isEventBasedGateway(newFlow.getSource())) {
      copyPropertiesTo(newFlow);
    }
    colaboration.addEdge(newFlow);
  }

  private boolean hasKey(Map<String, Map<String, ProcessNode>> map,
          String key1, String key2) {
    if (!map.containsKey(key1)) {
      return false;
    }
    return map.get(key1).containsKey(key2);
  }

  private void copyPropertiesTo(ProcessEdge target) {
    Utils.copyProperties(edge, target);
  }
}
