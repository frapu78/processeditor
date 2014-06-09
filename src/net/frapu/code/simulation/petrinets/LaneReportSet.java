/**
 *
 * Process Editor - Petri net Simulation Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.Lane;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * Provides a reporting functionality for cost/time of Lanes.
 *
 * @author fpu
 */
public class LaneReportSet implements PetriNetEngineListener {

    private PetriNetModel model;
    private Map<Lane,LaneReport> reports;

    public LaneReportSet(PetriNetModel m) {
        model = m;
        engineReset();
    }

    public void transitionFired(Transition t, int instanceId, double cost, double duration) {
        Set<Lane> lanes = new HashSet<Lane>();
        for (ProcessNode node: model.getNodes()) {
            if (node instanceof Lane) {
                Lane lane = (Lane)node;
                lanes.add(lane);
            }
        }
        // Figure out in which Lane(s) the current Transition is located
        for (Lane lane: lanes) {
            if (lane.isContainedGraphically(model.getNodes(), t, true)) {
                LaneReport r = reports.get(lane);
                if (r==null) {
                    // No reporting found, create new one
                    r = new LaneReport(lane);
                    reports.put(lane, r);
                }
                // Add values to report
                r.addCost(instanceId, cost);
                r.addDuration(instanceId, duration);
            }
        }
    }

    public void engineReset() {
        reports = new HashMap<Lane,LaneReport>();
    }

    public Set<LaneReport> getLaneReports() {
        Set<LaneReport> result = new HashSet<LaneReport>();
        for (Lane l: reports.keySet()) {
            result.add(reports.get(l));
        }
        return result;
    }

    public void printReport() {
        for (Lane lane: reports.keySet()) {
            reports.get(lane).printReport();
        }
    }

}
