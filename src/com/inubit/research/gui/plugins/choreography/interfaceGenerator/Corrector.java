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
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;

/**
 * does correcting and improving work on a generated colaboration diagram
 * @author tmi
 */
class Corrector {

  private BPMNModel model;
  private boolean avoidImplicitSplitsAndJoins;

  public Corrector(BPMNModel model, boolean avoidImplicitSplitsAndJoins) {
    this.model = model;
    this.avoidImplicitSplitsAndJoins = avoidImplicitSplitsAndJoins;
  }

  /**
   * does correcting work on the colaboration diagram: removes illegal
   * SequenceFlow; eliminates edges, that do not change the semantic of the model;
   * removes Gateways, that do neither split nor join the SequenceFlow; unites
   * consecutive gateways, where appropriate, but splits gateways, so that they
   * either join or split - not both
   */
  public void correctAndOptimize() {
    removeIllegalIncomingSequenceFlowToStartEvents();
    boolean changed = true;
    while (changed) {
      changed = removeNeedlessEdgesAndGatewaysAndUniteConsecutiveGateways();
    }
    pullEventsToEventBasedGateways();
    changed = true;
    while (changed) {
      changed = removeNeedlessEdgesAndGatewaysAndUniteConsecutiveGateways();
      changed |= removeNodesWithoutIncomingSequenceFlow();
      changed |= eliminateDuplicates();
    }
    if(avoidImplicitSplitsAndJoins) {
      removeImplicitJoins();
      removeImplicitSplits();
    }
    removeObsoleteFragments();
    generateTimeouts();
  }

  /**
   * @return true, if changes were done to the model.
   * @see #removeNeedlessEdges()
   * @see #removeNeedlessGateways()
   * @see #uniteConsecutiveGateways()
   */
  private boolean removeNeedlessEdgesAndGatewaysAndUniteConsecutiveGateways() {
    boolean changed = removeNeedlessEdges();
    changed |= removeNeedlessGateways();
    changed |= uniteConsecutiveGateways();
    return changed;
  }

