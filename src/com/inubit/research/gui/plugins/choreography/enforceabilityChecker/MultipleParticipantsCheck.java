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
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;

/**
 * Checks, that the initial participant of a node is never an MI-participant.
 * @author tmi
 */
public class MultipleParticipantsCheck extends AbstractChoreographyCheck {

  private static final String DESC_InitiatorNotMultiple =
          "The initiator of any choreography activity must not be an MI-participant.";

  public MultipleParticipantsCheck(BPMNModel model) {
    super(model);
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>(2);
    classes.add(ChoreographyTask.class);
    classes.add(ChoreographySubProcess.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if(!Utils.isChoreographyActivity(object)) {
      return new HashSet<EnforceabilityProblem>(0);
    }
    return checkChoreographyActivity((ProcessNode)object);
  }

  /**
   * checks for one ChoreographyActivity, that the initiator of this activity
   * is no MI-participant.
   */
  private Collection<EnforceabilityProblem> checkChoreographyActivity(
          ProcessNode activity) {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    if(Utils.isMultipleParticipantOf(Utils.initiatorOf(activity), activity)) {
      problems.add(new EnforceabilityProblem(DESC_InitiatorNotMultiple, activity));
    }
    return problems;
  }
}
