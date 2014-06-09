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
import java.util.Map;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.StartEvent;

/**
 * @author tmi
 */
class ParallelSplit extends AbstractSplit {

  public ParallelSplit(BranchingTree parent) {
    super(parent);
  }

  public ParallelSplit(BranchingTree parent, ProcessNode node) {
    super(parent, node);
  }

  @Override
  public void closePath(BranchingTree from) {
    getParent().closePath(this);
  }
  
  @Override
  public boolean allAlternativesInvolve(String participant) {
    if (!isErased && isParticipant(participant)) {
      return true;
    }
    for (BranchingTree tree : pathes) {
      if (tree.allAlternativesInvolve(participant)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean removeClosedPathes() {
    for (BranchingTree path : pathes) {
      if (path.removeClosedPathes()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    if (!isErased && Utils.isChoreographyActivity(getNode())) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (path.allAlternativesContainChoreographyActivities()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    if (!isErased && Utils.isChoreographyActivity(getNode())) {
      for (BranchingTree path : pathes) {
        if (path.allAlternativesContainChoreographyActivities()) {
          return true;
        }
      }
    } else {
      for (BranchingTree path : pathes) {
        if (path.allAlternativesContainMultipleChoreographyActivities()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean allPathesContainOneOf(Collection<ProcessNode> nodes) {
    if (!isErased && nodes.contains(getNode())) {
      return true;
    }
    Collection<BranchingTree> doContain = new HashSet<BranchingTree>(),
            doNotContain = new HashSet<BranchingTree>();
    for (BranchingTree path : pathes) {
      if (path.allPathesContainOneOf(nodes)) {
        doContain.add(path);
      } else {
        doNotContain.add(path);
      }
    }
    for (BranchingTree path : doNotContain) {
      if (!path.synchronizesWithOneOf(doContain, occurenceMap(doContain))) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  private Map<String, Collection<BranchingTree>> occurenceMap(
          Collection<BranchingTree> trees) {
    Map<String, Collection<BranchingTree>> occurences =
            new HashMap<String, Collection<BranchingTree>>();
    for (BranchingTree tree : trees) {
      for(Map.Entry<String, Collection<BranchingTree>> entry :
            tree.processNodeOccurenceMap().entrySet()) {
          if(occurences.containsKey(entry.getKey())) {
            occurences.get(entry.getKey()).addAll(entry.getValue());
          } else {
            occurences.put(entry.getKey(), entry.getValue());
          }
        }
    }
    return occurences;
  }

  @Override
  protected boolean synchronizesWithOneOf(
          Collection<BranchingTree> otherPathes,
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    if (synchronizesLocally(processNodeOccurences)) {
      return true;
    } else {
      return super.synchronizesWithOneOf(otherPathes, processNodeOccurences);
    }
  }

  private boolean synchronizesLocally(
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    if (isErased || !Utils.isParallelGateway(getNode())) {
      return false;
    }
    if (!processNodeOccurences.containsKey(getNode().getId())) {
      return false;
    }
    return ! locallySynchronizedBranches(processNodeOccurences).isEmpty();
  }

  private Collection<BranchingTree> locallySynchronizedBranches(
          Map<String, Collection<BranchingTree>> processNodeOccurences) {
    Collection<BranchingTree> synchronizedBranches =
            new HashSet<BranchingTree>();
    for (BranchingTree other : processNodeOccurences.get(getNode().getId())) {
      if (!other.getParent().getNode().equals(getParent().getNode())) {
        synchronizedBranches.add(other);
      }
    }
    return synchronizedBranches;
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    if(! isErased
            && (isParticipant(participant)
                || Utils.isNonEmptyStartEvent(getNode())
                || Utils.isNonEmptyIntermediateEvent(getNode()))) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (path.allAlternativesContainNonEmptyNonEndEventOrInvolve(participant)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(ProcessNode node) {
    if(getNode().equals(node)) return true;
    Map<String, Collection<BranchingTree>> occurenceMap = occurenceMap(pathes);
    for(BranchingTree path : pathes) {
      Collection<BranchingTree> otherPathes = new HashSet<BranchingTree>(pathes);
      otherPathes.remove(path);
      if(!path.synchronizesWithAllBeforeAndKeepsSynchronized(
              otherPathes, occurenceMap, node)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          ProcessNode before) {
    if(getNode().equals(before)) return branches.isEmpty();
    Collection<BranchingTree> unsynchronizedBranches =
            new HashSet<BranchingTree>(branches);
    removePathesContainingOneOfFrom(
            locallySynchronizedBranches(nodeOccurences),unsynchronizedBranches);
    if(unsynchronizedBranches.isEmpty()) {
      return synchronizesWithAllBeforeAndKeepsSynchronized(
              branches, nodeOccurences, before);//the "andKeepsSynchronized"-part
    } else {
      for(BranchingTree path : pathes) {
        if(!path.synchronizesWithAllBeforeAndKeepsSynchronized(
                unsynchronizedBranches, nodeOccurences, before)) {
          return false;
        }
      }
      return !pathes.isEmpty();
    }
  }

  private void removePathesContainingOneOfFrom(
          Collection<BranchingTree> oneOf, Collection<BranchingTree> from) {
    outer: for(Iterator<BranchingTree> iter = from.iterator(); iter.hasNext();) {
      BranchingTree node = iter.next();
      for(BranchingTree branch : oneOf) {
        if(node.contains(branch)) {
          iter.remove();
          break;
        }
      }
    }
  }

  @Override
  public ProcessNode createInstantiatingGateways(
          String participant, BPMNModel model,
          Map<String, Map<String, StartEvent>> startEvents,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Pool> pools) {
    if(definitelyHasPriorActivity(participant)) {
      return null;
    }
    else {
      return super.createInstantiatingGateways(
              participant, model, startEvents, firstNode, pools);
    }
  }

  private boolean definitelyHasPriorActivity(String participant) {
    //a new tree must be built in order to pay attention to the joining behavior
    BranchingTree history = (new TreeBuilder(
            (BPMNModel)getNode().getContexts().iterator().next())).
            buildTreeFor(getNode(), TreeBuilder.FlowDirection.flowBefore);
    history.eraseFirstNode();//if I am the activity myself, it does not matter
    return history.allAlternativesInvolve(participant);
  }

  @Override
  protected boolean isParallel() {
    return true;
  }

  @Override
  protected void setInstantiatingProperty(EventBasedGateway gateway) {
    gateway.setProperty(EventBasedGateway.PROP_INSTANTIATE,
            EventBasedGateway.TYPE_INSTANTIATE_PARALLEL);
  }

  @Override
  public boolean allAlternativesContainMessageReceive() {
    if(!isErased && isReceive()) return true;
    for(BranchingTree path : pathes) {
      if(path.allAlternativesContainMessageReceive()) return true;
    }
    return false;
  }
}
