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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.*;

/**
 *
 * Represents a place of a workflow net.
 * 
 * @todo: Needs to be updated to support serialization of Tokens!!!
 * 
 * @author frank
 */
public class Place extends ProcessNode {

    /** The set of tokens contained in this Place */
    private List<Token> tokens = new LinkedList<Token>();

    /** A cache for getToken */
    private Map<Integer,Token> getTokenCache = new HashMap<Integer,Token>();

    private int warningLevel = 0;

    public Place() {
        super();
        customInit();
    }

    public Place(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        customInit();
    }

    private void customInit() {
        setSize(30, 30);
    // Set tokens
    }

    private void clearCaches() {
        getTokenCache.clear();
    }

    @Override
    public void setSize(int diameter, int ignored) {
        super.setSize(diameter, diameter);
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public void addToken(Token t) {
        tokens.add(t);
    }

    public void removeToken(Token t) {
        tokens.remove(t);
        // Clear caches
        clearCaches();
    }

    public void removeAllTokens() {
        tokens = new LinkedList<Token>();
        clearCaches();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public int getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(int warningLevel) {
        if (warningLevel<0 || warningLevel>255) return;
        this.warningLevel = warningLevel;
    }

    /**
     * Returns a random Token of a certain process instance
     * @param processInstance
     * @return
     */
    public Token getToken(int processInstance) {
        // Look up cache
        if (getTokenCache.containsKey(processInstance))
            return getTokenCache.get(processInstance);

        for (Token t : tokens) {
            if (t.getProcessInstance() == processInstance) {
                // Add to cache
                getTokenCache.put(processInstance, t);
                // Return
                return t;
            }
        }
        return null;
    }

    /**
     * Returns all Tokens for a certain process instance
     * @param processInstane
     * @return
     */
    public Set<Token> getTokens(int processInstance) {
        Set<Token> result = new HashSet<Token>();

        for (Token t : tokens) {
            if (t.getProcessInstance() == processInstance) {
                result.add(t);
            }
        }

        return result;
    }

    public Token removeToken(int processInstance) {
        Token t = getToken(processInstance);
        // Check if existing
        if (t == null) {
            return null;
        }
        tokens.remove(t);
        clearCaches();
        return t;
    }

    protected void paintInternal(Graphics g) {
        drawPlace(g);
    }

    protected Shape getOutlineShape() {
        Ellipse2D outline = new Ellipse2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().width / 2), getSize().width, getSize().width);
        return outline;
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           textBounds = PetriNetUtils.drawText(g2, getPos().x, getPos().y + (getSize().width / 2), 100,
                    getText(), PetriNetUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    /**
     * Draws a Petri net place.
     */
    private void drawPlace(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(PetriNetUtils.defaultStroke);

        Shape outline = getOutlineShape();

        g2.setPaint(new Color(255, 255-warningLevel, 255-warningLevel));
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
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
            PetriNetUtils.drawText(g2, x, y - 2, this.getSize().width - 10, "" + getTokenCount(),
                    PetriNetUtils.Orientation.CENTER);
        }

        // Draw text
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        if (getText() != null) {
            PetriNetUtils.drawText(g2, x, y + (getSize().width / 2), 100,
                    getText(), PetriNetUtils.Orientation.TOP);
        }
    }

    public String toString() {
        return "Place (" + getTokenCount() + " tokens)";
    }
}
