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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 *
 * Implements a BPMN-Message.
 *
 * @author fpu
 */
public class Message extends FlowObject {
	
	public static final String PROP_INITIATE = "initiate";

	public static final String INITIATE_TRUE = "1";
	public static final String INITIATE_FALSE = "0";
	
    public Message() {
        super();
        initializeProperties();
    }
    
    public Message(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {
        int w=30; int h=20;
        setSize(w, h);
        setProperty(PROP_INITIATE, INITIATE_TRUE);
        setPropertyEditor(PROP_INITIATE, new BooleanPropertyEditor());
    }
        
    @Override
    public void paintInternal(Graphics g) {
        drawMessage(g);
    }
    
    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x-(getSize().width/2),
                getPos().y-(getSize().height/2), getSize().width, getSize().height);
        return outline;
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
           textBounds =         BPMNUtils.drawText(g2, getPos().x, getPos().y+getSize().height/2+12,
                   getSize().width*2, getText(), BPMNUtils.Orientation.CENTER);

        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }
     
    private void drawMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        if (getProperty(PROP_INITIATE).equals(INITIATE_FALSE)) {
            g2.setPaint(Color.LIGHT_GRAY);
        } else {
            g2.setPaint(Color.WHITE);
        }
        g2.fill(outline);
        
        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.drawLine(getPos().x-getSize().width/2, getPos().y-getSize().height/2,
                getPos().x, getPos().y);
        g2.drawLine(getPos().x+getSize().width/2, getPos().y-getSize().height/2,
                getPos().x, getPos().y);

        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        BPMNUtils.drawText(g2, getPos().x, getPos().y+getSize().height/2+12, getSize().width*2, getText(), BPMNUtils.Orientation.CENTER);
    }
    
}
