/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.interfaceGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Pool;

/**
 * Provides an abstraction for classes, that take part in generating a behavioral
 * interface from a ChoreographyDiagram
 * @author tmi
 */
abstract class AbstractGenerator {
  /**
   * mapping from the id of a ProcessNode in the choreography diagram to a mapping
   * from participants´ names to the first node (according to SequenceFlow-relation),
   * that is engaged in realizing the specified node for the specified participant
   */
  protected Map<String, Map<String, ProcessNode>> firstNode =
          new HashMap<String, Map<String, ProcessNode>>();
  /**
   * mapping from the id of a ProcessNode in the choreography diagram to a mapping
   * from participants´ names to the last node (according to SequenceFlow-relation),
   * that is engaged in realizing the specified node for the specified participant
   */
  protected Map<String, Map<String, ProcessNode>> lastNode =
          new HashMap<String, Map<String, ProcessNode>>();
  /**
   * mapping from participants´ names to the pool of the participant.
   */
  protected Map<String, Pool> pools;
  protected BPMNModel choreography, colaboration;
  /**
   * mapping from the id of a ProcessNode in the choreography diagram to a mapping
   * from participants´ names to the collection of all nodes, that jointly realize
   * this choreography-node in the colaboration diagram.
   */
  protected Map<String, Map<String, Collection<ProcessNode>>> allNodes;

  public AbstractGenerator(Map<String, Map<String, ProcessNode>> firstNode,
          Map<String, Map<String, ProcessNode>> lastNode,
          Map<String, Map<String, Collection<ProcessNode>>> allNodes,
          Map<String, Pool> pools,
          BPMNModel choreography,
          BPMNModel colaboration) {
    this.firstNode = firstNode;
    this.lastNode = lastNode;
    this.allNodes = allNodes;
    this.pools = pools;
    this.choreography = choreography;
    this.colaboration = colaboration;
  }

  /**
   * lets the generator do its work
   */
  public abstract void generate();

  /**
   * registers firstNode as the first node, that realizes forNode for participant
   */
  protected void registerFirstNode(
          ProcessNode forNode, String participant, ProcessNode firstNode) {
    registerTo(this.firstNode, forNode, participant, firstNode);
  }

  /**
   * registers lasNode as the last node, that realizes forNode for participant
   */
  protected void registerLastNode(
          ProcessNode forNode, String participant, ProcessNode lastNode) {
    registerTo(this.lastNode, forNode, participant, lastNode);
  }

  /**
   * adds a mapping to the registry, such that 
   * registry.get(forNode.getId()).get(participant).equals(node) will be true.
   */
  protected void registerTo(
          Map<String, Map<String, ProcessNode>> registry,
          ProcessNode forNode, String participant, ProcessNode node) {
    if (!registry.containsKey(forNode.getId())) {
      registry.put(forNode.getId(), new HashMap<String, ProcessNode>());
    }
    registry.get(forNode.getId()).put(participant, node);
  }

  /**
   * registers, that node is one node engaged in realizing forNode for participant
   */
  protected void registerNode(
          ProcessNode forNode, String participant, ProcessNode node) {
    if(! allNodes.containsKey(forNode.getId())) {
      allNodes.put(forNode.getId(),new HashMap<String, Collection<ProcessNode>>());
    }
    if(! allNodes.get(forNode.getId()).containsKey(participant)) {
      allNodes.get(forNode.getId()).put(participant, new HashSet<ProcessNode>());
    }
    allNodes.get(forNode.getId()).get(participant).add(node);
  }

  /**
   * adds node to the colaboration and to the pool of participant
   */
  protected void addToPool(ProcessNode node, String participant) {
    colaboration.addNode(node);
    pools.get(participant).addProcessNode(node);
  }

  /**
   * adds node to the colaboration and to the pool of participant and registers
   * it as one node, that is engaged in realizing registerFor for participant
   */
  protected void addToPoolRegistered(
          ProcessNode node, String participant, ProcessNode registerFor) {
    addToPool(node, participant);
    registerNode(registerFor, participant, node);
  }

  /**
   * registers forNode as the first, last and one node, that is engaged in
   * realizing forNode for participant
   */
  protected void registerOnlyNode(ProcessNode forNode, String participant, ProcessNode node) {
    registerFirstNode(forNode, participant, node);
    registerLastNode(forNode, participant, node);
    registerNode(forNode, participant, node);
  }
}
