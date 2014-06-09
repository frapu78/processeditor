/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

/**
 *
 * This interface might be implemented by nodes that consume time. It is
 * called by different time sources to update the node.
 *
 * @author fpu
 */
public interface TimeConsumer {

    /**
     * Adds one time unit to the current time.
     */
    public void addTick();

    /**
     * Adds ticks ticks to the current time.
     * @param ticks
     */
    public void addTicks(int ticks);

    /**
     * Resets the ticks to zero.
     */
    public void resetTicks();

}
