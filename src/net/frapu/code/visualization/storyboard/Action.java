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
import java.awt.geom.RoundRectangle2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author fpu
 */
public class Action extends ProcessNode {

    public final static String PROP_MULTIPLE = "multiple";

    private final static int MARKER_ICON_SIZE = 10;

    public Action() {
        super();
        setBackground(Color.BLACK);
        setSize(100,72);
        setText("Action");
        setProperty(PROP_MULTIPLE, FALSE);
        setPropertyEditor(PROP_MULTIPLE, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        // Fill outline
        Shape outline = getOutlineShape();
        g2.setPaint(Color.WHITE);
        g2.fill(outline);

        // Draw outline
        g2.setPaint(Color.BLACK);
        g2.setStroke(StoryboardUtils.boldStroke);
        g2.draw(outline);

        Point p = getTopLeftPos();
        Dimension d = getSize();
        Shape rect = new RoundRectangle2D.Double(p.x+5, p.y+5, d.width-10, d.height-10,10,10);

        // Fill inner area
        g2.setPaint(getBackground());
        g2.fill(rect);

        // Draw title
        g2.setPaint(Color.WHITE);
        g2.setFont(StoryboardUtils.defaultFont);
        StoryboardUtils.drawFitText(g2, getPos().x, p.y, d.width-20, d.height-20, getText());

        // Draw marker
        if (getProperty(PROP_MULTIPLE).equals(TRUE)) {
            drawStandardLoop(g2, p.x+d.width-MARKER_ICON_SIZE-5, p.y+15);
        }
    }

    public static void drawStandardLoop(Graphics2D g2, int x, int y) {
        g2.setStroke(StoryboardUtils.defaultStroke);
        g2.drawArc(x - MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, MARKER_ICON_SIZE, MARKER_ICON_SIZE, 315, 270);

        g2.drawLine(x, y, x, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x, y + MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new RoundRectangle2D.Double(p.x, p.y, d.width, d.height,10,10);
    }

}
