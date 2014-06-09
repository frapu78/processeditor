/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ActivityAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author tmi
 */
public class EventBasedGatewayValidator {

    private GatewayAdaptor eventBasedGateway;
    private ModelAdaptor model;
    private BPMNValidator validator;
    List<NodeAdaptor> followingNodes;

    public EventBasedGatewayValidator(GatewayAdaptor eventBasedGateway,
            ModelAdaptor model, BPMNValidator validator) {
        this.eventBasedGateway = eventBasedGateway;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        followingNodes = model.getSucceedingNodes(
                SequenceFlow.class, eventBasedGateway);
        if (!(model.hasChoreography() && eventBasedGateway.isRootNode())) {
            checkForIllegalSuccessors();
            checkForMessageEventsAndReceiveTasks();
        }
        checkSuccessorsForOtherIncommingSequenceFlow();
        checkFollowingNodesForAttachedEvents();
        checkNoConditionalFlow();
        checkNumberOfOutgoingFlows();
    }

    private void checkForIllegalSuccessors() {
        List<ProcessObjectAdaptor> violators =
                new LinkedList<ProcessObjectAdaptor>();
        for (NodeAdaptor node : followingNodes) {
            if (! isAllowedSuccessor(node)) {
                violators.add(node);
                violators.add(model.getEdgeFromTo(SequenceFlow.class,
                        eventBasedGateway, node));
            }
        }
        if (!violators.isEmpty()) {
            validator.addMessage("illegalNodeAfterEventBasedGateway",
                    eventBasedGateway, violators);
        }
    }

    private boolean isAllowedSuccessor(NodeAdaptor node) {
        if (node.isEvent()) {
            return isAllowedSuccessor((EventAdaptor)node);
        } else if (node.isActivity()) {
            return ((ActivityAdaptor)node).isReceiveTask();
        } else {
            return false;
        }
    }

    private boolean isAllowedSuccessor(EventAdaptor node) {
        return node.isCatchingMessageIntermediateEvent() ||
                node.isTimerIntermediateEvent() ||
                node.isCatchingSignalIntermediateEvent() ||
                node.isConditionalIntermediateEvent()||
                node.isCatchingMultipleIntermediateEvent() ||
                node.isCatchingParallelMultipleIntermediateEvent();
    }

    /**
     * Checks, whether the EventBasedGateway is followed by
     * MessageIntermediateEvents and Receive-Tasks. This is not allowed due to
     * the BPMN-specification. Nevertheless only a Warning will be created, when
     * this configuration is found.
     */
    private void checkForMessageEventsAndReceiveTasks() {
        List<NodeAdaptor> receiveTasks = new LinkedList<NodeAdaptor>(),
                          messageEvents = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : followingNodes) {
            if (node.isReceiveTask()) receiveTasks.add(node);
            if (node.isMessageIntermediateEvent()) messageEvents.add(node);
        }
        if (! (receiveTasks.isEmpty() || messageEvents.isEmpty())) {
            List<ProcessObjectAdaptor> relatedObjects =
                    new LinkedList<ProcessObjectAdaptor>(receiveTasks);
            relatedObjects.addAll(messageEvents);
            validator.addMessage("receiveTaskAndMessageEventAfterEBGW",
                    eventBasedGateway, relatedObjects);
        }
    }

    /**
     * A node following an EventBasedGateway may not have any other incomming
     * SequenceFlow than the edge comming from the EventBasedGateway.
     */
    private void checkSuccessorsForOtherIncommingSequenceFlow() {
        for (NodeAdaptor node : followingNodes) {
            List<EdgeAdaptor> incommingFlow =
                    model.getIncomingEdges(SequenceFlow.class, node);
            if (incommingFlow.size() > 1) {
                List<ProcessObjectAdaptor> related =
                        new LinkedList<ProcessObjectAdaptor>(incommingFlow);
                related.add(eventBasedGateway);
                validator.addMessage("nodeAfterEBGWWithOtherIncommingSequenceFlow",
                        node, related);
            }
        }
    }

    private void checkFollowingNodesForAttachedEvents() {
        for (NodeAdaptor node : followingNodes) {
            List<NodeAdaptor> attachedEvents =
                    node.getAttachedEvents(model);
            if (! attachedEvents.isEmpty()) {
                validator.addMessage("taskFollowingEBGWHasAttachedEvent",
                        node, attachedEvents);
            }
        }
    }

    private void checkNoConditionalFlow() {
        for (EdgeAdaptor edge :
            model.getOutgoingEdges(SequenceFlow.class, eventBasedGateway)) {
            if (edge.isConditionalSequenceFlow()) {
                validator.addMessage("conditionalFlowFromEBGW", edge);
            }
        }
    }

    private void checkNumberOfOutgoingFlows() {
        List<EdgeAdaptor> outgoingEdges =
                model.getOutgoingEdges(SequenceFlow.class, eventBasedGateway);
        if (outgoingEdges.size() < 2) {
            validator.addMessage("onlyOneSequenceFlowFromEBGW",
                    eventBasedGateway, outgoingEdges);
        }
    }
}
