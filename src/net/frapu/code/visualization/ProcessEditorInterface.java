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

import java.io.File;

/**
 *
 * Listener interface for actions that happen in the ProcessEditor.
 *
 * @author fpu
 */
public interface ProcessEditorInterface {

    /**
     * Called if a new ProcessModel has been opened.
     * @param model
     */
    public void processModelOpened(ProcessModel model);

    /**
     * Called if a ProcessModel has been saved.
     * @param model
     * @param f
     */
    public void processModelSaved(ProcessModel model, File f);

    /**
     * Returns the currently selected model of the ProcessEditor.
     * @return
     */
    public ProcessModel getSelectedModel();

}
