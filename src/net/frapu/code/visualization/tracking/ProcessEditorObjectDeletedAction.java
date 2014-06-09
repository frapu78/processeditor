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
 * This class tracks the deletion of a ProcessNode.
 *
 * @author frank
 */
public class ProcessEditorObjectDeletedAction extends ProcessEditorActionRecord {

    private ProcessObject node;

    public ProcessEditorObjectDeletedAction(ProcessObject node) {
        this.node = node;
    }

    public ProcessObject getProcessObject() {
        return node;
    }

    @Override
    public String toString() {
        return "Node "+node+" deleted.";
    }



}
