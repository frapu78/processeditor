/**
 *
 * Process Editor - Gantt Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.gantt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class GanttChart extends Cluster {

    public final static String PROP_DURATION = "duration";

    public final static int ACTIVITYWIDTH = 100;
    public final static int PERFORMERWIDTH = 100;

    public GanttChart() {
        setSize(500,300);
        setText("My Project");
        setProperty(PROP_DURATION, "100");
    }
    
    public int getDuration() {
        try {
            return Integer.parseInt(getProperty(PROP_DURATION));
        } catch (Exception ex) {}
        return 100;
    }

    @Override
    public void addProcessNode(ProcessNode n) {
        super.addProcessNode(n);
        // Check if Activity is added
        if (n instanceof Activity) ((Activity)n).setParent(this);
    }

    @Override
    public void removeProcessNode(ProcessNode n) {
        super.removeProcessNode(n);
        // Check if Activity is removed
        if (n instanceof Activity) ((Activity)n).setParent(null);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        Shape outline = getOutlineShape();

        g2.setStroke(ProcessUtils.defaultStroke);
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 16));

        g2.setColor(getBackground());
        g2.fill(outline);

        Point p = getTopLeftPos();
        Dimension d = getSize();

        g2.setColor(Color.BLACK);

        ProcessUtils.drawText(g2, getPos().x, p.y+8+g2.getFont().getBaselineFor('a'), d.width, getText(), ProcessUtils.Orientation.CENTER);

        g2.drawLine(p.x, p.y+40, p.x+d.width, p.y+40);

        g2.drawLine(p.x+ACTIVITYWIDTH, p.y+24, p.x+ACTIVITYWIDTH, p.y+d.height);
        g2.drawLine(p.x+ACTIVITYWIDTH+PERFORMERWIDTH, p.y+24, p.x+ACTIVITYWIDTH+PERFORMERWIDTH, p.y+d.height);

        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));
        ProcessUtils.drawText(g2, p.x+ACTIVITYWIDTH/2, p.y+28+g2.getFont().getBaselineFor('a'), d.width, "Activity", ProcessUtils.Orientation.CENTER);
        ProcessUtils.drawText(g2, p.x+ACTIVITYWIDTH+PERFORMERWIDTH/2, p.y+28+g2.getFont().getBaselineFor('a'), d.width, "Performer", ProcessUtils.Orientation.CENTER);

        g2.draw(outline);

    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);
    }

}
