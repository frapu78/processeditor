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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class ProcessGroup extends ProcessNode {

    public ProcessGroup() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(150,60);
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

        g2.setFont(ProcessMapUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y,
                getSize().width, getText(),
                ProcessUtils.Orientation.CENTER);

    }

    @Override
    protected Shape getOutlineShape() {
        Path2D path = new Path2D.Double();

        Point p = getTopLeftPos();
        Dimension d = getSize();
        final int INSET = 20;

        path.moveTo(p.x, p.y);

        path.lineTo(p.x+d.width-INSET, p.y);
        path.lineTo(p.x+d.width, p.y+d.height/2);
        path.lineTo(p.x+d.width-INSET, p.y+d.height);
        path.lineTo(p.x, p.y+d.height);
        path.lineTo(p.x+INSET, p.y+d.height/2);

        path.closePath();

        return path;
    }

}
