/**
 *
 * Process Editor - Lifecycle Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.lifecycle;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author fpu
 */
public class LifecycleModel extends ProcessModel {

    public final static String PROP_SCHEMAREF = "#schema_ref";

    public LifecycleModel() {
        processUtils = new LifecycleUtils();
        setProperty(PROP_SCHEMAREF, "");
    }

    @Override
    public String getDescription() {
        return "Lifecycle Model";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(DataObject.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        /**
         * The edge are in order of their "strength" as defined by the spec
         */
        result.add(Transition.class);
        return result;
    }

}
