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
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.CancelEndEvent;
import net.frapu.code.visualization.bpmn.CancelIntermediateEvent;
import net.frapu.code.visualization.bpmn.CompensationEndEvent;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.ErrorEndEvent;
import net.frapu.code.visualization.bpmn.ErrorIntermediateEvent;
import net.frapu.code.visualization.bpmn.ErrorStartEvent;
import net.frapu.code.visualization.bpmn.EscalationEndEvent;
import net.frapu.code.visualization.bpmn.EscalationIntermediateEvent;
import net.frapu.code.visualization.bpmn.EscalationStartEvent;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageEndEvent;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.MultipleEndEvent;
import net.frapu.code.visualization.bpmn.SignalEndEvent;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 * Checks, that only nodes, that are allowed in ChoreographyDiagrams occur in
 * this BPMNModel.
 * @author tmi
 */
public class ForbiddenNodesCheck extends AbstractChoreographyCheck {
  private static final String DESC_ForbiddenNode =
          "This type of node is not allowed in choreography diagrams";
  private static final String DESC_OnlyAttached =
          "Only allowed as an attached event";
  private static final String DESC_NotAttached =
          "Not allowed as an attached Event";
  private static final String DESC_OnlyAttachedToTask =
          "May only be attached to a Choreography Activity";

  public ForbiddenNodesCheck(BPMNModel model){
    super(model);
  }

  /**
   * checks, wheter one node is forbidden to occur in a Choreography diagram
   * @return true, if the node is forbidden.
   */
  private boolean isForbiddenNode(ProcessNode node){
    if(node instanceof Activity || node instanceof SubProcess
            || node instanceof CancelEndEvent
            || node instanceof CompensationEndEvent
            || node instanceof ErrorEndEvent
            || node instanceof EscalationEndEvent
            || node instanceof MessageEndEvent
            || node instanceof MultipleEndEvent
            || node instanceof SignalEndEvent
            || node instanceof MessageStartEvent
            || node instanceof ErrorStartEvent
            || node instanceof ErrorIntermediateEvent
            || node instanceof EscalationIntermediateEvent
            || node instanceof EscalationStartEvent){
      return true;
    }
    return false;
  }

  /**
   * checks, that a specified node is allowed in Choreography diagrams and that
   * it is attached, if it must be attached and not attached if it must not
   * be attached.
   */
  public Collection<EnforceabilityProblem> checkNode(ProcessNode node) {
    if(isForbiddenNode(node)) {
      return (new EnforceabilityProblem(DESC_ForbiddenNode, node)).
              inCollection();
    }
    return checkPosition(node);
  }

  /**
   * checks, that a node´s attached-state is allowed.
   */
  private Collection<EnforceabilityProblem> checkPosition(ProcessNode node) {
    if(node.getProperty(IntermediateEvent.PROP_CLASS_TYPE).
            equals("net.frapu.code.visualization.bpmn.IntermediateEvent")) {
      if(((AttachedNode)node).getParentNode(model) != null){
        return (new EnforceabilityProblem(DESC_NotAttached, node)).inCollection();
      }
    }
    if(node instanceof MessageIntermediateEvent
            || node instanceof CancelIntermediateEvent
            || node instanceof CompensationIntermediateEvent){
      if(((AttachedNode)node).getParentNode(model) == null) {
        return (new EnforceabilityProblem(DESC_OnlyAttached, node)).inCollection();
      }
      if(!Utils.isChoreographyTask(((AttachedNode)node).getParentNode(model))) {
        return (new EnforceabilityProblem(DESC_OnlyAttachedToTask, node,
                ((AttachedNode)node).getParentNode(model))).inCollection();
      }
    }
    return new HashSet<EnforceabilityProblem>();
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>();
    classes.add(ProcessNode.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if(! (object instanceof ProcessNode)) return new HashSet<EnforceabilityProblem>();
    return checkNode((ProcessNode)object);
  }

}
