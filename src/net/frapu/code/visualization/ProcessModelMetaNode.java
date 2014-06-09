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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * This class abstracts a ProcessModel to a MetaNode that can be used
 * to configure the properties of the model.
 *
 * @author fpu
 */
public class ProcessModelMetaNode extends ProcessObject {

    private ProcessModel sourceModel = null;

    public ProcessModelMetaNode() {
        super();        
    }

    public ProcessModelMetaNode(ProcessModel source) {
        super();
        setProcessModel(source);
    }

    @Override
    public Object clone() {
        // @todo: Implement clone
        return null;
    }

    public void setProcessModel(ProcessModel source) {
       sourceModel = source;
       init();
    }
    
    protected void init() {
        // Override id and type with model information
        setProperty(PROP_ID, sourceModel.getId());
        setProperty(PROP_CLASS_TYPE, sourceModel.getClass().getName());
    }

    @Override
    public String getProperty(String key) {
        // Check if key is in properties of model
        if (sourceModel.getProperty(key)!=null) return sourceModel.getProperty(key);
        // Return default
        return super.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<String>();
        keys.add(PROP_ID);
        keys.add(PROP_CLASS_TYPE);
        keys.addAll(sourceModel.getPropertyKeys());
        return keys;
    }

    @Override
    public void setProperty(String key, String value) {
        if (sourceModel.getPropertyKeys().contains(key)) {
            sourceModel.setProperty(key, value);            
        } else {
            super.setProperty(key, value);
        }
    }



    @Override
    protected String getXmlTag() {
        return "META";
    }

}
