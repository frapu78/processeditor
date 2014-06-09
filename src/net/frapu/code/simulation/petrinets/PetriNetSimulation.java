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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.Edge;
import net.frapu.code.visualization.petrinets.LaborPlace;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.ResourcePlace;
import net.frapu.code.visualization.petrinets.Token;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * @author fpu
 */
public class PetriNetSimulation implements Runnable {

    private Set<PetriNetSimulationListener> listeners = new HashSet<PetriNetSimulationListener>();
    private PetriNetEngine engine;
    private PetriNetModel model;
    private Place startPlace;
    private int tokenCount = 100;
    final int instanceOffset = 1000;
    private int simulationSpeed = 1000;
    private boolean done = false;
    private boolean kill = false;
    private Thread simulationRunner = null;
    private LaneReportSet laneReporting;

    private final static int STOP = 0;
    //private final static int PAUSE = 1;
    private final static int RUN = 2;
    private int status = STOP;

    private HashMap<String, String> simulationResults = new HashMap<String, String>();

    public final static String SIM_COST_AVG = "sim_cost_avg";
    public final static String SIM_COST_MIN = "sim_cost_min";
    public final static String SIM_COST_MAX = "sim_cost_max";

    public final static String SIM_MIN_DUR_AVG = "sim_min_dur_avg";
    public final static String SIM_MIN_DUR_MIN = "sim_min_dur_min";
    public final static String SIM_MIN_DUR_MAX = "sim_min_dur_max";

    public final static String SIM_MAX_DUR_AVG = "sim_max_dur_avg";
    public final static String SIM_MAX_DUR_MIN = "sim_max_dur_min";
    public final static String SIM_MAX_DUR_MAX = "sim_max_dur_max";
    public final static String SIM_MAX_DUR_DATA = "sim_max_dur_data";

    public final static String SIM_MAX_WAIT_AVG = "sim_max_wait_avg";
    public final static String SIM_MAX_WAIT_MIN = "sim_max_wait_min";
    public final static String SIM_MAX_WAIT_MAX = "sim_max_wait_max";
    public final static String SIM_MAX_WAIT_DATA = "sim_max_wait_data";

    public final static String SIM_INVENTORY_LEVEL = "sim_inventory_level";

    public final static String SIM_FLOW_RATE = "sim_flow_rate";

    public final static int SIM_INVENTORY_LEVEL_RATE = 60;

    public PetriNetSimulation() {
        // do nothing here...
    }

    public PetriNetSimulation(
            PetriNetEngine engine, PetriNetModel model,
            Place startPlace, int tokenCount) {
        this.engine = engine;
        this.model = model;
        this.startPlace = startPlace;
        this.tokenCount = tokenCount;
    }

    public PetriNetEngine getEngine() {
        return engine;
    }

    public void setEngine(PetriNetEngine engine) {
        this.engine = engine;
    }

    public PetriNetModel getModel() {
        return model;
    }

    public void setModel(PetriNetModel model) {
        this.model = model;
    }

