/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 inubit AG
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.general.ColoredFrame;

/**
 *
 * @author frank
 */
public class DomainModel extends ProcessModel {

    public final static String PROP_SOURCE = "#source";
    public final static String PROP_DATA = "#data";
    public final static String PROP_NAMESPACE_PREFIX = "namespacePrefix";
    public final static String PROP_NAMESPACE_URI = "namespaceUri";

    public DomainModel() {
        this(null);
    }

    public DomainModel(String name) {
        super(name);
        processUtils = new DomainUtils();
        setProperty(PROP_SOURCE, "");
        setProperty(PROP_DATA, "");
        setProperty(PROP_NAMESPACE_PREFIX, "");
        setProperty(PROP_NAMESPACE_URI, "");
    }

    public String getDescription() {
        return "Domain Model";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(DomainClass.class);
        result.add(DomainClassReference.class);
        result.add(Comment.class);
        result.add(ColoredFrame.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Association.class);
        result.add(Aggregation.class);
        result.add(Inheritance.class);
        return result;
    }
    
    /**
     * Returns the first root instance of the DomainModel (if any exists)
     * @return
     */
    public DomainClass getRootInstance() {
        DomainClass root = null;
        for (ProcessNode node: getNodes()) {
            if (node instanceof DomainClass && node.getStereotype().equals("root_instance"))
                return (DomainClass)node;
        }

        return root;
    }

    @Override
    public String toString() {
        return this.getProcessName() + " (Domain Model)";
    }
}
