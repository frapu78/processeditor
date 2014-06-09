/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import com.inubit.research.gui.plugins.choreography.Utils;
import com.inubit.research.gui.plugins.choreography.branchingTree.BranchingTree;
import com.inubit.research.gui.plugins.choreography.branchingTree.TreeBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Checks, that all participants, who are affected by an InclusiveGateway, have
 * been participating in the choreography before.
 * @author tmi
 */
public class InclusiveGatewayCheck extends AbstractChoreographyCheck {

  public InclusiveGatewayCheck(BPMNModel model) {
    super(model);
  }

  /**
   * returns a problem description stating, that the supplied participant must be
   * involved in the choreography before this gateway, by actually is not.
   */
  private String descriptionFor(String participant) {
    return "Participant " + participant + " is affected by this gateway, but "
            + "cannot know the gateway decision because he is not involved in "
            + "the choreography prior to this gateway in every alternative path.";
  }

  @Override
  public Collection<Class<? extends ProcessObject>> getRelevantClasses() {
    Collection<Class<? extends ProcessObject>> classes =
            new HashSet<Class<? extends ProcessObject>>();
    classes.add(InclusiveGateway.class);
    return classes;
  }

  @Override
  public Collection<EnforceabilityProblem> checkObject(ProcessObject object) {
    if (Utils.isInclusiveGateway(object)) {
      return checkInclusiveGateway((InclusiveGateway) object);
    } else {
      return new HashSet<EnforceabilityProblem>();
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
  private Collection<EnforceabilityProblem> checkInclusiveGateway(
          InclusiveGateway gateway) {
    TreeBuilder treeBuilder = new TreeBuilder(model);
    BranchingTree history = treeBuilder.buildTreeFor(gateway,
            TreeBuilder.FlowDirection.flowBefore);
    Map<String, Collection<Set<ProcessNode>>> firstNodeSets =
            getFirstNodeSets(gateway);
    return checkFirstNodes(firstNodeSets, history, gateway);
  }

  //If I had had enough time to do so, the following code would have been refactored,
  //but I do not have enough time.
  /**
   * generates a mapping from every participant following the gateway to the
   * Collection of alll following nodes, that involve the participant
   */
  private Map<String, Collection<Set<ProcessNode>>> getFirstNodeSets(
          InclusiveGateway gateway) {
    TreeBuilder treeBuilder = new TreeBuilder(model);
    Map<String, Collection<Set<ProcessNode>>> firstNodeSets =
            new HashMap<String, Collection<Set<ProcessNode>>>();
    Collection<String> participants = (treeBuilder.buildTreeFor(
            gateway, TreeBuilder.FlowDirection.flowAfter)).getParticipants();
    for (String participant : participants) {
      firstNodeSets.put(participant, new LinkedList<Set<ProcessNode>>());
    }
    for (ProcessNode node : model.getSucceedingNodes(SequenceFlow.class, gateway)) {
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
  private Collection<EnforceabilityProblem> checkFirstNodes(
          Map<String, Collection<Set<ProcessNode>>> firstNodeSets,
          BranchingTree history, InclusiveGateway gateway) {
    Collection<EnforceabilityProblem> problems = new HashSet<EnforceabilityProblem>();
    for (Map.Entry<String, Collection<Set<ProcessNode>>> entry : firstNodeSets.entrySet()) {
      checkFirstNodesForParticipant(
              entry.getKey(), entry.getValue(), history, problems, gateway);
    }
    return problems;
  }

  /**
   * checks the condition described above for one participant
   */
  private void checkFirstNodesForParticipant(
          String participant,
          Collection<Set<ProcessNode>> nodeSets,
          BranchingTree history, Collection<EnforceabilityProblem> problems,
          InclusiveGateway gateway) {
    if (!(nodeSets.isEmpty()
            || history.allAlternativesInvolve(participant))) {
      Set<ProcessNode> reference = nodeSets.iterator().next();
      for (Set<ProcessNode> comparatee : nodeSets) {
        if (!comparatee.equals(reference)) {
          Set<ProcessNode> affected = new HashSet<ProcessNode>(reference);
          affected.removeAll(comparatee);
          comparatee.removeAll(reference);
          affected.addAll(comparatee);
          problems.add(new EnforceabilityProblem(descriptionFor(
                  participant), gateway, affected));
          break;
        }
      }
    }
  }
}
