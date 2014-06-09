/**
 *
 * Process Editor - Lifecycle Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.lifecycle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;


/**
 *
 * @author frank
 */
public class DataObject extends ProcessNode {


    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 60;
    private static final int CURVESIZE = 10;
    private static final double EDGEVALUE = 0.4;

    /** Property to hold the state of the DataObject */
    public final static String PROP_STATE = "state";

    public DataObject() {
        super();
        initializeProperties();
    }

    public DataObject(int xPos, int yPos, String text) {
        super();
        setPos(xPos, yPos);
        setText(text);
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        setProperty(PROP_STATE, "");
    }

    public String getState() {
        return getProperty(PROP_STATE);
    }

    public void setState(String state) {
        setProperty(PROP_STATE, state);
    }

    @Override
    protected void paintInternal(Graphics g) {
        drawDataObject(g);
    }

    @Override
    protected Shape getOutlineShape() {
        Point p = getTopLeftPos();
        Dimension d = getSize();

        Path2D outline = new Path2D.Double();
        outline.moveTo(p.x+d.width*(1.0-EDGEVALUE), p.y);
        outline.lineTo(p.x+d.width, p.y+d.width*EDGEVALUE);

        outline.lineTo(p.x+d.width, p.y+d.height-CURVESIZE);
        outline.curveTo(p.x+d.width, p.y+d.height,
                p.x+d.width, p.y+d.height,
                p.x+d.width-CURVESIZE, p.y+d.height);

        outline.lineTo(p.x+CURVESIZE, p.y+d.height);
        outline.curveTo(p.x, p.y+d.height,
                p.x, p.y+d.height,
                p.x, p.y+d.height-CURVESIZE);

        outline.lineTo(p.x, p.y+CURVESIZE);
        outline.curveTo(p.x, p.y,
                p.x, p.y,
                p.x+CURVESIZE, p.y);

        outline.closePath();

        return outline;
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(ProcessUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           String text = getText();
           if (!getState().isEmpty()) text += "\\n["+getState()+"]";
           textBounds = ProcessUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, text, ProcessUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    private void drawDataObject(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(ProcessUtils.defaultStroke);

        Shape currentDataObject = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(currentDataObject);
        g2.setPaint(Color.black);
        g2.draw(currentDataObject);

        // Draw inner lines
        Point p = getTopLeftPos();
        Dimension d = getSize();
        g2.drawLine((int)(p.x+(1.0-EDGEVALUE)*d.width), p.y,
                (int)(p.x+(1.0-EDGEVALUE)*d.width), (int)(p.y+EDGEVALUE*d.width)-CURVESIZE);
        g2.drawLine((int)(p.x+(1.0-EDGEVALUE)*d.width+CURVESIZE), (int)(p.y+EDGEVALUE*d.width),
                p.x+d.width, (int)(p.y+EDGEVALUE*d.width)
                );
        g2.drawArc((int)(p.x+(1.0-EDGEVALUE)*d.width), (int)(p.y+EDGEVALUE*d.width-2*CURVESIZE),
                (int)(2*CURVESIZE), (int)(2*CURVESIZE),
                180,90);

        String text = getText();
        if (!getState().isEmpty()) {
            if (!text.isEmpty()) text += "\n";
            text +="["+getState()+"]";
        }
        
        g2.setFont(ProcessUtils.defaultFont);
        ProcessUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2), getSize().width + 100, text, ProcessUtils.Orientation.TOP);
    }


    @Override
    public String toString() {
        return "Data Object ("+getText()+")";
    }
}
