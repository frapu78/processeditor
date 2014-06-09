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
 * This interface provides methods to capture events from a ProcessModel.
 * @author fpu
 */
public interface ProcessModelListener {

    /**
     * Is called if a ProcessNode is added to the model.
     * @return
     */
    public void processNodeAdded(ProcessNode newNode);
    
    /**
     * Is called if a ProcessNode is removed from the model.
     * @param remNode
     */
    public void processNodeRemoved(ProcessNode remNode);
    
    /**
     * Is called if a ProcessEdge is added to the model.
     * @param edge
     */
    public void processEdgeAdded(ProcessEdge edge);
    
    /**
     * Is called if a ProcessEdge is removed from the model.
     * @param edge
     */
    public void processEdgeRemoved(ProcessEdge edge);

    /**
     * Is called each time a property of a contained ProcessObject changes.
     * @param obj
     * @param name
     * @param oldValue
     * @param newValue
     */
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue);
        
}
