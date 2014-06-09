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

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.editors.ColorPropertyEditor;

/**
 *
 * @author fpu
 */
public abstract class Event extends QuadraticFlowObject {

    private Color eventTypeOutlineColor = Color.BLACK;
    private Color eventTypeFillColor = Color.WHITE;

    public Event() {
        super();
        initializeProperties();
    }

    @Override
    public void setProperty(String key, String value) {
// disabled in order to allow delete animations in Process merger
//        if (key.equals(PROP_HEIGHT)) value = "30";
//        if (key.equals(PROP_WIDTH)) value = "30";
        super.setProperty(key, value);
    }

    private void initializeProperties() {
        // Set default size
        setSize(30, 30);
        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
    }

    @Override
    public abstract void paintInternal(Graphics g);

    public static Polygon getMultiplePolygon(Point position, Dimension size) {
        int[] multx = new int[]{
            round(position.x),
            round(position.x + size.width / 3.2),
            round(position.x + size.width / 4.2),
            round(position.x - size.width / 4.2),
            round(position.x - size.width / 3.2),};
        int[] multy = new int[]{
            round(position.y - size.height / 2.8),
            round(position.y - size.height / 8),
            round(position.y + size.height / 3.5),
            round(position.y + size.height / 3.5),
            round(position.y - size.height / 8),};
        Polygon multi = new Polygon(multx, multy, multx.length);
        return multi;
    }

    /**
     * @param d
     * @return
     */
    private static int round(double d) {
        if (d - (int) d > 0.5) {
            return ((int) d) + 1;
        }
        return (int) d;
    }

