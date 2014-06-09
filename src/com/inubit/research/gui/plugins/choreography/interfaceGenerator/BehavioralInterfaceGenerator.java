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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 *
 * @author tmi
 */
public class BehavioralInterfaceGenerator {

  private BPMNModel choreography,
          colaboration = null;
  private Map<String, Pool> pools;
  /**
   * gives, for each node-id, a mapping from a participant´s name to the first
   * of the nodes (i.e. the node, to which inbound sequence flow should be
   * routed), which relize the node with the given ID.
   */
  private Map<String, Map<String, ProcessNode>> firstNode =
          new HashMap<String, Map<String, ProcessNode>>();
  /**
   * gives, for each node-id, a mapping from a participant´s name to the first
   * of the nodes (i.e. the node, from which outgoing sequence flow should
   * emerge), which relize the node with the given ID.
   */
  private Map<String, Map<String, ProcessNode>> lastNode =
          new HashMap<String, Map<String, ProcessNode>>();
  /**
   * gives, for each node-id, a mapping from a participant´s name to all the
   * nodes, that jointly realize the node with the given ID for this participant
   */
  private Map<String, Map<String, Collection<ProcessNode>>> allNodes =
          new HashMap<String, Map<String, Collection<ProcessNode>>>();

  /**
   * maps from choreography-node-ids to a mapping from participant names to
   * the StartEvent, that realizes the given node for the given participant.
   */
  private Map<String, Map<String, StartEvent>> startEvents =
          new HashMap<String, Map<String, StartEvent>>();

  /**
   * those participants, who were selected to be generated in detail
   */
  private Collection<String> selectedParticipants;
  private boolean useMessageFlowWithEnvelope,
          avoidImplicitSplitsAndJoins;

  /**
   * creates a new instance of BehavioralInterfaceGenerator. The colaboration
   * will not be generated when calling this constructor, but when calling
   * getBehavioralInterface for the first time or when calling generateInterface().
   * @param choreography the choreography model, from wich the colaboration should
   * be generated
   * @param selectedParticipants the participants, whose pools should be shown
   * as white-box pools
   * @param useMessageFlowWithEnvelope determines, wheter the edge-type
   * MessageFlowWithEnvelope or usual MessageFlow should be used.
   */
  public BehavioralInterfaceGenerator(
          BPMNModel choreography,
          Collection<String> selectedParticipants,
          boolean useMessageFlowWithEnvelope,
          boolean avoidImplicitSplitsAndJoins) {
    this.choreography = choreography;
    this.selectedParticipants = selectedParticipants;
    this.useMessageFlowWithEnvelope = useMessageFlowWithEnvelope;
    this.avoidImplicitSplitsAndJoins = avoidImplicitSplitsAndJoins;
  }

  /**
   * returns the generated behvioral interface (generates it before, if not done
   * yet)
   */
  public BPMNModel getBehavioralInterface() {
    if (colaboration == null) {
      generateInterface();
    }
    return colaboration;
  }

  public void generateInterface() {
    colaboration = new BPMNModel("Generated Behavioral interface");
    generatePools();
    generateTasks();
    generateEvents();
    generateGateways();
    createSubProcesses();
    generateSequenceflow();
    generateInstantiatingGateways();
    correctBehavioralInterface();
    hidePoolDetails();
  }

  private void generatePools() {
    Collection<String> participants =
            Utils.participantsOf(choreography.getNodes());
    pools = new HashMap<String, Pool>(participants.size());
    for (String participant : participants) {
      Pool pool = new Pool(0, 0, participant);
      pools.put(participant, pool);
      colaboration.addNode(pool);
      if(isMultipleInstanceParticipant(participant)) {
        pool.setProperty(Pool.PROP_MULTI_INSTANCE, Pool.TRUE);
      }
    }
  }

  private boolean isMultipleInstanceParticipant(String participant) {
    for(ProcessNode node : choreography.getNodes()) {
      if(Utils.isMultipleParticipantOf(participant, node)) return true;
    }
    return false;
  }

  private void generateTasks() {
    for (ProcessNode node : choreography.getNodes()) {
      if (Utils.isChoreographyTask(node)) {
        new TaskTranslator(choreography, colaboration, (ChoreographyTask) node,
                firstNode, lastNode, allNodes, pools, startEvents, useMessageFlowWithEnvelope).
                generate();
      }
    }
  }

  private void generateEvents() {
    for (ProcessNode node : choreography.getNodes()) {
      if (Utils.isIntermediateEvent(node)) {
        handleIntermediateEvent((IntermediateEvent) node);
      } else if (Utils.isEndEvent(node) || Utils.isNonEmptyStartEvent(node)) {
        generateStartOrEndEvent((Event) node);
      }
    }
  }

  /**
   * does the generating for an IntermediateEvent from the choreography diagram
   */
  private void handleIntermediateEvent(IntermediateEvent event) {
    if (!Utils.isAttached(event, choreography)) {
      //Attached events have already been translated by TaskTranslator
      (new IntermediateEventGenerator(choreography, colaboration, event,
              firstNode, lastNode, startEvents, allNodes, pools)).generate();
    }
  }

  /**
   * does the generating for a Start- or EndEvent from the choreography
   */
  private void generateStartOrEndEvent(Event event) {
    for (Pool pool : pools.values()) {
      Event copy = (Event) event.copy();
      colaboration.addNode(copy);
      pool.addProcessNode(copy);
      registerFirstNode(event.getId(), pool.getText(), copy);
      registerLastNode(event.getId(), pool.getText(), copy);
      registerNode(event.getId(), pool.getText(), copy);
      if(Utils.isStartEvent(copy)) {
        registerStartEvent(event.getId(), pool.getText(), (StartEvent)copy);
      }
    }
  }

