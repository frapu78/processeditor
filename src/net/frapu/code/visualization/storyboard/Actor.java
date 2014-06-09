/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * This class provides an Actor for a Story.
 *
 * @author fpu
 */
public class Actor extends ProcessNode {

    public Actor() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(30, 80);
        setText("Actor");
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Prepare
        g2.setStroke(StoryboardUtils.defaultStroke);
        g2.setFont(StoryboardUtils.defaultFont);
        int x1 = getPos().x - getSize().width / 2;
        int x2 = getPos().x + getSize().width / 2;
        int y1 = getPos().y - getSize().height / 2;
        int y2 = getPos().y + getSize().height / 2;

        // Fill background
        g2.setPaint(getBackground());
        g2.fillOval(x1, y1, x2 - x1, (int) ((y2 - y1) * 0.33));

        // Draw Actor here...
        g2.setPaint(Color.BLACK);
        g2.drawLine(x1, getPos().y, x2, getPos().y);
        g2.drawLine(getPos().x, (int) (y1 + ((y2 - y1) * 0.33)), getPos().x, (int) (y1 + ((y2 - y1) * 0.66)));
        g2.drawLine(getPos().x, (int) (y1 + ((y2 - y1) * 0.66)), x1, y2);
        g2.drawLine(getPos().x, (int) (y1 + ((y2 - y1) * 0.66)), x2, y2);
        g2.drawOval(x1, y1, x2 - x1, (int) ((y2 - y1) * 0.33));

        // Draw title
        StoryboardUtils.drawText(g2, getPos().x, y2, (x2 - x1) * 4, getText(), Orientation.TOP);
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        int x1 = getPos().x - getSize().width / 2;
        int x2 = getPos().x + getSize().width / 2;
        //int y1 = getPos().y - getSize().height / 2;
        int y2 = getPos().y + getSize().height / 2;
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(StoryboardUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
            textBounds = StoryboardUtils.drawText(g2, getPos().x, y2 + g2.getFont().getSize(), (x2 - x1) * 4, getText(), Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(getPos().x - getSize().width / 2, getPos().y - getSize().height / 2,
                getSize().width, getSize().height);
    }
}
