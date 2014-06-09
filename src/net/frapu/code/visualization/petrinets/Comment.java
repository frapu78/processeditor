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
import net.frapu.code.visualization.*;

/**
 *
 * @author frank
 */
public class Comment extends ProcessNode {

    /** The font size */
    public final static String PROP_FONTSIZE = "font_size";
    /** The font style (see java.awt.Font) for values */
    public final static String PROP_FONTSTYLE = "font_style";
    /** The background color (see java.awt.Color) for values */
    public final static String PROP_BACKGROUND = "color_background";

    //private boolean enabled = false;


    public Comment() {
        super();
        initializeProperties();
    }
    
    public Comment(int x, int y, String label) {
        super();
        initializeProperties();
        setPos(x,y);
        setText(label);
    }

    private void initializeProperties() {
        setSize(100,50);
        setProperty(PROP_FONTSIZE, "12");
        setProperty(PROP_FONTSTYLE, ""+Font.PLAIN);
        setProperty(PROP_BACKGROUND, ""+new Color(228,239,14).getRGB());
    }
    
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }
    
    protected void paintInternal(Graphics g) {
        drawComment(g);
    }
    
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x-(getSize().width/2),
                getPos().y-(getSize().height/2), getSize().width, getSize().height);
        return outline;
    }
    
    /**
     * Draws a comment.
     */
    protected void drawComment(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setStroke(PetriNetUtils.gatterStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        Color backgroundColor = Color.YELLOW;
        try {
            backgroundColor =
                new Color(Integer.parseInt(this.getProperty(PROP_BACKGROUND)));
            } catch (NumberFormatException e) {};

        g2.setPaint(backgroundColor);
        g2.fill(outline);
        
        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Set font size
        int fontSize = 12;
        try {
            fontSize = Integer.parseInt(this.getProperty(PROP_FONTSIZE));
        } catch (NumberFormatException e) {};

        int fontStyle = Font.PLAIN;
        try {
            fontStyle = Integer.parseInt(this.getProperty(PROP_FONTSTYLE));
        } catch (NumberFormatException e) {};
        
        // Set font
        g2.setFont(new Font("Arial Narrow", fontStyle, fontSize));

        // Draw text
        if (getText() != null) {
            PetriNetUtils.drawFitText(g2, getPos().x, getPos().y-getSize().height/2,
                    getSize().width-5, getSize().height-5, getText());
        }

       // String metaData = "";

    }
    
    public String toString() {
        return "Comment";
    }
    
}
