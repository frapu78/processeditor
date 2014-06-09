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

import net.frapu.code.visualization.*;

/**
 *
 * This class holds a property change action of the process editor.
 *
 * @author frank
 */
public class ProcessEditorPropertyChangedAction extends ProcessEditorActionRecord {

    private ProcessObject processObject;
    private String key;
    private String oldValue;
    private String newValue;

    public ProcessEditorPropertyChangedAction(ProcessObject processObject, String key, String oldValue, String newValue) {
        this.processObject = processObject;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getKey() {
        return key;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public ProcessObject getProcessObject() {
        return processObject;
    }

    @Override
    public String toString() {
        return processObject+" key="+key+" oldValue="+oldValue+" newValue="+newValue;
    }

    

}
