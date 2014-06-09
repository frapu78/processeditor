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

import net.frapu.code.visualization.editors.DefaultPropertyEditor;
import net.frapu.code.visualization.editors.PropertyEditor;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ff, fpu
 *
 */
public abstract class ProcessObject extends SerializableProcessObject {

    /**
     * Serialization properties
     */
    public final static String PROP_ID = "#id";
    public final static String PROP_CLASS_TYPE = "#type";
    public final static String TRUE = "1";
    public final static String FALSE = "0";
//    /** Holds the properties of the ProcessObject */
//    private HashMap<String, String> properties = new HashMap<String, String>();
    /** Holds the list of listeners */
    private Set<ProcessObjectListener> listeners = new HashSet<ProcessObjectListener>();
    /** Holds the property editors */
    private HashMap<String, PropertyEditor> propertyEditors = new HashMap<String, PropertyEditor>();
    /** Flag to hold selection status */
    private boolean selected = false;
    /** Flag to hold highlight status */
    private boolean highlighted = false;
    /** The current alpha value of this object */
    private float alphaValue = 1.0f;
    /** Stores the list of contexts for this ProcessNode, will not be serialized */
    protected Set<ProcessModel> contexts = new HashSet<ProcessModel>();

    /**
     * Default constructor. Needs always to be called!
     */
    public ProcessObject() {
        super.setProperty(PROP_ID, "" + this.hashCode());
        super.setProperty(PROP_CLASS_TYPE, this.getClass().getName());
    }



    /**
     * Tests if two ProcessObjects are equal based on their id. If the
     * argument obj is not an instance of ProcessObject, the super
     * function is called.
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessObject) {
            ProcessObject po = (ProcessObject) obj;
            return (po.getId().equals(this.getId()));
        }

        return super.equals(obj);
    }

    @Override
    public Object clone() {
        ProcessObject copy =  (ProcessObject) super.clone();
        copy.contexts = new HashSet<ProcessModel>(this.contexts);
        //TODO listeners are not cloned that way, since the hold a reference to the object they are registered for
        //copy.listeners = new HashSet<ProcessObjectListener>(this.listeners);
        copy.listeners = new HashSet<ProcessObjectListener>();
        copy.propertyEditors = new HashMap<String, PropertyEditor>(propertyEditors);
        return copy;
    }



    /**
     * Returns the PropertyEditor for a given property.
     * @param key
     * @return
     */
    public PropertyEditor getPropertyEditor(String key) {
        PropertyEditor editor = propertyEditors.get(key);
        if (editor == null) {
            return new DefaultPropertyEditor();
        }
        return editor;
    }

    /** Sets the PropertyEditor for a given property.
     */
    public void setPropertyEditor(String key, PropertyEditor editor) {
        propertyEditors.put(key, editor);
    }

    /**
     * Sets a property.
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
          // Get old value
        String oldValue = getProperty(key);
            // Check if value really changed
            if ( (value != null && !value.equals(oldValue)) || (oldValue != null && value == null) )  {
                // Update value
                super.setProperty( key, value );
//                properties.put(key, value);
                // Inform listeners
                if (listeners.size() > 0) {
                    for (ProcessObjectListener l : listeners) {
                        l.propertyChanged(this, key, oldValue, value);
                    }
                }
            }
        // Mark all containing contexts as dirty
        for (ProcessModel context : getContexts()) {
            context.markAsDirty(true);
        }
    }

    /** Returns the Id of this ProcessObject */
    public void setId(String id) {
        setProperty(PROP_ID, id);
    }

    /** Returns the Id of this ProcessObject */
    public String getId() {
        return getProperty(PROP_ID);
    }

    /** Returns the Name of this ProcessObject */
    public String getName() {
        return getProperty(PROP_ID);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns whether this node should be highlighted or not.
     * @return
     */
    public boolean isHighlighted() {
        return highlighted;
    }

    public float getAlpha() {
        return alphaValue;
    }

    public void setAlpha(float value) {
        if (value > 1.0f) {
            alphaValue = 1.0f;
        }
        if (value < 0.0f) {
            alphaValue = 0.0f;
        }
        alphaValue = value;
    }

    /**
     * Enables or disables the highlight of this node.
     * @param highlighted
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public void addListener(ProcessObjectListener l) {
        listeners.add(l);
    }

    public void removeListener(ProcessObjectListener l) {
        listeners.remove(l);
    }

    /**
     * Adds a context (ProcessModel) to this ProcessObject. This method
     * must be called each time this object is added to a ProcessModel.
     * @param context
     */
    public void addContext(ProcessModel context) {
        contexts.add(context);
    }

    /**
     * Removes a context (ProcessModel) from this ProcessObject. This method
     * must be called each time this object is removed from a ProcessModel.
     * @param context
     */
    public void removeContext(ProcessModel context) {
        contexts.remove(context);
    }

    /**
     * Returns the set of the current ProcessObject's contexts (ProcessModels).
     * @return
     */
    public Set<ProcessModel> getContexts() {
        return contexts;
    }
}
