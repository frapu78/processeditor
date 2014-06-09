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
import java.awt.geom.Ellipse2D;

/**
 *
 * Represents a place of a Petri net that matches all process instances.
 * 
 * @author frank
 */
public class ResourcePlace extends Place {

    /** The number of tokens in this resource place */
    public final static String PROP_TOKEN_COUNT = "token_count";
    /** A cache integer for the token count (uninitialized == -1) */
    private int countCache = -1;

    public ResourcePlace() {
        super();
        customInit();
    }

    public ResourcePlace(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        customInit();
    }

    protected void clearCaches() {
        countCache = -1;
    }

    private void customInit() {
        setSize(30, 30);
        // Set tokens
        this.setProperty(PROP_TOKEN_COUNT, "0");
    }

    public void setSize(int diameter, int ignored) {
        super.setSize(diameter, diameter);
    }

    /**
     * Returns the number of all Tokens in this place.
     * @return
     */
    public int getTokenCount() {
        // Check cache
        if (countCache != -1) {
            return countCache;
        }
        // Parse integer value
        int count = Integer.parseInt(this.getProperty(PROP_TOKEN_COUNT));
        // Update cache
        countCache = count;
        // Return result
        return count;
    }

    /**
     * Never goes beyond zero.
     */
    public void decreaseTokenCount() {
        if (getTokenCount() > 0) {
            this.setProperty(PROP_TOKEN_COUNT, "" + (getTokenCount() - 1));
        }
        clearCaches();
    }

    public void increaseTokenCount() {
        this.setProperty(PROP_TOKEN_COUNT, "" + (getTokenCount() + 1));
        clearCaches();

    }

    public void addToken(Token t) {
        increaseTokenCount();
    }

    public void removeToken(Token t) {
        decreaseTokenCount();
    }

    public void removeAllTokens() {
        this.setProperty(PROP_TOKEN_COUNT, "0");
        clearCaches();
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

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        clearCaches();
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
     * Draws a Workflow net place. 
     */
    private void drawPlace(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(PetriNetUtils.defaultStroke);

        Shape outline = getOutlineShape();

        g2.setPaint(Color.LIGHT_GRAY);
        g2.fill(outline);

        g2.setPaint(Color.GRAY);
        g2.draw(outline);

        // Get Pos
        int x = getPos().x;
        int y = getPos().y;

        // Set text
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));
        // Draw token count
        if (getTokenCount() == 1) {
            g2.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
        }
        if (getTokenCount() == 2) {
            g2.fill(new Ellipse2D.Double(x - 10, y - 10, 10, 10));
            g2.fill(new Ellipse2D.Double(x, y, 10, 10));
        }
        if (getTokenCount() == 3) {
            g2.fill(new Ellipse2D.Double(x - 5, y - 10, 10, 10));
            g2.fill(new Ellipse2D.Double(x - 10, y, 10, 10));
            g2.fill(new Ellipse2D.Double(x + 2, y, 10, 10));
        }
        if (getTokenCount() > 3) {
            PetriNetUtils.drawText(g2, x, y - 2, 20, "" + getTokenCount(),
                    PetriNetUtils.Orientation.CENTER);
        }

        // Draw text
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        if (getText() != null) {
            PetriNetUtils.drawText(g2, x, y + (getSize().width / 2), 50,
                    getText(), PetriNetUtils.Orientation.TOP);
        }
    }

    public String toString() {
        return "Place (" + getTokenCount() + " tokens)";
    }
}
