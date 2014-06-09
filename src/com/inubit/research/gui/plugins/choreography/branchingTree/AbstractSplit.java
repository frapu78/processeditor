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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;

//Also refer to the base-class (BranchingTree) for Javadoc
/**
 * This is an abstract superclass for BranchingTree-nodes, that represent a
 * split.
 * @author tmi
 */
abstract class AbstractSplit extends BranchingTree {

  /**
   * the pathes emerging from this BranchingTree-node
   */
  protected java.util.List<BranchingTree> pathes;
  /**
   * Will be true, if, due to a call to eraseFirstNode or
   * eraseFirstChoreographyActivity, the tree needs to pretend the none-existence
   * of the ProcessNode cotained in this BranchingTree-node.
   */
  protected boolean isErased;

  public AbstractSplit(BranchingTree parent) {
    super(parent);
    pathes = new LinkedList<BranchingTree>();
  }

  public AbstractSplit(BranchingTree parent, ProcessNode node) {
    super(parent, node);
    pathes = new LinkedList<BranchingTree>();
  }

  public void addPath(BranchingTree path) {
    pathes.add(path);
  }

  protected abstract boolean isParallel();

  /**
   * sets the property EventBasedGateway.PROP_INSTANTIATE for the supplied gateway
   * according to the split type
   */
  protected abstract void setInstantiatingProperty(EventBasedGateway gateway);

  @Override
  public Collection<ProcessNode> activitiesWithParticipant(String participant) {
    Collection<ProcessNode> activities = new HashSet<ProcessNode>();
    if (!isErased && isParticipant(participant)) {
      activities.add(getNode());
    }
    for (BranchingTree next : pathes) {
      activities.addAll(next.activitiesWithParticipant(participant));
    }
    return activities;
  }

