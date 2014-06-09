/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import com.inubit.research.gui.plugins.choreography.Utils;
import java.util.Collection;
import java.util.HashSet;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographyActivity;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;

/**
 * Checks the requirement, that the initiator of a task has to be a participant
 * of any preceding activity.
 * @author tmi
 */
public class FlowCheck extends AbstractChoreographyCheck {

  private static final String DESC_FlowDependency =
          "The initiator of any choreography activity must be a "
          + "participant of every preceding choreography activity";

  public FlowCheck(BPMNModel model) {
    super(model);
  }

  /**
   * checks all incoming SequenceFlow of one node.
   * @param node the node, which´s incoming SequenceFlow is to be checked.
   */
  private Collection<EnforceabilityProblem> checkSequenceFlowTo(ProcessNode node) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    for (ProcessNode source : precedingChoreographyActivities(node)) {
      problems.addAll(checkFlowFromTo(source, node));
    }
    return problems;
  }

  /**
   * checks, wheter a SequenceFlow-connection between two nodes would be ok.
   */
  private Collection<EnforceabilityProblem> checkFlowFromTo(
          ProcessNode source, ProcessNode target) {
    if (Utils.isChoreographyActivity(source)
            && Utils.isChoreographyActivity(target)) {
      Collection<ProcessNode> nodesWithoutParticipation =
              getFinalTasksWithoutParticipant(source, Utils.initiatorOf(target));
      if(! nodesWithoutParticipation.isEmpty()) {
        return (new EnforceabilityProblem(
                  DESC_FlowDependency,
                  target,
                  nodesWithoutParticipation)).inCollection();
      }
    }
    return new HashSet<EnforceabilityProblem>();
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>();
    classes.add(ChoreographyActivity.class);
    classes.add(ChoreographySubProcess.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if (!(object instanceof ChoreographyActivity
            || object instanceof ChoreographySubProcess)) {
      return new HashSet<EnforceabilityProblem>();
    }
    return checkSequenceFlowTo((ProcessNode) object);
  }
}
