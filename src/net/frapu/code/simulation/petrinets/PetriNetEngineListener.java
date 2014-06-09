/**
 *
 * Process Editor - Petri net Simulation Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import net.frapu.code.visualization.petrinets.Transition;

/**
 * Provides a listener class for the PetriNetEngine. Should be used for
 * statistical simulation analysis of the Petri net.
 *
 * @author frank
 */
public interface PetriNetEngineListener {

    /**
     * Is called each time the PetriNetEngine fires a transition.
     * @param t - The transition that has been fired.
     * @param instanceId - The instance that has been fired by the transition.
     * @param cost - The costs produced by firing this transition.
     * @param duration - The time elapsed by firing this transition.
     */
    public void transitionFired(Transition t, int instanceId, 
            double cost, double duration);

    /**
     * Is called when the engine is reseted.
     */
    public void engineReset();

}
