package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.bpmn.BPMNUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @version 14.10.2014
 * @author Stephan
 */
public class TimerEventListener extends EventListener {

    @Override
    protected void drawMarker(Graphics2D g2) {
        drawTimer(g2);
    }

    /**
     * Copied from package net.frapu.code.visualization.bpmn.Event.java
     * @param g2 the graphics object
     */
    protected void drawTimer(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        Rectangle2D.Double circle = new Rectangle2D.Double(getPos().x - (getSize().width / 3),
                getPos().y - round(getSize().height / 3), (getSize().width / 1.5), (getSize().height / 1.5));
        g2.setColor(Color.WHITE);
        g2.fillOval(round(circle.x), round(circle.y), round(circle.width), round(circle.height));
        g2.setColor(Color.BLACK);
        g2.drawOval(round(circle.x), round(circle.y), round(circle.width), round(circle.height));
        //drawing small lines which indicate time

        //left
        g2.drawLine(round(circle.x) + 1, round(circle.y + circle.height / 2), round(circle.x + 3), round(circle.y + circle.height / 2));
        //top
        g2.drawLine(round(circle.x + circle.width / 2), round(circle.y + circle.height) - 1, round(circle.x + circle.width / 2), round(circle.y + circle.height - 3));
        //right
        g2.drawLine(round(circle.x + circle.width / 2), round(circle.y), round(circle.x + circle.width / 2) + 1, round(circle.y + 3));
        //down
        g2.drawLine(round(circle.x + circle.width) - 1, round(circle.y + circle.height / 2), round(circle.x + circle.width - 3), round(circle.y + circle.height / 2));
        //drawing time pointers
        //long one
        g2.drawLine(round(circle.x + circle.width / 2),
                round(circle.y + circle.height / 2),
                round(circle.x + circle.width / 1.5), round(circle.y + circle.height * 0.05));
        //short one
        g2.drawLine(round(circle.x + circle.width / 2),
                round(circle.y + circle.height / 2),
                round(circle.x + circle.width / 1.4), round(circle.y + circle.height / 2));

        g2.setStroke(oldStroke);

    }

    /**
     * Copied from package net.frapu.code.visualization.bpmn.Event.java
     * @param d the number which schould be rounded
     * @return nearest int
     */
    private static int round(double d) {
        if (d - (int) d > 0.5) {
            return ((int) d) + 1;
        }
        return (int) d;
    }

    /**
     * Copied from package net.frapu.code.visualization.bpmn.Event.java
     * @param i the number which should be rounded
     * @return i
     */
    private static int round(int i) {
        //nothing to do, but speeds round(double) up if used on an int
        return i;
    }
}
