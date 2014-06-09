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
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * translates a gateway in the choreography a gateway in the colaboration
 * @author tmi
 */
class GatewayGenerator extends AbstractGenerator {

  private Gateway gateway;
  boolean isJoin = false;

  public GatewayGenerator(BPMNModel choreography,
          BPMNModel colaboration,
          Gateway gateway,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools) {
    super(firstNode, lastNode, allNodes, pools, choreography, colaboration);
    this.gateway = gateway;
    isJoin = choreography.getIncomingEdges(SequenceFlow.class, gateway).size() > 1;
  }

  public void generate() {
    if (Utils.isExclusiveGateway(gateway) || Utils.isEventBasedGateway(gateway)) {
      handleExclusiveOrEventBasedGateway();
    } else {
      addToAllPools();
    }
  }

  /**
   * adds a copy of the gateway to all participants´ pools
   */
  private void addToAllPools() {
    for (Map.Entry<String, Pool> pool : pools.entrySet()) {
      ProcessNode node = gateway.copy();
      colaboration.addNode(node);
      pool.getValue().addProcessNode(node);
      registerOnlyNode(pool.getKey(), node);
    }
  }

  /**
   * registers node as the first and last node and as one node, that realizes the
   * gateway for participant
   */
  private void registerOnlyNode(String participant, ProcessNode node) {
    registerOnlyNode(gateway, participant, node);
  }

  /**
   * creates gateways in the colaboration, that realize an EventBased- or
   * ExclusiveGateway in the choreography.<br />
   * It makes no difference at all, wheter the gateway is exclusive or
   * event-based. This is due to the BPMN specification.
   */
  private void handleExclusiveOrEventBasedGateway() {
    Collection<String> succeedingInitiators =
            Utils.initiatorsOf(Utils.getSucceedingNodes(gateway, choreography));
    for (String participant : pools.keySet()) {
      Gateway addedGateway;
      if (succeedingInitiators.contains(participant) || isJoin) {
        addedGateway = new ExclusiveGateway();
      } else {
        addedGateway = new EventBasedGateway();
      }
      addedGateway.setPos(gateway.getPos());
      addedGateway.setText(gateway.getText());
      addToPool(addedGateway, participant);
      registerOnlyNode(participant, addedGateway);
    }
  }
}
