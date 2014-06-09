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
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.LinkedList;
import java.util.List;

/**
 * Checks, that only nodes, that are allowed in ChoreographyDiagrams occur in
 * this BPMNModel.
 * @author tmi
 */
public class ForbiddenNodesCheck extends AbstractChoreographyCheck {

    public ForbiddenNodesCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    /**
     * checks, that a specified node is allowed in Choreography diagrams and that
     * it is attached, if it must be attached and not attached if it must not
     * be attached.
     */
    @Override
    public void checkNode(NodeAdaptor node) {
        if (!node.isAllowedInChoreography()) {
            validator.addMessage("forbiddenNodeInChoreography", node);
        } else if (node.isEvent()) {
            checkPosition((EventAdaptor)node);
        }
    }

    /**
     * checks, that a node´s attached-state is allowed.
     */
    private void checkPosition(EventAdaptor event) {
        if (event.isNoneIntermediateEvent() && event.isAttached()) {
            validator.addMessage("mustNotBeAttachedInChoreography", event);

        }
        if (event.isMessageIntermediateEvent()
                || event.isCancelIntermediateEvent()
                || event.isCompensationIntermediateEvent()) {
            if (!event.isAttached()) {
                validator.addMessage("mustBeAttachedInChoreography", event);
            } else if (!event.getParentNode().isChoreographyTask()) {
                List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                related.add(event.getParentNode());
                validator.addMessage("mustNotBeAttachedToTaskInChoreography",
                        event, related);
            }
        }
    }
}