    public Place getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(Place startPlace) {
        this.startPlace = startPlace;
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setSimulationSpeed(int simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public void addListener(PetriNetSimulationListener l) {
        listeners.add(l);
    }

    public void removeListener(PetriNetSimulationListener l) {
        listeners.remove(l);
    }

    private void broadcastRequireDisplayRefresh() {
        for (PetriNetSimulationListener l : listeners) {
            l.refreshDisplay();
        }
    }

    private void broadcastSimulationStarted() {
        for (PetriNetSimulationListener l : listeners) {
            l.simulationStarted();
        }
    }

    private void broadcastSimulationFinished() {
        for (PetriNetSimulationListener l : listeners) {
            l.simulationFinished();
        }
    }

    public void kill() {
        System.out.println("Killing Simulation");
        stopSimulation();
        kill = true;
    }

    public void startSimulation() {
        if (simulationRunner == null) {
            simulationRunner = new Thread(this);
            simulationRunner.start();
        }
        done = false;
        status = RUN;
    }

    protected void stopSimulation() {
        done = true;
        status = STOP;
    }

    public void setInitialTokens(Place startPlace) {
        // Remove all Tokens with id > instanceOffset from the net and
        // initialize LaborPlaces
        for (ProcessNode n: getModel().getNodes()) {
            if (n instanceof Place) {
                Place p = (Place)n;
                // Iterate over all Tokens
                Set<Token> remove = new HashSet<Token>();
                for (Token t: p.getTokens()) {                    
                    if (t.getProcessInstance()>=instanceOffset) {
                        // Remove this Token
                        remove.add(t);
                    }
                }
                // Remove tokens
                for (Token t: remove) {
                    p.removeToken(t);
                }
            }
            if (n instanceof LaborPlace) {
                LaborPlace p = (LaborPlace)n;
                p.removeAllTokens();

            }
        }

        // Insert tokenCount Tokens
        for (int i = 0; i < tokenCount; i++) {
            startPlace.addToken(new Token(instanceOffset + i));
        }
    }

    public Set<LaneReport> getLaneReports() {
        return laneReporting.getLaneReports();
    }


    public String getSimulationResult(String key) {
        return simulationResults.get(key);
    }

    /**
     * Runs a simple Petri net simulation.
     * 
     * @todo: Refactor (outsource) all reports!!!
     * @todo: Add hocks to extend simulation in sub-classes!!!
     */
    public void run() {

        // Outer loop
        while (!kill) {

            // Start if status changes to START

            while (this.status != RUN) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                if (kill) break;
            }

            // Runs as long as Transitions are enabled or killed
            broadcastSimulationStarted();

            // The count of ticks, where nothing happened
            int idleCount = 0;
            int displayCount = 0;

            // The inventory values
            String inventoryLoad = "";
            String finishedItems = "";
            int lastFinishedCount = 0;

            // Reset cost table
            engine.resetEngine();

            System.out.println("Starting simulation");

            setInitialTokens(startPlace);

            // Add a LaneReportSet
            laneReporting = new LaneReportSet(model);
            engine.addListener(laneReporting);

            Random rand = new Random();

            while (!done) {

                int tDone = 0; // Counting how many instances are active
                // Iterate over all instances
                for (int i = 0; i < tokenCount; i++) {
                    boolean instanceDone = false;

                    while (!instanceDone) {
                        // Get active transitions of current instance
                        Set<Transition> tSet = engine.calculateEnabledTransitions(instanceOffset + i);
                        // Choose one random transition
                        if (tSet.size() > 0) {
                            // Group transitions by common input places
                            // (simple variant, but should work)
                            // @todo: Remove duplicates!!! (for tick-timing)
                            HashMap<Place, List<Transition>> placeMap =
                                    new HashMap<Place, List<Transition>>();
                            for (Transition t : tSet) {
                                // Get preset
                                List<Place> preSet = engine.getPreSet(t, Edge.class);
                                for (Place p : preSet) {
                                    List<Transition> ts = placeMap.get(p);
                                    if (ts == null) {
                                        // Not yet contained
                                        ts = new LinkedList<Transition>();
                                        placeMap.put(p, ts);
                                    }
                                    ts.add(t);
                                }
                            }
                            // @todo: Implement that all Transitions are fired in
                            //        tick-time mode!
                            // IF: time-ticks disabled: Select one item of placeMap randomly
                            // ELSE: Fire all (since they can not be in conflict due
                            //       to prior sorting
                            //
                            Place[] pArray = placeMap.keySet().toArray(new Place[0]);
                            Place keyPlace = pArray[rand.nextInt(pArray.length)];
                            // Pick the first Transition (at least one Transition should be contained)
                            assert placeMap.get(keyPlace).size() > 0;
                            Transition t = placeMap.get(keyPlace).get(0);
                            // Check if more than one Transition is contained
                            if (placeMap.get(keyPlace).size() > 1) {
                                // Get the probability distributions
                                List<Transition> tList = placeMap.get(keyPlace);
                                int[] probs = new int[tList.size()];
                                int sum = 0;
                                for (int i1 = 0; i1 < tList.size(); i1++) {
                                    int value = 100;
                                    try {
                                        value = Integer.parseInt(tList.get(i1).getProperty(Transition.PROP_PROBABILITY));
                                    } catch (NumberFormatException e) {
                                    }
                                    ;
                                    probs[i1] = sum + value;
                                    sum += value;
                                }
                                // Select one random Transition of the group according to the prob. dist.
                                int sel = rand.nextInt(sum);
                                for (int i1 = 0; i1 < tList.size(); i1++) {
                                    if (sel < probs[i1]) {
                                        t = tList.get(i1);
                                        break;
                                    }
                                }
                            }

                            engine.fireTransition(t, i + instanceOffset);
                            tDone++;

                            if (simulationSpeed > 24) {
                                engine.calculateEnabledTransitions(instanceOffset + i);
                            }

                        }

                        // Instance is done after one random transition if timings are
                        // disabled
                        if (!engine.isTimingEnabled()) {
                            instanceDone = true;
                        }
                        // Instance is always done if no other transitions are active
                        if (tSet.size() == 0) {
                            instanceDone = true;
                        }
                    }
                }

                //
                // Count inventory & flow rate if interval passed
                //
                if (engine.isTimingEnabled()) {
                    if (engine.getTicks() % SIM_INVENTORY_LEVEL_RATE==0) {
                        // Count all tokens with id > instanceOffset
                        int inventoryCount = 0;
                        int finishedCount = 0;
                        for (ProcessNode n: getModel().getNodes()) {
                            if (n instanceof Place) {
                                //
                                boolean check1 = false, check2 = false;;
                                // Check if *p und *p > 1
                                for (ProcessNode pre: getModel().getPredecessors(n) ) {
                                    if (pre instanceof Transition) check1 = true;
                                }
                                for (ProcessNode post: getModel().getSuccessors(n)){
                                    if (post instanceof Transition) check2 = true;
                                }
                                if (check1 && check2) {
                                    // Intermediate place, add to inventory
                                    Place p = (Place)n;
                                    for (Token t: p.getTokens()) {
                                        if (t.getProcessInstance()>=instanceOffset) inventoryCount++;
                                    }
                                }
                                if (check1 && !check2) {
                                    // Final place, add to flow rate
                                    Place p = (Place)n;
                                    finishedCount += p.getTokenCount();
                                }
                            }
                        }
                        inventoryLoad += ""+inventoryCount+",";
                        finishedItems += ""+(finishedCount-lastFinishedCount)+",";
                        lastFinishedCount = finishedCount;
                    }
                }


                // Repaint
                if (simulationSpeed > 24) {
                    broadcastRequireDisplayRefresh();
                } else {
                    // Only each 10 transitions
                    displayCount++;
                    if (displayCount > 9) {
                        broadcastRequireDisplayRefresh();
                        displayCount = 0;
                    }
                }

                // Break if no transition in any instance has been fired
                if (tDone == 0) {
                    if (!engine.isTimingEnabled()) {
                        done = true;
                    } else {
                        // break only if no waitings remaining
                        if (!engine.isWaitingEmpty()) {
                            idleCount = 0;
                        } else {
                            idleCount++;

                            // Break if all ordinary places with |p*|>0 are empty
                            done = true;
                            for (ProcessNode n : model.getNodes()) {
                                if (n instanceof Place) {
                                    if (n instanceof LaborPlace) {
                                        continue;
                                    }
                                    if (n instanceof ResourcePlace) {
                                        continue;
                                    }
                                    // Figure out if a postset exists
                                    if (model.getSuccessors(n).size() > 0) {
                                        // Skip if all successors != Transition
                                        boolean cont = false;
                                        for (ProcessNode succ : model.getSuccessors(n)) {
                                            if (succ instanceof Transition) {
                                                cont = true;
                                            }
                                        }
                                        if (cont == false) {
                                            continue;
                                        }

                                        // This Place has to be empty
                                        Place p = (Place) n;
                                        if (p.getTokenCount() > 0) {
                                            done = false;
                                        }
                                    }
                                }
                            }
                        }

                        // Break if nothing happened for a day
                        if (!done) {
                            done = (idleCount >= 1440);
                        }
                    }
                }

                // Tick engine
                if (engine.isTimingEnabled() && !done) {
                    // Check/set turbo mode
                    engine.setTurboMode(getSimulationSpeed()==-1?true:false);
                    // Tick engine
                    engine.tick();
                }

                try {
                    if (simulationSpeed>0) Thread.sleep(simulationSpeed);
                } catch (InterruptedException ex) {
                }

            }

            System.out.println("Finished simulation");
            broadcastRequireDisplayRefresh();

            // @todo: Refactor lines below to external reporting class

            // Initialize result variables
            double totalCosts = 0.0;
            double minCosts = 9999999.0;
            double maxCosts = 0.0;

            double totalMinDur = 0.0;
            double minMinDur = 9999999.0;
            double maxMinDur = 0.0;

            double totalMaxDur = 0.0;
            double minMaxDur = 9999999.0;
            double maxMaxDur = 0.0;

            int totalMaxWait = 0;
            int minMaxWait = 99999999;
            int maxMaxWait = 0;

            String maxWaitStr = "";
            String maxDurStr = "";

            System.out.println("INSTANCE |  COSTS  | MIN DUR. | MAX DUR. | MAX WAIT ");
            System.out.println("----------------------------------------------------");
            //for (String instanceId : engine.getAllInstancesCosts().keySet()) {
            // @todo: Hack, refactor to stable reporting!
            for (int i = instanceOffset; i < tokenCount+instanceOffset; i++) {

                String instanceId = ""+i;
                // Calculate costs
                double costs = Double.parseDouble(engine.getAllInstancesCosts().get(instanceId));
                totalCosts += costs;
                // Find min/max duration
                double minDur = 99999999.0;
                double maxDur = 0.0;
                int maxWait = 0;
                for (Token tok : engine.getCurrentTokens(Integer.parseInt(instanceId))) {

                    double tokTime = 0.0;
                    int tokWait = 0;

                    try {
                        tokTime = Double.parseDouble(tok.getProperty(PetriNetEngine.PROP_TOKEN_TIME));
                    } catch (NumberFormatException e) {
                    }

                    if (tokTime < minDur) {
                        minDur = tokTime;
                    }
                    if (tokTime > maxDur) {
                        maxDur = tokTime;
                    }

                    try {
                        tokWait = Integer.parseInt(tok.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME));
                    } catch (NumberFormatException e) {}

                    if (tokWait > maxWait) {
                        maxWait = tokWait;
                    }
                }

                totalMinDur += minDur;
                if (minDur < minMinDur) {
                    minMinDur = minDur;
                }
                if (minDur > maxMinDur) {
                    maxMinDur = minDur;
                }

                totalMaxDur += maxDur;
                if (maxDur < minMaxDur) {
                    minMaxDur = maxDur;
                }
                if (maxDur > maxMaxDur) {
                    maxMaxDur = maxDur;
                }

                if (minCosts > costs) {
                    minCosts = costs;
                }
                if (maxCosts < costs) {
                    maxCosts = costs;
                }

                totalMaxWait += maxWait;
                if (maxWait < minMaxWait) {
                    minMaxWait = maxWait;
                }
                if (maxWait > maxMaxWait) {
                    maxMaxWait = maxWait;
                }

                maxWaitStr += maxWait+",";
                maxDurStr += maxDur+",";

                System.out.println((instanceId + "           ").substring(0, 9) + "| " +
                        ("" + engine.getAllInstancesCosts().get(instanceId) + "        ").substring(0, 7) + " | " +
                        ("" + minDur + "        ").substring(0, 8) + " | " +
                        ("" + maxDur + "        ").substring(0, 8) + " | " +
                        ("" + maxWait + "        ").substring(0, 8));
            }

            if (maxWaitStr.length()>0) maxWaitStr =
                    maxWaitStr.substring(0, maxWaitStr.length()-1);
            if (maxDurStr.length()>0) maxDurStr =
                    maxDurStr.substring(0, maxDurStr.length()-1);
            if (inventoryLoad.length()>0) inventoryLoad =
                    inventoryLoad.substring(0, inventoryLoad.length()-1);
            if (finishedItems.length()>0) finishedItems =
                    finishedItems.substring(0, finishedItems.length()-1);

            // Calculate aggregation
            simulationResults.put(SIM_COST_AVG, "" + (totalCosts / engine.getAllInstancesCosts().size()));
            simulationResults.put(SIM_COST_MIN, "" + (minCosts));
            simulationResults.put(SIM_COST_MAX, "" + (maxCosts));

            simulationResults.put(SIM_MIN_DUR_AVG, ""+totalMinDur / engine.getAllInstancesCosts().size());
            simulationResults.put(SIM_MIN_DUR_MIN, ""+minMinDur);
            simulationResults.put(SIM_MIN_DUR_MAX, ""+maxMinDur);

            simulationResults.put(SIM_MAX_DUR_AVG, ""+(totalMaxDur / engine.getAllInstancesCosts().size()));
            simulationResults.put(SIM_MAX_DUR_MIN, ""+minMaxDur);
            simulationResults.put(SIM_MAX_DUR_MAX, ""+maxMaxDur);
            simulationResults.put(SIM_MAX_DUR_DATA, ""+maxDurStr);

            simulationResults.put(SIM_MAX_WAIT_AVG, ""+(totalMaxWait / engine.getAllInstancesCosts().size()));
            simulationResults.put(SIM_MAX_WAIT_MIN, ""+minMaxWait);
            simulationResults.put(SIM_MAX_WAIT_MAX, ""+maxMaxWait);
            simulationResults.put(SIM_MAX_WAIT_DATA, maxWaitStr);

            simulationResults.put(SIM_INVENTORY_LEVEL, inventoryLoad);

            simulationResults.put(SIM_FLOW_RATE, finishedItems);

            // Show aggregation
            try {
                System.out.println("=================================================");
                System.out.println(("AVERAGE     ").substring(0, 9) + "| " +
                        ((totalCosts / engine.getAllInstancesCosts().size()) + "      ").substring(0, 7) + " | " +
                        ((totalMinDur / engine.getAllInstancesCosts().size()) + "       ").substring(0, 8) + " | " +
                        ((totalMaxDur / engine.getAllInstancesCosts().size()) + "       ").substring(0, 8) + " | " +
                        ((totalMaxWait / engine.getAllInstancesCosts().size() + "       ").substring(0, 8)));
                System.out.println(("MIN         ").substring(0, 9) + "| " +
                        (minCosts + "        ").substring(0, 7) + " | " +
                        (minMinDur + "        ").substring(0, 8) + " | " +
                        (minMaxDur + "        ").substring(0, 8) + " | " +
                        (minMaxWait + "        ").substring(0, 8));
                System.out.println(("MAX         ").substring(0, 9) + "| " +
                        (maxCosts + "        ").substring(0, 7) + " | " +
                        (maxMinDur + "        ").substring(0, 8) + " | " +
                        (maxMaxDur + "        ").substring(0, 8) + " | " +
                        (maxMaxWait + "        ").substring(0, 8));
                System.out.println("=================================================");
            } catch (Exception e) {
            }

            // Print LANE report
            laneReporting.printReport();

            // Print simulation run-time if timing is enabled
            if (engine.isTimingEnabled()) {
                int ticks = engine.getTicks() - idleCount +1;
                System.out.println("Simulation run-time: " +
                        (ticks / 1440) + "d " +
                        ((ticks % 1440) / 60) + "h " +
                        ((ticks % 60) + "m"));
            }

            // STOP simulation
            stopSimulation();

            // Inform Listeners
            broadcastSimulationFinished();
        }

    }
}
