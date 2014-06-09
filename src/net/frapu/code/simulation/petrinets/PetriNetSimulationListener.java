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

/**
 * Provides a callback interface for the PetriNetSimulation
 * @author fpu
 */
public interface PetriNetSimulationListener {

    /** 
     * Is called each time the simulation thinks the display should be refreshed.
     */
    public void refreshDisplay();

    /**
     * Is called each time the simulation Thread starts.
     */
    public void simulationStarted();

    /**
     * Is called each time the simulation Thread finishes.
     */
    public void simulationFinished();

}
