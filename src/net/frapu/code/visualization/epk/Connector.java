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
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class Connector extends ProcessNode {

    public Connector() {
        init();
    }

    protected void init() {
        setSize(30,30);
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(30, 30);
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

        drawMarker(g2);
    }

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();

        result.add(XORConnector.class);
        result.add(ANDConnector.class);
        result.add(ORConnector.class);

        return result;
    }

    /**
     * 
     * @param g2
     */
    protected void drawMarker(Graphics2D g2) {
        // Do nothing here
    }

    @Override
    protected Shape getOutlineShape() {
        return new Ellipse2D.Double(getPos().x-getSize().width/2, getPos().y-getSize().height/2,
                getSize().width, getSize().height);
    }

}
