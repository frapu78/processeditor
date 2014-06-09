/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography.branchingTree;

import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author tmi
 */
class ParallelSplit extends AbstractSplit {

  public ParallelSplit(BranchingTree parent) {
    super(parent);
  }

  public ParallelSplit(BranchingTree parent, NodeAdaptor node) {
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
    if (!isErased && getNode().isChoreographyActivity()) {
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
    if (!isErased && getNode().isChoreographyActivity()) {
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
  public boolean allPathesContainOneOf(Collection<NodeAdaptor> nodes) {
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
    if (isErased || !getNode().isParallelGateway()) {
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
  public boolean allParallelPathesSynchronizeBefore(NodeAdaptor node) {
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
          NodeAdaptor before) {
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
  protected boolean isParallel() {
    return true;
  }
}
