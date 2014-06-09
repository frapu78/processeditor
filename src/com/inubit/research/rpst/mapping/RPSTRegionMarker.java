/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.bpmn.BPMNUtils;

/**
 *
 * @author fel
 */
public class RPSTRegionMarker extends ProcessHelper {
    private static final Color DEFAULT_COLOR = new Color( 255, 0, 0, 180 );
    private static final Color SELECTION_COLOR = new Color( 50, 205, 50 );
    private Point topLeft;
    private Point bottomRight;
    private String text;

    private Color currentColor = DEFAULT_COLOR;

    public RPSTRegionMarker( Point topLeft, Point bottomRight, String text ) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.text = text;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = ( Graphics2D ) g;
        g2.setStroke(BPMNUtils.dashedStroke);
        g2.setPaint( currentColor );
        Shape s = getShape();
        g2.draw(s);
        g2.drawString(text , s.getBounds().x + 5, s.getBounds().y + 15);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public void setHighlighted( boolean highlighted ) {
        if ( highlighted )
            this.currentColor = SELECTION_COLOR;
        else
            this.currentColor = DEFAULT_COLOR;
    }

    private Shape getShape() {
        RoundRectangle2D rect = new RoundRectangle2D.Double(
                topLeft.getX(),
                topLeft.getY(),
                bottomRight.getX() - topLeft.getX(),
                bottomRight.getY() - topLeft.getY(),
                10.0,
                10.0);

        return rect;
    }
}
