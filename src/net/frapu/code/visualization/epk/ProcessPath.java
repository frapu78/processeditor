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
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class ProcessPath extends ProcessNode {

    public ProcessPath() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(80,60);
        setBackground(new Color(204,204,204));
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);

        g2.setPaint(getBackground());
        g2.fill(getEventShape());
        g2.setPaint(Color.BLACK);
        g2.draw(getEventShape());

        g2.setPaint(getBackground());
        g2.fill(getFunctionShape());
        g2.setPaint(Color.BLACK);
        g2.draw(getFunctionShape());

        g2.setFont(EPKUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y,
                getSize().width, getText(),
                ProcessUtils.Orientation.CENTER);

    }

    @Override
    protected Shape getOutlineShape() {
        // Merge Shapes
        Area area = new Area(getEventShape());
        area.add(new Area(getFunctionShape()));
        return area.getBounds();
    }
    
    protected Shape getFunctionShape() {
        RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height,10,10);
        return outline;
    }

    private Shape getEventShape() {
        Path2D path = new Path2D.Double();

        final int OFFSET = 10;

        path.moveTo(getPos().x+getSize().width/3+OFFSET, getPos().y-getSize().height/2+OFFSET);

        path.lineTo(getPos().x+getSize().width/2+OFFSET, getPos().y+OFFSET);
        path.lineTo(getPos().x+getSize().width/3+OFFSET, getPos().y+getSize().height/2+OFFSET);
        path.lineTo(getPos().x-getSize().width/3+OFFSET, getPos().y+getSize().height/2+OFFSET);
        path.lineTo(getPos().x-getSize().width/2+OFFSET, getPos().y+OFFSET);
        path.lineTo(getPos().x-getSize().width/3+OFFSET, getPos().y-getSize().height/2+OFFSET);

        path.closePath();
        return path;
    }

}
