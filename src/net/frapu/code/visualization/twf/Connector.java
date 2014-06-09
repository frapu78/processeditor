/**
 *
 * Process Editor - TWF Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.twf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class Connector extends Module {

    public Connector() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setText("Connector");
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(new Color(50,150,50));
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.setFont(TWFUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                getSize().width+50, getText(),
                ProcessUtils.Orientation.TOP);

    }

    @Override
    protected Shape getOutlineShape() {
        Path2D path = new Path2D.Double();

        path.moveTo(getPos().x-getSize().width/3, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/3, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/2, getPos().y-getSize().height/3);
        path.lineTo(getPos().x+getSize().width/2, getPos().y+getSize().height/3);
        
        
        path.lineTo(getPos().x+getSize().width/3, getPos().y+getSize().height/2);
        path.lineTo(getPos().x-getSize().width/3, getPos().y+getSize().height/2);

        path.lineTo(getPos().x-getSize().width/2, getPos().y+getSize().height/3);
        path.lineTo(getPos().x-getSize().width/2, getPos().y-getSize().height/3);

        path.closePath();

        return path;
    }

}
