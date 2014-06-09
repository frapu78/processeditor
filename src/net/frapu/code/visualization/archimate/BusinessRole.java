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
public class BusinessRole extends RectangularElement {

    private static final int SYMBOL_WIDTH = 20;
    private static final int SYMBOL_HEIGHT = 10;

    public BusinessRole() {
        super();
        setText("Role");
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
        g2.setStroke(ArchimateUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        // Draw left arc
        g2.drawArc(r.x, r.y, (int)(0.3*r.width), r.height, 90, 180);
        // Draw lines
        g2.drawLine((int)(r.x+(0.15*r.width)), r.y, (int)(r.x+(0.7*r.width)), r.y);
        g2.drawLine((int)(r.x+(0.15*r.width)), r.y+r.height, (int)(r.x+(0.7*r.width)), r.y+r.height);
        // Draw circle
        g2.drawOval((int)(r.x+(0.6*r.width)), r.y, (int)(0.3*r.width), r.height);
    }
}
