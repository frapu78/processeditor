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

import net.frapu.code.visualization.petrinets.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author frank
 */
public class PetriNetEngine {

    /** Token property for storing the actual working time for a token */
    public final static String PROP_TOKEN_TIME = "work_time";
    /** Token property for storing the waiting time for a token */
    public final static String PROP_TOKEN_WAIT_TIME = "wait_time";
    /** Token property for storing the latest update (time-tick) for a token */
    public final static String PROP_TOKEN_LAST_TICK = "last_tick";
    /** The set of listeners for this engine */
    public Set<PetriNetEngineListener> listeners =
            new HashSet<PetriNetEngineListener>();
    /** Internal Petri net model */
    private PetriNetModel model;
    /** Internal map of instance costs (instance, cost) */
    private Map<String, String> instanceCosts = new HashMap<String, String>();
    /** Defines whether enabled transitions should be highlighted or not */
    private boolean highlightEnabledTransitions = false;
    /** Defines the waiting queue for fired transitions (time-ticks enabled */
    private List<TimedTransitionData> waitingList = new LinkedList<TimedTransitionData>();
    /** True if time-ticks are enabled */
    private boolean timingEnabled = false;
    /** The current tick count */
    private int ticks = 0;
    /** Turbo mode enabled or not (single tick counting vs. jump to next event) */
    private boolean turboMode = false;
    /** A time stamp converter cache */
    private static Map<String, Integer> timeStampToTicksCache = new HashMap<String, Integer>();

    public boolean isTurboMode() {
        return turboMode;
    }

    public void setTurboMode(boolean turbo) {
        this.turboMode = turbo;
    }

    public boolean isTimingEnabled() {
        return timingEnabled;
    }

    public void setTimingEnabled(boolean timingEnabled) {
        this.timingEnabled = timingEnabled;
    }

    /**
     * Creates a new Petri net engine.
     * @param m
     */
    public PetriNetEngine(PetriNetModel m) {
        model = m;
    }

    /**
     * Calculates and highlights the enabled Transitions for a given
     * process instance.
     * @param processInstance The process instance id.
     */
    public Set<Transition> calculateEnabledTransitions(int processInstance) {
        Set<Transition> result = new HashSet<Transition>();
        for (ProcessNode n : model.getNodes()) {
            // Consider only Transitions
            if (n instanceof Transition) {
                // Cast ProcessNode to Transition
                Transition t = (Transition) n;
                if (t.isEnabled(getPredecessors(t), processInstance)) {
                    t.setEnabledHighlight(true);
                    result.add(t);
                } else {
                    t.setEnabledHighlight(false);
                }
            }
        }

        return result;
    }

