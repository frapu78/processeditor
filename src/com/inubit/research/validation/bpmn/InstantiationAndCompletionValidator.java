/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.LaneableClusterAdaptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tmi
 */
public class InstantiationAndCompletionValidator {

    private ModelAdaptor model;
    private BPMNValidator validator;
    private Map<ClusterAdaptor, List<EventAdaptor>> noneStartEvents,
            triggeredStartEvents,
            endEvents;
    private Map<ClusterAdaptor, List<NodeAdaptor>> exclusiveInstantiatingGateways,
            parallelInstantiatingGateways;

    public InstantiationAndCompletionValidator(
            ModelAdaptor model, BPMNValidator validator) {
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        findStartAndEndEventsAndInstantiatingGateways();
        for (ClusterAdaptor process : getProcessClusters()) {
            validateStartEventsOfProcess(process);
        }
        checkForMultiplePoolsContainingNoneStartEvents();
        checkForStartAndEndEventUsage();
        checkForNodesWithoutIncommingOrOutgoingSequenceFlow();
    }

    private void validateStartEventsOfProcess(ClusterAdaptor process) {
        if (process.isPool()) {
            if (noneStartEvents.get(process).size() > 1) {
                multipleNoneStartEventsInOnePool(process);
            }
            if (!(exclusiveInstantiatingGateways.get(process).isEmpty()
                    || triggeredStartEvents.get(process).isEmpty())) {
                instantiatingGatewaysAndTriggeredStartEventsInOnePool(process);
            } else if (triggeredStartEvents.get(process).size() > 1) {
                multipleTriggeredStartEventsInOnePool(process);
            } else if (exclusiveInstantiatingGateways.get(process).size() > 1) {
                multipleExclusiveInstantiatingGatewaysInOnePool(process);
            }
        }
    }

    private void multipleExclusiveInstantiatingGatewaysInOnePool(
            ClusterAdaptor pool) {
        validator.addMessage(
                "multipleExclusiveInstantiatingGatewaysInOnePool",
                exclusiveInstantiatingGateways.get(pool));
    }

    private void multipleTriggeredStartEventsInOnePool(
            ClusterAdaptor pool) {
        validator.addMessage(
                "multipleTriggeredStartEventsInOnePool",
                triggeredStartEvents.get(pool));
    }

    private void multipleNoneStartEventsInOnePool(ClusterAdaptor pool) {
        validator.addMessage(
                "multipleNoneStartEventsInOnePool",
                noneStartEvents.get(pool));
    }

    private void instantiatingGatewaysAndTriggeredStartEventsInOnePool(
            ClusterAdaptor pool) {
        List<NodeAdaptor> related =
                new LinkedList<NodeAdaptor>(
                exclusiveInstantiatingGateways.get(pool));
        related.addAll(triggeredStartEvents.get(pool));
        validator.addMessage(
                "instantiatingGatewaysAndTriggeredStartEvents", related);
    }

    private void checkForMultiplePoolsContainingNoneStartEvents() {
        List<LaneableClusterAdaptor> poolsWithNoneStartEvent =
                new LinkedList<LaneableClusterAdaptor>();
        List<EventAdaptor> noneStartEventsList = new LinkedList<EventAdaptor>();
        for (LaneableClusterAdaptor pool : model.getPools()) {
            if (!noneStartEvents.get(pool).isEmpty()) {
                poolsWithNoneStartEvent.add(pool);
                noneStartEventsList.addAll(noneStartEvents.get(pool));
            }
        }
        if (poolsWithNoneStartEvent.size() > 1) {
            validator.addMessage(
                    "noneStartEventsInMultiplePools",
                    noneStartEventsList);
        }
    }

    private void checkForStartAndEndEventUsage() {
        for (ClusterAdaptor process : getProcessClusters()) {
            if (endEvents.get(process).isEmpty()
                    ^ (noneStartEvents.get(process).isEmpty()
                    && triggeredStartEvents.get(process).isEmpty()
                    && exclusiveInstantiatingGateways.get(process).isEmpty()
                    && parallelInstantiatingGateways.get(process).isEmpty())) {
                reportInconsistentStartEndEventUsage(process);
            }
        }
    }

    private void reportInconsistentStartEndEventUsage(ClusterAdaptor process) {
        if (process.isNull()) {
            LinkedList<NodeAdaptor> related =
                    new LinkedList<NodeAdaptor>(endEvents.get(process));
            related.addAll(noneStartEvents.get(process));
            related.addAll(triggeredStartEvents.get(process));
            validator.addMessage(
                    "inconsistentUsageOfStartAndEndEventsAtTopLevel",
                    related);
        } else {
            validator.addMessage(
                    "inconsistentUsageOfStartAndEndEvents",
                    process);
        }
    }

    private void checkForNodesWithoutIncommingOrOutgoingSequenceFlow() {
        for (ClusterAdaptor process : getProcessClusters()) {
            List<NodeAdaptor> nodesWithoutIncommingSequenceFlow =
                    nodesWithoutIncommingSequenceFlowIn(process),
                    nodesWithoutOutgoingSequenceFlow =
                    nodesWithoutOutgoingSequenceFlowIn(process);
            if (!nodesWithoutIncommingSequenceFlow.isEmpty()) {
                messageForNodesWithoutIncommingSequenceFlow(
                        nodesWithoutIncommingSequenceFlow, process);
            }
            if (!nodesWithoutOutgoingSequenceFlow.isEmpty()) {
                messageForNodesWithoutOutgoingSequenceFlow(
                        nodesWithoutOutgoingSequenceFlow, process);
            }
        }
    }

