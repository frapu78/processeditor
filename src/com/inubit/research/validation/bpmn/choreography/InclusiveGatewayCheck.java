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
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.choreography.branchingTree.BranchingTree;
import com.inubit.research.validation.bpmn.choreography.branchingTree.TreeBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Checks, that all participants, who are affected by an InclusiveGateway, have
 * been participating in the choreography before.
 * @author tmi
 */
public class InclusiveGatewayCheck extends AbstractChoreographyCheck {

    public InclusiveGatewayCheck(ModelAdaptor model, BPMNValidator validator) {
        super(model, validator);
    }

    @Override
    public void checkNode(NodeAdaptor node) {
        if (node.isGateway()
                && ((GatewayAdaptor) node).isInclusiveGateway()) {
            checkInclusiveGateway((GatewayAdaptor) node);
        }
    }

    /**
     * checks that all participants, who are affected by a gateway are able to know
     * the decision of the gateway. This means, that every participant, who has
     * different following ProcessNodes from the different pathes emerging from the
     * gateway, has been participating in the choreography before the gateway.
     * This constraint is necessary, because InclusiveGateways can only be enforced
     * as InclusiveGateways, i.e. no event-based reaction to the gateway decision
     * is possible.
     */
    private void checkInclusiveGateway(
            GatewayAdaptor gateway) {
        TreeBuilder treeBuilder = new TreeBuilder();
        BranchingTree history = treeBuilder.buildTreeFor(gateway,
                TreeBuilder.FlowDirection.flowBefore);
        Map<String, Collection<Set<NodeAdaptor>>> firstNodeSets =
                getFirstNodeSets(gateway);
        checkFirstNodes(firstNodeSets, history, gateway);
    }

    /**
     * generates a mapping from every participant following the gateway to the
     * Collection of alll following nodes, that involve the participant
     */
    private Map<String, Collection<Set<NodeAdaptor>>> getFirstNodeSets(
            GatewayAdaptor gateway) {
        TreeBuilder treeBuilder = new TreeBuilder();
        Map<String, Collection<Set<NodeAdaptor>>> firstNodeSets =
                new HashMap<String, Collection<Set<NodeAdaptor>>>();
        Collection<String> participants = (treeBuilder.buildTreeFor(
                gateway, TreeBuilder.FlowDirection.flowAfter)).getParticipants();
        for (String participant : participants) {
            firstNodeSets.put(participant, new LinkedList<Set<NodeAdaptor>>());
        }
        for (NodeAdaptor node : gateway.getSucceedingNodes()) {
            BranchingTree tree =
                    treeBuilder.buildTreeFor(node, TreeBuilder.FlowDirection.flowAfter);
            for (String participant : participants) {
                firstNodeSets.get(participant).add(tree.firstNodesOf(participant));
            }
        }
        return firstNodeSets;
    }

    /**
     * checks, that all participants, who are mentioned in firstNodeSets, have the
     * same first nodes for all alternatives, or are definitely involved in prior
     * activity
     */
    private void checkFirstNodes(
            Map<String, Collection<Set<NodeAdaptor>>> firstNodeSets,
            BranchingTree history, GatewayAdaptor gateway) {
        for (Map.Entry<String, Collection<Set<NodeAdaptor>>> entry :
                firstNodeSets.entrySet()) {
            checkFirstNodesForParticipant(
                    entry.getKey(), entry.getValue(), history, gateway);
        }
    }

    /**
     * checks the condition described above for one participant
     */
    private void checkFirstNodesForParticipant(
            String participant,
            Collection<Set<NodeAdaptor>> nodeSets,
            BranchingTree history,
            GatewayAdaptor gateway) {
        if (!(nodeSets.isEmpty()
                || history.allAlternativesInvolve(participant))) {
            Set<NodeAdaptor> reference = nodeSets.iterator().next();
            for (Set<NodeAdaptor> comparatee : nodeSets) {
                if (!comparatee.equals(reference)) {
                    Set<NodeAdaptor> affected = new HashSet<NodeAdaptor>(reference);
                    affected.removeAll(comparatee);
                    comparatee.removeAll(reference);
                    affected.addAll(comparatee);
                    validator.addMessage(
                            "inclusiveGatewayDecisionNotClearForAllParticipants",
                            gateway, affected);
                    break;
                }
            }
        }
    }
}