  /**
   * Illegal means: its source is no instantiating gateway and it does not come
   * from the outside of a SubProcess into it.
   */
  private void removeIllegalIncomingSequenceFlowToStartEvents() {
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isStartEvent(node)) {
        correctIncomingSequenceFlowFor((StartEvent) node);
      }
    }
  }

  private void correctIncomingSequenceFlowFor(StartEvent start) {
    Cluster cluster = model.getClusterForNode(start);
    for (ProcessEdge edge : model.getIncomingEdges(SequenceFlow.class, start)) {
      if (!(isSubProcess(cluster)
              && !model.getClusterForNode(edge.getSource()).equals(cluster))
              && !Utils.isInstantiatingGateway(edge.getSource())) {
        model.removeEdge(edge);
      }
    }
  }

  /**
   * removes nodes, that have no incoming SequenceFlow and are not StartEvents,
   * InstantiatingGateways or catching LinkIntermediateEvents
   */
  private boolean removeNodesWithoutIncomingSequenceFlow() {
    Collection<ProcessNode> nodesWithoutIncomingFlow =
            Utils.relevantNodesWithoutIncomingSequenceFlow(model);
    boolean removed = false;
    for (ProcessNode node : nodesWithoutIncomingFlow) {
      if (!(Utils.isInstantiatingGateway(node)
              || Utils.isCatchingLinkIntermediateEvent(node)
              || Utils.isStartEvent(node))) {
        model.removeNode(node);
        removed = true;
      }
    }
    return removed;
  }

  /**
   * an edge is considered needless, iff:<br />
   * - its source and target are gateways of the same type (or one is Exclusive
   * and the other event-based) or its target is an InclusiveGateway AND there is
   * another edge with the same source and target<br />
   * or<br />
   * - its source and target is a parallel gateway and there exists another path
   * the source to the target (this path may involve more than one edge=
   */
  private boolean removeNeedlessEdges() {
    Collection<ProcessEdge> edgesToBeRemoved = new HashSet<ProcessEdge>();
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isGateway(node)) {
        Map<ProcessNode, Collection<ProcessEdge>> edgesToNode =
                sortEgesAccordingToTarget(model.getOutgoingEdges(SequenceFlow.class, node));
        edgesToBeRemoved.addAll(collectNeedlessEdges(edgesToNode, node));
        if (Utils.isParallelGateway(node)) {
          edgesToBeRemoved.addAll(collectNeedlessParallelWays(edgesToNode, node));
        }
      }
    }
    for (ProcessEdge edge : edgesToBeRemoved) {
      model.removeEdge(edge);
    }
    return !edgesToBeRemoved.isEmpty();
  }

  /**
   * creates a map, that maps from a ProcessNode to the edges, that lead to this
   * node (out of the nodes, that are supplied in the parameter)
   */
  private Map<ProcessNode, Collection<ProcessEdge>> sortEgesAccordingToTarget(
          Collection<ProcessEdge> edges) {
    Map<ProcessNode, Collection<ProcessEdge>> edgesToNode =
            new HashMap<ProcessNode, Collection<ProcessEdge>>();
    for (ProcessEdge edge : edges) {
      if (!edgesToNode.containsKey(edge.getTarget())) {
        edgesToNode.put(edge.getTarget(), new HashSet<ProcessEdge>());
      }
      edgesToNode.get(edge.getTarget()).add(edge);
    }
    return edgesToNode;
  }

  /**
   * collects, out of a set of edges, which are needless
   * @param edgesToNode a mapping from edges´ targets to ProcessEdges leading to
   * this edge from node
   * @param node the edges´ origin
   * @return the edges, out of the supplied edges, where an edge with same source
   * and target exists, that makes this edge needless
   */
  private Collection<ProcessEdge> collectNeedlessEdges(
          Map<ProcessNode, Collection<ProcessEdge>> edgesToNode, ProcessNode node) {
    Collection<ProcessEdge> needlessEdges = new HashSet<ProcessEdge>();
    for (Map.Entry<ProcessNode, Collection<ProcessEdge>> entry : edgesToNode.entrySet()) {
      if (entry.getValue().size() > 1
              && (Utils.sameType(entry.getKey(), node)
              || Utils.isInclusiveGateway(entry.getKey())
              || (Utils.isEventBasedGateway(node)
              && Utils.isExclusiveGateway(entry.getKey()))
              || (Utils.isEventBasedGateway(entry.getKey())
              && Utils.isExclusiveGateway(node))
              || ((Utils.isExclusiveGateway(node)
              || Utils.isEventBasedGateway(node))
              && !Utils.isGateway(entry.getKey())))) {
        entry.getValue().remove(entry.getValue().iterator().next());
        needlessEdges.addAll(entry.getValue());
      }
    }
    return needlessEdges;
  }

  /**
   * collects edges, that have ParallelGateways as source and target, between
   * which another way in the same direction exists, which contains at least one node.
   * @param edgesToNode a mapping from edges´ targets to ProcessEdges leading to
   * this edge from node
   * @param node the edges´ origin
   */
  private Collection<ProcessEdge> collectNeedlessParallelWays(
          Map<ProcessNode, Collection<ProcessEdge>> edgesToNode, ProcessNode node) {
    Collection<ProcessEdge> needlessEdges = new HashSet<ProcessEdge>();
    for (Map.Entry<ProcessNode, Collection<ProcessEdge>> entry : edgesToNode.entrySet()) {
      if (Utils.isParallelGateway(entry.getKey())) {
        if (((new TreeBuilder(model)).buildTreeFor(node,
                TreeBuilder.FlowDirection.flowAfter)).eraseFirstNode().
                containsOnIndirectWay(entry.getKey())) {
          //the condition states containsOnIndirectWay in order to  exclude the
          //currently examined edge
          needlessEdges.addAll(entry.getValue());
        }
      }
    }
    return needlessEdges;
  }

  /**
   * removes gateways, that have at most one incoming and one outgoing edge
   */
  private boolean removeNeedlessGateways() {
    Collection<ProcessNode> nodesToBeRemoved = new HashSet<ProcessNode>();
    for (ProcessNode node : model.getNodes()) {
      Collection<ProcessEdge> incomingEdges =
              model.getIncomingEdges(SequenceFlow.class, node),
              outgoingEdges =
              model.getOutgoingEdges(SequenceFlow.class, node);
      if (Utils.isGateway(node)
              && incomingEdges.size() <= 1
              && outgoingEdges.size() <= 1) {
        nodesToBeRemoved.add(node);
        bypassNeedlessGateway(incomingEdges, outgoingEdges);
      }
    }
    removeAll(nodesToBeRemoved);
    return !nodesToBeRemoved.isEmpty();
  }

  private void removeAll(Collection<ProcessNode> nodes) {
    for (ProcessNode node : nodes) {
      model.removeNode(node);
    }
  }

  /**
   * bypasses a node, that has at most one incoming and at most one outgoing
   * SequenceFlow
   * @param incomingEdges collection of all incomming edges; may not contain
   * more than one element
   * @param outgoingEdges collection of all outgoing edges; may not contain
   * more than one element
   */
  private void bypassNeedlessGateway(Collection<ProcessEdge> incomingEdges,
          Collection<ProcessEdge> outgoingEdges) {
    if (!(incomingEdges.isEmpty() || outgoingEdges.isEmpty())) {
      incomingEdges.iterator().next().setTarget(
              outgoingEdges.iterator().next().getTarget());
      model.removeEdge(outgoingEdges.iterator().next());
    } else if (!incomingEdges.isEmpty()) {
      model.removeEdge(incomingEdges.iterator().next());
    } else if (!outgoingEdges.isEmpty()) {
      model.removeEdge(outgoingEdges.iterator().next());
    }
  }

  /**
   * unites gateways, that directly follow each other, if this can be done without
   * changing the diagram´s semantics (i.e. they have the same type (or one is
   * exclusive and the other is event based) and the first of the gateways is
   * only a join or the second is only a split)
   */
  private boolean uniteConsecutiveGateways() {
    Collection<ProcessNode> nodesToBeRemoved = new HashSet<ProcessNode>();
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isGateway(node) && !nodesToBeRemoved.contains(node)) {
        uniteWithFollowingGateways(node, nodesToBeRemoved);
      }
    }
    for (ProcessNode node : nodesToBeRemoved) {
      model.removeNode(node);
    }
    return !nodesToBeRemoved.isEmpty();
  }

  /**
   * unites node with those directly following gateways, with wich it can be
   * united without changing the diagram´s semantics.
   * @see #uniteConsecutiveGateways()
   * @see #uniteable(ProcessNode)
   */
  private void uniteWithFollowingGateways(
          ProcessNode node, Collection<ProcessNode> nodesToBeRemoved) {
    Collection<ProcessEdge> outgoing = model.getOutgoingEdges(SequenceFlow.class, node);
    for (ProcessEdge edge : outgoing) {
      if (uniteable(node, edge.getTarget())
              && !(nodesToBeRemoved.contains(edge.getTarget())
              || nodesToBeRemoved.contains(node))
              && (model.getIncomingEdges(SequenceFlow.class, edge.getTarget()).
              size() == 1
              || model.getOutgoingEdges(SequenceFlow.class, node).
              size() == 1)) {
        nodesToBeRemoved.add(unite(node, edge.getTarget()));
      }
    }
  }

  /**
   * checks, wheter two gateways can be united without changing semantics of the
   * model
   */
  private boolean uniteable(ProcessNode node1, ProcessNode node2) {
    if (Utils.sameType(node1, node2) && !Utils.isInstantiatingGateway(node1)) {
      return true;
    } else if (Utils.isExclusiveGateway(node1)) {
      return Utils.isEventBasedGateway(node2);
      //if node2 is ExclusiveGateway, true has already been returned
    } else if (Utils.isInstantiatingGateway(node1)
            && Utils.isEventBasedGateway(node2)) {
      if (!Utils.isInstantiatingGateway(node2)) {
        return node1.getProperty(EventBasedGateway.PROP_INSTANTIATE).
                equals(EventBasedGateway.TYPE_INSTANTIATE_EXCLUSIVE);
      } else {
        return node1.getProperty(EventBasedGateway.PROP_INSTANTIATE).
                equals(node2.getProperty(EventBasedGateway.PROP_INSTANTIATE));
      }
    } else {
      //if node1 is usual EventBasedGateway and node2 ExclusiveGateway,
      //they will be treated by EBGWCorrector
      return false;
    }
  }

  /**
   * unites two nodes (does no check, wheter this is possible without changing
   * semantics
   */
  private ProcessNode unite(ProcessNode node1, ProcessNode node2) {
    if (Utils.isEventBasedGateway(node2)
            && !Utils.isEventBasedGateway(node1)) {
      return unite(node2, node1);
    } else {
      node1.setText(node1.getText() + ", " + node2.getText());
      for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, node2)) {
        if (!edge.getTarget().equals(node1)) {
          edge.setSource(node1);
        }
      }
      for (ProcessEdge edge : model.getIncomingEdges(SequenceFlow.class, node2)) {
        if (!edge.getSource().equals(node1)) {
          edge.setTarget(node1);
        }
      }
      return node2;
    }
  }

  /**
   * removes implicit Joins and Joins at Event-based Gateways
   */
  private void removeImplicitJoins() {
    Collection<ProcessNode> modelNodes = new HashSet<ProcessNode>(model.getNodes());
    for (ProcessNode node : modelNodes) {
      if (!Utils.isGateway(node) || Utils.isEventBasedGateway(node)) {
        Collection<ProcessEdge> incomingEdges =
                model.getIncomingEdges(SequenceFlow.class, node);
        if (incomingEdges.size() > 1) {
          insertNewExclusiveGatewayBefore(node, incomingEdges);
        }
      }
    }
  }

  /**
   * will insert a new exclusive gateway before node (i.e. creates a new
   * ExclusiveGateway in the same cluster and routs all incoming SequenceFlow
   * of node to the new gateway)
   * @param incomingSequenceFlow the incoming SequenceFlow-edges of node
   */
  private void insertNewExclusiveGatewayBefore(ProcessNode node,
          Collection<ProcessEdge> incomingSequenceFlow) {
    Gateway gateway = new ExclusiveGateway();
    model.addNode(gateway);
    Cluster cluster = model.getClusterForNode(node);
    if (cluster != null) {
      cluster.addProcessNode(gateway);
    }
    model.addEdge(new SequenceFlow(gateway, node));
    for (ProcessEdge edge : incomingSequenceFlow) {
      edge.setTarget(gateway);
    }
  }

  private void removeImplicitSplits() {
    Collection<ProcessNode> modelNodes = new HashSet<ProcessNode>(model.getNodes());
    for (ProcessNode node : modelNodes) {
      if (!Utils.isGateway(node)) {
        Collection<ProcessEdge> outgoingEdges =
                model.getOutgoingEdges(SequenceFlow.class, node);
        if (outgoingEdges.size() > 1) {
          insertNewParallelGatewayAfter(node, outgoingEdges);
        }
      }
    }
  }

  /**
   * will insert a new parallel gateway after node (i.e. creates a new
   * ParallelGateway in the same cluster and changes the source of all outgoing
   * SequenceFlow of node to the new Gateway)
   * @param outgoingSequenceFlow the outgoing SequenceFlow-edges of node
   */
  private void insertNewParallelGatewayAfter(ProcessNode node,
          Collection<ProcessEdge> outgoingSequenceFlow) {
    Gateway gateway = new ParallelGateway();
    model.addNode(gateway);
    Cluster cluster = model.getClusterForNode(node);
    if (cluster != null) {
      cluster.addProcessNode(gateway);
    }
    model.addEdge(new SequenceFlow(node, gateway));
    for (ProcessEdge edge : outgoingSequenceFlow) {
      edge.setSource(gateway);
    }
  }

  /**
   * A set of nodes S is an obsolete fragment, iff:<br />
   * -no node in S has any incoming/outgoing SequenceFlow from/to a node, that
   * is not contained in S and<br />
   * -S does not contain any of the following nodes: Activity, SubProcess,
   * MessageIntermediateEvent, MessageStartEvent, throwing IntermediateEvent,
   * non-empty Cluster<br /><br />
   *
   * Messages and EdgeDockers will not be removed
   */
  private boolean removeObsoleteFragments() {
    boolean changed = false;
    Collection<Collection<ProcessNode>> fragments = findFragments();
    for (Collection<ProcessNode> fragment : fragments) {
      if (!containsValuableNodes(fragment)) {
        for (ProcessNode node : fragment) {
          if (!(Utils.isMessage(node) || isEdgeDocker(node))) {
            model.removeNode(node);
            changed = true;
          }
        }
      }
    }
    return changed;
  }

  private Collection<Collection<ProcessNode>> findFragments() {
    Collection<Collection<ProcessNode>> fragments = oneFragmentForEachNode();
    for (ProcessEdge edge : model.getEdges()) {
      fragments = uniteFragments(fragments, edge.getSource(), edge.getTarget());
    }
    return fragments;
  }

  /**
   * creates a Collection of Collections, that each contain exactly one
   * ProcessNode and that jointly contain all ProcessNodes of the model.
   */
  private Collection<Collection<ProcessNode>> oneFragmentForEachNode() {
    Collection<Collection<ProcessNode>> fragments =
            new HashSet<Collection<ProcessNode>>();
    for (ProcessNode node : model.getNodes()) {
      Collection<ProcessNode> current = new HashSet<ProcessNode>();
      current.add(node);
      fragments.add(current);
    }
    return fragments;
  }

  /**
   * unites the collections, that contain node1 or node2
   */
  private Collection<Collection<ProcessNode>> uniteFragments(
          Collection<Collection<ProcessNode>> fragments,
          ProcessNode node1, ProcessNode node2) {
    Collection<Collection<ProcessNode>> newFragments =
            new HashSet<Collection<ProcessNode>>();
    Collection<ProcessNode> unitedFragment = new HashSet<ProcessNode>();
    for (Collection<ProcessNode> fragment : fragments) {
      if (fragment.contains(node1) || fragment.contains(node2)) {
        unitedFragment.addAll(fragment);
      } else {
        newFragments.add(fragment);
      }
    }
    newFragments.add(unitedFragment);
    return newFragments;
  }

  private boolean containsValuableNodes(Collection<ProcessNode> fragment) {
    for (ProcessNode node : fragment) {
      if (isActivity(node)
              || Utils.isMessageIntermediateEvent(node)
              || Utils.isMessageStartEvent(node)
              || Utils.isThrowingIntermediateEvent(node)
              || Utils.isNonEmptyCluster(node)) {
        return true;
      }
    }
    return false;
  }

  /**
   * tries to change the model in a way that all EventBasedGateways are directly
   * followed only by IntermediateEvents
   */
  private void pullEventsToEventBasedGateways() {
    Collection<EventBasedGateway> gateways = allEventBasedGateways();
    while (!gateways.isEmpty()) {
      for (Iterator<EventBasedGateway> iter = gateways.iterator(); iter.hasNext();) {
        EventBasedGateway gateway = iter.next();
        if (!isAcyclicallyFollowedByAnyOf(gateway, gateways)) {
          (new EventBasedGatewayCorrector(gateway, model)).correct();
          iter.remove();
        }
      }
    }
  }


  private Collection<EventBasedGateway> allEventBasedGateways() {
    Collection<EventBasedGateway> gateways = new HashSet<EventBasedGateway>();
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isEventBasedGateway(node)) {
        gateways.add((EventBasedGateway) node);
      }
    }
    return gateways;
  }

  /**
   * checks for an EventBasedGateway node wheter it is followed by any of the
   * specified EventBasedGateways, that is not itself followed by node.
   */
  private boolean isAcyclicallyFollowedByAnyOf(
          EventBasedGateway node, Collection<EventBasedGateway> any) {
    TreeBuilder builder = new TreeBuilder(model);
    Collection<EventBasedGateway> followingEBGWs =
            (builder.buildTreeFor(node, TreeBuilder.FlowDirection.flowAfter)).
            getAllEventBasedGateways();
    for (EventBasedGateway gateway : followingEBGWs) {
      if (any.contains(gateway)
              && !(builder.buildTreeFor(gateway, TreeBuilder.FlowDirection.flowAfter)).
                  contains(node)) {
        return true;
      }
    }
    return false;
  }

  /**
   * eliminates unnecesarily duplicated Message- and TimerIntermediateEvents
   */
  private boolean eliminateDuplicates() {
    return (new DuplicateEliminator(model)).eliminateDuplicates();
  }

  /**
   * generates timeouts after EventBasedGateways, where this is necessary
   */
  private void generateTimeouts() {
    Collection<EventBasedGateway> gateways = allEventBasedGateways();
    for (EventBasedGateway gateway : gateways) {
      if (!Utils.isInstantiatingGateway(gateway)) {
        generateTimeouts(gateway);
      }
    }
  }

  /**
   * generates timeouts on outgoing edges of this gateway, if and where necessary
   */
  private void generateTimeouts(EventBasedGateway gateway) {
    for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, gateway)) {
      if (!(Utils.isActivity(edge.getTarget())
              || Utils.isEvent(edge.getTarget()))) {
        if (!((new TreeBuilder(model)).buildTreeFor(gateway, TreeBuilder.FlowDirection.flowAfter)).allAlternativesContainMessageReceive()) {
          generateTimeoutOnEdgeFor(edge, gateway);
        }
      }
    }
  }

  private void generateTimeoutOnEdgeFor(ProcessEdge edge, Gateway gateway) {
    TimerIntermediateEvent timeout = new TimerIntermediateEvent();
    timeout.setText("Timeout");
    timeout.setPos(edge.getTarget().getPos());
    model.addNode(timeout);
    Cluster cluster = model.getClusterForNode(gateway);
    if (cluster != null) {
      cluster.addProcessNode(timeout);
    }
    edge.setSource(timeout);
    model.addEdge(new SequenceFlow(gateway, timeout));
  }

  private boolean isSubProcess(ProcessNode node) {
    return node instanceof SubProcess;
  }

  private boolean isActivity(ProcessNode node) {
    return node instanceof Activity || node instanceof SubProcess;
  }

  private boolean isEdgeDocker(ProcessNode node) {
    return node instanceof EdgeDocker;
  }
}
