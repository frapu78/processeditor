/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

/**
 * This interface provides methods to capture events from ProcessObjects.
 *
 * @author frank
 */
public interface ProcessObjectListener {

    public void propertyChanged(ProcessObject o, String key, String oldValue, String newValue);

}
