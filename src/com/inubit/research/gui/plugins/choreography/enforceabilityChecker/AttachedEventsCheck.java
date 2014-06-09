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
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Checks, that attached events have no incomming sequence flow
 * @author tmi
 */
public class AttachedEventsCheck extends AbstractChoreographyCheck {
  private static final String DESC_NoIncomingFlow ="An attached even must not" +
          " have incoming sequence flow.";

  public AttachedEventsCheck(BPMNModel model) {
    super(model);
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>();
    classes.add(IntermediateEvent.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if(Utils.isIntermediateEvent(object)) {
      return checkIntermediateEvent((IntermediateEvent) object);
    } else {
      return new HashSet<EnforceabilityProblem>();
    }
  }

  private Collection<EnforceabilityProblem> checkIntermediateEvent(
          IntermediateEvent event) {
    Collection<EnforceabilityProblem> problems = new HashSet<EnforceabilityProblem>();
    if(Utils.isAttached(event, model)) {
      Collection<ProcessEdge> incomingEdges =
              model.getIncomingEdges(SequenceFlow.class, event);
      if(!incomingEdges.isEmpty()) {
        problems.add(new EnforceabilityProblem(
                event, incomingEdges, DESC_NoIncomingFlow));
      }
    }
    return problems;
  }
}
