/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography;

import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder.FlowDirection;
import com.inubit.research.gui.plugins.choreography.interfaceGenerator.MessageFlowWithEnvelope;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographyActivity;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.ComplexGateway;
import net.frapu.code.visualization.bpmn.ConditionalIntermediateEvent;
import net.frapu.code.visualization.bpmn.ConditionalStartEvent;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.LinkIntermediateEvent;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.MultipleIntermediateEvent;
import net.frapu.code.visualization.bpmn.MultipleStartEvent;
import net.frapu.code.visualization.bpmn.ParallelGateway;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SignalIntermediateEvent;
import net.frapu.code.visualization.bpmn.SignalStartEvent;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;

/**
 * Not for public use! A helper class with commonly used methods for the package
 * com.inubit.research.gui.plugins.choreography<br />
 * This class must be public, because it is needed in subpackages
 * @author tmi
 */
public class Utils {

    public static boolean isNode(ProcessObject object) {
        return object instanceof ProcessNode;
    }

    public static boolean isEdge(ProcessObject object) {
        return object instanceof ProcessEdge;
    }

    public static boolean isIntermediateEvent(ProcessObject node) {
        return node instanceof IntermediateEvent;
    }

    public static boolean isIntermediateEvent(AttachedNode node) {
        return node instanceof IntermediateEvent;
    }

    public static boolean isNonEmptyIntermediateEvent(ProcessNode node) {
        return isIntermediateEvent(node) && !isEmptyIntermediateEvent(node);
    }

