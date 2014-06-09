/**
 *
 * Process Editor - Executable BPMN Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */

package net.frapu.code.simulation.bpmn;

import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * This class provides a BPMN 2.0 compliant process engine for interpreting
 * executable BPMN 2.0.
 *
 * @author frank
 */
public class BPMNEngine {

    private BPMNModel model;

    /**
     * Creates a new BPMN engine.
     * @param model
     */
    public BPMNEngine(BPMNModel model) {
        this.model = model;
    }

    public BPMNModel getModel() {
    	return model;
    }
    
    /**
     * Returns the set Activities in the READY state.
     * @return
     */
    public Set<Activity> calculateReadyActivities() {
        Set<Activity> result = new HashSet<Activity>();

        return result;
    }

}
