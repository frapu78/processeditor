/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * Provides a clock that shows the current time
 *
 * @author frank
 */
public class Clock extends ProcessNode implements TimeConsumer {

    /** The current tick count */
    private int tickCount = 0;
    /** Saves if the clock has already counted time */
    private boolean freshClock = true;

    public Clock() {
        super();
        this.setSize(150, 80);
    }

    public void setSize(int w, int h) {
        // Fixed size for the clock!
        super.setSize(120, 50);
    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(PetriNetUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        Color backgroundColor = Color.WHITE;

        g2.setPaint(backgroundColor);
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Set font size and style
        int fontSize = 18;
        int fontStyle = Font.BOLD;

        // Set font
        g2.setFont(new Font("Courier Narrow", fontStyle, fontSize));

        // Set text
        String hours = ""+((tickCount % 1440) / 60);
        if (hours.length()==1) hours = "0"+hours;
        String mins = ""+(tickCount % 60);
        if (mins.length()==1) mins = "0"+mins;

        String text = ""+ (tickCount / 1440) + "d " +
                hours + ":" +
                mins;

        // Initialize "fresh" clock with "- --:--"
        if (freshClock) text = "- --:--";

        // Draw text
        PetriNetUtils.drawText(g2, getPos().x, getPos().y+5,
                    getSize().width - 5, text, PetriNetUtils.Orientation.CENTER);

        // Draw title
        g2.setFont(new Font("Courier Narrow", fontStyle, 14));
        PetriNetUtils.drawText(g2, getPos().x, getPos().y-15,
                    getSize().width - 5, "Clock", PetriNetUtils.Orientation.CENTER);
    }

    @Override
    public void addTick() {
        freshClock = false;
        tickCount++;
    }

    @Override
    public void addTicks(int ticks) {
        freshClock = false;
        tickCount+=ticks;
    }

    @Override
    public void resetTicks() {
        tickCount=0;
    }
}
