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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class TimeBar extends ProcessNode {

    private Activity parent;
    public final static String PROP_DURATION = "duration";
    public final static String PROP_START = "start";

    public TimeBar() {
        setSize(50, 18);
        setBackground(new Color(200, 200, 255));
        setProperty(PROP_DURATION, "20");
        setProperty(PROP_START, "0");
    }

    public int getDuration() {
        try {
            int duration = Integer.parseInt(getProperty(PROP_DURATION));
            return duration;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 20;
    }

    public int getStart() {
        try {
            int start = Integer.parseInt(getProperty(PROP_START));
            if (parent != null) {
                if (parent.getParent() != null) {
                    if (parent.getParent().getDuration() < start) {
                        start = parent.getParent().getDuration();
                    }
                }
            }
            return start;
        } catch (Exception ex) {
        }
        return 0;
    }

    public Activity getParent() {
        return parent;
    }

    public void setParent(Activity parent) {
        this.parent = parent;
    }

    @Override
    public void setProperty(String key, String value) {
        if (parent != null) {
            if (parent.getParent() != null) {
                if (key.equalsIgnoreCase(PROP_XPOS)) {
                    // Set XPOS if inside GanttChart
                    int width = getSize().width;
                    int newX = 0;
                    try {
                        newX = Integer.parseInt(value);
                    } catch (Exception ex) {
                    }
                    if (((newX - width / 2) > parent.getParent().getTopLeftPos().x + GanttChart.ACTIVITYWIDTH + GanttChart.PERFORMERWIDTH )
                            && (newX + width / 2) < parent.getParent().getTopLeftPos().x + parent.getParent().getSize().width - 10) {
                        double timeWidth = parent.getSize().width - GanttChart.ACTIVITYWIDTH - GanttChart.PERFORMERWIDTH - 10;
                        double factor = parent.getParent().getDuration()/timeWidth;
                        newX = newX - getSize().width/2;
                        int newStart = newX - parent.getParent().getTopLeftPos().x - GanttChart.ACTIVITYWIDTH - GanttChart.PERFORMERWIDTH ;
                        System.out.println(newStart);
                        newStart = (int)(newStart*factor);
                        setProperty(PROP_START, ""+newStart);
                        return;
                    }
                }
            }
        }
        super.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        if (parent != null) {
            if (parent.getParent() != null) {
                if (key.equalsIgnoreCase(ProcessNode.PROP_XPOS)) {
                    double timeWidth = parent.getSize().width - GanttChart.ACTIVITYWIDTH - GanttChart.PERFORMERWIDTH - 10;
                    double factor = timeWidth / parent.getParent().getDuration();
                    int width = (int) (getDuration() * factor);
                    return "" + (int) (parent.getTopLeftPos().x + GanttChart.ACTIVITYWIDTH + GanttChart.PERFORMERWIDTH + 5 + getStart() * factor + width / 2);
                }
                if (key.equalsIgnoreCase(ProcessNode.PROP_YPOS)) {
                    return "" + parent.getPos().y;
                }
                if (key.equalsIgnoreCase(ProcessNode.PROP_WIDTH)) {
                    double timeWidth = parent.getSize().width - GanttChart.ACTIVITYWIDTH - GanttChart.PERFORMERWIDTH - 10;
                    double factor = timeWidth / parent.getParent().getDuration();
                    int width = (int) (getDuration() * factor);
                    return "" + width;
                }
                if (key.equalsIgnoreCase(ProcessNode.PROP_HEIGHT)) {
                    return "18";
                }
            }
        }
        return super.getProperty(key);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Shape outline = getOutlineShape();

        g2.setStroke(ProcessUtils.defaultStroke);

        Point p = getTopLeftPos();
        Dimension d = getSize();

        g2.setColor(getBackground());
        g2.fill(outline);

        if (parent == null) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.draw(outline);

    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new RoundRectangle2D.Double(p.x, p.y, d.width, d.height, 5, 5);
    }

    @Override
    public String toString() {
        return "TimeBar";
    }
}
