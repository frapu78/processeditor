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

import java.awt.Graphics2D;

/**
 *
 * @author frank
 */
public class ConditionalStartEvent extends StartEvent {

    @Override
    protected void drawMarker(Graphics2D g2) {
        this.drawConditional(g2);
    }
    
}
