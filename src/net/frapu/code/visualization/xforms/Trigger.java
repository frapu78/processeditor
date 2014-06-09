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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.ProcessUtils.Position;

/**
 *
 * Provides a trigger (button) element.
 *
 * @author frank
 */
public class Trigger extends BaseElement {

    public Trigger() {
        super();
    }

    @Override
    public void initializeProperties() {
        super.initializeProperties();
        setSize(100, 20);
        setText("Ok");
        setBackground(new Color(228,228,228));
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Draw Label
        drawLabel(g2);

        // Draw Input field at remaining space
        Shape inputBox = getOutlineShape();
        g2.setPaint(getBackground());
        g2.fill(inputBox);
        if (isEnabled()) {
            g2.setPaint(Color.BLACK);
        } else {
            g2.setPaint(Color.GRAY);
        }

        // Draw Text
        g2.setFont(XFormsUtils.defaultFont);
        XFormsUtils.drawText(g2,
                getPos().x, (int) (getPos().y-g2.getFont().getSize()/3),
                getSize().width-10, getText(), Orientation.CENTER);

        // Draw Reflex
        XFormsUtils.drawReflex(g2, this, Position.BOTTOM);
        // Draw outline box
        g2.setStroke(XFormsUtils.defaultStroke);
        g2.setPaint(Color.GRAY);
        g2.draw(inputBox);
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(
                getPos().x - getSize().width / 2, getPos().y - getSize().height / 2,
                getSize().width, getSize().height);
    }
}
