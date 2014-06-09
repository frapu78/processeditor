/**
 *
 * Process Editor - Executable BPMN Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.bpmn;

/**
 *
 * This class holds the lifecycle of a BPMN 2.0 Activity Instance.
 *
 * @author frank
 */
public class ActivityLifecycle {

    public final static String READY = "READY";
    public final static String ACTIVE = "ACTIVE";
    public final static String COMPLETING = "COMPLETING";
    public final static String COMPLETED = "COMPLETED";
    public final static String WITHDRAWN = "WITHDRAWN";
    public final static String COMPENSATING = "COMPENSATING";
    public final static String COMPENSATED = "COMPENSATED";
    public final static String FAILED = "FAILED";
    public final static String TERMINATED = "TERMINATED";

}
