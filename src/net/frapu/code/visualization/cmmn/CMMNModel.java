/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Case Management Model and Notation
 * http://www.omg.org/spec/CMMN/
 * 
 * @version 14.10.2014
 * @author Stephan
 */
public class CMMNModel extends ProcessModel {
    /** Map for changing the marker position */
    protected static final Map<Integer,Integer> markerPosMap = new HashMap<Integer, Integer>(4);

    static {
        markerPosMap.put(0, 1);
        markerPosMap.put(1, -1);
        markerPosMap.put(-1, 2);
        markerPosMap.put(2, -2);
    }

    public CMMNModel() { processUtils = new CMMNUtils(); }

    public CMMNModel(String name) {
        super(name);
        processUtils = new CMMNUtils();
    }

    @Override
    public String getDescription() {
        return "CMMN 1.0";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Task.class);
        result.add(CasePlanModel.class);
        result.add(CaseFileItem.class);
        result.add(Milestone.class);
        result.add(Stage.class);
        result.add(EventListener.class);
        result.add(Criterion.class);
        result.add(PlanFragment.class);

        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Association.class);
        return result;
    }
}
