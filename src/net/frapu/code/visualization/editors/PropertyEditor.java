/**
 *
 * Process Editor - Editor Package
 *
 * (C) 2008-2017 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

import net.frapu.code.visualization.*;
import java.awt.Component;

/**
 *
 * @author frank
 */
public abstract class PropertyEditor {
    protected ProcessObject po = null;
    protected String key = null;

    /** Returns the editor component */
    public abstract Component getComponent();

    /** Sets the input */
    public abstract void setValue(String value);

    /** Returns the result as String */
    public abstract String getValue();

    public abstract boolean isReadOnly();

    public abstract void setReadOnly(boolean b);
    
    public boolean containsLabel() {
        return false;
    }

    public ProcessObject getProcessObject() {
        return po;
    }

    public String getPropertyKey() {
        return key;
    }

    public PropertyEditorType getType() {
        return PropertyEditorType.DEFAULT;
    }

    public void setProcessObject(ProcessObject po, String key) {
        this.po = po;
        this.key = key;
    }

    /**
     * Updates the content of this PropertyEditor. Might be overriden by
     * Subclasses.
     */
    public void update() {
        // Do nothing by default
    }


    /**
     * Frees the resources of this PropertyEditor. Might be overridden by
     * Subclasses.
     */
    public void free() {
        // Do nothing here
    }

}
