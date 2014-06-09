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

/**
 *
 * @author frank
 */
public class DataStore extends Artifact {
    
    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 60;
	
    public DataStore() {
        super();
        setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    public DataStore(int xPos, int yPos, String text) {
        super();
        setPos(xPos, yPos);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setText(text);
    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x-(getSize().width/2),
                getPos().y-(getSize().height/2), getSize().width, getSize().height);
        return outline;
    }

    @Override
    protected void paintInternal(Graphics g) {
        drawDataStore(g);
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
           textBounds = BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, getText(), BPMNUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }
         
    private void drawDataStore(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setStroke(BPMNUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x1 = getPos().x-getSize().width/2;
        int x2 = getPos().x+getSize().width/2;
        int y1 = getPos().y-getSize().height/2;
        int y2 = getPos().y+getSize().height/2;

        // Draw bottom ellipse
        g2.setPaint(getBackground());
        g2.fillOval(x1, y2-getSize().height/8, getSize().width, getSize().height/8);
        g2.setPaint(Color.GRAY);
        g2.drawArc(x1, y2-getSize().height/8, getSize().width, getSize().height/8, 0, -180);

        // Draw middle rect
        g2.setPaint(getBackground());
        g2.fillRect(x1, y1 + (getSize().height/16), getSize().width, (int)(getSize().height*(7.0/8.0)));
        g2.setPaint(Color.GRAY);
        g2.drawLine(x1, (int)(y1+getSize().height/8*0.5), x1, (int)(y2-getSize().height/8*0.5));
        g2.drawLine(x2, (int)(y1+getSize().height/8*0.5), x2, (int)(y2-getSize().height/8*0.5));
        // Draw top ellipses
        g2.setPaint(Color.GRAY);
        g2.drawArc(x1, y1+(getSize().height/8), getSize().width, getSize().height/8,0,-180);
        g2.drawArc(x1, y1+(getSize().height/16), getSize().width, getSize().height/8,0,-180);
        g2.setPaint(getBackground());
        g2.fillOval(x1, y1, getSize().width, getSize().height/8);
        g2.setPaint(Color.GRAY);
        g2.drawOval(x1, y1, getSize().width, getSize().height/8);

        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.GRAY);
        //BPMNUtils.drawText(g2, getPos().x, getPos().y+5 , getSize().width-5, "Data\nStore", BPMNUtils.Orientation.CENTER);
        BPMNUtils.drawText(g2, getPos().x, getPos().y+(getSize().height/2), getSize().width+100, getText(), BPMNUtils.Orientation.TOP);
    }
    
    public String toString() {
        return "BPMN data store";
    }

}
