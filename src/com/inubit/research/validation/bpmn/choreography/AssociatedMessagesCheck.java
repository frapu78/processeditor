/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.choreography;

import com.inubit.research.validation.bpmn.BPMNValidator;
import com.inubit.research.validation.bpmn.adaptors.ChoreographyNodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.Collection;
import java.util.HashSet;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.Message;

/**
 * Checks that at maximum one initial and one replying message is associated to
 * any ChoreographyActivity.
 * @author tmi
 */
public class AssociatedMessagesCheck extends AbstractChoreographyCheck {

    public AssociatedMessagesCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    /*@Override
    public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObjectAdaptor>> classes =
    new HashSet<Class<? extends ProcessObjectAdaptor>>(2);
    classes.add(ChoreographyTask.class);
    classes.add(ChoreographySubProcess.class);
    return classes;
    }*/
    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isChoreographyActivity()) {
            checkActivity((ChoreographyNodeAdaptor) node);
        }
    }

    private void checkActivity(ChoreographyNodeAdaptor activity) {
        Collection<NodeAdaptor> initiating = initiatingMessages(activity),
                replies = replies(activity);
        if (initiating.size() > 1) {
            validator.addMessage("multipleInitiatingMessages",
                    activity.asNodeAdaptor(), initiating);
        }
        if (replies.size() > 1) {
            validator.addMessage("multipleReplyingMessages",
                    activity.asNodeAdaptor(), replies);
        }
    }

    private Collection<NodeAdaptor> initiatingMessages(ChoreographyNodeAdaptor activity) {
        return messages(activity, true);
    }

    private Collection<NodeAdaptor> replies(ChoreographyNodeAdaptor activity) {
        return messages(activity, false);
    }

    private Collection<NodeAdaptor> messages(
            ChoreographyNodeAdaptor activity, boolean initiating) {
        String init = initiating ? Message.INITIATE_TRUE : Message.INITIATE_FALSE;
        Collection<NodeAdaptor> messages = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node :
                model.getNeighborNodes(Association.class, activity.asNodeAdaptor())) {
            if (node.isMessage()
                    && node.getProperty(Message.PROP_INITIATE).equals(init)) {
                messages.add(node);
            }
        }
        return messages;
    }
}
