/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import com.inubit.research.gui.plugins.choreography.Utils;
import com.inubit.research.gui.plugins.choreography.branchingTree.BranchingTree;
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import com.inubit.research.gui.plugins.choreography.branchingTree.
        TreeBuilder.FlowDirection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.Task;

/**
 * Generates ProcessNodes in the colaboration from a ChoreographyTask and its
 * attached nodes.
 * @author tmi
 */
class TaskTranslator extends AbstractGenerator {

  private Collection<ProcessNode> attachedNodes;
  private ChoreographyTask task;
  private String initiator, receiver, initialMessage, reply;
  private StartEvent receiverStart = null;
  private ProcessNode receiveInitial = null;
  private Task initiatorTask = null, sendReply = null;
  private boolean mayBeStartForReceiver, mayBeStartForInitiator,
          isStartForReceiver, isLoop, useMessageFlowWithEnvelope;
  private Map<String, Map<String, StartEvent>> startEvents;

  public TaskTranslator(BPMNModel choreography,
          BPMNModel colaboration,
          ChoreographyTask task,
          Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools,
          Map<String, Map<String, StartEvent>> startEvents,
          boolean useMessageFlowWithEnvelope) {
    super(firstNode, lastNode, allNodes, pools, choreography, colaboration);
    this.task = task;
    attachedNodes = Utils.attachedNodesAt(task, choreography);
    initiator = Utils.initiatorOf(task);
    receiver = Utils.receiverOf(task);
    determineMessageNames();
    checkForStart();
    isLoop = !task.getProperty(ChoreographyTask.PROP_LOOP_TYPE).
            equals(ChoreographyTask.LOOP_NONE);
    this.startEvents = startEvents;
    this.useMessageFlowWithEnvelope = useMessageFlowWithEnvelope;
  }

  /**
   * Finds out about the names of the initiating message and the reply (if there
   * is any). The names are determined from the associated Messages or, if there
   * is no associated initiating message, from the task´s text.
   */
  private void determineMessageNames() {
    for (ProcessNode node : choreography.getNeighbourNodes(Association.class, task)) {
      if (Utils.isMessage(node)) {
        if (node.getProperty(Message.PROP_INITIATE).equals(Message.INITIATE_TRUE)) {
          initialMessage = node.getText();
        } else {
          reply = node.getText();
        }
      }
    }
    if (initialMessage == null) {
      initialMessage = task.getText();
    }
  }

  /**
   * Determines, wheter this task may be the initial node for the receiver or
   * sender and stores the determined values in the corresponding fields
   */
  private void checkForStart() {
    BranchingTree precedingInteraction =
            (new TreeBuilder(choreography)).buildTreeFor(task, FlowDirection.flowBefore);
    precedingInteraction.eraseFirstChoreographyActivity();
    boolean noNonEmptyStartEvents =
            precedingInteraction.noPathesContainNonEmptyStartEvent();
    mayBeStartForReceiver = !precedingInteraction.
            allAlternativesContainNonEmptyNonEndEventOrInvolve(receiver);
    mayBeStartForInitiator = !precedingInteraction.
            allAlternativesContainNonEmptyNonEndEventOrInvolve(initiator);
    isStartForReceiver = precedingInteraction.noAlternativesInvolve(receiver)
            && noNonEmptyStartEvents;
  }

  @Override
  public void generate() {
    generateInitiatorTask();
    receiveNodesForInitialMessage();
    sendTaskForReply();
    messageFlow();
    attachedEvents();
  }

  /**
   * Generates a Task (send or service) for the receiver.
   */
  private void generateInitiatorTask() {
    initiatorTask = new Task(task.getPos().x, task.getPos().y, task.getText());
    initiatorTask.setProperty(Task.PROP_LOOP_TYPE,
            translateLoopType(task.getProperty(ChoreographyTask.PROP_LOOP_TYPE)));
    if (reply == null) {
      initiatorTask.setStereotype(Task.TYPE_SEND);
    } else {
      initiatorTask.setStereotype(Task.TYPE_SERVICE);
    }
    addToPoolRegistered(initiatorTask, initiator);
    registerFirstNode(initiator, initiatorTask);
    registerLastNode(initiator, initiatorTask);
  }

