/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 * Provides a full enforceability-check for BPMN-Choreography diagrams. It
 * combines the following ChoreographyChecks in order to do so:
 * ForbiddenNodesCheck, EventBasedGatewayCheck, FlowCheck, SubProcessCheck,
 * AssociatedMessagesCheck, AttachedEventsCheck, InclusiveGatewayCheck,
 * MultipleParticipantsCheck.
 * @author tmi
 */
public class CombinedEnforceabilityCheck {
  BPMNModel model;

  /**
   * Provides a mapping from a class to an AbstractChoreographyCheck.
   */
  private class RegistryEntry {
    public RegistryEntry(Class<? extends ProcessObject> classKey,
            AbstractChoreographyCheck checker) {
      this.key = classKey;
      this.check = checker;
    }
    private Class<? extends ProcessObject> key;
    private AbstractChoreographyCheck check;

    /**
     * performs the check for a ProcessObject, if this ProcessObject is an
     * instance of the key.
     */
    public Collection<EnforceabilityProblem> check(ProcessObject object) {
      if(key.isInstance(object)) return check.checkObject(object);
      else return new HashSet<EnforceabilityProblem>();
    }
  }
  Collection<RegistryEntry> registry;

  public CombinedEnforceabilityCheck(BPMNModel model) {
    this.model = model;
    registry = new LinkedList<RegistryEntry>();
    register(new ForbiddenNodesCheck(model));
    register(new EventBasedGatewayCheck(model));
    register(new FlowCheck(model));
    register(new SubProcessCheck(model));
    register(new AssociatedMessagesCheck(model));
    register(new AttachedEventsCheck(model));
    register(new InclusiveGatewayCheck(model));
    register(new MultipleParticipantsCheck(model));
  }

  private void register(AbstractChoreographyCheck checker) {
    for(Class<? extends ProcessObject> c: checker.getRelevantClasses()) {
      registry.add(new RegistryEntry(c, checker));
    }
  }

  public Collection<EnforceabilityProblem> checkModel() {
    Collection<EnforceabilityProblem> problems =
            new HashSet<EnforceabilityProblem>();
    for(ProcessObject object: model.getObjects()) {
      for(RegistryEntry entry: registry) {
        problems.addAll(entry.check(object));
      }
    }
    return problems;
  }
}
