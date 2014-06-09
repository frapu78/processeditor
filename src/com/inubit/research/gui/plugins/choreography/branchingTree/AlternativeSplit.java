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
import java.util.ListIterator;
import java.util.Map;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.EventBasedGateway;

/**
 *
 * @author tmi
 */
class AlternativeSplit extends AbstractSplit {

  /**
   * is true, if a call to closePath lead to closing to last path, that was left
   * and since then no pathes have been added.
   */
  private boolean isClosed = false;

  public AlternativeSplit(BranchingTree parent) {
    super(parent);
  }

  public AlternativeSplit(BranchingTree parent, ProcessNode node) {
    super(parent, node);
  }

  @Override
  public void closePath(BranchingTree from) {
    if (from == null) {
      if(pathes.isEmpty()) isClosed = true;
    } else {
      pathes.remove(from);
      if (pathes.isEmpty()) {
        isClosed = true;
      }
    }
  }

  @Override
  public boolean allAlternativesInvolve(String participant) {
    if (!isErased && isParticipant(participant)) {
      return true;
    }
    for (BranchingTree tree : pathes) {
      if (!tree.allAlternativesInvolve(participant)) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public void addPath(BranchingTree path) {
    super.addPath(path);
    isClosed = false;
  }

  @Override
  public boolean removeClosedPathes() {
    if (!(isClosed || pathes.isEmpty())) {
      for (ListIterator<BranchingTree> iter = pathes.listIterator(); iter.hasNext();) {
        if (iter.next().removeClosedPathes()) {
          iter.remove();
        }
      }
      isClosed = pathes.isEmpty();
    }
    return isClosed;
  }

  @Override
  public boolean allAlternativesContainChoreographyActivities() {
    if (!isErased && Utils.isChoreographyActivity(getNode())) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (!path.allAlternativesContainChoreographyActivities()) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public boolean allAlternativesContainMultipleChoreographyActivities() {
    if (!isErased && Utils.isChoreographyActivity(getNode())) {
      for (BranchingTree path : pathes) {
        if (!path.allAlternativesContainChoreographyActivities()) {
          return false;
        }
      }
    } else {
      for (BranchingTree path : pathes) {
        if (!path.allAlternativesContainMultipleChoreographyActivities()) {
          return false;
        }
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public boolean trimAndEliminateToEndingAtNode(ProcessNode node) {
    return isClosed || super.trimAndEliminateToEndingAtNode(node);
  }

  @Override
  public boolean allPathesContainOneOf(Collection<ProcessNode> nodes) {
    if (!isErased && nodes.contains(getNode())) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (!path.allPathesContainOneOf(nodes)) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public boolean allAlternativesContainNonEmptyNonEndEventOrInvolve(String participant) {
    if(!isErased
            && (isParticipant(participant)
                || Utils.isNonEmptyStartEvent(getNode())
                || Utils.isNonEmptyIntermediateEvent(getNode()))) {
      return true;
    }
    for (BranchingTree path : pathes) {
      if (!path.allAlternativesContainNonEmptyNonEndEventOrInvolve(participant)) {
        return false;
      }
    }
    return !pathes.isEmpty();
  }

  @Override
  public boolean allParallelPathesSynchronizeBefore(ProcessNode node) {
    for(BranchingTree path : pathes) {
      if(!path.allParallelPathesSynchronizeBefore(node)) return false;
    }
    return true;
  }

  @Override
  public boolean synchronizesWithAllBeforeAndKeepsSynchronized(
          Collection<BranchingTree> branches,
          Map<String, Collection<BranchingTree>> nodeOccurences,
          ProcessNode before) {
    if(getNode().equals(before)) {
      if(branches.isEmpty()) {
        return allParallelPathesSynchronizeBefore(before);
      }
      else {
        return false;
      }
    } else {
      for(BranchingTree path : pathes) {
        if(!path.synchronizesWithAllBeforeAndKeepsSynchronized(branches,
                nodeOccurences, before)) {
          return false;
        }
      }
      return !pathes.isEmpty();
    }
  }

  @Override
  protected boolean isParallel() {
    return true;
  }

  @Override
  protected void setInstantiatingProperty(EventBasedGateway gateway) {
    gateway.setProperty(EventBasedGateway.PROP_INSTANTIATE,
            EventBasedGateway.TYPE_INSTANTIATE_EXCLUSIVE);
  }

  @Override
  public boolean allAlternativesContainMessageReceive() {
    if(!isErased && isReceive()) return true;
    for(BranchingTree path : pathes) {
      if(!path.allAlternativesContainMessageReceive()) return false;
    }
    return !pathes.isEmpty();
  }
}
