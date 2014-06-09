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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Eliminates unnecesarily duplicated Timer- and MessageIntermediateEvents.
 * More precisely, it removes a Timer- oder MessageIntermediateEvent e, iff:<br />
 * - e has exactly one preceding node and exactly one succeeding node  according
 * to SequenceFlow and<br />
 * - the preceding and succeeding nodes of e according to SequenceFlow are
 * Exclusive- or non-instantiating EventBasedGateways<br />
 * - if e is a MessageIntermediateEvent: e has exactly one incoming MessageFlow<br />
 * -there is another IntermediateEvent f of the same type, that has exactly the
 * same preceding and succeeding nodes as e (according to as well MessageFlow as
 * SequenceFlow)<br />
 * @author tmi
 */
class DuplicateEliminator {

  private BPMNModel model;

  public DuplicateEliminator(BPMNModel model) {
    this.model = model;
  }

  public boolean eliminateDuplicates() {
    Collection<Collection<ProcessNode>> messageReceives =
            groupMessageIntermediateEventsByText();
    Collection<ProcessNode> timeouts = collectTimeouts();
    rejectNodesWithInadequateFlow(messageReceives);
          //(deals with Message- and SequenceFlow)
    rejectNodesWithInadequateSequenceFlow(timeouts);
    Collection<Collection<ProcessNode>> groups = groupByFlow(messageReceives);
    groups.addAll(groupPartBySequenceFlow(timeouts));
    return removeAllButOnePerGroupFromModel(groups);
  }

