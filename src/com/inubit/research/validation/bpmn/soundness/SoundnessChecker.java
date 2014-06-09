/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.soundness;

import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.validation.bpmn.BPMNValidator;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import com.inubit.research.validation.bpmn.adaptors.rpst.AdaptorMappedRPST;
import com.inubit.research.validation.bpmn.adaptors.rpst.AdaptorMappedTriconnectedComponent;
import com.inubit.research.validation.bpmn.adaptors.rpst.Mapping;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author tmi
 */
public class SoundnessChecker {

    private ModelAdaptor model;
    private BPMNValidator validator;
    private Map<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>> generatedObjectsMap;

    protected class Direction {

        private NodeAdaptor source, target;

        public Direction(NodeAdaptor source, NodeAdaptor target) {
            this.source = source;
            this.target = target;
        }

        public NodeAdaptor getSource() {
            return source;
        }

        public NodeAdaptor getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Direction)) {
                return false;
            }
            Direction otherDirection = (Direction) other;
            if (source == null) {
                if (otherDirection.getSource() != null) {
                    return false;
                }
            } else {
                if (!source.equals(otherDirection.getSource())) {
                    return false;
                }
            }
            if (target == null) {
                return otherDirection.getTarget() == null;
            } else {
                return target.equals(otherDirection.getTarget());
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.source != null ? this.source.hashCode() : 0);
            hash = 89 * hash + (this.target != null ? this.target.hashCode() : 0);
            return hash;
        }

        public List<NodeAdaptor> asList() {
            List<NodeAdaptor> nodes = new LinkedList<NodeAdaptor>();
            nodes.add(source);
            nodes.add(target);
            return nodes;
        }
    }

    protected enum SplitOrJoinType {

        EXCLUSIVE,
        INCLUSIVE,
        PARALLEL,
        COMPLEX;

        public static SplitOrJoinType typeOfGateway(GatewayAdaptor gateway) {
            if (gateway.isParallelGateway()) {
                return PARALLEL;
            } else if (gateway.isInclusiveGateway()) {
                return INCLUSIVE;
            } else if (gateway.isComplexGateway()) {
                return COMPLEX;
            } else {//exclusive or event-based
                return EXCLUSIVE;
            }
        }

        public static SplitOrJoinType forSplitAt(NodeAdaptor node) {
            if (node.isGateway()) {
                return typeOfGateway((GatewayAdaptor) node);
            } else {
                return PARALLEL;
            }
        }

        public static SplitOrJoinType forJoinAt(NodeAdaptor node) {
            if (node.isGateway()) {
                return typeOfGateway((GatewayAdaptor) node);
            } else {
                return EXCLUSIVE;
            }
        }
    }

    public SoundnessChecker(ModelAdaptor model, BPMNValidator validator) {
        this.model = model.copy();
        this.validator = validator;
        generatedObjectsMap =
                new HashMap<ProcessObjectAdaptor, Set<ProcessObjectAdaptor>>();
    }

    private void addMessage(String messageID,
            List<? extends ProcessObjectAdaptor> relatedObjects) {
        validator.addMessage(messageID, translateGeneratedObjects(relatedObjects));
    }

    private void addMessage(String messageID, ProcessObjectAdaptor primaryObject,
            List<? extends ProcessObjectAdaptor> relatedObjects) {
        List<ProcessObjectAdaptor> primaryObjects =
                translateGeneratedObject(primaryObject);
        if (primaryObjects.size() == 1) {
            validator.addMessage(messageID, primaryObjects.get(0),
                    translateGeneratedObjects(relatedObjects));
        } else {
            primaryObjects.addAll(relatedObjects);
            validator.addMessage(messageID, primaryObjects);
        }
    }

    private List<ProcessObjectAdaptor> translateGeneratedObjects(
            Collection<? extends ProcessObjectAdaptor> objects) {
        List<ProcessObjectAdaptor> translation =
                new LinkedList<ProcessObjectAdaptor>();
        for (ProcessObjectAdaptor object : objects) {
            translation.addAll(translateGeneratedObject(object));
        }
        return translation;
    }

    private List<ProcessObjectAdaptor> translateGeneratedObject(
            ProcessObjectAdaptor object) {
        if (generatedObjectsMap.containsKey(object)) {
            return translateGeneratedObjects(generatedObjectsMap.get(object));
        } else {
            List<ProcessObjectAdaptor> result =
                    new LinkedList<ProcessObjectAdaptor>();
            result.add(object);
            return result;
        }
    }

    public void perform() {
        doModelPreprocessing();
        Mapping mapping = new Mapping(model);
        AdaptorMappedRPST rpst;
        try {
            rpst = new AdaptorMappedRPST(mapping);
        } catch (SinkNodeException ex) {
            Logger.getLogger(SoundnessChecker.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (SourceNodeException ex) {
            Logger.getLogger(SoundnessChecker.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        isSound(rpst.getRoot());
        cleanUp();
    }

    private void doModelPreprocessing() {
        model.removeArtifactsAndMessages();
        generatedObjectsMap.putAll(model.connectLinkEventsViaSequenceFlow());
        generatedObjectsMap.putAll(model.reduceToOneStartEvent());
        generatedObjectsMap.putAll(model.reduceToOneEndEventPerProcess());
        generatedObjectsMap.putAll(model.transformAttachmentsToGateways());
        model.removeEventSubProcessesAndCompenstaionHandlers();
        generatedObjectsMap.putAll(model.interceptDirectEdgesFromStartToEnd());
        /*Workbench wb = new Workbench(false);
        wb.addModel("preprocessedModel", model.copy().getAdaptee());
        wb.setVisible(true);*/
    }

    private void cleanUp() {
        model.removeAll();
    }

    private boolean isSound(AdaptorMappedTriconnectedComponent component) {
        switch (component.getType()) {
            case BOND:
                return isBondSound(component);
            case POLYGON:
                return isPolygonSound(component);
            case TRICONNECTED:
                return isRigidSound(component);
            case TRIVIAL:
                return true;
            default:
                return true;
        }
    }

    private boolean isBondSound(AdaptorMappedTriconnectedComponent component) {
        Direction direction = directionOfBondChildren(component);
        if (direction == null) {
            return isLoopSound(component);
        } else {
            return isDirectedBondSound(component, direction);
        }
    }

    private boolean isDirectedBondSound(AdaptorMappedTriconnectedComponent component,
            Direction direction) {
        boolean isSound = true;
        for (AdaptorMappedTriconnectedComponent child : component.getChildren()) {
            isSound &= isSound(child);
        }
        switch (SplitOrJoinType.forSplitAt(direction.getSource())) {
            case EXCLUSIVE:
                isSound &= isExclusiveSplittingBondSound(direction);
                break;
            case PARALLEL:
                isSound &= isParallelSplittingBondSound(direction);
                break;
            case INCLUSIVE:
            case COMPLEX:
                isSound &= isInclusiveSplittingBondSound(direction);
                break;
        }
        return isSound;
    }

    private boolean isExclusiveSplittingBondSound(Direction direction) {
        if (SplitOrJoinType.forJoinAt(direction.getTarget()).
                equals(SplitOrJoinType.PARALLEL)) {
            addMessage("blockStartsExclusiveEndsParallel", direction.asList());
            return false;
        } else {
            return true;
        }
    }

    private boolean isParallelSplittingBondSound(Direction direction) {
        if (SplitOrJoinType.forJoinAt(direction.getTarget()).
                equals(SplitOrJoinType.EXCLUSIVE)) {
            addMessage("lackOfSynchronization", direction.asList());
            return false;
        } else {
            return true;
        }
    }

    private boolean isInclusiveSplittingBondSound(Direction direction) {
        SplitOrJoinType joinType =
                SplitOrJoinType.forJoinAt(direction.getTarget());
        if (joinType.equals(SplitOrJoinType.PARALLEL)) {
            addMessage("blockStartsInclusiveEndsParallel", direction.asList());
            return false;
        } else if (joinType.equals(SplitOrJoinType.EXCLUSIVE)) {
            addMessage("blockStartsInclusiveEndsExclusive", direction.asList());
            return false;
        } else {
            return true;
        }
    }

    private boolean isLoopSound(AdaptorMappedTriconnectedComponent component) {
        Direction direction = directionOf(component);
        if (direction == null) {
            return false;//Should not come here!
        }
        if (!SplitOrJoinType.forSplitAt(direction.getSource()).
                equals(SplitOrJoinType.PARALLEL)) {
            if (SplitOrJoinType.forJoinAt(direction.getTarget()).
                    equals(SplitOrJoinType.PARALLEL)) {
                addMessage("infiniteLoop", direction.asList());
                return false;
            }
        } else if (direction.getSource().isParallelGateway()) {
            addMessage("parallelLoopStart", direction.asList());
            return false;
        }
        return true;
    }

    private boolean isIncomingBorderNodeInComponent(NodeAdaptor node,
            AdaptorMappedTriconnectedComponent component) {
        if (!isBorderNodeOf(node, component)) {
            return false;
        }
        List<EdgeAdaptor> edgesOfComponent = edgesOf(component),
                          outgoingFlow = node.getOutgoingSequenceFlow(),
                          incomingFlow = node.getIncomingSequenceFlow();
        if (edgesOfComponent.containsAll(outgoingFlow) &&
                !outgoingFlow.isEmpty()) {
            return true;
        } else if (containsNone(edgesOfComponent, incomingFlow)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isOutgoingBorderNodeInComponent(NodeAdaptor node,
            AdaptorMappedTriconnectedComponent component) {
        if (!isBorderNodeOf(node, component)) {
            return false;
        }
        List<EdgeAdaptor> edgesOfComponent = edgesOf(component);
        if (edgesOfComponent.containsAll(node.getIncomingSequenceFlow())) {
            return true;
        } else if (containsNone(edgesOfComponent, node.getOutgoingSequenceFlow())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean containsNone(Collection container, Collection containees) {
        for (Object containee : containees) {
            if (container.contains(containee)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPolygonSound(AdaptorMappedTriconnectedComponent component) {
        boolean isSound = true;//false will not immediately be returned, because
        //all sub-components should be checked in order to issue appropriate warnings
        if (directionOf(component) == null) {
            isSound = false;
            //validator.addMessage("undirectedPolygon", edgesOf(component));
        }
        for (AdaptorMappedTriconnectedComponent subComponent : component.getChildren()) {
            if (!isSound(subComponent)) {
                isSound = false;
            }
        }
        return isSound;
    }

    private boolean isRigidSound(AdaptorMappedTriconnectedComponent component) {
        ModelAdaptor componentModel = modelFromComponent(component);
        Direction direction = directionOf(component);
        NodeAdaptor intialNode = NodeAdaptor.adapt(
                direction.getSource().getAdaptee(), componentModel),
                finalNode = NodeAdaptor.adapt(
                direction.getTarget().getAdaptee(), componentModel);
        ReachabilityGraph graph;
        try {
            graph = new ReachabilityGraph(componentModel, intialNode, finalNode,
                    getCancellationNodes());
        } catch (StateSpaceException ex) {
            validator.addMessage("tooManyStates", componentModel.getObjects());
            return false;
        }
        boolean isSound = true;
        isSound &= checkForDeadlocks(graph, componentModel);
        isSound &= checkForLifelocks(graph, componentModel);
        isSound &= checkForProperCompletion(graph, componentModel, direction.getTarget());
        isSound &= checkForDeadNodes(graph, componentModel);
        componentModel.removeAll();
        return isSound;
    }

    private Set<NodeAdaptor> getCancellationNodes() {
        Set<NodeAdaptor> cancellations = new HashSet<NodeAdaptor>();
        for (NodeAdaptor node : model.getNodes()) {
            if (isTerminationOrCancellation(node)) {
                cancellations.add(node);
            }
        }
        return cancellations;
    }

    private boolean checkForDeadlocks(ReachabilityGraph graph, ModelAdaptor model) {
        if (graph.hasDeadlock()) {
            //addMessage("deadlockInRigid", model.getObjects());
            addMessage("deadlockInRigid", graph.getDeadlockCausingNodes());
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForLifelocks(ReachabilityGraph graph, ModelAdaptor model) {
        if (graph.hasLifelock()) {
            addMessage("lifelockInRigid", model.getObjects());
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForProperCompletion(ReachabilityGraph graph,
            ModelAdaptor model, NodeAdaptor finalNode) {
        if (graph.hasProperCompletion()) {
            return true;
        } else {
            addMessage("improperCompletion", finalNode, model.getObjects());
            return false;
        }
    }

    private boolean checkForDeadNodes(ReachabilityGraph graph, ModelAdaptor model) {
        List<ProcessObjectAdaptor> executedObjects =
                translateGeneratedObjects(graph.getExecutedObjects());
        List<ProcessObjectAdaptor> allObjects =
                translateGeneratedObjects(model.getObjects());
        if (!executedObjects.containsAll(allObjects)) {
            List<ProcessObjectAdaptor> deadObjects =
                    new LinkedList<ProcessObjectAdaptor>(allObjects);
            deadObjects.removeAll(executedObjects);
            addMessage("deadNodes", nodesIn(deadObjects));
            return false;
        }
        return true;
    }

    private List<NodeAdaptor> nodesIn(List<ProcessObjectAdaptor> objects) {
        List<NodeAdaptor> nodes = new LinkedList<NodeAdaptor>();
        for (ProcessObjectAdaptor object : objects) {
            if (object.isNode()) {
                nodes.add((NodeAdaptor)object);
            }
        }
        return nodes;
    }

    private ModelAdaptor modelFromComponent(
            AdaptorMappedTriconnectedComponent component) {
        List<EdgeAdaptor> edges = virtualAndRealEdges(component);
        Set<NodeAdaptor> nodes = new HashSet<NodeAdaptor>();
        BPMNModel newModel = new BPMNModel();
        for (EdgeAdaptor edge : edges) {
            if (!nodes.contains(edge.getSource())) {
                nodes.add(edge.getSource());
                newModel.addNode(edge.getSource().getAdaptee());
            }
            if (!nodes.contains(edge.getTarget())) {
                nodes.add(edge.getTarget());
                newModel.addNode(edge.getTarget().getAdaptee());
            }
            newModel.addEdge(edge.getAdaptee());
        }
        return new ModelAdaptor(newModel);
    }

    private List<EdgeAdaptor> virtualAndRealEdges(
            AdaptorMappedTriconnectedComponent component) {
        List<EdgeAdaptor> edges = component.getEdges();
        for (AdaptorMappedTriconnectedComponent subComponent : component.getChildren()) {
            if (containsCancellationOrTermination(subComponent)) {
                edges.addAll(virtualAndRealEdges(subComponent));
            } else {
                Direction direction = directionOf(subComponent);
                EdgeAdaptor newEdge = new EdgeAdaptor(
                        new SequenceFlow(direction.getSource().getAdaptee(),
                        direction.getTarget().getAdaptee()), model);
                newEdge.getAdaptee().setProperty(SequenceFlow.PROP_SEQUENCETYPE,
                        sequenceTypeFor(subComponent));
                generatedObjectsMap.put(newEdge,
                        new HashSet<ProcessObjectAdaptor>(edgesOf(subComponent)));
                generatedObjectsMap.get(newEdge).addAll(nodesOf(subComponent));
                edges.add(newEdge);
            }
        }
        return edges;
    }

    private String sequenceTypeFor(AdaptorMappedTriconnectedComponent component) {
        NodeAdaptor source = directionOf(component).getSource();
        boolean hasStandardFlow = false,
                hasDefaultFlow = false,
                hasConditionalFlow = false;
        List<EdgeAdaptor> componentEdges = edgesOf(component);
        for (EdgeAdaptor edge : source.getOutgoingSequenceFlow()) {
            if (componentEdges.contains(edge)) {
                if (edge.isConditionalSequenceFlow()) {
                    hasConditionalFlow = true;
                } else if (edge.isDefaultSequenceFlow()) {
                    hasDefaultFlow = true;
                } else {
                    hasStandardFlow = true;
                }
            }
        }
        if (hasStandardFlow) {
            return SequenceFlow.TYPE_STANDARD;
        }
        if (hasDefaultFlow) {
            return SequenceFlow.TYPE_DEFAULT;
        }
        return SequenceFlow.TYPE_CONDITIONAL;
    }

    private boolean containsCancellationOrTermination(
            AdaptorMappedTriconnectedComponent component) {
        for (EdgeAdaptor edge : edgesOf(component)) {
            if (isTerminationOrCancellation(edge.getSource())
                    || isTerminationOrCancellation(edge.getTarget())) {
                return true;
            } else if (containsCancellationOrTermination(
                    generatedObjectsMap.get(edge.getSource()))) {
                return true;
            } else if (containsCancellationOrTermination(
                    generatedObjectsMap.get(edge.getTarget()))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCancellationOrTermination(
            Collection<ProcessObjectAdaptor> objects) {
        if (objects == null) {
            return false;
        }
        for (ProcessObjectAdaptor object : objects) {
            if (object.isNode()
                    && isTerminationOrCancellation((NodeAdaptor) object)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTerminationOrCancellation(NodeAdaptor node) {
        List<ProcessObjectAdaptor> representedObjects =
                translateGeneratedObject(node);
        if (representedObjects.size() != 1) {
            return false;
        } else if (representedObjects.get(0).isEdge()) {
            return false;
        } else {
            node = (NodeAdaptor) representedObjects.get(0);
            if (!node.isEndEvent()) {
                return false;
            } else {
                EventAdaptor event = (EventAdaptor) node;
                return event.isTerminateEndEvent() || event.isCancelEndEvent();
            }
        }
    }

    private Direction directionOf(EdgeAdaptor edge) {
        return new Direction(edge.getSource(),edge.getTarget());
    }

    private Direction directionOf(AdaptorMappedTriconnectedComponent component) {
        List<NodeAdaptor> borderNodes = borderNodes(component);
        if (borderNodes.size() == 1) {
            borderNodes.add(borderNodes.get(0));
        }
        if (borderNodes.size() != 2) {
            System.out.println("borderNodes should have 2 elements, but has " +
                    borderNodes.size());

            /*Workbench wb = new Workbench(false);
            wb.addModel("componentModel", modelFromComponent(component).getAdaptee().clone());
            wb.setVisible(true);*/
        }
        assert borderNodes.size() == 2 : "borderNodes should have 2 elements, "
                + "but has " + borderNodes.size();
        if (isIncomingBorderNodeInComponent(borderNodes.get(0), component)
                || isOutgoingBorderNodeInComponent(borderNodes.get(1), component)) {
            return new Direction(borderNodes.get(0), borderNodes.get(1));
        } else  {
            return new Direction(borderNodes.get(1), borderNodes.get(0));
        }
    }

    private Direction directionOfBondChildren(
            AdaptorMappedTriconnectedComponent component) {
        Direction direction = null;
        for (EdgeAdaptor edge : component.getEdges()) {
            if (direction == null) {
                direction = directionOf(edge);
            } else if (!direction.equals(directionOf(edge))) {
                return null;
            }
        }
        for (AdaptorMappedTriconnectedComponent subComponent : component.getChildren()) {
            Direction subDirection = directionOf(subComponent);
            if (subDirection == null) {
                return null;
            } else if (direction == null) {
                direction = subDirection;
            } else if (!direction.equals(subDirection)) {
                return null;
            }
        }
        return direction;
    }

    private List<NodeAdaptor> borderNodes(AdaptorMappedTriconnectedComponent component) {
        List<NodeAdaptor> borderNodes = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : model.getNodes()) {
            if (isBorderNodeOf(node, component)) {
                borderNodes.add(node);
            }
        }
        return borderNodes;
    }

    private boolean isBorderNodeOf(NodeAdaptor node,
            AdaptorMappedTriconnectedComponent component) {
        boolean hasEdgesInComponent = false, hasEdgesOutOfComponent = false;
        List<EdgeAdaptor> edgesOfComponent = edgesOf(component);
        for (EdgeAdaptor edge : node.getAdjacentEdges(SequenceFlow.class)) {
            if (edgesOfComponent.contains(edge)) {
                hasEdgesInComponent = true;
            } else {
                hasEdgesOutOfComponent = true;
            }
        }
        return (hasEdgesInComponent && hasEdgesOutOfComponent)
                || (hasEdgesInComponent
                && (node.getIncomingSequenceFlow().isEmpty()
                || node.getOutgoingSequenceFlow().isEmpty()));
    }

    private List<EdgeAdaptor> edgesOf(AdaptorMappedTriconnectedComponent component) {
        List<EdgeAdaptor> edges = new LinkedList<EdgeAdaptor>();
        for (EdgeAdaptor edge : component.getEdges()) {
            edges.add(edge);
        }
        for (AdaptorMappedTriconnectedComponent subComponent : component.getChildren()) {
            edges.addAll(edgesOf(subComponent));
        }
        return edges;
    }

    private List<NodeAdaptor> nodesOf(AdaptorMappedTriconnectedComponent component) {
         List<NodeAdaptor> nodes = new LinkedList<NodeAdaptor>();
        for (EdgeAdaptor edge : component.getEdges()) {
            nodes.add(edge.getSource());
            nodes.add(edge.getTarget());
        }
        for (AdaptorMappedTriconnectedComponent subComponent : component.getChildren()) {
            nodes.addAll(nodesOf(subComponent));
        }
        return nodes;
    }
}
