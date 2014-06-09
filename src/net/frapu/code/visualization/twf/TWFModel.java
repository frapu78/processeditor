/**
 *
 * Process Editor - TWF Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.twf;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.general.ColoredFrame;

/**
 *
 * @author fpu
 */
public class TWFModel extends ProcessModel {

    public TWFModel() {
        this(null);
    }

    public TWFModel(String name) {
        super(name);
        processUtils = new TWFUtils();
    }

    @Override
    public String getDescription() {
        return "Technical Workflow";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Adapter.class);
        result.add(Converter.class);
        result.add(Connector.class);
        result.add(Control.class);
        result.add(Tool.class);
        result.add(ColoredFrame.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Connection.class);
        result.add(ErrorConnection.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (Technical Workflow Diagram)";
    }

}
