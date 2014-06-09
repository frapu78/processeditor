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

import javax.swing.JTextField;

/**
 * This interface provides methods to capture events from a ProcessEditor.
 * @author fpu
 */
public interface ProcessEditorListener {
    
    /**
     * is invoked when editing the text of an element starts.
     * @param o
     * @param textfield
     */
    public void processNodeEditingStarted(ProcessNode o,JTextField textfield);
    
    /**
     * is invoked when editing the text of an element is finished
     * @param o
     * @param textfield
     */
    public void processNodeEditingFinished(ProcessNode o);
        
    /**
     * Is called each time a node is clicked. Returns null if background is
     * selected.
     *
     */
    public void processObjectClicked(ProcessObject o);
    
    /**
     * Is called each time a double click on a node is performed.
     * This also means that a single click has been send before
     * @see processNodeClicked()
     */
    public void processObjectDoubleClicked(ProcessObject o);
    
    /**
     * Is called each time a new model is loaded.
     */
    public void modelChanged(ProcessModel m);

    /**
     * Is called each time a ProcessObject is dragged.
     * @param o
     */
    public void processObjectDragged(Dragable o, int oldX, int oldY);
}
