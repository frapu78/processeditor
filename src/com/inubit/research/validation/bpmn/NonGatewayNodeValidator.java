/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tmi
 */
public class NonGatewayNodeValidator {

    private NodeAdaptor node;
    private BPMNValidator validator;

    public NonGatewayNodeValidator(NodeAdaptor node, BPMNValidator validator) {
        this.node = node;
        this.validator = validator;
    }

    public void doValidation() {
        validateNotOnlyConditionalOutgoingFlow();
        validateNoImplicitSplit();
        if (!followsEventBasedGateway()) {//if a node follows an EventBasedGateway,
            //it is an error, if it has other incomming SequenceFlow => do not
            //report an additional info for the implicit join
            validateNoImplicitJoin();
        }
        validateDefaultFlow();
    }

    private void validateNotOnlyConditionalOutgoingFlow() {
        List<EdgeAdaptor> outgoing = node.getOutgoingSequenceFlow();
        if (outgoing.isEmpty()) {
            return;
        }
        for (EdgeAdaptor edge : outgoing) {
            if (!edge.isConditionalSequenceFlow()) {
                return;
            }
        }
        errorForOnlyConditionalFlow(outgoing);
    }

    private void errorForOnlyConditionalFlow(List<EdgeAdaptor> outgoingEdges) {
        List<ProcessObjectAdaptor> related =
                new LinkedList<ProcessObjectAdaptor>(outgoingEdges);
        validator.addMessage("onlyConditionalOutgoingSequenceFlow",
                node, related);
    }

    private void validateNoImplicitSplit() {
        List<EdgeAdaptor> outgoing = node.getOutgoingSequenceFlow();
        if (outgoing.size() > 1) {
            validator.addMessage("implicitSplit", node, outgoing);
        }
    }

    private void validateNoImplicitJoin() {
        List<EdgeAdaptor> incoming = node.getIncomingSequenceFlow();
        if (incoming.size() > 1) {
            validator.addMessage("implicitJoin", node, incoming);
        }
    }

    private boolean followsEventBasedGateway() {
        for (NodeAdaptor predecessor : node.getPrecedingNodes()) {
            if (predecessor.isEventBasedGateway()) {
                return true;
            }
        }
        return false;
    }

    private void validateDefaultFlow() {
        Set<EdgeAdaptor> defaultFlows = new HashSet<EdgeAdaptor>(),
                         conditionalFlows = new HashSet<EdgeAdaptor>();
        for (EdgeAdaptor edge : node.getOutgoingSequenceFlow()) {
            if (edge.isDefaultSequenceFlow()) {
                defaultFlows.add(edge);
            } else if (edge.isConditionalSequenceFlow()) {
                conditionalFlows.add(edge);
            }
        }
        if (defaultFlows.size() > 1) {
            validator.addMessage("multipleDefaultFlows", node, defaultFlows);
        }
        if (!defaultFlows.isEmpty() && conditionalFlows.isEmpty()) {
            validator.addMessage("defaultFlowButNoConditionalFlow",
                    node, defaultFlows);
        }
    }
}
