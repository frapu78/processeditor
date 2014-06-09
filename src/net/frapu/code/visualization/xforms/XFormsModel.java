/**
 *
 * Process Editor - XForms 1.1 Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.xforms;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * This class provides a model for a XForms 1.1 compliant form.
 *
 * @author fpu
 */
public class XFormsModel extends ProcessModel {

    public final static String PROP_BUSINESS_OBJECT_LINK = "business_object";

    public XFormsModel() {
        this(null);
    }

    public XFormsModel(String name) {
        super(name);
        processUtils = new XFormsUtils();
        initializeProperties();
    }

    private void initializeProperties() {
        this.setProperty(PROP_BUSINESS_OBJECT_LINK, "");
    }

    @Override
    public String getDescription() {
        return "XForms 1.1";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Input.class);
        result.add(CheckBox.class);
        result.add(Trigger.class);
        result.add(Panel.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (XForms 1.1 Diagram)";
    }

}