  @Override
  public boolean contains(BranchingTree tree) {
    if (equals(tree)) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (path.contains(tree)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean contains(ProcessNode node) {
    if (!isErased && getNode().equals(node)) {
      return true;
    }
    for (BranchingTree tree : pathes) {
      if (tree.contains(node)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsOnIndirectWay(ProcessNode node) {
    if (!isErased && getNode().equals(node)) {
      return false;//was a direct way
    } else {//if this node is not erased, the node can no more occur on a direct way
      for (BranchingTree path : pathes) {
        if (isErased && path.containsOnIndirectWay(node)) {
          return true;
        }
        if (!isErased && path.contains(node)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public BranchingTree eraseFirstNode() {
    if (!isErased) {
      isErased = true;
    } else {
      List<BranchingTree> newPathes = new LinkedList<BranchingTree>();
      for (BranchingTree path : pathes) {
        newPathes.add(path.eraseFirstNode());
      }
      pathes = newPathes;
    }
    return this;
  }

  @Override
  public BranchingTree eraseFirstChoreographyActivity() {
    if (!isErased && Utils.isChoreographyActivity(getNode())) {
      isErased = true;
    } else {
      List<BranchingTree> newPathes = new LinkedList<BranchingTree>();
      for (BranchingTree path : pathes) {
        newPathes.add(path.eraseFirstChoreographyActivity());
      }
      pathes = newPathes;
    }
    return this;
  }

  @Override
  public Set<ProcessNode> firstNodesOf(String participant) {
    Set<ProcessNode> result = new HashSet<ProcessNode>();
    if (isParticipant(participant)) {
      result.add(getNode());
    } else {
      for (BranchingTree path : pathes) {
        result.addAll(path.firstNodesOf(participant));
      }
    }
    return result;
  }

  @Override
  public Collection<String> getParticipants() {
    Collection<String> participants = new HashSet<String>();
    for (BranchingTree path : pathes) {
      participants.addAll(path.getParticipants());
    }
    if (!isErased) {
      participants.addAll(Utils.participantsOf(getNode()));
    }
    return participants;
  }

  @Override
  public Collection<ProcessNode> nextRealizedNodes(
          Map<String, Map<String, ProcessNode>> realizedNodes, String participant) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
    if (!isErased && containsKey(realizedNodes, getNode().getId(), participant)) {
      nodes.add(realizedNodes.get(getNode().getId()).get(participant));
      return nodes;
    } else {
      for (BranchingTree path : pathes) {
        nodes.addAll(path.nextRealizedNodes(realizedNodes, participant));
      }
      return nodes;
    }
  }

  @Override
  public boolean noPathesContainNonEmptyStartEvent() {
    if (!isErased
            && Utils.isStartEvent(getNode()) //is possible: implicit split-/join-behavior
            && !Utils.isEmptyStartEvent(getNode())) {
      return false;
    }
    for (BranchingTree path : pathes) {
      if (!path.noPathesContainNonEmptyStartEvent()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean noAlternativesInvolve(String participant) {
    if (!isErased && isParticipant(participant)) {
      return false;
    }
    for (BranchingTree tree : pathes) {
      if (!tree.noAlternativesInvolve(participant)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Map<String, Collection<BranchingTree>> processNodeOccurenceMap() {
    Map<String, Collection<BranchingTree>> occurences =
            new HashMap<String, Collection<BranchingTree>>();
    for (BranchingTree path : pathes) {
      occurences.putAll(path.processNodeOccurenceMap());
    }
    if (!isErased) {
      if (!occurences.containsKey(getNode().getId())) {
        occurences.put(getNode().getId(), new HashSet<BranchingTree>());
      }
      occurences.get(getNode().getId()).add(this);
    }
    return occurences;
  }

  /**
   * In a split-node, setNext will add a path to the split.
   */
  @Override
  public void setNext(BranchingTree next) {
    addPath(next);
  }

  @Override
  public boolean trimAndEliminateToEndingAtNode(ProcessNode node) {
    if (!isErased && getNode().equals(node)) {
      pathes.clear();
      return false;
    } else {
      for (Iterator<BranchingTree> iter = pathes.iterator(); iter.hasNext();) {
        if (iter.next().trimAndEliminateToEndingAtNode(node)) {
          iter.remove();
        }
      }
      return pathes.isEmpty();
    }
  }

  @Override
  protected boolean synchronizesWithOneOf(Collection<BranchingTree> otherPathes,
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    for (BranchingTree path : pathes) {
      if (!path.synchronizesWithOneOf(otherPathes, processNodeOccurences)) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public Collection<ProcessNode> parallelGatewaysBeforeFirstParticipationOf(
          Collection<String> participants) {
    Collection<String> participantsWithoutMyself = new HashSet<String>(participants);
    participantsWithoutMyself.removeAll(Utils.participantsOf(getNode()));
    Collection<ProcessNode> result = new HashSet<ProcessNode>();
    if (!participantsWithoutMyself.isEmpty()) {
      for (BranchingTree path : pathes) {
        if (path.getParticipants().containsAll(participants)) {
          result.addAll(path.parallelGatewaysBeforeFirstParticipationOf(participants));
        }
      }
      if (isParallel()) {
        result.add(getNode());
      }
    }
    return result;
  }

  @Override
  public ProcessNode createInstantiatingGateways(String participant,
          BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    if (!isErased && isParticipant(participant)) {
      return (new ActivityNode(getParent(), getNode())).createInstantiatingGateways(
              participant, model, startEvents, firstNode, pools);
      //I know, that this is a very cruel workaround, but this is truly a
      //last-second bugfix.
    } else {
      return createInstantiatingSplitGateway(
              participant, model, startEvents, firstNode, pools);
    }
  }

  /**
   * creates an instantiating gateway for this node.
   */
  private ProcessNode createInstantiatingSplitGateway(String participant,
          BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    EventBasedGateway gateway = new EventBasedGateway();
    setInstantiatingProperty(gateway);
    model.addNode(gateway);
    pools.get(participant).addProcessNode(gateway);
    for (BranchingTree path : pathes) {
      ProcessNode start = path.createInstantiatingGateways(
              participant, model, startEvents, firstNode, pools);
      if (start != null) {
        model.addEdge(new SequenceFlow(gateway, start));
      }
    }
    return gateway;
  }

  @Override
  public Collection<EventBasedGateway> getAllEventBasedGateways() {
    Collection<EventBasedGateway> gateways = new HashSet<EventBasedGateway>();
    if (Utils.isEventBasedGateway(getNode())) {
      gateways.add((EventBasedGateway) getNode());
    }
    for (BranchingTree path : pathes) {
      gateways.addAll(path.getAllEventBasedGateways());
    }
    return gateways;
  }
}
