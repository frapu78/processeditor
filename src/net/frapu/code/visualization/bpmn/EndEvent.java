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

/**
 *
 * @author fpu
 */
public class EndEvent extends Event {

    public EndEvent() {
        super();
    }
    
    public EndEvent(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label); 
    }

    public void paintInternal(Graphics g) {
       Graphics2D g2 = (Graphics2D) g;

       // Draw intermediate event
       g2.setStroke(BPMNUtils.boldStroke);
       drawEventBasicShape(g2);

       // Call stub
       drawMarker(g2);
    }
    /**
     * stub
     * @param g2
     */
    protected void drawMarker(Graphics2D g2) {
        // Just a stub here...
    }

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(EndEvent.class);
        result.add(MessageEndEvent.class);
        result.add(ErrorEndEvent.class);
        result.add(EscalationEndEvent.class);
        result.add(CancelEndEvent.class);
        result.add(CompensationEndEvent.class);
        result.add(SignalEndEvent.class);
        result.add(TerminateEndEvent.class);
        result.add(MultipleEndEvent.class);
        return result;
    }
    
    @Override
    public String toString() {
        return "End Event ("+getText()+")";
    }
    
}
