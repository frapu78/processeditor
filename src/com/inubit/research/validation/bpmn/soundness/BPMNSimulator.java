/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.soundness;

import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * this is a simple Simulator for BPMN used for checking soundness of
 * BPMN-diagrams. This Simulator does not simulate attached events, as they are
 * removed and replaced by a decision-gateway while preprocessing the model.
 * @author tmi
 */
public class BPMNSimulator {

    private ModelAdaptor model;
    private Collection<NodeAdaptor> cancellationNodes;
    private NodeAdaptor outgoingNode;

    public BPMNSimulator(ModelAdaptor model,
            Collection<NodeAdaptor> cancellationNodes,
            NodeAdaptor outgoingNode) {
        this.model = model;
        this.cancellationNodes = cancellationNodes;
        this.outgoingNode = outgoingNode;
        System.out.println("Cancellation-nodes: " + cancellationNodes.toString());
        System.out.println("final node: " + outgoingNode.toString());
    }

    public List<ProcessState> executeInitial(NodeAdaptor initialNode) {
        ProcessState initialState = new ProcessState(model);
        if (initialNode.isEventBasedGateway()) {
            List<ProcessState> result = new LinkedList<ProcessState>();
            initialState.addTokenToEventBasedGateway((GatewayAdaptor)initialNode);
            result.add(initialState);
            return result;
        } else {
            return forceExecute(initialNode, initialState);
        }
    }

