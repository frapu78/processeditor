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
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import net.frapu.code.simulation.petrinets.PetriNetEngine;

/**
 *
 * Represents a place of a Petri net that matches all process instances and
 * provides "working time", i.e. times, where the Tokens are ready to work
 * 
 * @author frank
 */
public class LaborPlace extends Place implements TimeConsumer {

    /** The maximum number of tokens in this labor place */
    public final static String PROP_MAX_TOKEN_COUNT = "max_token_count";
    /** The start time of the tokens in this place Format: HHMM **/
    public final static String PROP_START_TIME = "start_time";
    /** The end time of the tokens in this place Format: HHMM **/
    public final static String PROP_END_TIME = "end_time";
    /** The changeover time in ticks */
    public final static String PROP_CHANGEOVER_TIME = "changeover_time";
    /** The number of tokens currently out of this place */
    private int outsideTokenCount = 0;
    /** The current ticks for this place */
    private int tickCount = 0;
    /** Tick counts used for calculating the load (working time ticks) */
    private int loadTickCount = 0;
    /** The average load level of this place */
    private int loadLevel = 0;
    /** The current load level of this place */
    private int currentLoad = 0;
    /** The sum of all loads */
    private int loadSum = 0;

    public LaborPlace() {
        super();
        customInit();
    }

    public LaborPlace(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        customInit();
    }

    private void customInit() {
        setSize(50, 50);
        this.setProperty(PROP_MAX_TOKEN_COUNT, "1");
        this.setProperty(PROP_START_TIME, "0800");
        this.setProperty(PROP_END_TIME, "1600");
        this.setProperty(PROP_CHANGEOVER_TIME, "5");
    }

    public void setSize(int diameter, int ignored) {
        super.setSize(diameter, diameter);
    }

    /**
     * Increases the tick count for this place. Used to calculate whether
     * the Tokens are working or not.
     */
    private void increaseTickCount() {
        tickCount++;

        // Calculate new load level if worktime
        if (isWorkingTime()) {
            try {
                int maxTokenCount = Integer.parseInt(getProperty(PROP_MAX_TOKEN_COUNT));
                int currentTokenCount = this.getTokenCount();
                loadTickCount++;
                currentLoad = (int)((1.0-(((double)currentTokenCount)/((double)maxTokenCount)))*100.0);
                loadSum += currentLoad;
                loadLevel = loadSum/loadTickCount;
            } catch (Exception e) {}
        }

    }

    /**
     * Returns the number of all Tokens in this place under consideration
     * of the working hours.
     * @return
     */
    public int getTokenCount() {
        // Return zero if outside of work time
        if (!isWorkingTime()) {
            return 0;
        }

        return getRealTokenCount();
    }

    private boolean isWorkingTime() {
        // Get working hours
        int startTicks = PetriNetEngine.convertTimeToTicks(this.getProperty(PROP_START_TIME));
        int endTicks = PetriNetEngine.convertTimeToTicks(this.getProperty(PROP_END_TIME));
        int ticksOfDay = PetriNetEngine.getTicksOfDay(tickCount);
        // Return zero if outside of working hours
        return !((ticksOfDay < startTicks) || (ticksOfDay > endTicks));
    }

    /**
     * Returns the real numbers of tokens inside this place, without considering
     * working hours.
     * @return
     */
    private int getRealTokenCount() {
        // Parse integer value
        int count = Integer.parseInt(this.getProperty(PROP_MAX_TOKEN_COUNT));
        // Return result
        return count - outsideTokenCount;
    }

    /**
     * Never goes beyond zero.
     */
    public void decreaseTokenCount() {
        if (getTokenCount() > 0) {
            outsideTokenCount++;
        }
    }

    public void increaseTokenCount() {
        outsideTokenCount--;
    }

    @Override
    public void addToken(Token t) {
        increaseTokenCount();
    }

    public void removeToken(Token t) {
        decreaseTokenCount();
    }

    public void removeAllTokens() {
        // Returns all tokens back into the place
        outsideTokenCount = 0;
        // Reset loadTickCount
        loadLevel = 0;
        loadTickCount = 0;
        currentLoad = 0;
        loadSum = 0;
    }

    public Token getToken(int processInstance) {
        if (getTokenCount() > 0) {
            return new Token(processInstance);
        }
        return null;
    }

    public Token removeToken(int processInstance) {
        if (getTokenCount() > 0) {
            decreaseTokenCount();
            return new Token(processInstance);
        }
        return null;
    }

    /**
     * Returns the average load level of this LaborPlace.
     * @return integer from 0-100
     */
    public int getAverageLoad() {
        return loadLevel;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    protected void paintInternal(Graphics g) {
        drawPlace(g);
    }

    protected Shape getOutlineShape() {
        Ellipse2D outline = new Ellipse2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().width / 2), getSize().width, getSize().width);
        return outline;
    }

    /**
     * Draws a labor place.
     */
    private void drawPlace(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(PetriNetUtils.defaultStroke);

        Shape outline = getOutlineShape();

        g2.setPaint(Color.WHITE);
        g2.fill(outline);

        // Draw average load level
        Shape loadShape = new Arc2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().width / 2), getSize().width, getSize().width,
                90.0, -(3.6) * loadLevel, Arc2D.PIE);

        // Set load level coloring
        if (loadLevel < 75) {
            g2.setPaint(Color.GREEN);
        } else if (loadLevel < 90) {
            g2.setPaint(Color.ORANGE);
        } else {
            g2.setPaint(Color.RED);
        }
        g2.fill(loadShape);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Set inner "outline"
        outline = new Ellipse2D.Double(getPos().x - (getSize().width / 2) + 5,
                getPos().y - (getSize().width / 2) + 5, getSize().width - 10, getSize().width - 10);

        g2.setPaint(Color.WHITE);
        if (!isWorkingTime()) {
            g2.setPaint(new Color(64, 64, 128));
        }
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Get Pos
        int x = getPos().x;
        int y = getPos().y;

        // Set text
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));
        // Draw token count
        if (getRealTokenCount() == 1) {
            g2.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
        }
        if (getRealTokenCount() == 2) {
            g2.fill(new Ellipse2D.Double(x - 10, y - 10, 10, 10));
            g2.fill(new Ellipse2D.Double(x, y, 10, 10));
        }
        if (getRealTokenCount() == 3) {
            g2.fill(new Ellipse2D.Double(x - 5, y - 10, 10, 10));
            g2.fill(new Ellipse2D.Double(x - 10, y, 10, 10));
            g2.fill(new Ellipse2D.Double(x + 2, y, 10, 10));
        }
        if (getRealTokenCount() > 3) {
            PetriNetUtils.drawText(g2, x, y - 2, this.getSize().width - 5, "" + getRealTokenCount(),
                    PetriNetUtils.Orientation.CENTER);
        }

        // Draw text
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        if (getText() != null) {
            PetriNetUtils.drawText(g2, x, y + (getSize().width / 2), getSize().width+20,
                    getText(), PetriNetUtils.Orientation.TOP);
        }

        // Draw load level as number
        PetriNetUtils.drawText(g2, x, y+(getSize().width/2)+12, 100, ""+getCurrentLoad()+"% ("+getAverageLoad()+"%)", PetriNetUtils.Orientation.TOP);
    }

    public String toString() {
        return "LaborPlace (" + getTokenCount() + " tokens)";
    }

    @Override
    public void addTick() {
        increaseTickCount();
    }

    @Override
    public void addTicks(int ticks) {
        for (int i=0; i<ticks; i++) increaseTickCount();
    }

    @Override
    public void resetTicks() {
        tickCount=0;
    }
}
