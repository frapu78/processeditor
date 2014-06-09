/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * @author frank
 */
public class Conversation extends FlowObject {
    
    private final static int xConversationPoints[] = {8,16,8,-8,-16,-8};
    private final static int yConversationPoints[] = {-15,0,15,15,0,-15};
    private final static Polygon conversationPoly = new Polygon(xConversationPoints, yConversationPoints, 6);

    /** Determines whether this Conversation is compound or not (1,0)*/
    public final static String PROP_COMPOUND = "compound";
    /** Determines whether this Conversation is compound or not (1,0)*/
    public final static String PROP_CALL = "call";
    
    public Conversation() {
        super();
        initializeProperties();
    }

    public Conversation(int xPos, int yPos, String text) {
        super();
        setPos(xPos, yPos);
        setText(text);
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(45, 40);
        setProperty(PROP_COMPOUND, FALSE);
        setProperty(PROP_CALL, FALSE);
        setPropertyEditor(PROP_COMPOUND, new BooleanPropertyEditor());
    }

    @Override
    public void setSize(int w, int h) {
        // Always width = height
        super.setSize(w, w);
    }
    
    protected void paintInternal(Graphics g) {
        drawConversation(g);
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(BPMNUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           textBounds = BPMNUtils.drawText(g2, getPos().x, getPos().y+(getSize().height/2)+5,
                   getSize().width+80, getText(), BPMNUtils.Orientation.TOP);

        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }
      
    protected Shape getOutlineShape() {
        // Clone dataObject
        Polygon currentElement = new Polygon(
                conversationPoly.xpoints, conversationPoly.ypoints, conversationPoly.npoints);
        BPMNUtils.scalePolygon(currentElement, getSize().width, getSize().height);
        BPMNUtils.movePolygon(currentElement, getPos().x, getPos().y);
        return currentElement;
    }
    
    private void drawConversation(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape currentElement = getOutlineShape();
        g2.setStroke(BPMNUtils.defaultStroke);
        if (getProperty(PROP_CALL).equals(TRUE)) {
            g2.setStroke(BPMNUtils.boldStroke);
        }
        g2.setPaint(Color.white);
        g2.fill(currentElement);
        g2.setPaint(Color.black);
        g2.draw(currentElement);        

        if (getProperty(PROP_COMPOUND).equals(TRUE) || getProperty(PROP_CALL).equals(TRUE)) {
            // Draw Compound Marker
            Activity.drawSubProcessMarker(g2, getPos().x, getPos().y+getSize().height/2-Activity.MARKER_ICON_SIZE);
        }

        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.black);
        BPMNUtils.drawText(g2, getPos().x, getPos().y+(getSize().height/2)+5, getSize().width+80, getText(), BPMNUtils.Orientation.TOP);
    }
    
    public String toString() {
        return "BPMN conversation";
    }

}
