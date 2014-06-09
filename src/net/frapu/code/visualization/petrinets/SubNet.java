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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author frank
 */
public class SubNet extends Transition {

    /** Property for the file name of the subnet */
    public final static String PROP_SUBNET = "subNet";
    /** An internal representation of the subnet */
   // private PetriNetModel subModel = null;
    /** Indicates that the subnet could not be found */
    private boolean subNetOk = true;

    public SubNet() {
        super();
        setSize(30, 30);
        initializeProperties();
    }

    public SubNet(int x, int y, String label) {
        super();
        int w = 30;
        int h = 30;
        setSize(w, h);
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {
        super.initializeProperties();
        setProperty(PROP_SUBNET, "");
    }

    /**
     * Returns the internal Petri net model for this subnet<br>
     * If the subnet has not yet been loaded, the file from the property
     * PROP_SUBNET is read.
     *
     * @todo: Add cache for ProcessModel
     * @todo: Validate if subnet has exactly one incoming and one outgoing edge
     */
    public PetriNetModel getSubNet() {
        subNetOk = false;
        String fileName = getProperty(PROP_SUBNET);
        if (fileName == null) 
            return null;
        if (fileName.length() == 0) 
            return null;
        // Create initial ProcessModel
        ProcessModel model = null;

        // Try to open the model
        try {
            FileInputStream in = new FileInputStream(fileName);
            model = ProcessUtils.parseProcessModelSerialization(in);
        } catch (Exception ex) {
            return null;
        }

        // Return model if type is PetriNetModel
        if (model instanceof PetriNetModel) {
            subNetOk = true;
            return (PetriNetModel) model;
        }

        // Return null otherwise
        return null;
    }

    /**
     * Sets the internal Petri net model for this subnet
     * @param m - not used so far
     */
    public void setSubNet(PetriNetModel m) {
        //subModel = m;
    }

    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    /**
     * Draws a subnet task.
     */
    @Override
    protected void drawTask(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(PetriNetUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2)+5, getSize().width-5, getSize().height-5);

        // Draw shadow
        Rectangle2D shadow = new Rectangle2D.Float(getPos().x - (getSize().width / 2) + 5,
                getPos().y - (getSize().height / 2), getSize().width-5, getSize().height-5);
        if (subNetOk) {            
            g2.setPaint(Color.WHITE);
        } else {
            g2.setPaint(Color.RED);
        }
        g2.fill(shadow);        
        g2.setPaint(Color.BLACK);
        g2.draw(shadow);

        g2.setPaint(Color.WHITE);
        if (isEnabledHighlight()) {
            g2.setPaint(Color.GREEN);
        }
        g2.fill(outline);

        // Render remaining time if > 0
        if (getRemainingTime() > 0) {
            int duration = 1;
            try {
                duration = Integer.parseInt(getProperty(PROP_DURATION));
                if (duration == 0) {
                    duration = 1;
                }
            } catch (NumberFormatException e) {
            }
            ;
            g2.setPaint(Color.GREEN);
            int barHeight = (int) ((double) getSize().height * ((double) getRemainingTime() / (double) duration));
            Rectangle2D bar = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                    (getPos().y - (getSize().height / 2)) + (getSize().height - barHeight),
                    getSize().width, barHeight);
            g2.fill(bar);
        }

        // Render instance count if > 0
        if (getInstanceCount() > 0) {
            g2.setPaint(Color.GRAY);
            g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
            g2.drawString("" + getInstanceCount(),
                    getPos().x - ((getSize().width / 2) - 3),
                    getPos().y - ((getSize().height / 2) - 12));
        }

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

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
            metaData += getProperty(PROP_COST) + "€";
        }

        // Show duration (if applicable)
        if (getProperty(PROP_DURATION).length() > 0) {
            metaData += " " + getProperty(PROP_DURATION) + "s";
        }

        PetriNetUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2) + 12,
                70, metaData, PetriNetUtils.Orientation.TOP);

        // Show probability (if applicable)
        if (getProperty(PROP_PROBABILITY).length() > 0) {
            // Don't show 100% probability!
            if (!getProperty(PROP_PROBABILITY).equals("100")) {
                PetriNetUtils.drawText(g2, getPos().x, getPos().y, getSize().width,
                        getProperty(PROP_PROBABILITY) + "%", PetriNetUtils.Orientation.CENTER);
            }
        }
    }

    public String toString() {
        return "Subnet";
    }
}
