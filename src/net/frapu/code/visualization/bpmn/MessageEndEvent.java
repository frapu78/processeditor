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

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author frank
 */
public class MessageEndEvent extends EndEvent {

    @Override
    protected void drawMarker(Graphics2D g2) {

        this.setEventTypeFillColor(Color.BLACK);
        this.setEventTypeOutlineColor(Color.WHITE);

        this.drawMessage(g2);
    }
}
