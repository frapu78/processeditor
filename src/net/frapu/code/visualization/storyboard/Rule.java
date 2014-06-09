/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * @author fpu
 */
public class Rule extends ProcessNode {

    public Rule() {
        super();
        setBackground(Color.WHITE);
        setSize(30,30);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        Shape s = getOutlineShape();
        g2.setPaint(getBackground());
        g2.fill(s);

        g2.setStroke(StoryboardUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        g2.draw(s);

        Point p = getTopLeftPos();
        Dimension d = getSize();
        Shape is = new Ellipse2D.Double(p.x+5, p.y+5, d.width-10, d.height-10);
        g2.fill(is);

        g2.setPaint(Color.WHITE);
        g2.setFont(StoryboardUtils.defaultFont);
        StoryboardUtils.drawText(g2, getPos().x-3, getPos().y-5, 30, "R", Orientation.CENTER);
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Ellipse2D.Double(p.x, p.y, d.width, d.height);
    }

}