    public static boolean isThrowingIntermediateEvent(ProcessNode node) {
        return isIntermediateEvent(node)
                && node.getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE).
                equals(IntermediateEvent.EVENT_SUBTYPE_THROWING);
    }

    public static boolean isChoreographyActivity(ProcessObject object) {
        return object instanceof ChoreographyActivity
                || object instanceof ChoreographySubProcess;
    }

    public static boolean isChoreographySubProcess(ProcessNode node) {
        return node instanceof ChoreographySubProcess;
    }

    public static boolean isChoreographyTask(ProcessNode node) {
        return node instanceof ChoreographyTask;
    }

    public static boolean isActivity(ProcessNode node) {
        return isTask(node) || isSubProcess(node);
    }

    public static boolean isTask(ProcessNode node) {
        return node instanceof Task;
    }

    public static boolean isSubProcess(ProcessNode node) {
        return node instanceof SubProcess;
    }

    public static boolean isEvent(ProcessNode node) {
        return node instanceof Event;
    }

    public static boolean isEndEvent(ProcessNode node) {
        return node instanceof EndEvent;
    }

    public static boolean isStartEvent(ProcessNode node) {
        return node instanceof StartEvent;
    }

    public static boolean isEmptyStartEvent(ProcessNode node) {
        return node.getClass().equals(StartEvent.class);
    }

    public static boolean isNonEmptyStartEvent(ProcessNode node) {
        return isStartEvent(node) && !isEmptyStartEvent(node);
    }

    public static boolean isMessageStartEvent(ProcessNode node) {
        return node instanceof MessageStartEvent;
    }

    public static boolean isParallelGateway(ProcessNode node) {
        return node instanceof ParallelGateway;
    }

    public static boolean isExclusiveGateway(ProcessNode node) {
        return node instanceof ExclusiveGateway
                || node.getClass().equals(Gateway.class);
    }

    public static boolean isEventBasedGateway(ProcessNode node) {
        return node instanceof EventBasedGateway;
    }

    public static boolean isInstantiatingGateway(ProcessNode node) {
        return isEventBasedGateway(node)
                && !node.getProperty(EventBasedGateway.PROP_INSTANTIATE).
                equals(EventBasedGateway.TYPE_INSTANTIATE_NONE);
    }

    public static boolean isInclusiveGateway(ProcessObject node) {
        return node instanceof InclusiveGateway;
    }

    public static boolean isComplexGateway(ProcessNode node) {
        return node instanceof ComplexGateway;
    }

    public static boolean isGateway(ProcessNode node) {
        return isParallelGateway(node) || isExclusiveGateway(node)
                || isEventBasedGateway(node) || isInclusiveGateway(node)
                || isComplexGateway(node);
    }

    public static boolean isMessage(ProcessNode node) {
        return node instanceof Message;
    }

    public static boolean isNonEmptyCluster(ProcessNode node) {
        return node instanceof Cluster
                && !((Cluster) node).getProcessNodes().isEmpty();
    }

    public static boolean isCluster(ProcessNode node) {
        return node instanceof Cluster;
    }

    public static boolean isPool(ProcessNode node) {
        return node instanceof Pool;
    }

    /**
     * checks, wheter node1 and node2 are instances of the same class
     */
    public static boolean sameType(ProcessNode node1, ProcessNode node2) {
        return node1.getClass().equals(node2.getClass());
    }

    public static boolean isSequenceFlow(ProcessEdge edge) {
        return edge instanceof SequenceFlow;
    }

    public static boolean isMessageFlowWithEnvelope(ProcessEdge edge) {
        return edge instanceof MessageFlowWithEnvelope;
    }

    /**
     * collects all the succeeding nodes of node in model, following LinkEvents
     * and ommitting them from the result.
     */
    public static Collection<ProcessNode> getSucceedingNodes(
            ProcessNode node, BPMNModel model) {
        Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
        nodes.addAll(removeLinksFrom(model.getSucceedingNodes(SequenceFlow.class, node)));
        nodes.addAll(successorsViaLinks(node, model));
        nodes.addAll(removeLinksFrom(attachedNodesAt(node, model)));
        return nodes;
    }

    /**
     * collects all the succeeding nodes in model of all nodes in the collection
     * nodes, following LinkEvents and ommitting them from the result.
     */
    public static Collection<ProcessNode> getSucceedingNodes(
            Collection<ProcessNode> nodes, BPMNModel model) {
        Collection<ProcessNode> result = new HashSet<ProcessNode>();
        for (ProcessNode node : nodes) {
            result.addAll(getSucceedingNodes(node, model));
        }
        return result;
    }

    /**
     * @return a copy of nodes, from which all LinkIntermediateEvents were removed
     */
    private static Collection<ProcessNode> removeLinksFrom(Collection<ProcessNode> nodes) {
        Collection<ProcessNode> result = new HashSet<ProcessNode>();
        for (ProcessNode node : nodes) {
            if (!(node instanceof LinkIntermediateEvent)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * collects all nodes, that follow node (according to SequenceFlow) via
     * LinkIntermediateEvents
     */
    private static Collection<ProcessNode> successorsViaLinks(
            ProcessNode node, BPMNModel model) {
        Collection<ProcessNode> links = getCorrespondingLinks(
                linkEventsIn(model.getSucceedingNodes(
                SequenceFlow.class, node),
                IntermediateEvent.EVENT_SUBTYPE_THROWING), model);
        Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
        for (ProcessNode link : links) {
            nodes.addAll(model.getSucceedingNodes(SequenceFlow.class, link));
        }
        return nodes;
    }

    /**
     * collects the LinkIntermediateEvents with the given type from the collection
     * nodes
     * @param type the event subtype - either Catching or Throwing
     */
    private static Collection<ProcessNode> linkEventsIn(
            Collection<ProcessNode> nodes, String type) {
        Collection<ProcessNode> links = new HashSet<ProcessNode>();
        for (ProcessNode node : nodes) {
            if (node instanceof LinkIntermediateEvent
                    && node.getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE).
                    equals(type)) {
                links.add(node);
            }
        }
        return links;
    }

    /**
     * Collects all LinkIntermediateEvents in model, that have the same text
     * and different type as one of the LinkIntermediateEvents in links
     * @param links a collection of LinkIntermediateEvents, for which corresponding
     * events should be found
     */
    private static Collection<ProcessNode> getCorrespondingLinks(
            Collection<ProcessNode> links, BPMNModel model) {
        Collection<ProcessNode> correspondingLinks = new HashSet<ProcessNode>();
        for (ProcessNode node : model.getNodes()) {
            for (ProcessNode link : links) {
                if ((!link.getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE).
                        equals(node.getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE)))
                        && node.getName().equals(link.getName())) {
                    correspondingLinks.add(node);
                    break;
                }
            }
        }
        return correspondingLinks;
    }

    public static Collection<ProcessNode> attachedNodesAt(
            ProcessNode node, BPMNModel model) {
        Collection<ProcessNode> attachedNodes = new HashSet<ProcessNode>();
        for (ProcessNode current : model.getNodes()) {
            if (isAttached(current, model)
                    && ((AttachedNode) current).getParentNode(model).equals(node)) {
                attachedNodes.add(current);
            }
        }
        return attachedNodes;
    }

    /**
     * collects all the preceding nodes of node in model, following LinkEvents
     * and ommitting them from the result.
     */
    public static Collection<ProcessNode> getPrecedingNodes(
            ProcessNode node, BPMNModel model) {
        Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
        if (isAttached(node, model)) {
            nodes.add(((AttachedNode) node).getParentNode(model));
            return nodes;
        }
        nodes.addAll(removeLinksFrom(model.getPrecedingNodes(SequenceFlow.class, node)));
        nodes.addAll(predecessorsViaLinks(node, model));
        return nodes;
    }

    /**
     * collects all nodes, that precede node (according to SequenceFlow) via
     * LinkIntermediateEvents
     */
    private static Collection<ProcessNode> predecessorsViaLinks(
            ProcessNode node, BPMNModel model) {
        Collection<ProcessNode> links = getCorrespondingLinks(
                linkEventsIn(model.getPrecedingNodes(
                SequenceFlow.class, node),
                IntermediateEvent.EVENT_SUBTYPE_CATCHING), model);
        Collection<ProcessNode> nodes = new HashSet<ProcessNode>();
        for (ProcessNode link : links) {
            nodes.addAll(model.getPrecedingNodes(SequenceFlow.class, link));
        }
        return nodes;
    }

    /**
     * a node is an implicit alternative split, if it is not a gateway and has
     * outgoing conditional SequenceFlow if direction is flowAfter or has
     * multiple incoming SequenceFlow and direction is flowBefore
     */
    public static boolean isImplicitAlternativeSplit(
            ProcessNode node, FlowDirection direction, BPMNModel model) {
        if (isGateway(node)) {
            return false;
        } else if (direction.equals(FlowDirection.flowAfter)) {
            for (ProcessEdge edge : model.getOutgoingEdges(SequenceFlow.class, node)) {
                if (edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                        equals(SequenceFlow.TYPE_CONDITIONAL)) {
                    return true;
                }
            }
            return false;
        } else {
            return model.getIncomingEdges(SequenceFlow.class, node).size() > 1;
        }
    }

    /**
     * a node is an implicit parallel split, if it is not a gateway and has
     * multiple outgoing SequenceFlow of which none is a conditional flow and
     * direction is flowAfter.
     */
    public static boolean isImplicitParallelSplit(
            ProcessNode node, FlowDirection direction, BPMNModel model) {
        if (isGateway(node)) {
            return false;
        } else if (direction.equals(FlowDirection.flowAfter)) {
            Collection<ProcessEdge> outgoing =
                    model.getOutgoingEdges(SequenceFlow.class, node);
            for (ProcessEdge edge : outgoing) {
                if (edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                        equals(SequenceFlow.TYPE_CONDITIONAL)) {
                    return false;
                }
            }
            return outgoing.size() > 1;
        } else {
            return false;
        }
    }

    public static boolean isParticipantOf(String name, ProcessNode node) {
        if (Utils.isChoreographyTask(node)) {
            return isParticipantOfChoreographyTask(name, (ChoreographyTask) node);
        } else if (Utils.isChoreographySubProcess(node)) {
            return isParticipantOfChoreographySubProcess(
                    name, (ChoreographySubProcess) node);
        } else {
            return false;
        }
    }

    private static boolean isParticipantOfChoreographySubProcess(
            String name, ChoreographySubProcess node) {
        for (String s : node.getProperty(
                ChoreographySubProcess.PROP_UPPER_PARTICIPANTS).split(";+")) {
            if (s.equals(name)) {
                return true;
            }
        }
        for (String s : node.getProperty(
                ChoreographySubProcess.PROP_LOWER_PARTICIPANTS).split(";+")) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isParticipantOfChoreographyTask(
            String name, ChoreographyTask node) {
        return node.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT).equals(name)
                || node.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT).equals(name);
    }

    public static Set<String> participantsOf(ProcessNode node) {
        Set<String> participants = new HashSet<String>();
        if (isChoreographyTask(node)) {
            participants.add(node.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT));
            participants.add(node.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT));
        } else if (isChoreographySubProcess(node)) {
            for (String curr : node.getProperty(
                    ChoreographySubProcess.PROP_UPPER_PARTICIPANTS).split(";+")) {
                if (!curr.isEmpty()) {
                    participants.add(curr);
                }
            }
            for (String curr : node.getProperty(
                    ChoreographySubProcess.PROP_LOWER_PARTICIPANTS).split(";+")) {
                if (!curr.isEmpty()) {
                    participants.add(curr);
                }
            }
        }
        return participants;
    }

    public static Set<String> participantsOf(Collection<ProcessNode> nodes) {
        Set<String> participants = new HashSet<String>();
        for (ProcessNode node : nodes) {
            participants.addAll(Utils.participantsOf(node));
        }
        return participants;
    }

    public static String initiatorOf(ProcessNode target) {
        if (Utils.isChoreographyTask(target)) {
            return target.getProperty(ChoreographyTask.PROP_ACTIVE_PARTICIPANT);
        } else {
            return target.getProperty(ChoreographySubProcess.PROP_ACTIVE_PARTICIPANTS);
        }
    }

    public static Set<String> initiatorsOf(Collection<ProcessNode> nodes) {
        Set<String> initiators = new HashSet<String>();
        for (ProcessNode node : nodes) {
            initiators.add(initiatorOf(node));
        }
        return initiators;
    }

    public static String receiverOf(ChoreographyTask task) {
        if (task.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT).
                equals(task.getProperty(ChoreographyTask.PROP_ACTIVE_PARTICIPANT))) {
            return task.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT);
        } else {
            return task.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT);
        }
    }

    public static boolean isMultipleParticipantOf(String participant, ProcessNode node) {
        if (isChoreographyTask(node)) {
            return (node.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT).
                    equals(participant)
                    && node.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT_MULTI).
                    equals(ChoreographyTask.TRUE))
                    || (node.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT).
                    equals(participant)
                    && node.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT_MULTI).
                    equals(ChoreographyTask.TRUE));
        } else if (isChoreographySubProcess(node)) {
            return isMultipleParticipantOfChoreographySubProcess(participant,
                    (ChoreographySubProcess) node);
        } else {
            return false;
        }
    }

    private static boolean isMultipleParticipantOfChoreographySubProcess(
            String participant, ChoreographySubProcess sub) {
        String[] participants = sub.getProperty(
                ChoreographySubProcess.PROP_UPPER_PARTICIPANTS).split(";+");
        String[] multi = sub.getProperty(
                ChoreographySubProcess.PROP_UPPER_PARTICIPANTS_MULTI).split(";+");
        if (isListedAsMultipleParticipantIn(participant, participants, multi)) {
            return true;
        }
        participants = sub.getProperty(
                ChoreographySubProcess.PROP_LOWER_PARTICIPANTS).split(";+");
        multi = sub.getProperty(
                ChoreographySubProcess.PROP_LOWER_PARTICIPANTS_MULTI).split(";+");
        return isListedAsMultipleParticipantIn(participant, participants, multi);
    }

    private static boolean isListedAsMultipleParticipantIn(
            String participant, String[] participants, String[] multi) {
        for (int i = 0; i < participants.length && i < multi.length; ++i) {
            if (participant.equals(participants[i])
                    && multi[i].equals(ChoreographySubProcess.TRUE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAttached(ProcessNode node, BPMNModel model) {
        if (!(node instanceof AttachedNode)) {
            return false;
        } else {
            return ((AttachedNode) node).getParentNode(model) != null;
        }
    }

    public static boolean isMessageIntermediateEvent(ProcessNode node) {
        return node instanceof MessageIntermediateEvent;
    }

    public static boolean isMessageIntermediateEvent(AttachedNode node) {
        return node instanceof MessageIntermediateEvent;
    }

    public static boolean isTimerIntermediateEvent(ProcessNode node) {
        return node instanceof TimerIntermediateEvent;
    }

    public static boolean isConditionalIntermediateEvent(ProcessNode node) {
        return node instanceof ConditionalIntermediateEvent;
    }

    public static boolean isSignalIntermediateEvent(ProcessNode node) {
        return node instanceof SignalIntermediateEvent;
    }

    public static boolean isMultipleIntermediateEvent(ProcessNode node) {
        return node instanceof MultipleIntermediateEvent;
    }

    public static boolean isEmptyIntermediateEvent(ProcessNode node) {
        return node.getClass().equals(IntermediateEvent.class);
    }

    public static boolean isLinkIntermediateEvent(ProcessNode node) {
        return node instanceof LinkIntermediateEvent;
    }

    public static boolean isCatchingLinkIntermediateEvent(ProcessNode node) {
        return isLinkIntermediateEvent(node)
                && node.getProperty(LinkIntermediateEvent.PROP_EVENT_SUBTYPE).
                equals(LinkIntermediateEvent.EVENT_SUBTYPE_CATCHING);
    }

    /**
     * copies all properties from from to to with exception of the id, the
     * sourcenode, the targetnode and the class type
     */
    public static void copyProperties(ProcessObject from, ProcessObject to) {
        for (String key : from.getPropertyKeys()) {
            if (!(key.equals(ProcessObject.PROP_ID)
                    || key.equals(ProcessEdge.PROP_SOURCENODE)
                    || key.equals(ProcessEdge.PROP_TARGETNODE)
                    || key.equals(ProcessObject.PROP_CLASS_TYPE))) {
                to.setProperty(key, from.getProperty(key));
            }
        }
    }

    /**
     * @return a StartEvent with the same kind of trigger as event
     */
    public static StartEvent correspondingStartEvent(IntermediateEvent event) {
        if (Utils.isMessageIntermediateEvent((ProcessNode) event)) {
            return new MessageStartEvent();
        } else if (Utils.isTimerIntermediateEvent(event)) {
            return new TimerStartEvent();
        } else if (Utils.isConditionalIntermediateEvent(event)) {
            return new ConditionalStartEvent();
        } else if (Utils.isSignalIntermediateEvent(event)) {
            return new SignalStartEvent();
        } else if (Utils.isMultipleIntermediateEvent(event)) {
            return new MultipleStartEvent();
        } else if (Utils.isEmptyIntermediateEvent(event)) {
            return new StartEvent();
        } else {
            return null;
        }
    }

    /**
     * collects all Activities, ChoreographyActivities, Gateways and Events in
     * model, that do not have any incoming SequenceFlow.
     */
    public static Collection<ProcessNode> relevantNodesWithoutIncomingSequenceFlow(
            BPMNModel model) {
        Collection<ProcessNode> result = new HashSet<ProcessNode>();
        for (ProcessNode node : model.getNodes()) {
            if (isRelevantNode(node)
                    && model.getIncomingEdges(SequenceFlow.class, node).isEmpty()
                    && !isAttached(node, model)
                    && !(isSubProcess(model.getClusterForNode(node))
                    || isChoreographySubProcess(model.getClusterForNode(node)))) {
                result.add(node);
            }
        }
        return result;
    }

    private static boolean isRelevantNode(ProcessNode node) {
        return isActivity(node) || isChoreographyActivity(node)
                || isEvent(node) || isGateway(node);
    }
}