  /**
   * Creates the nodes concerned with receiving the initial message.
   */
  private void receiveNodesForInitialMessage() {
    if (mayBeStartForReceiver) {
      createReceiverStart();
    }
    if (isLoop) {
      createInitialReceiveLoopTask();
    } else if (!isStartForReceiver) {
      createInitialReceiveEvent();
    }
  }

  /**
   * creates a StartEvent in the receiver´s pool. It will be a plain StartEvent,
   * if the task is a loop and a MessageStartEvent otherwise.
   */
  private void createReceiverStart() {
    if (isLoop) {
      receiverStart = new StartEvent();
    } else {
      receiverStart = new MessageStartEvent();
      receiverStart.setText(initialMessage);
    }
    addToPoolRegistered(receiverStart, receiver);
    registerFirstNode(receiver, receiverStart);
    registerLastNode(receiver, receiverStart);
    registerStartEvent(receiver, receiverStart);
  }

  /**
   * creates a loop-Task for receiving the initial message
   */
  private void createInitialReceiveLoopTask() {
    receiveInitial = new Task();
    receiveInitial.setProperty(
            Task.PROP_LOOP_TYPE,
            translateLoopType(task.getProperty(ChoreographyTask.PROP_LOOP_TYPE)));
    receiveInitial.setText(initialMessage);
    receiveInitial.setStereotype(Task.TYPE_RECEIVE);
    addToPoolRegistered(receiveInitial, receiver);
    if (!isStartForReceiver) {
      registerFirstNode(receiver, receiveInitial);
    }
    registerLastNode(receiver, receiveInitial);
    if (mayBeStartForReceiver) {
      colaboration.addEdge(new SequenceFlow(receiverStart, receiveInitial));
    }
  }

  /**
   * creates a MessageIntermediateEvent for receiving the initial message
   */
  private void createInitialReceiveEvent() {
    receiveInitial = new MessageIntermediateEvent();
    receiveInitial.setText(initialMessage);
    addToPoolRegistered(receiveInitial, receiver);
    registerFirstNode(receiver, receiveInitial);
    //(simply override prior registration, if any)
    registerLastNode(receiver, receiveInitial);
    if (mayBeStartForReceiver) {
      ExclusiveGateway gateway = new ExclusiveGateway();
      addToPoolRegistered(gateway, receiver);
      colaboration.addEdge(new SequenceFlow(receiveInitial, gateway));
      colaboration.addEdge(new SequenceFlow(receiverStart, gateway));
      registerLastNode(receiver, gateway);
    }
  }

  /**
   * creates a Task for sending the reply (if any)
   */
  private void sendTaskForReply() {
    if (reply == null) {
      return;
    }
    sendReply = new Task(task.getPos().x, task.getPos().y, reply);
    addToPoolRegistered(sendReply, receiver);
    sendReply.setStereotype(Task.TYPE_SEND);
    colaboration.addEdge(new SequenceFlow(
            lastNode.get(task.getId()).get(receiver), sendReply));
    registerLastNode(receiver, sendReply);
  }

  /**
   * creates the MessageFlow for the task
   */
  private void messageFlow() {
    if (receiveInitial != null) {
      messageFlow(initiatorTask, receiveInitial, initialMessage, true);
    }
    if (mayBeStartForReceiver && !isLoop) {
      messageFlow(initiatorTask, receiverStart, initialMessage, true);
    }
    if (sendReply != null) {
      messageFlow(sendReply, initiatorTask, reply, false);
    }
  }

  /**
   * creates the nodes, that realize the attached Events of the task
   */
  private void attachedEvents() {
    for (ProcessNode node : attachedNodes) {
      if (Utils.isMessageIntermediateEvent(node)) {
        handleMessageIntermediateEvent((MessageIntermediateEvent) node);
      } else if (Utils.isIntermediateEvent(node)) {
        handleNonMessageIntermediateEvent((IntermediateEvent) node);
      }
    }
  }

