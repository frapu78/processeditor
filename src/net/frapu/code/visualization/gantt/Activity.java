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
public class Activity extends Cluster {

    public static final int ACTIVITY_HEIGHT = 20;
    public final static String PROP_POSITION = "position";
    public final static String PROP_PERFORMER = "performer";
    GanttChart parent = null;

    public Activity() {
        setSize(200, 50);
        setText("New Activity");
        setProperty(PROP_PERFORMER, "Jane");
        setProperty(PROP_POSITION, "0");
    }

    public GanttChart getParent() {
        return parent;
    }

    public void setParent(GanttChart parent) {
        this.parent = parent;
    }

    @Override
    public void addProcessNode(ProcessNode n) {
        super.addProcessNode(n);
        // Only accept TimeBar
        if (n instanceof TimeBar) {
            ((TimeBar) n).setParent(this);
        }

    }

    @Override
    public void removeProcessNode(ProcessNode n) {
        super.removeProcessNode(n);
        if (n instanceof TimeBar) {
            ((TimeBar) n).setParent(null);
        }
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Shape outline = getOutlineShape();

        g2.setStroke(ProcessUtils.defaultStroke);
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));

        Point p = getTopLeftPos();
        Dimension d = getSize();


        if (parent == null) {
            g2.setColor(getBackground());
            g2.fill(outline);
            g2.setColor(Color.BLACK);
            g2.draw(outline);
            ProcessUtils.drawText(g2, getPos().x, getPos().y + g2.getFont().getBaselineFor('a'), d.width, "Please move Activity into Gantt Chart", ProcessUtils.Orientation.CENTER);
        } else {
            // Render content of Activity
            g2.setColor(Color.BLACK);
            ProcessUtils.drawText(g2, p.x + 3, p.y + g2.getFont().getBaselineFor('a'), GanttChart.ACTIVITYWIDTH - 6, getText(), ProcessUtils.Orientation.LEFT, false, false);
            ProcessUtils.drawText(g2, p.x + 3 + GanttChart.ACTIVITYWIDTH, p.y + g2.getFont().getBaselineFor('a'), GanttChart.PERFORMERWIDTH - 6, getProperty(PROP_PERFORMER), ProcessUtils.Orientation.LEFT, false, false);
        }

    }

    /**
     * Returns the ordering position inside a Gantt Chart.
     * @return
     */
    public int getPosition() {
        try {
            return Integer.parseInt(getProperty(PROP_POSITION));
        } catch (Exception ex) {
        }
        return 0;
    }

    @Override
    public String getProperty(String key) {
        if (parent!=null) {
            if (key.equalsIgnoreCase(PROP_XPOS)) {
                return ""+parent.getPos().x;
            }
            if (key.equalsIgnoreCase(PROP_YPOS)) {
                return ""+(parent.getPos().y - parent.getSize().height / 2 + getPosition() * ACTIVITY_HEIGHT + 40 + ACTIVITY_HEIGHT - 5);
            }
            if (key.equalsIgnoreCase(PROP_WIDTH)) {
                return ""+(parent.getSize().width - 2);
            }
            if (key.equalsIgnoreCase(PROP_HEIGHT)) {
                return ""+ACTIVITY_HEIGHT;
            }
        }
        return super.getProperty(key);
    }


//    @Override
//    public Point getPos() {
//        if (parent != null) {
//            return new Point(parent.getPos().x, parent.getPos().y - parent.getSize().height / 2 + getPosition() * ACTIVITY_HEIGHT + 40 + ACTIVITY_HEIGHT - 5);
//        }
//        return super.getPos();
//    }
//
//    @Override
//    public Dimension getSize() {
//        if (parent != null) {
//            return new Dimension(parent.getSize().width - 2, ACTIVITY_HEIGHT);
//        }
//        return super.getSize();
//    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);

    }
}
