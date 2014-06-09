/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author fpu
 */
public class StickyNote extends FlowObject {

    public StickyNote() {
        setProperty(PROP_SHADOW, TRUE);
        setBackground(new Color(255,233,127));
        setSize(80,80);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setStroke(BPMNUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        Point p = getPos();
        Dimension d = getSize();

        // Draw text
        g2.setFont(BPMNUtils.defaultFont.deriveFont(Font.PLAIN,10.0f));
        BPMNUtils.drawFitText(g2, p.x, p.y-d.height/2, d.width-5, d.height-10, getText());
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);
    }

}
