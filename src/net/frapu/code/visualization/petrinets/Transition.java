/**
 *
 * Process Editor - Petri Net Package
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
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import net.frapu.code.simulation.petrinets.Predecessor;
import net.frapu.code.visualization.*;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author frank
 */
public class Transition extends ProcessNode {

    private boolean enabledHighlight = false;
    private int remainingTime = 0;
    private int instanceCount = 0;

    public final static String PROP_COST = "cost";
    public final static String PROP_DURATION = "duration";
    public final static String PROP_PROBABILITY = "probability";
    public final static String PROP_RESET_WAITING_TIME = "reset_waiting";

    public Transition() {
        super();
        setSize(30, 30);
        initializeProperties();
    }

    public Transition(int x, int y, String label) {
        super();
        int w = 30;
        int h = 30;
        setSize(w, h);
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {
        setProperty(PROP_COST, "");
        setProperty(PROP_DURATION, "");
        setProperty(PROP_PROBABILITY, "100");
        
        setProperty(PROP_RESET_WAITING_TIME, FALSE);
        setPropertyEditor(PROP_RESET_WAITING_TIME, new BooleanPropertyEditor());
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    /**
     * Return if this Transition is enabled for a given process instance.
     * This method might be overwritten to provide its own execution semantics.
     * @return
     */
    public boolean isEnabled(List<Predecessor> preSet, int processInstance) {
        boolean enabled = true;
        int count = 0;
        // Check all incoming places
        for (Predecessor pre : preSet) {
            // Consider only Places
            if (pre.getNode() instanceof Place) {
                Place p = (Place) pre.getNode();
                Token tok = p.getToken(processInstance);
                count++;
                // Edge
                if (pre.getEdgeFromNode() instanceof Edge) {
                    if (tok == null) {
                        enabled = false;
                    }
                }
                // InhibitorEdge
                if (pre.getEdgeFromNode() instanceof InhibitorEdge) {
                    if (tok != null) {
                        enabled = false;
                    }
                }
            }
        }
        if (count == 0) {
            enabled = false;
        }
        // Set instances to 0 (hack @todo: fix)
        setInstanceCount(0);

        return enabled;
    }

    /**
     * Fires this Transition. This method might be overwritten to provide
     * its own execution semantics. It is called by the engine when
     * the transition is executed.
     * @param inTokens 
     *
     * @param preSet The set of places before this Transition.
     * @param postSet The set of places after this Transition.
     * @return true if fired, false if not possible or error.
     */
    public boolean fire(Set<Token> inTokens) {
        //@todo: Implement fire in Transition!
        return false;
    }

    /**
     * @param highlightEnabledTransition
     */
    public void setEnabledHighlight(boolean highlightEnabledTransition) {
        this.enabledHighlight = highlightEnabledTransition;
    }

    public boolean isEnabledHighlight() {
        return this.enabledHighlight;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public int getProbability() {
        int prob = 1;
        try {
            prob = Integer.parseInt(getProperty(PROP_PROBABILITY));
        } catch (Exception e) {}
        return prob;
    }

    public boolean isResetWaiting() {
        return getProperty(PROP_RESET_WAITING_TIME).equals("1");
    }

    protected void paintInternal(Graphics g) {
        drawTask(g);
    }

    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
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
     * Draws a Workflow net task.
     */
    protected void drawTask(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(PetriNetUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();
        
        g2.setPaint( this.getBackground() );        
//        g2.setPaint(Color.WHITE);
        if (isEnabledHighlight()) {
            g2.setPaint(Color.GREEN);
        }
        g2.fill(outline);

        // Render remaining time if > 0
        if (remainingTime>0) {
            int duration = 1;
            try {
                duration = Integer.parseInt(getProperty(PROP_DURATION));
                if (duration==0) duration = 1;
            } catch (NumberFormatException e) {};
            g2.setPaint(Color.GREEN);
            int barHeight = (int)((double)getSize().height*((double)remainingTime/(double)duration));
            Rectangle2D bar = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                (getPos().y - (getSize().height / 2))+(getSize().height-barHeight),
                getSize().width, barHeight);
            g2.fill(bar);
        }

        // Render instance count if > 0
        if (instanceCount>0) {
            g2.setPaint(Color.GRAY);
            g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
            g2.drawString(""+instanceCount,
                    getPos().x-((getSize().width/2)-3),
                    getPos().y-((getSize().height/2)-12));
        }

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw Reset Waiting Timer if applicable
        if (getProperty(PROP_RESET_WAITING_TIME).equals("1")) drawTimer(g2);

        // Set font
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));

        // Draw text
        if (getText() != null) {
            PetriNetUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                    70, getText(), PetriNetUtils.Orientation.TOP);
        }

        String metaData = "";

        // Show cost (if applicable)
        if (getProperty(PROP_COST).length() > 0) {
            metaData += getProperty(PROP_COST) + "\u20ac";
        }

        // Show duration (if applicable)
        if (getProperty(PROP_DURATION).length() > 0) {
            metaData += " " + getProperty(PROP_DURATION) + "m";
        }

        PetriNetUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2) + 12,
                100, metaData, PetriNetUtils.Orientation.TOP);

        // Show probability (if applicable)
        if (getProperty(PROP_PROBABILITY).length() > 0) {
            // Don't show 100% probability!
            if (!getProperty(PROP_PROBABILITY).equals("100"))
            PetriNetUtils.drawText(g2, getPos().x, getPos().y, getSize().width,
                    getProperty(PROP_PROBABILITY)+"%", PetriNetUtils.Orientation.CENTER);
        }
    }

    protected void drawTimer(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(ProcessUtils.defaultStroke);
        Rectangle2D.Double circle = new Rectangle2D.Double(getPos().x + (getSize().width/2)-12,
                getPos().y - (getSize().height/2)-4, 16,16);
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

    private static int round(double d) {
        if (d - (int) d > 0.5) {
            return ((int) d) + 1;
        }
        return (int) d;
    }

    public String toString() {
        return "Transition";
    }
}
