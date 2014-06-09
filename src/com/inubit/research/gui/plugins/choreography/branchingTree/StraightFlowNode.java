/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.branchingTree;

import com.inubit.research.gui.plugins.choreography.Utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 *
 * @author tmi
 */
abstract class StraightFlowNode extends BranchingTree{
  protected BranchingTree next;

  public StraightFlowNode(BranchingTree parent) {
    super(parent);
  }

  public StraightFlowNode(BranchingTree parent, ProcessNode node) {
    super(parent, node);
  }

  protected abstract StartEvent correspondingStartEvent();

  @Override
  public void setNext(BranchingTree next) {
    this.next = next;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<ProcessNode> nodes) {
    if (nodes.contains(getNode())) {
      return true;
    } else {
      return next.allPathesContainOneOf(nodes);
    }
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(ProcessNode node) {
    if (getNode().equals(node)) {
      return false;
    } else {
      return next.allParallelPathesSynchronizeBefore(node);
    }
  }

  @Override
  public void closePath(BranchingTree from) {
    getParent().closePath(this);
  }

  @Override
  public boolean containsOnIndirectWay(ProcessNode node) {
    if (getNode().equals(node)) {
      return false;
    } else {
      return next.contains(node);
    }
  }

  @Override
  public BranchingTree eraseFirstNode() {
    next.setParent(getParent());
    return next;
  }

  protected StartEvent getStartEvent(
          String participant,
          BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    if (!startEvents.containsKey(getNode().getId())) {
      startEvents.put(getNode().getId(), new HashMap<String, StartEvent>());
    }
    if (startEvents.get(getNode().getId()).containsKey(participant)) {
      return startEvents.get(getNode().getId()).get(participant);
    } else {
      return createNewStartEvent(participant, model, startEvents, firstNode, pools);
    }
  }

  @Override
  public Collection<ProcessNode> nextRealizedNodes(
          Map<String, Map<String, ProcessNode>> realizedNodes,
          String participant) {
    if (containsKey(realizedNodes, getNode().getId(), participant)) {
      Collection<ProcessNode> nodes = new HashSet<ProcessNode>(1);
      nodes.add(realizedNodes.get(getNode().getId()).get(participant));
      return nodes;
    }
    return next.nextRealizedNodes(realizedNodes, participant);
  }

  @Override
  protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
    Map<String, Collection<BranchingTree>> occurences = next.processNodeOccurenceMap();
    if (!occurences.containsKey(getNode().getId())) {
      occurences.put(getNode().getId(), new HashSet<BranchingTree>());
    }
    occurences.get(getNode().getId()).add(this);
    return occurences;
  }

  @Override
  public boolean removeClosedPathes() {
    return next.removeClosedPathes();
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          ProcessNode before) {
    if (getNode().equals(before)) {
      return branches.isEmpty();
    } else {
      return next.synchronizesWithAllBeforeAndKeepsSynchronized(
              branches, nodeOccurences, before);
    }
  }

  @Override
  protected boolean synchronizesWithOneOf(
          Collection<BranchingTree> pathes,
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    return next.synchronizesWithOneOf(pathes, processNodeOccurences);
  }

  @Override
  public boolean trimAndEliminateToEndingAtNode(ProcessNode node) {
    if (getNode().equals(node)) {
      next = new EmptyBranchingTree(this);
      return false;
    } else {
      return next.trimAndEliminateToEndingAtNode(node);
    }
  }

  @Override
  public boolean contains(BranchingTree tree) {
    if (equals(tree)) {
      return true;
    } else {
      return next.contains(tree);
    }
  }

  @Override
  public boolean contains(ProcessNode node) {
    if (node.equals(getNode())) {
      return true;
    } else {
      return next.contains(node);
    }
  }

  private StartEvent createNewStartEvent(
          String participant,
          BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    StartEvent start = correspondingStartEvent();
    model.addNode(start);
    pools.get(participant).addProcessNode(start);
    model.addEdge(new SequenceFlow(start, firstNode.get(getNode().getId()).
            get(participant)));
    startEvents.get(getNode().getId()).put(participant, start);
    return start;
  }

  protected Set<ProcessNode> nodeInNewSet() {
    Set<ProcessNode> result = new HashSet<ProcessNode>();
    result.add(getNode());
    return result;
  }

  @Override
  public Collection<EventBasedGateway> getAllEventBasedGateways() {
    Collection<EventBasedGateway> gateways = next.getAllEventBasedGateways();
    if(Utils.isEventBasedGateway(getNode())) {
      gateways.add((EventBasedGateway)getNode());
    }
    return gateways;
  }

  @Override
  public boolean allAlternativesContainMessageReceive() {
    if(isReceive()) return true;
    else return next.allAlternativesContainMessageReceive();
  }
}
