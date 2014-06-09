/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import net.frapu.code.visualization.bpmn.SequenceFlow;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;

/**
 *
 * @author tmi
 */
public class SequenceFlowValidator extends EdgeValidator {

    public SequenceFlowValidator(EdgeAdaptor edge, ModelAdaptor model,
            BPMNValidator validator) {
        super(edge, model, validator);
    }

    @Override
    public void doValidation() {
        super.doValidation();
        checkClusterCrossingRules();
        checkSourceAndTargetAllowed();
        checkLabeling();
    }

    private void checkClusterCrossingRules() {
        NodeAdaptor sourceCluster = model.getClusterForNode(edge.getSource()),
                targetCluster = model.getClusterForNode(edge.getTarget());
        if (!sourceCluster.equals(targetCluster)) {
            if (sourceCluster.isSubProcess()
                    || targetCluster.isSubProcess()
                    || sourceCluster.isSubChoreography()
                    || targetCluster.isSubChoreography()) {
                validator.addMessage(
                        "sequenceFlowCrossingSubProcess", edge);
            }
            if (!model.getPoolForNode(edge.getSource()).equals(
                    model.getPoolForNode(edge.getTarget()))) {
                validator.addMessage(
                        "sequenceFlowCrossingPool", edge);
            }
        }
    }

    private void checkSourceAndTargetAllowed() {
        //An EventBasedGateway has special requirements on which nodes are allowed
        //to follow it via SequenceFlow. In order to not report errors two times,
        //SequenceFlow emerging from an EventBasedGateway won´t be checked at
        //this point.
        if (edge.getSource().isEventBasedGateway()) {
            return;
        }
        if (!edge.getSource().mayHaveOutgoingSequenceFlow()) {
            validator.addMessage("illegalSequenceFlowSource", edge);
        }
        if (!edge.getTarget().mayHavIncommingSequenceFlow()) {
            if (edge.getTarget().isEvent()
                    && ((EventAdaptor) edge.getTarget()).isAttached()) {
                validator.addMessage("sequenceFlowToAttachedEvent", edge);
            } else {
                validator.addMessage("illegalSequenceFlowTarget", edge);
            }
        }
    }

    private void checkLabeling() {
        if (edge.getLabel() == null || edge.getLabel().equals("")) {
            if (edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                    equals(SequenceFlow.TYPE_CONDITIONAL)) {
                validator.addMessage("unlabeledConditionalFlow", edge);
            } else if (sourceIsDecisionGateway()
                    && !isDefault()) {
                validator.addMessage("unlabeledFlowFromDecisionGateway", edge);
            }
        }
    }

    private boolean sourceIsDecisionGateway() {
        if (!edge.getSource().isGateway()) {
            return false;
        }
        GatewayAdaptor source = (GatewayAdaptor) edge.getSource();
        return source.isDecisionGateway()
                && model.getOutgoingEdges(SequenceFlow.class, edge.getSource()).
                size() != 1;
    }

    private boolean isDefault() {
        return edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                equals(SequenceFlow.TYPE_DEFAULT);
    }
}
