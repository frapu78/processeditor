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
public class MessageIntermediateEvent extends IntermediateEvent {

	/**
	 * 
	 */
	public MessageIntermediateEvent() {
		super();
		setThrowable(true);
	}
	
    @Override
    protected void drawMarker(Graphics2D g2) {
        if (getProperty(PROP_EVENT_SUBTYPE).toLowerCase().equals(EVENT_SUBTYPE_THROWING.toLowerCase())) {
            this.setEventTypeFillColor(Color.BLACK);
            this.setEventTypeOutlineColor(Color.WHITE);
        }else {
        	this.setEventTypeFillColor(Color.WHITE);
            this.setEventTypeOutlineColor(Color.BLACK);
        }
        this.drawMessage(g2);
    }

}
