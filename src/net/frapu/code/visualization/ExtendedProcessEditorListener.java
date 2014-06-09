/**
 *
 * Process Editor - Core Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

/**
 *
 * @author fpu
 */
public interface ExtendedProcessEditorListener extends ProcessEditorListener {

    /**
     * Is called if a new ProcessModel should be openend somewhere.
     * @param model
     */
    public void requestNewProcessEditor(ProcessModel model);

}
