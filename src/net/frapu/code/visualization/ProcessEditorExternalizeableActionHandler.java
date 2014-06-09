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
 * This interface encapsulates actions that might be handled by other
 * implementations such as the default one.
 *
 * @author fpu
 */
public interface ProcessEditorExternalizeableActionHandler {

    /**
     * Opens a new model.
     * @param model
     */
    public ProcessEditor openNewModel(ProcessModel model);

}
