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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * Provides an abstract element, which all other XForms elements derive from.
 *
 * @author frank
 */
public abstract class BaseElement extends ProcessNode {

    public final static String PROP_REF = "ref";
    public final static String PROP_LABEL = "label";
    public final static String PROP_ENABLED = "enabled";

    public BaseElement() {
        initializeProperties();
    }

    public String getLabel() {
        return getProperty(PROP_LABEL);
    }

    public void initializeProperties() {
        setSize(200, 20);
        setProperty(PROP_REF, "");
        setProperty(PROP_ENABLED, TRUE);
        setPropertyEditor(PROP_ENABLED, new BooleanPropertyEditor());
    }

    /**
     * Might be overwritten; is called by getBoundingBox
     * @param g2
     * @return
     */
    public Rectangle drawLabel(Graphics2D g2) {
        return getOutlineShape().getBounds();
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(500, 500, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(XFormsUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getLabel() != null) {
            textBounds = drawLabel(g2);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    public boolean isEnabled() {
        return getProperty(PROP_ENABLED).equals(TRUE);
    }
    
    public void setEnabled(boolean enabled) {
        setProperty(PROP_ENABLED, enabled?TRUE:FALSE);
    }

}
