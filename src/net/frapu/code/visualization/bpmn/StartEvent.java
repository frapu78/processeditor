/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author fpu
 */
public class StartEvent extends Event {

    /** The interruption type of the event. Possible values are "0" or "Throwing" **/
    public final static String PROP_NON_INTERRUPTING = "non_interupting";
    public final static String EVENT_NON_INTERRUPTING_FALSE = "0";
    public final static String EVENT_NON_INTERRUPTING_TRUE = "1";

    public StartEvent() {
        super();
        initializeProperties();
    }

    public StartEvent(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {
        setProperty(PROP_NON_INTERRUPTING, EVENT_NON_INTERRUPTING_FALSE);
        setPropertyEditor(PROP_NON_INTERRUPTING, new BooleanPropertyEditor());
    }

    @Override
    public void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (getProperty(PROP_NON_INTERRUPTING).toLowerCase().equals(EVENT_NON_INTERRUPTING_TRUE))  {
            g2.setStroke(BPMNUtils.longDashedStroke);
        } else {
            g2.setStroke(BPMNUtils.defaultStroke);
        }

        drawEventBasicShape(g2);
        // Call stub
        drawMarker(g2);
    }

    /**
     * 
     * @param g2
     */
    protected void drawMarker(Graphics2D g2) {
        // Just a stub here...
    }

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(StartEvent.class);
        result.add(MessageStartEvent.class);
        result.add(TimerStartEvent.class);
        result.add(ErrorStartEvent.class);
        result.add(EscalationStartEvent.class);
        result.add(CompensationStartEvent.class);
        result.add(ConditionalStartEvent.class);
        result.add(SignalStartEvent.class);
        result.add(MultipleStartEvent.class);
        result.add(ParallelMultipleStartEvent.class);
        return result;
    }

    public String toString() {
        return "Start Event ("+getText()+")";
    }
}
