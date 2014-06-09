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
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 *
 * @author tmi
 */
public class ReachabilityGraph {

    private Map<ProcessState, ReachabilityGraphNode> states;
    private List<ProcessState> initialStates;
    private BPMNSimulator simulator;
    private NodeAdaptor initialNode, finalNode;
    private ModelAdaptor model;

    public ReachabilityGraph(ModelAdaptor model, NodeAdaptor initialNode,
            NodeAdaptor finalNode, Collection<NodeAdaptor> cancellationNodes) throws StateSpaceException {
        this.initialNode = initialNode;
        this.finalNode = finalNode;
        this.model = model;
        states = new HashMap<ProcessState, ReachabilityGraphNode>();
        simulator = new BPMNSimulator(model, cancellationNodes, finalNode);
        /*Workbench wb = new Workbench(false);
        wb.addModel("component-model", model.copy().getAdaptee());
        wb.setVisible(true);*/
        build();
        System.out.println("Number of states: " + states.size());
        /*Workbench wb = new Workbench(false);
        wb.addModel("reachability-graph", toModel());
        wb.setVisible(true);*/

    }

    public void build() throws StateSpaceException {
        List<ProcessState> activeStates = initialStates();
        for (ProcessState state : activeStates) {
            states.put(state, new ReachabilityGraphNode(state));
        }

        while (!activeStates.isEmpty()) {
            activeStates = performNextStep(activeStates);
            if (states.size() > 1000) {
                throw new StateSpaceException();
            }
        }
    }

    private List<ProcessState> initialStates() {
        List<ProcessState> initial = simulator.executeInitial(initialNode);
        for (ProcessState state : initial) {
            states.put(state, new ReachabilityGraphNode(state));
        }
        initialStates = initial;
        return initial;
    }

    private List<ProcessState> performNextStep(List<ProcessState> currentStates) {
        List<ProcessState> reachedStates = new LinkedList<ProcessState>();
        for (ProcessState state : currentStates) {
            reachedStates.addAll(proceedState(state));
        }
        return reachedStates;
    }

    private List<ProcessState> proceedState(ProcessState sourceState) {
        List<ProcessState> resultingStates = new LinkedList<ProcessState>();
        for (NodeAdaptor executedNode : simulator.getEnabledNodes(sourceState)) {
            for (ProcessState reachedState :
                    simulator.execute(executedNode, sourceState)) {
                if (reachedState.hasTokenAccumulation()
                        && isWorthless(reachedState)) {
                    continue;
                }
                if (!states.containsKey(reachedState)) {
                    states.put(reachedState, new ReachabilityGraphNode(reachedState));
                    resultingStates.add(reachedState);
                }
                ReachabilityGraphEdge edge =
                        states.get(reachedState).
                        addEdgeFrom(states.get(sourceState), executedNode);
                states.get(sourceState).addOutgoingEdge(edge);
            }
        }
        return resultingStates;
    }

