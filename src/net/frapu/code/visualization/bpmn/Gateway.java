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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * Represents an unspecified BPMN gateway (typically XOR).
 * 
 * @author frank
 */
public class Gateway extends QuadraticFlowObject {

    private final static int xGatewayPoints[] = {0, 10, 0, -10};
    private final static int yGatewayPoints[] = {-10, 0, 10, 0};
    /** Defines the shape of the gateway */
    private final static int xANDPoints[] = {1, 1, 5, 5, 1, 1, -1, -1, -5, -5, -1, -1};
    private final static int yANDPoints[] = {-5, -1, -1, 1, 1, 5, 5, 1, 1, -1, -1, -5};

    public Gateway() {
        super();
        initializeProperties();
    }

    public Gateway(int x, int y, String text) {
        super();
        setPos(x, y);
        setText(text);
        initializeProperties();
    }

    @Override
    public void setProperty(String key, String value) {
// disabled in order to allow delete animations in Process merger
//        if (key.equals(PROP_HEIGHT)) value = "40";
//        if (key.equals(PROP_WIDTH)) value = "40";
        super.setProperty(key, value);
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());

        // Set default size
        setSize(40, 40);
    }

    @Override
    protected Shape getOutlineShape() {
        Polygon gateway = new Polygon(xGatewayPoints, yGatewayPoints, xGatewayPoints.length);

        // Move and scale gateway
        ProcessUtils.scalePolygon(gateway, getSize().width, getSize().height);
        ProcessUtils.movePolygon(gateway, getPos().x, getPos().y);

        return gateway;
    }

    public String getType() {
        return getProperty(PROP_CLASS_TYPE);
    }

    public void setType(String type) {
        setProperty(PROP_CLASS_TYPE, type);
    }

    @Override
    protected void paintInternal(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        // draw default shape
        drawGateway(g);

        //add something inside if needed
        drawMarker(g2);
    }

    /**
	 * @param g2  
	 */
    protected void drawMarker(Graphics2D g2) {
        // Just a stub here...
    }

    protected void drawGateway(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(BPMNUtils.defaultStroke);
        drawGatewayBasicShape(g2);
    }

    protected void drawXORGateway(Graphics2D g2) {
        drawXOR(g2);
    }

    protected void drawComplexGateway(Graphics2D g2) {
        drawXOR(g2);
        drawAND(g2);
    }

    protected void drawANDGateway(Graphics2D g2) {
        drawAND(g2);
    }

    /**
     * @param g
     */
    protected void drawInitiatingXOREventGateway(Graphics2D g) {

        Stroke oldStroke = g.getStroke();

        // Draw circles
        g.drawOval((int) (getPos().x - getSize().width / 3.5)+1,
                (int) (getPos().y - getSize().height / 3.5)+1,
                (int) (2.0 * (getSize().width / 3.5)),
                (int) (2.0 * (getSize().height / 3.5)));

        double scaleFactor = 0.55;
        Polygon p = Event.getMultiplePolygon(getPos(),
                new Dimension(
                (int) (getSize().width * scaleFactor),
                (int) (getSize().height * scaleFactor)));
        g.drawPolygon(p);

        g.setStroke(oldStroke);
    }

    /**
     * @param g
     */
    protected void drawInitiatingParallelEventGateway(Graphics2D g) {

        Stroke oldStroke = g.getStroke();

        // Draw circles
        g.drawOval((int) (getPos().x - getSize().width / 3.5)+1,
                (int) (getPos().y - getSize().height / 3.5)+1,
                (int) (2.0 * (getSize().width / 3.5)),
                (int) (2.0 * (getSize().height / 3.5)));

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
        g.draw(shape);

        g.setStroke(oldStroke);
    }

    /**
     * @param g
     */
    protected void drawXOREventGateway(Graphics2D g) {

        Stroke oldStroke = g.getStroke();

        // Draw circles
        g.drawOval((int) (getPos().x - getSize().width / 3.5)+1,
                (int) (getPos().y - getSize().height / 3.5)+1,
                (int) (2.0 * (getSize().width / 3.5)),
                (int) (2.0 * (getSize().height / 3.5)));

        g.drawOval((int) (getPos().x - getSize().width / 3.0)+1,
                (int) (getPos().y - getSize().height / 3.0)+1,
                (int) (2.0 * (getSize().width / 3.0)),
                (int) (2.0 * (getSize().height / 3.0)));

        double scaleFactor = 0.55;
        Polygon p = Event.getMultiplePolygon(getPos(),
                new Dimension(
                (int) (getSize().width * scaleFactor),
                (int) (getSize().height * scaleFactor)));
        g.drawPolygon(p);

        g.setStroke(oldStroke);
    }

    /**
     * @param g
     */
    protected void drawORGateway(Graphics2D g) {
        g.drawOval((int) (getPos().x - getSize().width / 3.5) + 1,
                (int) (getPos().y - getSize().height / 3.5) + 1,
                (int) (2 * getSize().width / 3.5),
                (int) (2 * getSize().height / 3.5));
    }

    private void drawAND(Graphics2D g2) {
        Polygon and = new Polygon(xANDPoints, yANDPoints, xANDPoints.length);
        // Scale and move polygon
        BPMNUtils.scalePolygon(and, getSize().width / 2, getSize().height / 2);
        BPMNUtils.movePolygon(and, getPos().x, getPos().y);

        g2.setPaint(Color.black);
        g2.fill(and);
    }

    private void drawXOR(Graphics2D g2) {
        Polygon and = new Polygon(xANDPoints, yANDPoints, xANDPoints.length);
        // Scale, rotate, and move polygon
        BPMNUtils.scalePolygon(and, getSize().width / 2, getSize().height / 2);
        BPMNUtils.rotatePolygon(and, Math.PI / 4);
        BPMNUtils.movePolygon(and, getPos().x, getPos().y);
        g2.setPaint(Color.black);
        g2.fill(and);
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
                    getSize().width + 100, getText(), Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    /**
     * Draws a basic BPMN gateway. The stroke etc. have to be set beforehand.
     * Additional graphics need to be drawn afterwards.
     */
    private void drawGatewayBasicShape(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape gateway = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(gateway);

        g2.setPaint(Color.BLACK);
        g2.draw(gateway);

        // Draw text
        g2.setFont(BPMNUtils.defaultFont);
        if (getText() != null) {
            BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().width / 2),
                    getSize().width + 100, getText(), Orientation.TOP);
        }
    }

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Gateway.class);
        result.add(ExclusiveGateway.class);
        result.add(EventBasedGateway.class);
        result.add(InclusiveGateway.class);
        result.add(ComplexGateway.class);
        result.add(ParallelGateway.class);
        return result;
    }

    @Override
    public String toString() {
        return "BPMN Gateway ("+getText()+")";
    }
}
