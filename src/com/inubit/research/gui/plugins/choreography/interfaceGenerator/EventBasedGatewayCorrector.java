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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Tries to pull events to an EventBasedGateway. The result of this correction
 * is, that, if possible, the EventBasedGateway is directly followed only by
 * IntermediateEvents (for a non-instantiating gateway)
 * @author tmi
 */
class EventBasedGatewayCorrector {

  private EventBasedGateway gateway;
  private BPMNModel model;

  public EventBasedGatewayCorrector(EventBasedGateway gateway, BPMNModel model) {
    this.gateway = gateway;
    this.model = model;
  }

  public void correct() {
    pullEventsTo(gateway, new HashSet<ProcessNode>());
  }

  /**
   * tries to pull events from succeding nodes, so that, if possible, gatewayNode
   * will only be succeeded by IntermediateEvents (for non-instantiating gateways).
   * It cannot be trusted, that there will only be IntermediateEvents following
   * gatewayNode, because there are situations, where this is not achievable
   */
  private void pullEventsTo(ProcessNode gatewayNode,
          Collection<ProcessNode> visitedNodes) {
    for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, gatewayNode)) {
      if (!isAcceptableNodeToFollowGateway(edge.getTarget(), gatewayNode)
              && !visitedNodes.contains(edge.getTarget())) {
        model.removeEdge(edge);
        Collection<ProcessNode> targets = getEventsAfterGateway(
                edge.getTarget(), new HashSet<ProcessNode>());
        if (targets.isEmpty()) {
          model.addEdge(edge);
        } else {
          for (ProcessNode node : targets) {
            model.addEdge(new SequenceFlow(gatewayNode, node));
          }
        }
      }
    }
  }

  private boolean isAcceptableNodeToFollowGateway(
          ProcessNode node, ProcessNode gatewayNode) {
    return (Utils.isIntermediateEvent(node)
            || Utils.isTask(node)
            || Utils.isStartEvent(node))
            || (Utils.isInstantiatingGateway(gatewayNode)
            && Utils.isInstantiatingGateway(node));
  }

  /**
   * collects nodes, that can replace the gatewayNode as successors of an
   * EventBasedGateway
   */
  private Collection<ProcessNode> getEventsAfterGateway(
          ProcessNode gatewayNode, Collection<ProcessNode> visitedNodes) {
    visitedNodes.add(gatewayNode);
    pullEventsTo(gatewayNode, visitedNodes);
    if (Utils.isExclusiveGateway(gatewayNode)
            || Utils.isEventBasedGateway(gatewayNode)) {
      return eventsAfterExclusiveOrEventBasedGateway(gatewayNode);
    } else if (Utils.isParallelGateway(gatewayNode)) {
      return eventsAfterParallelGateway(gatewayNode);
    } else {
      return new HashSet<ProcessNode>();
    }
  }

  /**
   * collects, for an Exclusive- or EventBasedGateway, that is directly followed
   * by IntermediateEvents as far as possible, nodes that can be used to replace
   * this gateway as succesors of an EventBasedGateway
   */
  private Collection<ProcessNode> eventsAfterExclusiveOrEventBasedGateway(
          ProcessNode gatewayNode) {
    Collection<ProcessNode> result = new HashSet<ProcessNode>();
    for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, gatewayNode)) {
      if (Utils.isTask(edge.getTarget())
              || Utils.isIntermediateEvent(edge.getTarget())
              || Utils.isStartEvent(edge.getTarget())) {
        Gateway newGateway = new ExclusiveGateway();
        insertAfter(newGateway, edge.getTarget());
        ProcessNode copy = addedCopyWithMessageFlow(edge.getTarget());
        model.addEdge(new SequenceFlow(copy, newGateway));
        result.add(copy);
      } else if (Utils.isParallelGateway(edge.getTarget())) {
        result.add(gatewayNode);
      } else {
        result.add(edge.getTarget());
      }
    }
    if (result.size() == 1 && result.contains(gatewayNode)) {
      return new HashSet<ProcessNode>();
    } else {
      return result;
    }
  }

  /**
   * collects, for a ParallelGateway, that is directly followed by
   * IntermediateEvents as far as possible, nodes that can be used to replace
   * this gateway as succesors of an EventBasedGateway
   */
  private Collection<ProcessNode> eventsAfterParallelGateway(
          ProcessNode parallelGateway) {
    if (!model.getIncomingEdges(SequenceFlow.class, parallelGateway).isEmpty()) {
      return new HashSet<ProcessNode>();
      //cannot cope with parallel joins and the one incoming edge, that is allowed
      //was removed for reconstruction (will be reconstructed with my return-value
    }
    Collection<ProcessNode> followingNodes = new HashSet<ProcessNode>(
            model.getSucceedingNodes(SequenceFlow.class, parallelGateway));
    Map<String, ExclusiveGateway> pathStartGateways =
            new HashMap<String, ExclusiveGateway>();
    Map<String, ExclusiveGateway> gatewaysAfterFirstNode =
            new HashMap<String, ExclusiveGateway>();
    prepareParallelPathes(parallelGateway, pathStartGateways, gatewaysAfterFirstNode);

    return createAlternativesFromParallelism(followingNodes, parallelGateway,
            gatewaysAfterFirstNode, pathStartGateways);
  }

  //TODO: as I am writing the documentation, it appears to me, that there should...
  //be a method eventsAfterInclusiveGateway(ProcessNode), but it is too late to
  //implement this now

  /**
   * will, for each path leading away from the parallelGateway, insert a new
   * ExclusiveGateway between parallelGateway and the first node on this path
   * and place this in pathStartGateways at the id of this first node, and insert
   * a new ExclusiveGateway after this first node and place it in
   * gatewaysAfterFirstNode at the id of this first node
   */
  private void prepareParallelPathes(ProcessNode parallelGateway,
          Map<String, ExclusiveGateway> pathStartGateways,
          Map<String, ExclusiveGateway> gatewaysAfterFirstNode) {
    for (ProcessEdge edge :
            model.getOutgoingEdges(SequenceFlow.class, parallelGateway)) {
      ExclusiveGateway newGateway = new ExclusiveGateway();
      addToModelAsSourceOf(newGateway, edge);
      pathStartGateways.put(edge.getTarget().getId(), newGateway);
      newGateway = new ExclusiveGateway();
      insertAfter(newGateway, edge.getTarget());
      gatewaysAfterFirstNode.put(edge.getTarget().getId(), newGateway);
    }
  }

  private Collection<ProcessNode> createAlternativesFromParallelism(
          Collection<ProcessNode> followingNodes,
          ProcessNode parallelGateway,
          Map<String, ExclusiveGateway> gatewaysAfterFirstNode,
          Map<String, ExclusiveGateway> pathStartGateways) {
    Collection<ProcessNode> result = new HashSet<ProcessNode>();
    for (ProcessNode currentNode : followingNodes) {
      if (Utils.isTask(currentNode)
              || Utils.isStartEvent(currentNode)
              || Utils.isIntermediateEvent(currentNode)) {
        ProcessNode copy = addedCopyWithMessageFlow(currentNode);
        result.add(copy);
        ParallelGateway fork = (ParallelGateway) addedCopy(parallelGateway);
        model.addEdge(new SequenceFlow(copy, fork));
        model.addEdge(new SequenceFlow(fork, gatewaysAfterFirstNode.get(
                currentNode.getId())));
        for (Map.Entry<String, ExclusiveGateway> entry : pathStartGateways.entrySet()) {
          if (!entry.getKey().equals(currentNode.getId())) {
            model.addEdge(new SequenceFlow(fork, entry.getValue()));
          }
        }
      }
    }
    return result;
  }

  /**
   * adds node to the model and to the cluster of the edge´s source, if it is
   * in any cluster and then sets the edge´s source to node.
   */
  private void addToModelAsSourceOf(ProcessNode node, ProcessEdge edge) {
    model.addNode(node);
    Cluster cluster = model.getClusterForNode(edge.getSource());
    if (cluster != null) {
      cluster.addProcessNode(node);
    }
    edge.setSource(node);
  }

  /**
   * @return a copy of original, that has the same incoming and outgoing message
   * flow as original, and was added to the model and the cluster of original,
   * if any
   */
  private ProcessNode addedCopyWithMessageFlow(ProcessNode original) {
    ProcessNode copy = addedCopy(original);
    for (ProcessEdge edge : model.getIncomingEdges(MessageFlow.class, original)) {
      ProcessEdge flow = addedCopy((MessageFlow) edge);
      flow.setTarget(copy);
    }
    for (ProcessEdge edge : model.getOutgoingEdges(MessageFlow.class, original)) {
      ProcessEdge flow = addedCopy((MessageFlow) edge);
      flow.setSource(copy);
    }
    return copy;
  }

  /**
   * @return a copy of original, that was added to the model and the cluster of
   * original, if any
   */
  private ProcessNode addedCopy(ProcessNode original) {
    ProcessNode copy = original.copy();
    model.addNode(copy);
    Cluster cluster = model.getClusterForNode(original);
    if (cluster != null) {
      cluster.addProcessNode(copy);
    }
    return copy;
  }

  /**
   * @return a copy of original, that was added to the model (respect is paid to
   * the fact, wheter original is MessageFlowWithEnvelope or just normal
   * MessageFlow
   */
  private MessageFlow addedCopy(MessageFlow original) {
    MessageFlow copy;
    if (Utils.isMessageFlowWithEnvelope(original)) {
      copy = new MessageFlowWithEnvelope(original.getSource(), original.getTarget());
    } else {
      copy = new MessageFlow(original.getSource(), original.getTarget());
    }
    Utils.copyProperties(original, copy);
    model.addEdge(copy);
    return copy;
  }

  /**
   * inserts node after afterNode, i.e. creates a SequenceFlow from afterNode to
   * node and sets the source of all other outgoing SequenceFlow of afterNode
   * to node.
   */
  private void insertAfter(ProcessNode node, ProcessNode afterNode) {
    model.addNode(node);
    Cluster cluster = model.getClusterForNode(afterNode);
    if (cluster != null) {
      cluster.addProcessNode(node);
    }
    for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, afterNode)) {
      edge.setSource(node);
    }
    model.addEdge(new SequenceFlow(afterNode, node));
  }
}
