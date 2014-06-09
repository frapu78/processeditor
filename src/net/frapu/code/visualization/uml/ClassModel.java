/**
 *
 * Process Editor - UML Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.uml;

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
public class ClassModel extends ProcessModel {

    public final static String PROP_SOURCE = "#source";
    public final static String PROP_DATA = "#data";

    public ClassModel() {
        this(null);
    }

    public ClassModel(String name) {
        super(name);
        processUtils = new UMLUtils();
        setProperty(PROP_SOURCE, "");
        setProperty(PROP_DATA, "");
    }

    public String getDescription() {
        return "Class diagram (UML)";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(UMLClass.class);
        result.add(UMLClassReference.class);
        result.add(UMLPackage.class);
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

    @Override
    public String toString() {
        return this.getProcessName() + " (UML Class Diagram)";
    }
}
