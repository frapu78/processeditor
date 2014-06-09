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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public abstract class RectangularElement extends Cluster {

    public static final String PROP_SHOW_ONLY_SYMBOL = "symbol_only";

    public RectangularElement() {
        super();
        setSize(80, 50);
        setBackground(new Color(222, 222, 255));
        setProperty(PROP_COLLAPSED, TRUE);
        setProperty(PROP_SHOW_ONLY_SYMBOL, FALSE);
        setPropertyEditor(PROP_SHOW_ONLY_SYMBOL, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Shape outline = getOutlineShape();

        g2.setStroke(ArchimateUtils.defaultStroke);
        g2.setPaint(getBackground());
        g2.fill(outline);
        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        Point p = getTopLeftPos();
        Dimension d = getSize();

        drawSymbol(g2);

        // Draw text
        if (isCollapsed()) {
            ArchimateUtils.drawText(g2, getPos().x, getPos().y, d.width, getText(), ArchimateUtils.Orientation.CENTER);
        } else {
            // Draw text at top left position
            ArchimateUtils.drawText(g2, p.x + 5, p.y + 5, d.width, getText(), ArchimateUtils.Orientation.LEFT);
        }
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);
    }

    protected abstract void drawSymbol(Graphics2D g2);

    /**
     * Draws the symbol inside the given Rectangle.
     * @param p
     * @param d
     */
    protected abstract void drawSymbol(Graphics2D g2, Rectangle r);
}
