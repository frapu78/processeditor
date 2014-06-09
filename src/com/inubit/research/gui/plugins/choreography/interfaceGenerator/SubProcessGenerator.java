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
import java.util.Collection;
import java.util.Map;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 * generates SubProcesses out of a ChoreographySubProcess
 * @author tmi
 */
class SubProcessGenerator extends AbstractGenerator {

  private ChoreographySubProcess subProcess;

  public SubProcessGenerator(Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools,
          BPMNModel choreography,
          BPMNModel colaboration,
          ChoreographySubProcess subProcess) {
    super(firstNode, lastNode, allNodes, pools, choreography, colaboration);
    this.subProcess = subProcess;
  }
  
  @Override
  public void generate() {
    for (String participant : Utils.participantsOf(subProcess)) {
      SubProcess sub = new SubProcess(subProcess.getPos().x,
              subProcess.getPos().y, subProcess.getText());
      Utils.copyProperties(subProcess, sub);
      addToPoolRegistered(sub, participant, subProcess);
      registerFirstNode(subProcess, participant, sub);
      registerLastNode(subProcess, participant, sub);
      for (ProcessNode node : subProcess.getProcessNodes()) {
        if (allNodes.containsKey(node.getId())
                && allNodes.get(node.getId()).containsKey(participant)) {
          for (ProcessNode realizingNode : allNodes.get(node.getId()).get(participant)) {
            moveToClusterInColaboration(realizingNode, sub);
            colaboration.moveAfter(realizingNode, sub);
          }
        }
      }
    }
  }

  /**
   * Moves a node from its older cluster(if it is contained in one) to another
   * cluster. If the nodeToBeMoved is not contained in any cluster, it will only
   * be added to the new one; if it is contained in a cluster, this cluster must
   * be in the model colaboration.
   * @param nodeToBeMoved the node, that should be moved to a new cluster. It
   * must have been added to the model before.
   * @param target the cluster, where nodeToBeMoved should be placed in
   */
  private void moveToClusterInColaboration(
          ProcessNode nodeToBeMoved, Cluster target) {
    Cluster oldCluster = colaboration.getClusterForNode(nodeToBeMoved);
    if(oldCluster != null) oldCluster.removeProcessNode(nodeToBeMoved);
    target.addProcessNode(nodeToBeMoved);
  }
}
