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

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;
import net.frapu.code.visualization.*;

/**
 * A Lane element. A Lane  stretches about the full horizontal size
 * of the panel. It can only be moved and resized vertically.
 *
 * @author frank
 */
public class Lane extends Cluster {

    /** The property for FLOATING {0=false, 1=true) **/
    public static final String PROP_FLOATING = "floating";

    public Lane() {
        super();
        initializeProperties();
    }

    public Lane(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(20, 150);
        setProperty(PROP_TEXT, "Lane");
        setProperty(PROP_FLOATING, FALSE);
        setPropertyEditor(PROP_FLOATING, new BooleanPropertyEditor());
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    @Override
    public void setPos(int x, int y) {
        if (getProperty(PROP_FLOATING).equals("0")) {
            x=getPos().x;
        }
        super.setPos(x,y);
    }
    
    /**
     * Returns whether a certain ProcessNode is graphically contained or not.
     */
    @Override
    public boolean isContainedGraphically(List<ProcessNode> nodes, ProcessNode node, boolean onTopRequired) {
        if (node == null) return false;
        // Check boundaries for floating Lane
        if (!getProperty(PROP_FLOATING).equals("0")) {
            return super.isContainedGraphically(nodes, node, true);
        }
        // Check boundaries for non-floating Lane
        if ((node.getPos().y >= (this.getPos().y - this.getSize().height / 2)) &&
                (node.getPos().y <= (this.getPos().y + this.getSize().height / 2))) {
            return true;
        }

        return false;
    }

    protected void paintInternal(Graphics g) {
        drawLane(g);
    }

    protected Shape getOutlineShape() {
        Rectangle2D outline;
        if (getProperty(PROP_FLOATING) != null && getProperty(PROP_FLOATING).equals("0")) {
            // Return outline for non-floating Lane
            outline = new Rectangle2D.Float(0.0f, getPos().y - (getSize().height / 2),
                    getSize().width, getSize().height);
        } else {
            // Return outline for floating Lane
            outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2), getPos().y - (getSize().height / 2),
                    getSize().width, getSize().height);
        }

        return outline;
    }

    /**
     * Draws a Workflow net task.
     */
    protected void drawLane(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(PetriNetUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        g2.setPaint(Color.WHITE);
        g2.fill(outline);

        g2.setPaint(Color.LIGHT_GRAY);

        int xLabelPos = 0;

        // Draw endless lines for non-floating Lanes
        if (getProperty(PROP_FLOATING).equals("0")) {
            g2.drawLine(getSize().width,
                    (int) (getPos().y - (getSize().height / 2)),
                    9999,
                    (int) (getPos().y - (getSize().height / 2)));
            g2.drawLine(getSize().width,
                    (int) (getPos().y + (getSize().height / 2)),
                    9999,
                    (int) (getPos().y + (getSize().height / 2)));
            xLabelPos = getSize().width / 2 + 5;
        } else {
            // Draw box
            g2.drawRect(getPos().x-getSize().width/2, getPos().y-getSize().height/2,
                    getSize().width, getSize().height);
            xLabelPos = (getPos().x-getSize().width/2)+15;
        }

        g2.draw(outline);

        // Set font
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));

        // Draw text
        if (getText() != null) {
            PetriNetUtils.drawTextVertical(g2, xLabelPos,
                    getPos().y,
                    getSize().height,
                    getText(),
                    PetriNetUtils.Orientation.TOP);
        }

    }

    @Override
    public String toString() {
        return "Lane " + getText();
    }
}