  /**
   * collects all MessageIntermediateEvents contained in the model and groups
   * them according to their text
   * @return a mapping from a text to the MessageIntermediateEvents holding
   * this text
   */
  private Collection<Collection<ProcessNode>> groupMessageIntermediateEventsByText() {
    Map<String, Collection<ProcessNode>> result =
            new HashMap<String, Collection<ProcessNode>>();
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isMessageIntermediateEvent(node)) {
        if (!result.containsKey(node.getText())) {
          result.put(node.getText(), new HashSet<ProcessNode>());
        }
        result.get(node.getText()).add(node);
      }
    }
    return result.values();
  }

  /**
   * collects all the timeouts contained in this model
   */
  private Collection<ProcessNode> collectTimeouts() {
    Collection<ProcessNode> timeouts = new HashSet<ProcessNode>();
    for (ProcessNode node : model.getNodes()) {
      if (Utils.isTimerIntermediateEvent(node)
              && node.getText().equals("Timeout")) {//TODO: ugly magic string
        timeouts.add(node);
      }
    }
    return timeouts;
  }

  /**
   * removes these nodes from the supplied set, that have not the MessageFlow-
   * and SequenceFlow-connections as described in the class description
   */
  private void rejectNodesWithInadequateFlow(
          Collection<Collection<ProcessNode>> nodeSets) {
    for (Iterator<Collection<ProcessNode>> iter = nodeSets.iterator(); iter.hasNext();) {
      Collection<ProcessNode> nodes = iter.next();
      rejectNodesWithInadequateSequenceFlow(nodes);
      rejectNodesWithInadequateMessageFlow(nodes);
      if (nodes.isEmpty()) {
        iter.remove();
      }
    }
  }

  /**
   * removes these nodes from the supplied Collection, that do not have exactly
   * one incoming and exactly one outgoing SequenceFlowConnection which both
   * originate from/terminate at an non-instantiating EventBased- or ExclusiveGateway
   */
  private void rejectNodesWithInadequateSequenceFlow(Collection<ProcessNode> nodes) {
    for (Iterator<ProcessNode> iter = nodes.iterator(); iter.hasNext();) {
      ProcessNode node = iter.next();
      List<ProcessEdge> incomingEdges = model.getIncomingEdges(SequenceFlow.class, node),
              outgoingEdges = model.getOutgoingEdges(SequenceFlow.class, node);
      if (!(incomingEdges.size() == 1
              && outgoingEdges.size() == 1)) {
        iter.remove();
      } else {
        ProcessNode predecessor = incomingEdges.iterator().next().getSource(),
                successor = outgoingEdges.iterator().next().getTarget();
        if (!(isExclusiveOrNonInstantiatingEventBasedGateway(predecessor)
                && isExclusiveOrNonInstantiatingEventBasedGateway(successor))) {
          iter.remove();
        }
      }
    }
  }

  private boolean isExclusiveOrNonInstantiatingEventBasedGateway(ProcessNode node) {
    return Utils.isExclusiveGateway(node)
           || (Utils.isEventBasedGateway(node)
             && !Utils.isInstantiatingGateway(node));
  }

  /**
   * removes nodes from the supplied connection, which do not have exactly one
   * incoming and no outgoing MessageFlow
   */
  private void rejectNodesWithInadequateMessageFlow(Collection<ProcessNode> nodes) {
    for (Iterator<ProcessNode> iter = nodes.iterator(); iter.hasNext();) {
      ProcessNode node = iter.next();
      if (!(model.getIncomingEdges(MessageFlow.class, node).size() == 1
              && model.getOutgoingEdges(MessageFlow.class, node).isEmpty())) {
        iter.remove();
      }
    }
  }

  private Collection<Collection<ProcessNode>> groupByFlow(
          Collection<Collection<ProcessNode>> nodeSets) {
    return groupByMessageFlow(groupBySequenceFlow(nodeSets));
  }

  /**
   * for each Collection in nodeSets, divides this Collection into one part for
   * each MessageFlow-source of the contained nodes
   * @return a Collection of Collections, that contain ProcessNodes, which have
   * the same MessageFlow-source and were in the same Collection in nodeSets
   */
  private Collection<Collection<ProcessNode>> groupByMessageFlow(
          Collection<Collection<ProcessNode>> nodeSets) {
    Collection<Collection<ProcessNode>> result =
            new HashSet<Collection<ProcessNode>>();
    for (Collection<ProcessNode> nodes : nodeSets) {
      result.addAll(groupPartByFlow(nodes, MessageFlow.class, true));
    }
    return result;
  }

  /**
   * divides nodes into groups according to the other end of incoming/outgoing
   * edges of the type flowType
   */
  private Collection<Collection<ProcessNode>> groupPartByFlow(
          Collection<ProcessNode> nodes, Class<? extends ProcessEdge> flowType,
          boolean incoming) {
    Map<String, Collection<ProcessNode>> groups =
            new HashMap<String, Collection<ProcessNode>>();
    for (ProcessNode node : nodes) {
      String id;
      if (incoming) {
        id = model.getPrecedingNodes(flowType, node).get(0).getId();
      } else {
        id = model.getSucceedingNodes(flowType, node).get(0).getId();
      }
      if (!groups.containsKey(id)) {
        groups.put(id, new HashSet<ProcessNode>());
      }
      groups.get(id).add(node);
    }
    return groups.values();
  }

  /**
   * for each Collection in nodeSets, divides this Collection into one part for
   * each MessageFlow-source of the contained nodes
   * @return a Collection of Collections, that contain ProcessNodes, which have
   * the same SequenceFlow-neighbors and were in the same Collection in nodeSets
   */
  private Collection<Collection<ProcessNode>> groupBySequenceFlow(
          Collection<Collection<ProcessNode>> nodeSets) {
    Collection<Collection<ProcessNode>> result =
            new HashSet<Collection<ProcessNode>>();
    for (Collection<ProcessNode> nodes : nodeSets) {
      result.addAll(groupPartBySequenceFlow(nodes));
    }
    return result;
  }

  /**
   * divides nodes into groups according to the neighbors (according to
   * SequenceFlow)
   */
  private Collection<Collection<ProcessNode>> groupPartBySequenceFlow(
          Collection<ProcessNode> nodes) {
    Collection<Collection<ProcessNode>> result =
            new HashSet<Collection<ProcessNode>>();
    for (Collection<ProcessNode> subset :
            groupPartByFlow(nodes, SequenceFlow.class, true)) {
      result.addAll(groupPartByFlow(subset, SequenceFlow.class, false));
    }
    return result;
  }

  /**
   * for each collection in groups, leaves only one of the ProcessNodes in the model
   */
  private boolean removeAllButOnePerGroupFromModel(
          Collection<Collection<ProcessNode>> groups) {
    boolean changed = false;
    for(Collection<ProcessNode> group : groups) {
      if(!group.isEmpty()) {
        group.remove(group.iterator().next());
        for(ProcessNode removee : group) {
          model.removeNode(removee);
          changed = true;
        }
      }
    }
    return changed;
  }
}
