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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author fpu
 */
public class Group extends Artifact {

	
	/**
	 * 
	 */
	public Group() {
		setSize(300, 150);
	}
	
    @Override
    protected void paintInternal(Graphics g) {
        drawGroup(g);
    }

    protected void drawGroup(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(BPMNUtils.dottedDashedStroke);
        g2.setPaint(Color.GRAY);
        g2.draw(getOutlineShape());
        g2.setFont(BPMNUtils.defaultFont);
        BPMNUtils.drawText(g2, getPos().x, getPos().y-getSize().height/2, getSize().width - 8, getText(), BPMNUtils.Orientation.TOP);
    }

    @Override
    protected Shape getOutlineShape() {
                RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
    }

}