    public List<NodeAdaptor> getEnabledNodes(ProcessState state) {
        List<NodeAdaptor> enabledNodes = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : model.getNodes()) {
            if (isEnabled(node, state)) {
                enabledNodes.add(node);
            }
        }
        return enabledNodes;
    }

    public boolean isEnabled(NodeAdaptor node, ProcessState state) {
        if (node.isGateway()) {
            return isEnabled((GatewayAdaptor) node, state);
        } else {
            return isOneIncommingFlowEnabled(node, state);
        }
    }

    public boolean isOneIncommingFlowEnabled(
            NodeAdaptor node, ProcessState state) {
        for (EdgeAdaptor edge : node.getIncomingSequenceFlow()) {
            if (state.getTokensOnEdge(edge) > 0
                    || (edge.getSource().isEventBasedGateway()
                    && state.getTokensOn(edge.getSource()) > 0)) {
                return true;
            }
        }
        return false;
    }

    public boolean areAllIncommingFlowsEnabled(
            NodeAdaptor node, ProcessState state) {
        List<EdgeAdaptor> incomming = node.getIncomingSequenceFlow();
        for (EdgeAdaptor edge : incomming) {
            if (state.getTokensOnEdge(edge) == 0) {
                return false;
            }
        }
        return !incomming.isEmpty();
    }

    private boolean isInclusiveGatewayEnabled(
            GatewayAdaptor gateway, ProcessState state) {
        List<EdgeAdaptor> enabledIncommingEdges =
                enabledIncommingSequenceFlow(gateway, state);
        if (enabledIncommingEdges.isEmpty()) {
            return false;
        }
        for (ProcessObjectAdaptor object : state.objectsWithTokens()) {
            Set<EdgeAdaptor> reachable = getReachableEdgesEndingAt(object, gateway);
            boolean reachesEnabledEdge = false, reachesDisabledEdge = false;
            for (EdgeAdaptor edge : reachable) {
                if (enabledIncommingEdges.contains(edge)) {
                    reachesEnabledEdge = true;
                } else {
                    reachesDisabledEdge = true;
                }
            }
            if (reachesDisabledEdge && !reachesEnabledEdge) {
                return false;
            }
        }

        return true;
    }

    private List<EdgeAdaptor> enabledIncommingSequenceFlow(
            NodeAdaptor node, ProcessState state) {
        List<EdgeAdaptor> enabled = new LinkedList<EdgeAdaptor>();
        for (EdgeAdaptor incomming : node.getIncomingSequenceFlow()) {
            if (state.hasToken(incomming)) {
                enabled.add(incomming);
            }
        }
        return enabled;
    }

    private Set<EdgeAdaptor> getReachableEdgesEndingAt(
            ProcessObjectAdaptor startingAt, NodeAdaptor endingAt) {
        Set<EdgeAdaptor> reachable = new HashSet<EdgeAdaptor>();
        Set<EdgeAdaptor> current = new HashSet<EdgeAdaptor>();
        if (startingAt.isEdge()) {
            current.add((EdgeAdaptor) startingAt);
        } else {
            current.addAll(((NodeAdaptor) startingAt).getOutgoingSequenceFlow());
        }
        Set<EdgeAdaptor> usedEdges = new HashSet<EdgeAdaptor>();
        usedEdges.addAll(current);

        while (!current.isEmpty()) {
            Set<EdgeAdaptor> copy = new HashSet<EdgeAdaptor>(current);
            current.clear();
            for (EdgeAdaptor edge : copy) {
                if (edge.getTarget().equals(endingAt)) {
                    reachable.add(edge);
                } else {
                    for (EdgeAdaptor outgoing : edge.getTarget().getOutgoingSequenceFlow()) {
                        if (!usedEdges.contains(outgoing)) {
                            usedEdges.add(outgoing);
                            current.add(outgoing);
                        }
                    }
                }
            }
        }

        return reachable;
    }

    public boolean isEnabled(
            GatewayAdaptor gateway, ProcessState state) {
        if (gateway.isParallelGateway()) {
            return areAllIncommingFlowsEnabled(gateway, state);
        } else if (gateway.isInclusiveGateway() || gateway.isComplexGateway()) {
            return isInclusiveGatewayEnabled(gateway, state);
        } else {//exclusive gateway, event-based gateway and gateway
            return isOneIncommingFlowEnabled(gateway, state);
        }
    }

    public List<ProcessState> execute(NodeAdaptor node, ProcessState state) {
        if (!isEnabled(node, state)) {
            List<ProcessState> list = new LinkedList<ProcessState>();
            list.add(state);
            return list;
        }
        List<ProcessState> startStates = consumeTokens(node, state.clone());
        List<ProcessState> resultState = new LinkedList<ProcessState>();
        for (ProcessState startState : startStates) {
            resultState.addAll(forceExecute(node, startState));
        }
        return resultState;
    }

    private List<ProcessState> consumeTokens(NodeAdaptor node, ProcessState state) {
        if (node.isExclusiveJoin()) {
            return consumeOneToken(node, state);
        } else {//parallel or inclusive
            return consumeAllIncomingTokens(node, state);
        }
    }

    private List<ProcessState> consumeOneToken(NodeAdaptor node, ProcessState state) {
        List<ProcessState> results = new LinkedList<ProcessState>();
        for (ProcessObjectAdaptor input : tokenInputsOf(node, state)) {
            results.add(state.cloneAndRemoveTokenFrom(input));
        }
        return results;
    }

    private List<ProcessState> consumeAllIncomingTokens(
            NodeAdaptor node, ProcessState state) {
        List<ProcessState> results = new LinkedList<ProcessState>();
        state.removeTokensFromAll(tokenInputsOf(node, state));
        results.add(state);
        return results;
    }

    private List<ProcessObjectAdaptor> tokenInputsOf(
            NodeAdaptor node, ProcessState state) {
        List<ProcessObjectAdaptor> inputs = new LinkedList<ProcessObjectAdaptor>();
        for (EdgeAdaptor edge : node.getIncomingSequenceFlow()) {
            if (edge.getSource().isEventBasedGateway()) {
                if (state.hasToken(edge.getSource())) {
                    inputs.add(edge.getSource());
                }
            } else if (state.hasToken(edge)) {
                inputs.add(edge);
            }
        }
        return inputs;
    }

    private List<ProcessState> forceExecute(NodeAdaptor node, ProcessState state) {
        if (node.isGateway()) {
            return forceExecute((GatewayAdaptor) node, state);
        } else if (cancellationNodes.contains(node)) {
            List<ProcessState> result = new LinkedList<ProcessState>();
            result.add(new ProcessState(model, true));//no tokens
            return result;
        } else {
            return forceExecuteUsualNode(node, state);
        }
    }

    private List<ProcessState> forceExecute(
            GatewayAdaptor gateway, ProcessState state) {
        if (gateway.isParallelGateway()) {
            state.sendTokenToAll(gateway.getOutgoingSequenceFlow());
            List<ProcessState> result = new LinkedList<ProcessState>();
            if (outgoingNode.equals(gateway)) {
                state.setFinal();
            }
            result.add(state);
            return result;
        } else if (gateway.isInclusiveGateway() || gateway.isComplexGateway()) {
            return forceExecuteInclusiveGateway(gateway, state);
        } else {//exclusive behaviour
            return forceExecuteExclusiveGateway(gateway, state);
        }
    }

    private List<ProcessState> forceExecuteInclusiveGateway(
            GatewayAdaptor gateway, ProcessState state) {
        List<List<EdgeAdaptor>> executionCombinations =
                powerSet(undefaultOutgoingFlow(gateway));
        if (!(gateway.getOutgoingSequenceFlow().isEmpty() ||
                outgoingNode.equals(gateway))) {
            executionCombinations.remove(new LinkedList<EdgeAdaptor>());
        }//execute at least one edge, if there are outgoing edges, except if there
        //is an outgoing edge not represented in the model.

        EdgeAdaptor defaultFlow = defaultOutgoingFlow(gateway);
        if (defaultFlow != null) {
            List<EdgeAdaptor> deaultCombination = new LinkedList<EdgeAdaptor>();
            deaultCombination.add(defaultFlow);
            executionCombinations.add(deaultCombination);
        }

        List<ProcessState> results = new LinkedList<ProcessState>();
        for (List<EdgeAdaptor> combination : executionCombinations) {
            ProcessState resultingState =
                    state.cloneAndSendTokensToAll(combination);
            results.add(resultingState);
            if (outgoingNode.equals(gateway)) {
                if (combination.isEmpty()) {
                    resultingState.setFinal();
                } else {
                    ProcessState finalClone = resultingState.clone();
                    finalClone.setFinal();
                    results.add(finalClone);
                }
            }
        }
        return results;
    }

    private List<EdgeAdaptor> standardOutgoingFlow(NodeAdaptor node) {
        List<EdgeAdaptor> flow = new LinkedList<EdgeAdaptor>();
        for (EdgeAdaptor edge : node.getOutgoingSequenceFlow()) {
            if (edge.isStandardSequenceFlow()) {
                flow.add(edge);
            }
        }
        return flow;
    }

    private List<EdgeAdaptor> conditionalOutgoingFlow(NodeAdaptor node) {
        List<EdgeAdaptor> flow = new LinkedList<EdgeAdaptor>();
        for (EdgeAdaptor edge : node.getOutgoingSequenceFlow()) {
            if (edge.isConditionalSequenceFlow()) {
                flow.add(edge);
            }
        }
        return flow;
    }

    private List<EdgeAdaptor> undefaultOutgoingFlow(NodeAdaptor node) {
        List<EdgeAdaptor> flow = new LinkedList<EdgeAdaptor>();
        for (EdgeAdaptor edge : node.getOutgoingSequenceFlow()) {
            if (!edge.isDefaultSequenceFlow()) {
                flow.add(edge);
            }
        }
        return flow;
    }

    private EdgeAdaptor defaultOutgoingFlow(NodeAdaptor node) {
        for (EdgeAdaptor edge : node.getOutgoingSequenceFlow()) {
            if (edge.isDefaultSequenceFlow()) {
                return edge;
            }
        }
        return null;
    }

    private List<ProcessState> forceExecuteExclusiveGateway(
            GatewayAdaptor gateway, ProcessState state) {
        List<ProcessState> results = new LinkedList<ProcessState>();
        for (EdgeAdaptor edge : gateway.getOutgoingSequenceFlow()) {
            List<EdgeAdaptor> edgeList = new LinkedList<EdgeAdaptor>();
            edgeList.add(edge);
            results.add(state.cloneAndSendTokensToAll(edgeList));
        }
        if (outgoingNode.equals(gateway)) {
            ProcessState clone = state.clone();
            clone.setFinal();
            results.add(clone);
        } else if (gateway.getOutgoingSequenceFlow().isEmpty()) {
            results.add(state.clone());
        }
        return results;
    }

    private <Element> List<List<Element>> powerSet(List<Element> elements) {
        List<List<Element>> powerSet = new LinkedList<List<Element>>();
        powerSet.add(new LinkedList<Element>());
        for (Element edge : elements) {
            List<List<Element>> toBeAdded =
                    new LinkedList<List<Element>>();
            for (List<Element> list : powerSet) {
                List<Element> copy = new LinkedList<Element>(list);
                copy.add(edge);
                toBeAdded.add(copy);
            }
            powerSet.addAll(toBeAdded);
        }
        return powerSet;
    }

    private List<ProcessState> forceExecuteUsualNode(
            NodeAdaptor node, ProcessState state) {
        List<EdgeAdaptor> unconditionalEdges = standardOutgoingFlow(node);
        EdgeAdaptor defaultEdge = defaultOutgoingFlow(node);
        List<List<EdgeAdaptor>> combinationsOfConditionalEdges =
                powerSet(conditionalOutgoingFlow(node));
        if (defaultEdge != null) {
            List<EdgeAdaptor> noConditionalFlow = new LinkedList<EdgeAdaptor>();
            combinationsOfConditionalEdges.remove(noConditionalFlow);
            noConditionalFlow.add(defaultEdge);
            combinationsOfConditionalEdges.add(noConditionalFlow);
        }

        List<ProcessState> results = new LinkedList<ProcessState>();
        for (List<EdgeAdaptor> combination : combinationsOfConditionalEdges) {
            List<EdgeAdaptor> selected = new LinkedList<EdgeAdaptor>(combination);
            selected.addAll(unconditionalEdges);
            ProcessState resultState = state.cloneAndSendTokensToAll(selected);
            if (outgoingNode.equals(node)) {
                resultState.setFinal();
            }
            results.add(resultState);
        }
        
        return results;
    }
}