  private void generateGateways() {
    for (ProcessNode node : choreography.getNodes()) {
      if (Utils.isGateway(node)) {
        (new GatewayGenerator(choreography, colaboration, (Gateway) node,
                firstNode, lastNode, allNodes, pools)).generate();
      }
    }
  }

  private void generateSequenceflow() {
    for (ProcessEdge edge : choreography.getEdges()) {
      if (Utils.isSequenceFlow(edge)) {
        (new SequenceFlowGenerator(firstNode, lastNode, allNodes, pools, choreography,
                colaboration, (SequenceFlow) edge)).generate();
      }
    }
  }

  private void registerFirstNode(String id, String participant, ProcessNode node) {
    registerAt(id, participant, node, firstNode);
  }

  private void registerLastNode(String id, String participant, ProcessNode node) {
    registerAt(id, participant, node, lastNode);
  }

  /**
   * registers, that node is some node concerned with relizing the node with id
   * for participant.
   */
  private void registerNode(String id, String participant, ProcessNode node) {
    if(!allNodes.containsKey(id)) {
      allNodes.put(id, new HashMap<String, Collection<ProcessNode>>());
    }
    if(!allNodes.get(id).containsKey(participant)) {
      allNodes.get(id).put(participant, new HashSet<ProcessNode>());
    }
    allNodes.get(id).get(participant).add(node);
  }

  private void registerAt(String id, String participant, ProcessNode node,
          Map<String, Map<String, ProcessNode>> registry) {
    if (!registry.containsKey(id)) {
      registry.put(id, new HashMap<String, ProcessNode>());
    }
    registry.get(id).put(participant, node);
  }

  private void registerStartEvent(String id, String participant, StartEvent start) {
    if(!startEvents.containsKey(id)) {
      startEvents.put(id, new HashMap<String, StartEvent>());
    }
    startEvents.get(id).put(participant, start);
  }

  private void createSubProcesses() {
    /*have to take care, that a cluster cannot be generated before all of its
     contained nodes were generated => if a SubProcess contains other
     ChoreographySubProcesses, that are not yet realized, first realize them*/
    Collection<Cluster> clusters = choreography.getClusters();
    while (!clusters.isEmpty()) {
      Collection<Cluster> postponedSubProcesses = new HashSet<Cluster>();
      for (Cluster cluster : clusters) {
        if (Utils.isChoreographySubProcess(cluster)) {
          if (containsUnrealizedSubProcesses(cluster)) {
            postponedSubProcesses.add(cluster);
          } else {
            allNodes.put(cluster.getId(), new HashMap<String, Collection<ProcessNode>>());
            (new SubProcessGenerator(
                    firstNode, lastNode, allNodes, pools,
                    choreography, colaboration, (ChoreographySubProcess) cluster)).
                    generate();
          }
        }
      }
      clusters = postponedSubProcesses;
    }
  }

  private boolean containsUnrealizedSubProcesses(Cluster cluster) {
    for (ProcessNode node : cluster.getProcessNodes()) {
      if (Utils.isChoreographySubProcess(node)
              && (!allNodes.containsKey(node.getId())) && node != cluster) {
        return true;
      }
    }
    return false;
  }

  /**
   * performs some corrections (and improvements) on the generated colaboration,
   * whithout paying respect to the choreography any longer
   */
  private void correctBehavioralInterface() {
    (new Corrector(colaboration, avoidImplicitSplitsAndJoins)).
            correctAndOptimize();
  }

  /**
   * generates StartEvents, that have not yet been generated and instantiating
   * gateways
   */
  private void generateInstantiatingGateways() {
    for(ProcessNode node :
          Utils.relevantNodesWithoutIncomingSequenceFlow(choreography)) {
      BranchingTree tree = (new TreeBuilder(choreography)).
              buildTreeFor(node, TreeBuilder.FlowDirection.flowAfter);
      for(String participant : pools.keySet()) {
        tree.createInstantiatingGateways(
                participant, colaboration, startEvents, firstNode, pools);
      }
    }
  }

  /**
   * collapses the pools of participants, who were not selected to be generated
   * in detail.
   */
  private void hidePoolDetails() {
    for(Map.Entry<String, Pool> entry : pools.entrySet()) {
      if(! selectedParticipants.contains(entry.getKey())) {
        hideDetails(entry.getValue());
      }
    }
  }

  /**
   * collapses a cluster. This involves setting the collapsed-property,
   * redirecting message flow from contained nodes to the cluster and removing
   * the contained nodes from the diagram.
   */
  private void hideDetails(Cluster cluster) {
    Collection<ProcessNode> nodes = new HashSet<ProcessNode>(cluster.getProcessNodes());
    for(ProcessNode node : nodes) {
      if(Utils.isCluster(node)) hideDetails((Cluster)node);
      for(ProcessEdge edge : colaboration.getIncomingEdges(MessageFlow.class, node)) {
        edge.setTarget(cluster);
      }
      for(ProcessEdge edge : colaboration.getOutgoingEdges(MessageFlow.class, node)) {
        edge.setSource(cluster);
      }
      cluster.removeProcessNode(node);
      colaboration.removeNode(node);
    }
    if(Utils.isPool(cluster)) {
      cluster.setProperty(Pool.PROP_BLACKBOX_POOL, Pool.TRUE);
    } else if(isSubProcess(cluster)) {
      cluster.setProperty(SubProcess.PROP_COLLAPSED, SubProcess.TRUE);
    }
  }

  private boolean isSubProcess(ProcessNode node) {
    return node instanceof SubProcess;
  }
}
