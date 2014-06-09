/**
 *
 * Process Editor - XForms Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.xforms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.ProcessUtils.Position;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author fpu
 */
public class Panel extends Cluster {

    public final static String PROP_BORDER = "border";

    public Panel() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setBackground(new Color(222,222,222));
        setSize(300,200);
        setProperty(PROP_BORDER, TRUE);
        setPropertyEditor(PROP_BORDER, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Draw Input field at remaining space
        Shape panelBox = getOutlineShape();
        g2.setPaint(getBackground());
        g2.fill(panelBox);
        
        Point pos = getTopLeftPos();

        // Draw title
        g2.setFont(XFormsUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        XFormsUtils.drawText(g2, pos.x+3, pos.y+g2.getFont().getBaselineFor('a'), getSize().width, getText(), Orientation.LEFT);

        // Draw Reflex
        XFormsUtils.drawReflex(g2, this, Position.BOTTOM_RIGHT);

        if (getProperty(PROP_BORDER).contains(TRUE)) {
            // Draw outline box
            g2.setStroke(XFormsUtils.defaultStroke);
            g2.setPaint(Color.BLACK);
            g2.draw(panelBox);
        }
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(
                getPos().x - getSize().width / 2, getPos().y - getSize().height / 2,
                getSize().width, getSize().height);
    }

}
