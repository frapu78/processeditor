/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization.reporting;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class ReportingModel extends ProcessModel {

    public ReportingModel() {
        super();
        processUtils = new ReportingUtils();
    }

    public ReportingModel(String name) {
        super(name);
        processUtils = new ReportingUtils();
    }

    public String getDescription() {
        return "Reports";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(BarChart.class);
        result.add(PieChart.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName() + " (Reporting Diagram)";
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        return result;
    }
}
