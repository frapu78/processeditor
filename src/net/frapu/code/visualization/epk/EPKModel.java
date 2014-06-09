/**
 *
 * Process Editor - EPK Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.epk;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class EPKModel extends ProcessModel {

    public EPKModel() {
        this(null);
    }

    public EPKModel(String name) {
        super(name);
        processUtils = new EPKUtils();
    }

    @Override
    public String getDescription() {
        return "EPK";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Event.class);
        result.add(Function.class);
        result.add(OrgUnit.class);
        result.add(System.class);
        result.add(ProcessPath.class);
        result.add(Connector.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Connection.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (EPK)";
    }

}