    private List<NodeAdaptor> nodesWithoutIncommingSequenceFlowIn(
            ClusterAdaptor process) {
        List<NodeAdaptor> nodesWithoutIncommingSequenceFlow =
                new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : process.getNodesOfContainedProcess()) {
            if (node.shouldHaveIncommingSequenceFlow()
                    && node.getIncomingSequenceFlow().isEmpty()
                    && !node.isEndEvent()) {//end-events are being ignored here,
                //because there is a special error for End-Events without incomming
                //SequenceFlow.
                nodesWithoutIncommingSequenceFlow.add(node);
            }
        }
        return nodesWithoutIncommingSequenceFlow;
    }

    private List<NodeAdaptor> nodesWithoutOutgoingSequenceFlowIn(
            ClusterAdaptor process) {
        List<NodeAdaptor> nodesWithoutOutgoingSequenceFlow =
                new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : process.getNodesOfContainedProcess()) {
            if (node.shouldHaveOutgoingSequenceFlow()
                    && node.getOutgoingSequenceFlow().isEmpty()
                    && !node.isStartEvent()) {
                //if a StartEvent has no outgoing SequenceFlow, an error will be
                //produced by the class, that checks StartEvents. One flaw should
                //not be mentioned twice.
                nodesWithoutOutgoingSequenceFlow.add(node);
            }
        }
        return nodesWithoutOutgoingSequenceFlow;
    }

    private void messageForNodesWithoutIncommingSequenceFlow(
            List<NodeAdaptor> nodesWithoutIncommingSequenceFlow,
            ClusterAdaptor process) {
        if (hasExplicitInstantiation(process)) {
            validator.addMessage("sometimesNoStartEventsUsed",
                    nodesWithoutIncommingSequenceFlow);
        } else if (!process.isSubProcess() || process.isEventSubProcess()) {
            validator.addMessage("noStartEventUsage",
                    nodesWithoutIncommingSequenceFlow);
        }
    }

    private boolean hasExplicitInstantiation(ClusterAdaptor process) {
        return !(noneStartEvents.get(process).isEmpty()
                && triggeredStartEvents.get(process).isEmpty()
                && exclusiveInstantiatingGateways.get(process).isEmpty()
                && parallelInstantiatingGateways.get(process).isEmpty());
    }

    private void messageForNodesWithoutOutgoingSequenceFlow(
            List<NodeAdaptor> nodesWithoutOutgoingSequenceFlow,
            ClusterAdaptor process) {
        if (!endEvents.get(process).isEmpty()) {
            validator.addMessage("sometimesNoEndEventsUsed",
                    nodesWithoutOutgoingSequenceFlow);
        } else if (!process.isSubProcess() || process.isEventSubProcess()) {
            validator.addMessage("noEndEventUsage",
                    nodesWithoutOutgoingSequenceFlow);
        }
    }

    private void findStartAndEndEventsAndInstantiatingGateways() {
        createMappingsForProcesses();
        for (ClusterAdaptor process : getProcessClusters()) {
            for (NodeAdaptor node : process.getNodesOfContainedProcess()) {
                findStartAndEndEventsAndInstantiatingGatewaysFor(node, process);
            }
        }
    }

    private void findStartAndEndEventsAndInstantiatingGatewaysFor(
            NodeAdaptor node, ClusterAdaptor process) {
        if (node.isStartEvent()) {
            if (node.isNoneStartEvent()) {
                noneStartEvents.get(process).add((EventAdaptor) node);
            } else {
                triggeredStartEvents.get(process).add((EventAdaptor) node);
            }
        } else if (node.isEndEvent()) {
            endEvents.get(process).add((EventAdaptor) node);
        } else if (node.isInstantiatingGateway()) {
            if (node.isExclusiveInstantiatingGateway()) {
                exclusiveInstantiatingGateways.get(process).add(node);
            } else {
                parallelInstantiatingGateways.get(process).add(node);
            }
        }
    }

    private void createMappingsForProcesses() {
        createMaps();
        for (ClusterAdaptor process : getProcessClusters()) {
            noneStartEvents.put(process, new LinkedList<EventAdaptor>());
            triggeredStartEvents.put(process, new LinkedList<EventAdaptor>());
            endEvents.put(process, new LinkedList<EventAdaptor>());
            exclusiveInstantiatingGateways.put(process, new LinkedList<NodeAdaptor>());
            parallelInstantiatingGateways.put(process, new LinkedList<NodeAdaptor>());
        }
    }

    private void createMaps() {
        noneStartEvents =
                new HashMap<ClusterAdaptor, List<EventAdaptor>>();
        triggeredStartEvents =
                new HashMap<ClusterAdaptor, List<EventAdaptor>>();
        endEvents = new HashMap<ClusterAdaptor, List<EventAdaptor>>();
        exclusiveInstantiatingGateways =
                new HashMap<ClusterAdaptor, List<NodeAdaptor>>();
        parallelInstantiatingGateways =
                new HashMap<ClusterAdaptor, List<NodeAdaptor>>();
    }

    private List<ClusterAdaptor> getProcessClusters() {
        List<ClusterAdaptor> processes =
                new LinkedList<ClusterAdaptor>(model.getPools());
        for (ClusterAdaptor cluster : model.getClusters()) {
            if (!(cluster.isLaneableCluster() || cluster.isAdHocSubProcess())) {
                processes.add(cluster);
            }
        }
        return processes;
    }
}
