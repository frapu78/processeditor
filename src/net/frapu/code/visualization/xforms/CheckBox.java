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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.ProcessUtils.Position;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * Provides an input element. (First version)
 *
 * @author frank
 */
public class CheckBox extends BaseElement {

    public final static String PROP_SELECTED = "selected";

    public CheckBox() {
        super();
    }

    @Override
    public void initializeProperties() {
        super.initializeProperties();
        setSize(20, 20);
        setProperty(PROP_LABEL, "Label");
        setProperty(PROP_SELECTED, FALSE);
        setPropertyEditor(PROP_SELECTED, new BooleanPropertyEditor());
    }

    /**
     * Set fixed size.
     * @param key
     * @param value
     */
    @Override
    public void setProperty(String key, String value) {
        if (key.equals(PROP_HEIGHT)) value = "20";
        if (key.equals(PROP_WIDTH)) value = "20";
        super.setProperty(key, value);
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

        // Draw Check mark
        if (getProperty(PROP_SELECTED).equals(TRUE)) {
            Point p = getTopLeftPos();
            Dimension d = getSize();

            g2.setStroke(XFormsUtils.boldStroke);
            g2.drawLine(p.x+5, p.y+5, p.x+d.width-5, p.y+d.height-5);
            g2.drawLine(p.x+5, p.y+d.height-5, p.x+d.width-5, p.y+5);
        }

        // Draw Reflex
        g2.setStroke(XFormsUtils.defaultStroke);
        XFormsUtils.drawReflex(g2, this, Position.TOP);
        // Draw outline box
        g2.setPaint(Color.BLACK);
        g2.draw(inputBox);
    }

    @Override
    public Rectangle drawLabel(Graphics2D g2) {
        if (getLabel()==null) return null;
        // Set Font
        g2.setPaint(Color.BLACK);
        g2.setFont(XFormsUtils.defaultFont);
        Rectangle2D textSize = XFormsUtils.drawText(g2,
                getPos().x + getSize().width / 2 + 5, getTopLeftPos().y+g2.getFont().getBaselineFor('a'),
                200, getLabel(), Orientation.LEFT);
        return (Rectangle)textSize;
    }

    @Override
    public Point getSelectionOffset() {
        Rectangle bounds = this.getBounds();

        BufferedImage dummyImg = new BufferedImage(500, 500, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        Rectangle textBounds = this.drawLabel(g2);

        return new Point((int) Math.round((bounds.x - textBounds.x) / 2.0), 0);
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(
                getPos().x - getSize().width / 2, getPos().y - getSize().height / 2,
                getSize().width, getSize().height);
    }
}
