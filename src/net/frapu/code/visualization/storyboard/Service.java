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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * @author fpu
 */
public class Service extends ProcessNode {

    public Service() {
        super();
        setBackground(Color.WHITE);
        setSize(50,80);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        // Prepare
        g2.setStroke(StoryboardUtils.defaultStroke);
        g2.setFont(StoryboardUtils.defaultFont);
        int x1 = getPos().x - getSize().width / 2;
        int x2 = getPos().x + getSize().width / 2;
        int y1 = getPos().y - getSize().height / 2;
        int y2 = getPos().y + getSize().height / 2;

        Shape s = getOutlineShape();
        g2.setPaint(getBackground());
        g2.fill(s);

        g2.setStroke(StoryboardUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        g2.draw(s);

        g2.setPaint(Color.WHITE);
        drawService(g2, getPos().x, getPos().y);

        g2.setPaint(Color.BLACK);
        g2.setFont(StoryboardUtils.defaultFont);
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
        Point p = getTopLeftPos();
        Dimension d = getSize();
        return new Rectangle2D.Double(p.x, p.y, d.width, d.height);
    }

    private  void drawService(Graphics2D g2, int x, int y) {
        Path2D path = new Path2D.Double();

        path.moveTo(-0.7, 5.0);

        path.lineTo(0.7, 5.0);
        path.lineTo(0.7, 3.6);
        path.lineTo(2.0, 3.0);
        path.lineTo(3.0, 4.0);
        path.lineTo(4.0, 3.0);
        path.lineTo(3.0, 2.0);
        path.lineTo(3.6, 0.7);
        path.lineTo(5.0, 0.7);

        path.lineTo(5.0, -0.7);
        path.lineTo(3.6, -0.7);
        path.lineTo(3.0, -2.0);
        path.lineTo(4.0, -3.0);
        path.lineTo(3.0, -4.0);
        path.lineTo(2.0, -3.0);
        path.lineTo(0.7, -3.6);
        path.lineTo(0.7, -5.0);

        path.lineTo(-0.7, -5.0);
        path.lineTo(-0.7, -3.6);
        path.lineTo(-2.0, -3.0);
        path.lineTo(-3.0, -4.0);
        path.lineTo(-4.0, -3.0);
        path.lineTo(-3.0, -2.0);
        path.lineTo(-3.6, -0.7);
        path.lineTo(-5.0, -0.7);

        path.lineTo(-5.0, 0.7);
        path.lineTo(-3.6, 0.7);
        path.lineTo(-3.0, 2.0);
        path.lineTo(-4.0, 3.0);
        path.lineTo(-3.0, 4.0);
        path.lineTo(-2.0, 3.0);
        path.lineTo(-0.7, 3.6);

        path.closePath();

        // Tranform and scale to target
        AffineTransform at = new AffineTransform();

        final int STEREOTYPE_ICON_SIZE = getSize().width-10;

        // Draw outer shape
        at.scale(STEREOTYPE_ICON_SIZE * 0.07, STEREOTYPE_ICON_SIZE * 0.07);
        Shape shape = at.createTransformedShape(path);
        at.setToTranslation(x, y);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw inner shape
        at = new AffineTransform();
        at.scale(STEREOTYPE_ICON_SIZE * 0.035, STEREOTYPE_ICON_SIZE * 0.035);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x, y);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw second outer shape
        at = new AffineTransform();
        at.scale(STEREOTYPE_ICON_SIZE * 0.07, STEREOTYPE_ICON_SIZE * 0.07);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x + STEREOTYPE_ICON_SIZE / 6, y + STEREOTYPE_ICON_SIZE / 6);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw second inner shape
        at = new AffineTransform();
        at.scale(STEREOTYPE_ICON_SIZE * 0.035, STEREOTYPE_ICON_SIZE * 0.035);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x + STEREOTYPE_ICON_SIZE / 6, y + STEREOTYPE_ICON_SIZE / 6);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);
    }

}
