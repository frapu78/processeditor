/**
 *
 * Process Editor - Archimate Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.archimate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class BusinessActor extends RectangularElement {

    private static final int SYMBOL_WIDTH = 10;
    private static final int SYMBOL_HEIGHT = 16;

    public BusinessActor() {
        super();
        setText("Actor");
    }

    /**
     * Draws the symbol at the default position
     * @param g2
     */
    @Override
    protected void drawSymbol(Graphics2D g2) {
        // Draw symbol
        Point p = getTopLeftPos();
        Dimension d = getSize();
        Rectangle symbolPos = new Rectangle(p.x+d.width-SYMBOL_WIDTH-3, p.y+3,
                SYMBOL_WIDTH, SYMBOL_HEIGHT);
        drawSymbol(g2, symbolPos);
    }

    /**
     * Draws the symbol inside the given Rectangle.
     * @param p
     * @param d
     */
    @Override
    protected void drawSymbol(Graphics2D g2, Rectangle r) {
        // Draw Actor symbol inside r
        g2.setStroke(ArchimateUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        // Draw head
        g2.drawOval((int) (r.x + (0.2 * r.width)), r.y,
                (int) (0.6 * r.width), (int) (r.height * 0.3));
        // Draw body
        g2.drawLine((int) (r.x + (0.5 * r.width)), (int) (r.y + (r.height * 0.3)),
                (int) (r.x + (0.5 * r.width)), (int) (r.y + (r.height * 0.6)));
        // Draw arms
        g2.drawLine(r.x, (int) (r.y + (r.height * 0.4)),
                r.x + r.width, (int) (r.y + (r.height * 0.4)));
        // Draw legs
        g2.drawLine((int) (r.x + (0.5 * r.width)), (int) (r.y + (r.height * 0.6)),
                r.x, r.y + r.height);
        g2.drawLine((int) (r.x + (0.5 * r.width)), (int) (r.y + (r.height * 0.6)),
                r.x+r.width, r.y + r.height);
    }
}
