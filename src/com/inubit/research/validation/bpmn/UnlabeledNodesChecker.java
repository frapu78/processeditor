/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ArtifactAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import java.util.List;
import net.frapu.code.visualization.bpmn.MessageFlow;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;

/**
 *
 * @author tmi
 */
class UnlabeledNodesChecker {

    ModelAdaptor model;
    BPMNValidator validator;

    public UnlabeledNodesChecker(ModelAdaptor model, BPMNValidator validator) {
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        for (NodeAdaptor node : model.getNodes()) {
            if (isUnlabeled(node)) {
                if (node.isMessageEvent()) {
                    validateUnlabeledMessageEvent(node);
                } else if (shouldBeLabeled(node)) {
                    validator.addMessage(errorIDFor(node), node);
                }
            }
        }
    }

    private boolean isUnlabeled(NodeAdaptor node) {
        return node.getText() == null || node.getText().equals("");
    }

    private boolean isUnlabeled(EdgeAdaptor edge) {
        return edge.getLabel() == null || edge.getLabel().equals("");
    }

    /**
     * returns the ID for the information-text, that is used to indicate, that
     * node should be labeled.
     * @param node The node, for which to return the text-ID. NOTE, that this
     * must not be a Message Event. Message Events are handled in a different
     * way than the other Nodes.
     */
    private String errorIDFor(NodeAdaptor node) {
        if (node.isActivity()) {
            return "unlabeledActivity";
        }
        if (node.isChoreographyActivity()) {
            return "unlabeledChoreographyActivity";
        }
        if (node.isEvent()) {
            return "unlabeledEvent";
        }
        if (node.isPool()) {
            return "unlabeledPool";
        }
        if (node.isLane()) {
            return "unlabeledLane";
        }
        if (node.isGateway()) {
            if (((GatewayAdaptor) node).isComplexGateway()) {
                return "unlabeledComplexGateway";
            }
        }
        if (node.isArtifact()) {
            if (((ArtifactAdaptor) node).isDataObject()) {
                return "unlabeledDataObject";
            }
            if (((ArtifactAdaptor) node).isDataStore()) {
                return "unlabeledDataStore";
            }
        }
        return null;
    }

    private void validateUnlabeledMessageEvent(NodeAdaptor node) {
        List<EdgeAdaptor> messageFlows =
                model.getIncomingEdges(MessageFlow.class, node);
        messageFlows.addAll(model.getOutgoingEdges(MessageFlow.class, node));
        boolean shouldBeLabeled = messageFlows.isEmpty();
        for (EdgeAdaptor edge : messageFlows) {
            shouldBeLabeled |= isUnlabeled(edge);
        }
        if (shouldBeLabeled) {
            reportMessageEventInfo(node, messageFlows);
        }
    }

    private void reportMessageEventInfo(NodeAdaptor node,
            List<EdgeAdaptor> messageFlows) {
        validator.addMessage("unlabeledMessageEvent", node, messageFlows);
    }

    private boolean shouldBeLabeled(NodeAdaptor node) {
        if (node.isEvent()) {
            return shouldEventBeLabeled((EventAdaptor) node);
        } else if (node.isCluster()) {
            return shouldClusterBeLabeled((ClusterAdaptor) node);
        } else if (node.isActivity() || node.isChoreographyActivity()) {
            return true;
        }else if (node.isGateway()) {
            return ((GatewayAdaptor) node).isComplexGateway();
        } else if (node.isArtifact()) {
            return node.isData();
        }
        return false;
    }

    private boolean shouldEventBeLabeled(EventAdaptor event) {
        if (event.isStartEvent()) {
            return shouldStartEventBeLabeled(event);
        } else if (event.isEndEvent()) {
            return shouldEndEventBeLabeled(event);
        } else if (event.isAttached()) {
            return shouldAttachedEventBeLabled(event);
        } else {
            return true;
        }
    }

    private boolean shouldStartEventBeLabeled(EventAdaptor event) {
        if (event.isNoneStartEvent()) {
            return countNodesOfSameTypeInSameProcess(event) > 1;
        }
        return !event.isCompensationStartEvent();
    }

    private boolean shouldEndEventBeLabeled(EventAdaptor event) {
        if (event.isNoneEndEvent()) {
            return countNodesOfSameTypeInSameProcess(event) > 1;
        }
        return !(event.isEscalationEndEvent()
                || event.isTerminateEndEvent()
                || event.isCancelEndEvent()
                || event.isCompensationEndEvent());
    }

    private boolean shouldAttachedEventBeLabled(EventAdaptor event) {
        if (event.isErrorIntermediateEvent()) {
            return countNodesOfSameTypeInSameProcess(event) > 1;
        }
        return !(event.isEscalationIntermediateEvent()
                || event.isCancelIntermediateEvent()
                || event.isCompensationIntermediateEvent());
    }

    /**
     * Counts the number of nodes in the process, which directly contains node,
     * that have the same type as node or are instances of a subtype of node´s
     * type. Note, that only nodes with the exact same types will be counted,
     * i.e. insances of subclasses will not be counted.
     */
    private int countNodesOfSameTypeInSameProcess(NodeAdaptor node) {
        int count = 0;
        for (NodeAdaptor containedNode :
                node.getContainingProcess().getNodesOfContainedProcess()) {
            if (node.getAdaptee().getClass().
                    isInstance(containedNode.getAdaptee())) {
                ++count;
            }
        }
        return count;
    }

    private boolean shouldClusterBeLabeled(ClusterAdaptor cluster) {
        return !(((cluster.isWhiteboxSubProcess() ||
                    cluster.isWhiteboxSubChoreography())) &&
                !cluster.getProcessNodes().isEmpty());
    }
}
