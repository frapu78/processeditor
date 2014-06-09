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

import net.frapu.code.visualization.petrinets.Token;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * This class holds the data necessary to realize a timed Transition.
 *
 * @author frank
 */
public class TimedTransitionData {

    private Transition transition;
    private Token token;
    private int remainingTime;

    public TimedTransitionData(Transition transition, Token token, int remainingTime) {
        this.transition = transition;
        this.token = token;
        this.remainingTime = remainingTime;
    }

    /**
     * Removes one time unit from the remaining time
     */
    public void tick() {
        this.remainingTime--;
    }

    /**
     * Removes a number of time units from the remaining time
     * @param count
     */
    public void tick(int count) {
        this.remainingTime -= count;
    }

    /**
     * Returns whether this Transition is ready to output the Token
     * @return
     */
    public boolean isReady() {
        return (this.remainingTime<=0);
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public Token getToken() {
        return token;
    }

    public Transition getTransition() {
        return transition;
    }

}
