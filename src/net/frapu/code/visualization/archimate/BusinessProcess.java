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
import java.awt.geom.Path2D;

/**
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class BusinessProcess extends RoundRectangularElement {

    private static final int SYMBOL_WIDTH = 16;
    private static final int SYMBOL_HEIGHT = 10;

    public BusinessProcess() {
        super();
        setText("Process");
    }

    @Override
    protected void drawSymbol(Graphics2D g2) {
        // Draw symbol
        Point p = getTopLeftPos();
        Dimension d = getSize();
        Rectangle symbolPos = new Rectangle(p.x+d.width-SYMBOL_WIDTH-5, p.y+3,
                SYMBOL_WIDTH, SYMBOL_HEIGHT);
        drawSymbol(g2, symbolPos);
    }

    @Override
    protected void drawSymbol(Graphics2D g2, Rectangle r) {
        // Draw Arrow symbol inside r
        g2.setStroke(ArchimateUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        // Draw arrow
        Path2D path = new Path2D.Double();
        path.moveTo(r.x, r.y+r.height*0.3);
        path.lineTo(r.x+r.width*0.7, r.y+r.height*0.3);
        path.lineTo(r.x+r.width*0.7, r.y);
        path.lineTo(r.x+r.width, r.y+r.height*0.5);
        path.lineTo(r.x+r.width*0.7, r.y+r.height);
        path.lineTo(r.x+r.width*0.7, r.y+r.height*0.7);
        path.lineTo(r.x, r.y+r.height*0.7);
        path.closePath();

        g2.draw(path);
    }

}
