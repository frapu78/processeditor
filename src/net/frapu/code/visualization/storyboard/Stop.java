/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

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
public class Stop extends ProcessNode {

    public Stop() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(40,40);
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(Color.BLACK);
        g2.fill(outline);

        g2.setFont(StoryboardUtils.defaultFont.deriveFont(12.0f));
        g2.setPaint(Color.WHITE);
        ProcessUtils.drawText(g2, getPos().x-2, getPos().y-g2.getFont().getBaselineFor('a')-5,
                getSize().width+50, "STOP",
                ProcessUtils.Orientation.CENTER);

    }

    @Override
    protected Shape getOutlineShape() {
        Path2D path = new Path2D.Double();

        path.moveTo(getPos().x-getSize().width/6, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/6, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/2, getPos().y-getSize().height/6);
        path.lineTo(getPos().x+getSize().width/2, getPos().y+getSize().height/6);
        
        
        path.lineTo(getPos().x+getSize().width/6, getPos().y+getSize().height/2);
        path.lineTo(getPos().x-getSize().width/6, getPos().y+getSize().height/2);

        path.lineTo(getPos().x-getSize().width/2, getPos().y+getSize().height/6);
        path.lineTo(getPos().x-getSize().width/2, getPos().y-getSize().height/6);

        path.closePath();

        return path;
    }

    public String toString() {
        return "STOP";
    }

}
