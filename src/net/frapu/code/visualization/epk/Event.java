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
import java.awt.geom.Path2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class Event extends ProcessNode {

    public Event() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(80,60);
        setBackground(new Color(255,204,255));
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

        g2.setFont(EPKUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y,
                getSize().width, getText(),
                ProcessUtils.Orientation.CENTER);

    }

    @Override
    protected Shape getOutlineShape() {
        Path2D path = new Path2D.Double();

        path.moveTo(getPos().x+getSize().width/3, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/2, getPos().y);
        path.lineTo(getPos().x+getSize().width/3, getPos().y+getSize().height/2);
        path.lineTo(getPos().x-getSize().width/3, getPos().y+getSize().height/2);
        path.lineTo(getPos().x-getSize().width/2, getPos().y);
        path.lineTo(getPos().x-getSize().width/3, getPos().y-getSize().height/2);

        path.closePath();

        return path;
    }

}
