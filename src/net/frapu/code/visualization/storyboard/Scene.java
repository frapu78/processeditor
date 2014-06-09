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
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * @author fpu
 */
public class Scene extends Cluster {

    public static String PROP_SCENE_SEQUENCE_NUMBER = "sequence_number";

    public Scene() {
        super();
        setSize(300,250);
        setText("Scene");
        setProperty(PROP_SCENE_SEQUENCE_NUMBER, TRUE);
    }

    @Override
    protected void paintInternal(Graphics g) {
        // Draw the scene cutboard
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(StoryboardUtils.defaultStroke);

        // Fill background
        Shape outline = getOutlineShape();
        g2.setPaint(getBackground());
        g2.fill(outline);

        // Draw outline
        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Fill top
        final int topHeight = 20;
        Point p = getTopLeftPos();
        Dimension d = getSize();
        Shape rect = new Rectangle2D.Double(p.x+5, p.y+5, d.width-105, topHeight);
        g2.fill(rect);
        Shape rect2 = new Rectangle2D.Double(p.x+d.width-95, p.y+5, 90, topHeight);
        g2.fill(rect2);
        g2.drawLine(p.x, p.y+topHeight+10, p.x+d.width, p.y+topHeight+10);

        // Draw Sequence number and title
        g2.setPaint(Color.WHITE);
        g2.setFont(StoryboardUtils.defaultFont);
        StoryboardUtils.drawText(g2, p.x+8, p.y+g2.getFont().getBaselineFor('a'),
                d.width-105, getText(), Orientation.LEFT);
        StoryboardUtils.drawText(g2, p.x+d.width-8, p.y+g2.getFont().getBaselineFor('a'),
                d.width-105, "#"+getProperty(PROP_SCENE_SEQUENCE_NUMBER), Orientation.RIGHT);
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);
    }

}