    public void clearEnabledTransitionHighlights() {
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Transition) {
                Transition t = (Transition) n;
                t.setEnabledHighlight(false);
            }
        }
    }

    /**
     * Returns the current tokens of a specific process instance.
     * @param processInstance
     * @return
     */
    public Set<Token> getCurrentTokens(int processInstance) {
        Set<Token> result = new HashSet<Token>();

        // Iterate over all places of the model an collect tokens
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Place) {
                Place p = (Place) n;
                result.addAll(p.getTokens(processInstance));
            }
        }

        return result;
    }

    /**
     * Returns the preset of a transition.
     * @param t
     * @return
     */
    public List<Place> getPreSet(Transition t, Class<?> edgeType) {
        List<Place> preSet = new LinkedList<Place>();
        for (Predecessor pre : getPredecessors(t)) {
            if (pre.getEdgeFromNode().getClass().isAssignableFrom(edgeType)) {
                if (pre.getNode() instanceof Place) {
                    preSet.add((Place) pre.getNode());
                }
            }
        }
        return preSet;
    }

    /**
     * Returns the postset of a transition.
     * @param t
     * @return
     */
    public List<Place> getPostSet(Transition t) {
        List<Place> postSet = new LinkedList<Place>();
        for (ProcessNode n : model.getSuccessors(t)) {
            if (n instanceof Place) {
                postSet.add((Place) n);
            }
        }
        return postSet;
    }

    public Token getLongestWaitingToken(Place p) {
        Token result = null;
        if (p.getTokens().size() > 0) {
            p.getTokens().get(0);
        }
        int highestWaitingTime = 0;

        for (Token t : p.getTokens()) {
            if (t.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME) != null) {
                // Wait time found
                int waitTime =
                        Integer.parseInt(t.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME));
                if (waitTime > highestWaitingTime) {
                    highestWaitingTime = waitTime;
                    result = t;
                }
            }
        }

        return result;
    }

    public Token getShortestWaitingToken(Place p) {
        Token result = null;
        if (p.getTokens().size() > 0) {
            p.getTokens().get(0);
        }
        int shortestWaitingTime = 99999999;

        for (Token t : p.getTokens()) {
            if (t.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME) != null) {
                // Wait time found
                int waitTime =
                        Integer.parseInt(t.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME));
                if (waitTime < shortestWaitingTime) {
                    shortestWaitingTime = waitTime;
                    result = t;
                }
            }
        }

        return result;
    }

    public Token getCurrentLongestWaitingToken(Place p) {
        Token result = null;
        if (p.getTokens().size() > 0) {
            p.getTokens().get(0);
        }
        int lastTickUpdate = 99999999;

        for (Token t : p.getTokens()) {
            if (t.getProperty(PetriNetEngine.PROP_TOKEN_LAST_TICK) != null) {
                // Wait time found
                int lastTick =
                        Integer.parseInt(t.getProperty(PetriNetEngine.PROP_TOKEN_LAST_TICK));
                if (lastTick < lastTickUpdate) {
                    lastTickUpdate = lastTick;
                    result = t;
                }
            }
        }

        return result;
    }

    /**
     * Fire a transition for a certain process instance.
     * @param t
     * @param processInstance
     */
    public void fireTransition(Transition t, int processInstance) {
        // Check if Transition is enabled 
        if (t.isEnabled(getPredecessors(t), processInstance)) {
            //
            // Remove one Token of each preceding Place according to the 
            // process instance.
            // @todo: Should be done in Transition.fire
            //
            List<Place> preSetFromEdges = getPreSet(t, Edge.class);
            List<Place> preSetFromResetEdges = getPreSet(t, ResetEdge.class);
            Set<Token> tokenSet = new HashSet<Token>();
            double highestTime = 0.0;
            int highestWaitTime = 0;
            int highestTickTime = 0;
            for (Place p : preSetFromEdges) {
                Token tok = p.removeToken(processInstance);
                // Consider null Tokens (from Inhibitor Arcs)
                if (tok != null) {
                    tokenSet.add(tok);
                    // Processing time
                    if (tok.getProperty(PROP_TOKEN_TIME) != null) {
                        // Time found
                        double tokTime =
                                Double.parseDouble(tok.getProperty(PROP_TOKEN_TIME));
                        if (tokTime > 0.0) {
                            if (tokTime > highestTime) {
                                highestTime = tokTime;
                            }
                        }
                    }
                    // Waiting time
                    if (tok.getProperty(PROP_TOKEN_WAIT_TIME) != null) {
                        // Waiting time found
                        int waitTime =
                                Integer.parseInt(tok.getProperty(PROP_TOKEN_WAIT_TIME));
                        if (waitTime > 0) {
                            if (waitTime > highestWaitTime) {
                                highestWaitTime = waitTime;
                            }
                        }
                    }
                    // Tick time
                    if (tok.getProperty(PROP_TOKEN_LAST_TICK) != null) {
                        // Tick time found
                        int tickTime =
                                Integer.parseInt(tok.getProperty(PROP_TOKEN_LAST_TICK));
                        if (tickTime > 0) {
                            if (tickTime > highestTickTime) {
                                highestTickTime = tickTime;
                            }
                        }
                    }
                }
            }

            // Empty Places connected via ResetEdges
            for (Place p: preSetFromResetEdges) {
                p.removeAllTokens();
            }

            // Add current waiting time to highest waiting time
            highestWaitTime += getTicks() - highestTickTime;

            //
            // Add costs to instance if applicable
            //
            String costStr = t.getProperty(Transition.PROP_COST);
            double cost = 0.0;
            if (costStr.length() > 0) {
                try {
                    cost = Double.parseDouble(costStr);
                } catch (NumberFormatException e) {
                }

            }
            String existingCostStr = instanceCosts.get("" + processInstance);
            if (existingCostStr != null) {
                double existingCost = 0.0;
                try {
                    existingCost = Double.parseDouble(existingCostStr);
                } catch (NumberFormatException e) {
                }

                instanceCosts.put("" + processInstance, "" + (cost + existingCost));
            } else {
                // New entry
                instanceCosts.put("" + processInstance, "" + (cost));
            }

            // Add duration to time if applicable
            int duration = 0;
            if (t.getProperty(Transition.PROP_DURATION) != null) {
                try {
                    duration = Integer.parseInt(t.getProperty(Transition.PROP_DURATION));
                    highestTime += duration;
                } catch (NumberFormatException e) {
                }
            }

            // Call "fire transition" hook
            t.fire(tokenSet);

            // Create new Token
            Token newTok = new Token(processInstance);
            // Add highest duration / waiting time to new Token
            newTok.setProperty(PROP_TOKEN_TIME, "" + highestTime);
            newTok.setProperty(PROP_TOKEN_WAIT_TIME, "" + highestWaitTime);
            //
            // Fire transition
            //
            if (isTimingEnabled() && (duration > 0)) {
                // Add Transition and Token to waiting list if timing enabled
                // and remaining time > 0
                waitingList.add(new TimedTransitionData(t, newTok, duration));
            } else {
                // Add one token for each succeeding Place else
                List<Place> postSet = getPostSet(t);
                // Add highest duration/waiting time to new Token                  
                newTok.setProperty(PROP_TOKEN_LAST_TICK, "" + getTicks());
                for (Place p : postSet) {
                    p.addToken(newTok);
                }
            }

            // Inform all listeners
            for (PetriNetEngineListener l : listeners) {
                l.transitionFired(t, processInstance, cost, duration);
            }
        }
    }

    /**
     * Processes the waiting list and adds Tokens to the postsets
     */
    public void tick() {
        // Tick only if somethings inside the waiting list...
        //if (waitingList.size()==0) return;

        //System.out.println("TICK: "+getTicks()+" WAIT_COUNT: "+waitingList.size());

        // Temporary list for TimedTransitionData to be removed
        List<TimedTransitionData> removalList = new LinkedList<TimedTransitionData>();
        // Counting the remaining time for visualization
        Map<Transition, Integer> remainingTime = new HashMap<Transition, Integer>();
        // Counting the instances for visualization
        Map<Transition, Integer> instanceCount = new HashMap<Transition, Integer>();

        // Figure out number of counts until next possible action
        int tickCounts = 1;

        if (turboMode) {
            tickCounts = Integer.MAX_VALUE;
            for (TimedTransitionData waiting : waitingList) {
                if (waiting.getRemainingTime() < tickCounts) {
                    tickCounts = waiting.getRemainingTime();
                }
            }
            if (tickCounts == Integer.MAX_VALUE) {
                tickCounts = 1;
            }
        }

        for (TimedTransitionData waiting : waitingList) {
            // Clear instance count
            waiting.getTransition().setInstanceCount(0);

            // Check if ready
            if (waiting.isReady()) {
                // Add current tick to token
                if (waiting.getTransition().isResetWaiting()) {
                    // Reset waiting time
                    waiting.getToken().setProperty(PROP_TOKEN_WAIT_TIME, "0");
                }
                // Add waiting time
                waiting.getToken().setProperty(PROP_TOKEN_LAST_TICK, "" + getTicks());
                // 1. Put Tokens in postset
                List<Place> postSet = getPostSet(waiting.getTransition());


                //System.out.println("TRANS: "+waiting.getTransition().getText());

                for (Place p : postSet) {
                    p.addToken(waiting.getToken().clone());
                    // 2. Add data to removal list
                    removalList.add(waiting);
                }
            } else {
                // Send tick to waitings
                waiting.tick(tickCounts);

                // Update ticks
                if (remainingTime.get(waiting.getTransition()) == null) {
                    // Create entries
                    remainingTime.put(waiting.getTransition(), waiting.getRemainingTime());
                    instanceCount.put(waiting.getTransition(), 1);
                } else {
                    // Update entries
                    if (waiting.getRemainingTime() > 0) {
                        if (waiting.getRemainingTime() < remainingTime.get(waiting.getTransition())) {
                            remainingTime.put(waiting.getTransition(), waiting.getRemainingTime());
                        }
                        instanceCount.put(waiting.getTransition(), instanceCount.get(waiting.getTransition()) + 1);
                    }
                }
            }
        }

        // Update remaining times (visualization)
        for (Transition t : remainingTime.keySet()) {
            t.setRemainingTime(remainingTime.get(t));
        }
        // Update instance count (visualization)
        for (Transition t : instanceCount.keySet()) {
            t.setInstanceCount(instanceCount.get(t));
        }

        // Process all Places and set warning level!
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Place) {
                Place p = (Place) n;
                // Set warning level
                Token tok = this.getCurrentLongestWaitingToken(p);
                int wt = 0;
                try {
                    if (tok != null) {
                        wt = Integer.parseInt(tok.getProperty(PROP_TOKEN_LAST_TICK));
                    }
                } catch (NumberFormatException e) {
                }
                // Check if p has postset
                List<ProcessNode> succ = model.getSuccessors(p);
                if (succ.size() > 0) {
                    if (tok != null) {
                        p.setWarningLevel((getTicks() - wt) / 5);
                    } else {
                        p.setWarningLevel(0);
                    }
                }
            }
        }

        waitingList.removeAll(removalList);

        // Send tick to all TimeConsumers
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof TimeConsumer) {
                TimeConsumer t = (TimeConsumer) n;
                t.addTicks(tickCounts);
            }
        }

        // Count the tick internally
        addTicks(tickCounts);
    }

    /**
     * Returns true if the waiting list is empty (for time-ticks)
     */
    public boolean isWaitingEmpty() {
        return (waitingList.size() == 0);
    }

    public boolean getHighlightEnabledTransitions() {
        return highlightEnabledTransitions;
    }
    
    public void setHighlightEnabledTransitions(boolean b) {
        highlightEnabledTransitions = b;
    }

    /**
     * Returns the current instance costs of all instances.
     * @return
     */
    public Map<String, String> getAllInstancesCosts() {
        return instanceCosts;
    }

    public double getInstanceCost(int processInstance) {
        double result = 0.0;
        // Iterate over all instances
        for (String key : instanceCosts.keySet()) {
            if (key.equals("" + processInstance)) {
                return Double.parseDouble(instanceCosts.get(key));
            }
        }
        return result;
    }

    /**
     * Returns the current tick count of the engine.
     * @return
     */
    public int getTicks() {
        return ticks;
    }

    /**
     * Adds a given number of ticks.
     */
    protected void addTicks(int tickCounts) {
        ticks += tickCounts;
    }

    /**
     * Adds a PetriNetEngineListener to this engine.
     * @param listener
     */
    public void addListener(PetriNetEngineListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a PetriNetEngineListener from this engine.
     * @param listener
     */
    public void removeListener(PetriNetEngineListener listener) {
        listeners.remove(listener);
    }

    /**
     * Resets the current instance costs for all instances.
     */
    public void resetEngine() {
        // Clear instance costs
        instanceCosts = new HashMap<String, String>();
        // Clear waiting list
        waitingList = new LinkedList<TimedTransitionData>();
        // Clear tick count
        ticks = 0;
        // Set remaining times and instances to zero
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Transition) {
                Transition t = (Transition) n;
                t.setRemainingTime(0);
                t.setInstanceCount(0);
            }
            // Clear warning level of Places
            if (n instanceof Place) {
                Place p = (Place) n;
                p.setWarningLevel(0);
            }
            // Reset LaborPlaces
            if (n instanceof TimeConsumer) {
                TimeConsumer t = (TimeConsumer) n;
                t.resetTicks();
            }
        }
        // Inform listeners
        for (PetriNetEngineListener l : listeners) {
            l.engineReset();
        }
    }

    /**
     * Converts a time stamp to ticks
     * @param time - HHMM
     * @return
     */
    public static int convertTimeToTicks(String time) {
        // Lookup cache
        if (timeStampToTicksCache.containsKey(time)) {
            return timeStampToTicksCache.get(time);
        }
        // Calculate result
        try {
            int hours = Integer.parseInt(time.substring(0, 2));
            int mins = Integer.parseInt(time.substring(2, 4));
            int result = (hours * 60) + mins;
            // Store result in cache
            timeStampToTicksCache.put(time, result);
            // Return result
            return result;
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * Returns the tick count for the current day; i.e. substracting all
     * ticks from prior days
     * @param ticks
     * @return
     */
    public static int getTicksOfDay(int ticks) {
        return (ticks % 1440);
    }

    public List<Predecessor> getPredecessors(Transition t) {
        List<Predecessor> result = new LinkedList<Predecessor>();

        for (ProcessEdge edge : model.getEdges()) {
            if (edge.getTarget() == t) {
                result.add(new Predecessor(edge.getSource(), edge));
            }
        }

        return result;
    }
}
