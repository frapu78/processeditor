/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.tracking;

import net.frapu.code.visualization.ProcessObject;

/**
 *
 * This class tracks the creation of a new ProcessNode.
 *
 * @author frank
 */
public class ProcessEditorObjectCreatedAction extends ProcessEditorActionRecord {

    private ProcessObject node;

    public ProcessEditorObjectCreatedAction(ProcessObject node) {
        this.node = node;
    }

    public ProcessObject getProcessObject() {
        return node;
    }

    @Override
    public String toString() {
        return "Node "+node+" created.";
    }



}
