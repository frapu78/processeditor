/**
 *
 * Process Editor - EPK Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.epk;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class OrgUnit extends ProcessNode {

    public OrgUnit() {
            super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(90,50);
        setBackground(new Color(255,255,204));
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.drawLine(getPos().x-getSize().width/2+8, getPos().y-getSize().height/2,
                getPos().x-getSize().width/2+8, getPos().y+getSize().height/2);

        g2.setFont(EPKUtils.defaultFont);

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
