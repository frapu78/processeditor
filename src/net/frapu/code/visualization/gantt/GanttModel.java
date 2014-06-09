/**
 *
 * Process Editor - Gantt Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.gantt;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class GanttModel extends ProcessModel {

    public GanttModel() {
        this(null);
    }

    public GanttModel(String name) {
        super(name);
        processUtils = new GanttUtils();
    }

    @Override
    public String getDescription() {
        return "Gantt Chart";
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        return null;
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(GanttChart.class);
        result.add(Activity.class);
        result.add(TimeBar.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName() + " (Gantt Model)";
    }
}