  /**
   * generates the nodes, that are necessary to realize an attached
   * MessageIntermediateEvent
   */
  private void handleMessageIntermediateEvent(MessageIntermediateEvent event) {
    ProcessNode send;
    Collection<ProcessNode> receive;
    boolean sentFromInitiator = sentFromInitiator(event);
    if (sentFromInitiator) {
      send = handleMessageIntermediateSendForInitiator(event);
      receive = addAlternativeEventsForInitialReceive(event);
    } else {
      receive = new HashSet<ProcessNode>(1);
      receive.add(handleMessageIntermediateReceiveForInitiator(event));
      send = handleMessageIntermediateSendForReceiver(event);
    }
    for (ProcessNode receiveNode : receive) {
      messageFlow(send, receiveNode, event.getText(), sentFromInitiator);
    }
  }

  /**
   * determines, wheter the message of an attached MessageIntermediateEvent is
   * sent from the initiator of the task.
   */
  private boolean sentFromInitiator(MessageIntermediateEvent event) {
    if (task.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT).equals(initiator)) {
      return event.getPos().y > task.getPos().y;
    } else {
      return event.getPos().y < task.getPos().y;
    }
  }

  /**
   * Generates the nodes in the pool of the tasks´s initiator, that are
   * necessary to realize an attached MessageIntermediateEvent, that was send
   * from the initiator of the task.
   * @return the send task that was generated for the event
   */
  private Task handleMessageIntermediateSendForInitiator(
          MessageIntermediateEvent event) {
    ProcessNode decision = new ExclusiveGateway();
    insertNodeBefore(decision, initiatorTask, initiator);
    registerNode(event, initiator, decision);
    if (!mayBeStartForInitiator) {
      registerFirstNode(initiator, decision);
    }
    Task newSend = new Task(event.getPos().x, event.getPos().y, event.getText());
    newSend.setStereotype(Task.TYPE_SEND);
    addToPoolRegistered(newSend, initiator, event);
    colaboration.addEdge(new SequenceFlow(decision, newSend));
    registerFirstNode(event, initiator, newSend);
    registerLastNode(event, initiator, newSend);
    return newSend;
  }

  /**
   * For an attached IntermediateEvent, adds alternatives to receiving the
   * initial message (as well intermediate receive as start) to the pool of the
   * task´s receiver
   */
  private Collection<ProcessNode> addAlternativeEventsForInitialReceive(
          IntermediateEvent event) {
    Collection<ProcessNode> events = new HashSet<ProcessNode>(2);
    if (!isStartForReceiver) {
      events.add(addAlternativeIntermediateEventForReceivingInitialMessage(event));
    }
    if (mayBeStartForReceiver) {
      events.add(addAlternativeStartEventToReceivingInitialMessage(event));
    }
    return events;
  }

  /**
   * Adds the possibility of intermediately receiving what an attached event
   * specified instead of the initial message
   * @param event the attached IntermediateEvent
   */
  private ProcessNode addAlternativeIntermediateEventForReceivingInitialMessage(
          IntermediateEvent event) {
    EventBasedGateway gateway = new EventBasedGateway();
    insertNodeBefore(gateway, firstNode.get(task.getId()).get(receiver), receiver);
    registerNode(receiver, gateway);
    registerFirstNode(receiver, gateway);
    ProcessNode catchEvent = event.copy();
    addToPoolRegistered(catchEvent, receiver, event);
    colaboration.addEdge(new SequenceFlow(gateway, catchEvent));
    registerFirstNode(event, receiver, catchEvent);
    registerLastNode(event, receiver, catchEvent);
    return catchEvent;
  }

  /**
   * Adds the possibility of receiving what an attached event specified instead
   * of the initial message (as StartEvent)
   * @param event the attached IntermediateEvent
   */
  private Event addAlternativeStartEventToReceivingInitialMessage(
          IntermediateEvent event) {
    StartEvent start = Utils.correspondingStartEvent(event);
    start.setText(event.getText());
    start.setPos(event.getPos());
    addToPoolRegistered(start, receiver, event);
    registerStartEvent(event, receiver, start);
    if (!isStartForReceiver) {
      Gateway gateway = new ExclusiveGateway();
      addToPoolRegistered(gateway, receiver, event);
      colaboration.addEdge(new SequenceFlow(start, gateway));
      colaboration.addEdge(new SequenceFlow(
              lastNode.get(event.getId()).get(receiver), gateway));
      registerLastNode(event, receiver, gateway);
    } else {
      registerLastNode(event, receiver, start);
    }
    return start;
  }

  /**
   * adds an attached IntermediateEvent to the initiator´s task in order to realize
   * an attached IntermediateEvent, that he receives
   */
  private MessageIntermediateEvent handleMessageIntermediateReceiveForInitiator(
          MessageIntermediateEvent event) {
    MessageIntermediateEvent receive = (MessageIntermediateEvent) event.copy();
    receive.setText(event.getText());
    addToPoolRegistered(receive, initiator, event);
    receive.setParentNode(initiatorTask);
    registerFirstNode(event, initiator, receive);
    registerLastNode(event, initiator, receive);
    return receive;
  }

  /**
   * gives the task´s receiver the chance to send the message of an attached
   * MessageIntermediateEvent instead of the usual reply-message (resp. instead
   * of no reply).
   */
  private Task handleMessageIntermediateSendForReceiver(
          MessageIntermediateEvent event) {
    ProcessNode decision = new ExclusiveGateway();
    insertNodeBefore(decision, sendReply, receiver);
    registerNode(receiver, decision);
    if (reply == null) {
      registerLastNode(receiver, decision);
    }
    Task newSend = new Task(event.getPos().x, event.getPos().y, event.getText());
    newSend.setStereotype(Task.TYPE_SEND);
    addToPoolRegistered(newSend, receiver, event);
    colaboration.addEdge(new SequenceFlow(decision, newSend));
    registerFirstNode(event, receiver, newSend);
    registerLastNode(event, receiver, newSend);
    return newSend;
  }

  /**
   * inserts insertNode before (in terms of SequenceFlow) beforeNode in the pool
   * of participant. This means, that it will add insertNode to the colaboration
   * and the pool of participant, it will change the target of all incoming
   * SequenceFlow of beforeNode to insertNode and add SequenceFlow from
   * insertNode to beforeNode.
   */
  private void insertNodeBefore(
          ProcessNode insertNode, ProcessNode beforeNode, String participant) {
    addToPool(insertNode, participant);
    if (beforeNode != null) {
      for (ProcessEdge edge :
        colaboration.getIncomingEdges(SequenceFlow.class, beforeNode)) {
        edge.setTarget(insertNode);
      }
      colaboration.addEdge(new SequenceFlow(insertNode, beforeNode));
    }
  }

  /**
   * creates all nodes, that are necessary in order to realize an attached
   * IntermediateEvent, that is not a MessageIntermediateEvent
   */
  private void handleNonMessageIntermediateEvent(IntermediateEvent event) {
    Collection<ProcessNode> receiverEvents =
            addAlternativeEventsForInitialReceive(event);
    registerStartEventAndFirstNodeIn(receiverEvents, event);
    addReceiverIntermediateEventToReplyTask(event);
    addInitiatorAttachedEvent(event);
  }

  /**
   * registers an IntermediateEvent out of events as firstNode (if there is any
   * IntermediateEvent, otherwise registers a StartEvent out of events as
   * firstNode) and a StartEvent as StartEvent (if any)
   * @param events the set of events, that must contain at least one Intermediate-
   * or StartEvent and should contain at most one IntermediateEvent and at most
   * one StartEvent
   * @param forEvent the event, for which the firstNode and startEvent shall
   * be registered
   */
  private void registerStartEventAndFirstNodeIn(
          Collection<ProcessNode> events, IntermediateEvent forEvent) {
    ProcessNode intermediateEvent = null;
    StartEvent startEvent = null;
    for (ProcessNode node : events) {
      if (Utils.isIntermediateEvent(node)) {
        intermediateEvent = node;
      } else if (Utils.isStartEvent(node)) {
        startEvent = (StartEvent) node;
      }
    }
    registerFirstNode(forEvent, receiver, intermediateEvent != null ?
          intermediateEvent : startEvent);
    if (startEvent != null) {
      registerStartEvent(forEvent, receiver, startEvent);
    }
  }

  /**
   * For an attached IntermediateEvent (no MessageIntermediateEvent), adds an
   * attached Event to the receiver´s reply task (if there is any) and joins
   * its outgoing flow with this of a previously registered lastNode of the
   * attached Event.
   */
  private void addReceiverIntermediateEventToReplyTask(IntermediateEvent event) {
    if (sendReply != null) {
      IntermediateEvent secondEvent = (IntermediateEvent) event.copy();
      addToPoolRegistered(secondEvent, receiver, event);
      secondEvent.setParentNode(sendReply);
      Gateway join = new ExclusiveGateway();
      addToPoolRegistered(join, receiver, event);
      colaboration.addEdge(new SequenceFlow(
              lastNode.get(event.getId()).get(receiver), join));
      colaboration.addEdge(new SequenceFlow(secondEvent, join));
      registerLastNode(event, receiver, join);
    }
  }

  /**
   * Adds a copy of originalEvent as an attached Event to the initiatorTask
   */
  private void addInitiatorAttachedEvent(IntermediateEvent originalEvent) {
    IntermediateEvent initiatorEvent = (IntermediateEvent) originalEvent.copy();
    addToPoolRegistered(initiatorEvent, initiator, originalEvent);
    initiatorEvent.setParentNode(initiatorTask);
    registerFirstNode(originalEvent, initiator, initiatorEvent);
    registerLastNode(originalEvent, initiator, initiatorEvent);
  }

  /**
   * creates a new MessageFlow or MessageFlowWithEnvelope (according to the value
   * of useMessageFlowWithEnvelope) from source to target.
   * @param source the new MessageFlow´s source
   * @param target the new MessageFlow´s target
   * @param label the label of the MessageFlow (if no Envelope wanted) or the
   * text of the envelope
   * @param initiating the initiating-property of the envelope; this will be
   * ignored if useMessageFlowWithEnvelope is false
   */
  private void messageFlow(ProcessNode source, ProcessNode target,
          String label, boolean initiating) {
    if (useMessageFlowWithEnvelope) {
      MessageFlowWithEnvelope flow =
              new MessageFlowWithEnvelope(source, target, label, initiating);
      colaboration.addEdge(flow);
    } else {
      MessageFlow flow = new MessageFlow(source, target);
      flow.setLabel(label);
      colaboration.addEdge(flow);
    }
  }

  private void registerFirstNode(String participant, ProcessNode node) {
    registerFirstNode(task, participant, node);
  }

  private void registerLastNode(String participant, ProcessNode node) {
    registerLastNode(task, participant, node);
  }

  private void registerNode(String participant, ProcessNode node) {
    registerNode(task, participant, node);
  }

  private void registerStartEvent(String participant, StartEvent start) {
    registerStartEvent(task, participant, start);
  }

  private void registerStartEvent(
          ProcessNode forNode, String participant, StartEvent start) {
    if (!startEvents.containsKey(forNode.getId())) {
      startEvents.put(forNode.getId(), new HashMap<String, StartEvent>());
    }
    startEvents.get(forNode.getId()).put(participant, start);
  }

  private void addToPoolRegistered(ProcessNode node, String participant) {
    addToPoolRegistered(node, participant, task);
  }

  /**
   * translates the value of the ChoreographyTask-property loop to the value
   * of the Task-property loop
   */
  private String translateLoopType(String choreographyLoopType) {
    if (choreographyLoopType.equals(ChoreographyTask.LOOP_MULTI_INSTANCE)) {
      return Task.LOOP_MULTI_PARALLEL;
    } else if (choreographyLoopType.equals(ChoreographyTask.LOOP_STANDARD)) {
      return Task.LOOP_STANDARD;
    } else if (choreographyLoopType.equals(ChoreographyTask.LOOP_NONE)) {
      return receiverIsMultiple() ? Task.LOOP_MULTI_PARALLEL : Task.LOOP_NONE;
    } else {
      return choreographyLoopType;
    }
  }

  private boolean receiverIsMultiple() {
    return Utils.isMultipleParticipantOf(receiver, task);
  }
}
