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
import java.util.Map;
import net.frapu.code.visualization.petrinets.Lane;

/**
 * Data-container for reporting the simulation of a Lane.
 * @author fpu
 */
public class LaneReport {

    /** The Lane that is reported */
    private Lane lane;
    /** The costs that are reported (InstanceId, Costs) */
    private Map<Integer,Double> costs = new HashMap<Integer,Double>();
    /** The durations that are reported (InstanceId, Duration) */
    private Map<Integer,Double> durations = new HashMap<Integer,Double>();
    /** The lane results */
    private Map<String,String> laneResults = new HashMap<String,String>();

    public final static String LANE_COST_AVG = "lane_cost_avg";
    public final static String LANE_COST_MIN = "lane_cost_min";
    public final static String LANE_COST_MAX = "lane_cost_max";

    public final static String LANE_DUR_AVG = "lane_dur_avg";
    public final static String LANE_DUR_MIN = "lane_dur_min";
    public final static String LANE_DUR_MAX = "lane_dur_max";

    public LaneReport(Lane lane) {
        this.lane = lane;
    }

    /**
     * Adds costs for a specific instance to a Lane.
     * @param instance
     * @param costs
     */
    public void addCost(int instance, double cost) {
        double newCost = cost;
        if (costs.containsKey(instance)) {
            newCost += costs.get(instance);
        }
        costs.put(instance, newCost);
    }

    /**
     * Adds duration for a specific instance to a Lane.
     * @param instance
     * @param costs
     */
    public void addDuration(int instance, double duration) {
        double newDuration = duration;
        if (durations.containsKey(instance)) {
            newDuration += durations.get(instance);
        }
        durations.put(instance, newDuration);
    }

    public Map<Integer, Double> getCosts() {
        return costs;
    }

    public Map<Integer, Double> getDurations() {
        return durations;
    }

    public String getLaneName() {
        return lane.getText();
    }

    public String getLaneResult(String key) {
        updateResults();
        return laneResults.get(key);
    }

    private void updateResults() {
        double minCosts = Double.MAX_VALUE;
        double maxCosts = 0.0;
        double totalCosts = 0.0;
        int costCount = 0;
        for (Integer instance: costs.keySet()) {
            double c = costs.get(instance);
            if (c>maxCosts) maxCosts = c;
            if (c<minCosts) minCosts = c;
            totalCosts += c;
            costCount++;
        }
        double avgCosts = 0.0;
        if (costCount>0) avgCosts = totalCosts/costCount;

        double minDur = 99999999.0;
        double maxDur = 0.0;
        double totalDur = 0.0;
        int durCount = 0;
        for (Integer instance: durations.keySet()) {
            double d = durations.get(instance);
            if (d>maxDur) maxDur = d;
            if (d<minDur) minDur = d;
            totalDur += d;
            durCount++;
        }
        double avgDur = 0.0;
        if (durCount>0) avgDur = totalDur/durCount;

        laneResults.put(LANE_COST_AVG, ""+avgCosts);
        laneResults.put(LANE_COST_MIN, ""+minCosts);
        laneResults.put(LANE_COST_MAX, ""+maxCosts);

        laneResults.put(LANE_DUR_AVG, ""+avgDur);
        laneResults.put(LANE_DUR_MIN, ""+minDur);
        laneResults.put(LANE_DUR_MAX, ""+maxDur);

    }

    /**
     * Prints the report to stdout.
     */
    public void printReport() {
        updateResults();
        System.out.println("LANE REPORT");
        System.out.println("===========");
        System.out.println("Name: "+getLaneName());
        System.out.println("Costs: "+getLaneResult(LANE_COST_MIN)+", "+
                getLaneResult(LANE_COST_MAX)+", "+getLaneResult(LANE_COST_AVG)+" (Min,Max,Avg)");
        System.out.println("Duration: "+getLaneResult(LANE_DUR_MIN)+", "+
                getLaneResult(LANE_DUR_MIN)+", "+getLaneResult(LANE_DUR_MIN)+" (Min,Max,Avg)");
    }

}
