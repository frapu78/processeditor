/**
 *
 * Process Editor - Process Map Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.processmap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import net.frapu.code.visualization.editors.ReferencePropertyEditor;

/**
 *
 * @author fpu
 */
public class Process extends ProcessNode {

    public static ReferenceChooserRestriction restrictions;

    public static ReferenceChooserRestriction getReferenceRestrictions() {
        if (restrictions == null) {
            restrictions = new ReferenceChooserRestriction(null, null);
        }
        return restrictions;
    }

    public Process() {
        super();
        initializeProperties();
        setProperty(PROP_REF,"");
        setPropertyEditor(PROP_REF, new ReferencePropertyEditor(getReferenceRestrictions()));
    }

    protected void initializeProperties() {
        setSize(100,50);
        setBackground(new Color(244,244,244));
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (getProperty(PROP_REF).isEmpty()) {
            g2.setStroke(ProcessUtils.defaultStroke);
        } else {
            g2.setStroke(ProcessUtils.boldStroke);
        }
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.drawLine(getPos().x-getSize().width/2+8, getPos().y-getSize().height/2,
                getPos().x-getSize().width/2+8, getPos().y+getSize().height/2);

        g2.setFont(ProcessMapUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y,
                getSize().width, getText(),
                ProcessUtils.Orientation.CENTER);

    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

}