    private boolean isWorthless(ProcessState state) {
        for (ProcessState existingState : states.keySet()) {
            if (state.hasTokensOnSameObjectsAs(existingState)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<ReachabilityGraph>\n");
        for (ReachabilityGraphNode node : states.values()) {
            builder.append(node.toString());
        }
        builder.append("</ReachabilityGraph>\n");
        return builder.toString();
    }

    public BPMNModel toModel() {
        BPMNModel generatedModel = new BPMNModel("reachability graph");
        Map<ProcessState, ProcessNode> nodes = new HashMap<ProcessState, ProcessNode>();
        for (ProcessState state : states.keySet()) {
            nodes.put(state, new SubProcess());
            nodes.get(state).setText(state.toShortString());
            generatedModel.addNode(nodes.get(state));
        }
        for (ReachabilityGraphNode node : states.values()) {
            for (ReachabilityGraphEdge edge : node.getOutgoingEdges()) {
                ProcessEdge flow = new SequenceFlow(
                        nodes.get(edge.getSource().getProcessState()),
                        nodes.get(edge.getTarget().getProcessState()));
                flow.setLabel(edge.getExecutedNode().toString());
                generatedModel.addEdge(flow);
            }
        }
        return generatedModel;
    }

    public boolean hasDeadlock() {
        for (ReachabilityGraphNode node : states.values()) {
            if (isDeadlock(node)) {
                return true;
            }
        }
        return false;
    }

    public List<NodeAdaptor> getDeadlockCausingNodes() {
        List<NodeAdaptor> deadlockNodes =
                new LinkedList<NodeAdaptor>();
        for (ReachabilityGraphNode node : states.values()) {
            if (isDeadlock(node)) {
                for (EdgeAdaptor edge : node.getProcessState().getEdges()) {
                    if (node.getProcessState().hasToken(edge)) {
                        deadlockNodes.add(edge.getTarget());
                    }
                }
            }
        }
        return deadlockNodes;
    }

    private boolean isDeadlock(ReachabilityGraphNode state) {
        return state.getProcessState().tokenSum() > 0
                && state.getOutgoingEdges().isEmpty();
    }

    public boolean hasLifelock() {
        for (ReachabilityGraphNode state : states.values()) {
            boolean isLifelock = true;
            for (ProcessState reachable : reachableStates(state)) {
                if (states.get(reachable).getOutgoingEdges().isEmpty()
                        || states.get(reachable).getProcessState().tokenSum() == 0) {
                    isLifelock = false;
                    break;
                }
            }
            if (isLifelock) {
                return true;
            }
        }
        return false;
    }

    private Collection<ProcessState> reachableStates(ReachabilityGraphNode from) {
        Set<ProcessState> reachable = new HashSet<ProcessState>();
        reachable.add(from.getProcessState());
        Set<ReachabilityGraphNode> currentStates = new HashSet<ReachabilityGraphNode>();
        currentStates.add(from);
        while (!currentStates.isEmpty()) {
            Set<ReachabilityGraphNode> copy = new HashSet<ReachabilityGraphNode>(currentStates);
            currentStates.clear();
            for (ReachabilityGraphNode state : copy) {
                for (ReachabilityGraphEdge edge : state.getOutgoingEdges()) {
                    if (!reachable.contains(edge.getTarget().getProcessState())) {
                        reachable.add(edge.getTarget().getProcessState());
                        currentStates.add(edge.getTarget());
                    }
                }
            }
        }
        return reachable;
    }

    public boolean hasProperCompletion() {
        for (ReachabilityGraphNode node : states.values()) {
            for (ReachabilityGraphEdge edge : node.getOutgoingEdges()) {
                if (edge.getExecutedNode().equals(finalNode)
                        && edge.getTarget().getProcessState().isFinal()
                        && edge.getTarget().getProcessState().tokenSum() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<ProcessObjectAdaptor> getExecutedObjects() {
        Set<ProcessObjectAdaptor> executedNodes = new HashSet<ProcessObjectAdaptor>();
        for (ReachabilityGraphNode node : states.values()) {
            for (ReachabilityGraphEdge edge : node.getOutgoingEdges()) {
                executedNodes.add(edge.getExecutedNode());
                executedNodes.addAll(executedProcessObjects(edge));
            }
        }
        executedNodes.add(initialNode);
        return executedNodes;
    }

    private Set<ProcessObjectAdaptor> executedProcessObjects(ReachabilityGraphEdge edge) {
        Set<ProcessObjectAdaptor> objects = new HashSet<ProcessObjectAdaptor>();
        ProcessState source = edge.getSource().getProcessState(),
                target = edge.getTarget().getProcessState();
        for (ProcessObjectAdaptor object : model.getObjects()) {
            if (source.getTokensOn(object) != target.getTokensOn(object)) {
                objects.add(object);
                if (object.isNode()) {
                    NodeAdaptor node = (NodeAdaptor) object;
                    if (node.isEventBasedGateway()) {
                        objects.add(model.getEdgeFromTo(
                                SequenceFlow.class, node, edge.getExecutedNode()));
                    }
                }
            }
        }
        objects.add(edge.getExecutedNode());
        return objects;
    }
}
