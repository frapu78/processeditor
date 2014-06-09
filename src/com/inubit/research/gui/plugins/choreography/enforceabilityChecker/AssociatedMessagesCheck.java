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
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Message;

/**
 * Checks that at maximum one initial and one replying message is associated to
 * any ChoreographyActivity.
 * @author tmi
 */
public class AssociatedMessagesCheck extends AbstractChoreographyCheck{

  private static final String DESC_OnlyOneIntiating = "Each choreography task " +
          "must not have more than one associated initiating Message!";
  
  private static final String DESC_OnlyOneReply = "Each choreography task " +
          "must not have more than one associated replying Message!";

  public AssociatedMessagesCheck(BPMNModel model) {
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
    if(Utils.isChoreographyActivity(object)) {
      return checkActivity((ProcessNode) object);
    } else {
      return new HashSet<EnforceabilityProblem>();
    }
  }

  private Collection<EnforceabilityProblem> checkActivity(ProcessNode activity) {
    Collection<EnforceabilityProblem> problems = new HashSet<EnforceabilityProblem>();
    Collection<ProcessNode> initiating = initiatingMessages(activity),
            replies = replies(activity);
    if(initiating.size() > 1) {
      problems.add(new EnforceabilityProblem(DESC_OnlyOneIntiating, activity, initiating));
    }
    if(replies.size() > 1) {
      problems.add(new EnforceabilityProblem(DESC_OnlyOneReply, activity, replies));
    }
    return problems;
  }

  private Collection<ProcessNode> initiatingMessages(ProcessNode activity) {
    return messages(activity, true);
  }

  private Collection<ProcessNode> replies(ProcessNode activity) {
    return messages(activity, false);
  }

  private Collection<ProcessNode> messages(ProcessNode activity, boolean initiating) {
    String init = initiating ? Message.INITIATE_TRUE : Message.INITIATE_FALSE;
    Collection<ProcessNode> messages = new HashSet<ProcessNode>();
    for(ProcessNode node : model.getNeighbourNodes(Association.class, activity)) {
      if(Utils.isMessage(node) && node.getProperty(Message.PROP_INITIATE).equals(init)) {
        messages.add((Message)node);
      }
    }
    return messages;
  }
}