    /**
     * @param d
     * @return
     */
    private static int round(int i) {
        //nothing to do, but speeds round(double) up if used on an int
        return i;
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
           textBounds = BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().width / 2),
                    getSize().width * 3, getText(), BPMNUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    protected Shape getOutlineShape() {
        Ellipse2D outline = new Ellipse2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().width / 2), getSize().width, getSize().width);
        return outline;
    }

    /**
     * Draws a basic BPMN event. The stroke etc. have to be set beforehand.
     * Additional graphics need to be drawn afterwards.
     */
    protected void drawEventBasicShape(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw text
        g2.setFont(BPMNUtils.defaultFont);
        if (getText() != null) {
            BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().width / 2),
                    getSize().width * 3, getText(), BPMNUtils.Orientation.TOP);
        }

    }

    /**
     * @param g2
     */
    protected void drawTerminate(Graphics2D g2) {
        g2.setColor(eventTypeFillColor);
        double scale = 3.5;
        g2.fillOval(round(getPos().x - getSize().width / scale), round(getPos().y - getSize().height / scale),
                round(2 * getSize().width / scale) + 2, round(2 * getSize().height / scale) + 2);
    }

    /**
     * @param g2
     */
    protected void drawCompensation(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        double scalew = 3.0;
        double scaleh = 4.5;
        double translateLeft = getSize().width / 20;
        int[] errorx = new int[]{
            round(getPos().x - translateLeft - getSize().width / scalew),
            round(getPos().x - translateLeft),
            round(getPos().x - translateLeft),
            round(getPos().x - translateLeft + getSize().width / scalew),
            round(getPos().x - translateLeft + getSize().width / scalew),
            round(getPos().x - translateLeft),
            round(getPos().x - translateLeft)
        };
        int[] errory = new int[]{
            round(getPos().y),
            round(getPos().y - getSize().height / scaleh),
            round(getPos().y),
            round(getPos().y - getSize().height / scaleh),
            round(getPos().y + getSize().height / scaleh),
            round(getPos().y),
            round(getPos().y + getSize().height / scaleh),};
        Polygon triangle = new Polygon(errorx, errory, errorx.length);
        g2.setColor(eventTypeFillColor);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    /**
     *
     * @param g2
     */
    protected void drawCancel(Graphics2D g2) {
        double far = 3.2;
        double nearlyfar = 4.0;
        double close = 15.0;
        int p1 = round(this.getPos().x - this.getSize().width / far);
        int p2 = round(this.getPos().x - this.getSize().width / nearlyfar);
        int p3 = round(this.getPos().x - this.getSize().width / close);
        int p4 = round(this.getPos().x);
        int p5 = round(this.getPos().x + this.getSize().width / close);
        int p6 = round(this.getPos().x + this.getSize().width / nearlyfar);
        int p7 = round(this.getPos().x + this.getSize().width / far);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(BPMNUtils.defaultStroke);
        int[] errorx = new int[]{
            p1, p2, p4, p6, p7, p5, p7, p6, p4, p2, p1, p3
        };

        p1 = round(this.getPos().y - this.getSize().height / far);
        p2 = round(this.getPos().y - this.getSize().height / nearlyfar);
        p3 = round(this.getPos().y - this.getSize().height / close);
        p4 = round(this.getPos().y);
        p5 = round(this.getPos().y + this.getSize().height / close);
        p6 = round(this.getPos().y + this.getSize().height / nearlyfar);
        p7 = round(this.getPos().y + this.getSize().height / far);

        int[] errory = new int[]{
            p2, p1, p3, p1, p2, p4, p6, p7, p5, p7, p6, p4
        };
        Polygon triangle = new Polygon(errorx, errory, errorx.length);
        g2.setColor(eventTypeFillColor);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    /**
     * @param g2
     */
    protected void drawError(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(BPMNUtils.defaultStroke);
        int[] errorx = new int[]{
            round(getPos().x - getSize().width / 2.9),
            round(getPos().x - getSize().width / 6.0),
            round(getPos().x + getSize().width / 8),
            round(getPos().x + getSize().width / 2.9),
            round(getPos().x + getSize().width / 6.0),
            round(getPos().x - getSize().width / 8),};
        int[] errory = new int[]{
            round(getPos().y + getSize().height / 2.9),
            round(getPos().y - getSize().height / 3.5),
            round(getPos().y + getSize().height / 20),
            round(getPos().y - getSize().height / 2.9),
            round(getPos().y + getSize().height / 3.5),
            round(getPos().y - getSize().height / 20),};
        Polygon triangle = new Polygon(errorx, errory, errorx.length);
        g2.setColor(eventTypeFillColor);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    protected void drawMultiple(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(BPMNUtils.defaultStroke);

        Polygon triangle = getMultiplePolygon(getPos(), getSize());
        g2.setColor(eventTypeFillColor);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    /**
     * @param g2
     */
    protected void drawSignal(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(BPMNUtils.defaultStroke);

        int[] trix = new int[]{
            round(getPos().x - getSize().width / 3.5),
            round(getPos().x),
            round(getPos().x + getSize().width / 3.5)
        };
        int[] triy = new int[]{
            round(getPos().y + getSize().height / 5),
            round(getPos().y - getSize().height / 3.5),
            round(getPos().y + getSize().height / 5)
        };
        Polygon triangle = new Polygon(trix, triy, trix.length);
        g2.setColor(eventTypeFillColor);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    /**
     * @param g2
     */
    protected void drawTimer(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        Rectangle2D.Double circle = new Rectangle2D.Double(getPos().x - (getSize().width / 3),
                getPos().y - round(getSize().height / 3), (getSize().width / 1.5), (getSize().height / 1.5));
        g2.setColor(eventTypeFillColor);
        g2.fillOval(round(circle.x), round(circle.y), round(circle.width), round(circle.height));
        g2.setColor(eventTypeOutlineColor);
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

    /**
     *
     */
    protected void drawMessage(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(BPMNUtils.defaultStroke);

        Rectangle2D.Double letter = new Rectangle2D.Double(getPos().x - (getSize().width / 3.5),
                getPos().y - round(getSize().height / 3.85), (getSize().width / 1.7), (getSize().height / 2.3));
        int[] trix = new int[]{
            round(letter.x),
            round(letter.x + (letter.width / 2)),
            round(letter.x + letter.width)
        };
        int[] triy = new int[]{
            round(letter.y),
            round(letter.y + (letter.height / 2.1)),
            round(letter.y)
        };
        Polygon triangle = new Polygon(trix, triy, trix.length);
        g2.setColor(eventTypeFillColor);
        g2.fill(letter);
        g2.fill(triangle);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(letter);
        g2.draw(triangle);

        g2.setStroke(oldStroke);
    }

    /**
     * @param g2
     */
    protected void drawConditional(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        Rectangle2D.Double rect = new Rectangle2D.Double(getPos().x - (getSize().width / 4.95),
                getPos().y - round(getSize().height / 3.4), (getSize().width / 2.2), (getSize().height / 1.7));
        int numLines = 4;
        g2.setColor(eventTypeFillColor);
        g2.fill(rect);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(rect);
        g2.setStroke(BPMNUtils.defaultStroke);
        for (int i = 0; i < numLines; i++) {
            int y = round(rect.getY() + i * (rect.getHeight() / numLines));
            g2.drawLine(round(rect.x), y, round(rect.x + rect.width), y);
        }

        g2.setStroke(oldStroke);
    }

    /**
     *
     * @param g2
     */
    protected void drawLink(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        int[] linkX = new int[]{
            -10, 2, 2, 10, 2, 2, -10
        };
        int[] linkY = new int[]{
            -4, -4, -10, 0, 10, 4, 4
        };
        Polygon linkPoly = new Polygon(linkX, linkY, linkX.length);

        // Tranform and scale to target
        AffineTransform at = new AffineTransform();
        at.scale(getSize().width * 0.03, getSize().width * 0.03);
        Shape shape = at.createTransformedShape(linkPoly);
        at.setToTranslation(getPos().x, getPos().y);
        shape = at.createTransformedShape(shape);
        g2.setColor(eventTypeFillColor);
        g2.fill(shape);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(shape);

        g2.setStroke(oldStroke);
    }

    protected void drawEscalation(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        int[] escalationX = new int[]{
            0, 6, 0, -6
        };
        int[] escalationY = new int[]{
            -10, 8, 0, 8
        };
        Polygon escalationPoly = new Polygon(escalationX, escalationY, escalationX.length);

        // Tranform and scale to target
        AffineTransform at = new AffineTransform();
        at.scale(getSize().width * 0.03, getSize().width * 0.03);
        Shape shape = at.createTransformedShape(escalationPoly);
        at.setToTranslation(getPos().x, getPos().y);
        shape = at.createTransformedShape(shape);
        g2.setColor(eventTypeFillColor);
        g2.fill(shape);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(shape);

        g2.setStroke(oldStroke);
    }

    protected void drawParallel(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(BPMNUtils.defaultStroke);
        int[] parallelX = new int[]{
            2, 2, 6, 6, 2, 2, -2, -2, -6, -6, -2, -2
        };
        int[] parallelY = new int[]{
            6, 2, 2, -2, -2, -6, -6, -2, -2, 2, 2, 6
        };
        Polygon parallelPoly = new Polygon(parallelX, parallelY, parallelX.length);

        // Tranform and scale to target
        AffineTransform at = new AffineTransform();
        at.scale(getSize().width * 0.04, getSize().width * 0.04);
        Shape shape = at.createTransformedShape(parallelPoly);
        at.setToTranslation(getPos().x, getPos().y);
        shape = at.createTransformedShape(shape);
        g2.setColor(eventTypeFillColor);
        g2.fill(shape);
        g2.setColor(eventTypeOutlineColor);
        g2.draw(shape);

        g2.setStroke(oldStroke);
    }

    public Color getEventTypeFillColor() {
        return eventTypeFillColor;
    }

    public void setEventTypeFillColor(Color eventTypeFillColor) {
        this.eventTypeFillColor = eventTypeFillColor;
    }

    public Color getEventTypeOutlineColor() {
        return eventTypeOutlineColor;
    }

    public void setEventTypeOutlineColor(Color eventTypeOutlineColor) {
        this.eventTypeOutlineColor = eventTypeOutlineColor;
    }

    public String toString() {
        return "BPMN Event (" + getText() + ")";
    }
}
