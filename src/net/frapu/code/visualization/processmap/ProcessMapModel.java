/**
 *
 * Process Editor - Process Map Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.processmap;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class ProcessMapModel extends ProcessModel {

    public ProcessMapModel() {
        this(null);
    }

    public ProcessMapModel(String name) {
        super(name);
        processUtils = new ProcessMapUtils();
    }

    @Override
    public String getDescription() {
        return "Process Map";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();

        result.add(ProcessGroup.class);
        result.add(Process.class);
        result.add(ProcessOwner.class);

        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Connection.class);
        result.add(Association.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (Process Map)";
    }

}
