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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * @author fpu
 */
public class Activity extends FlowObject {

    /** Loop-Type: "NONE, STANDARD, SEQUENCE, PARALLEL" */
    public static final String PROP_LOOP_TYPE = "loop_type";
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_SEND = "SEND";
    public static final String TYPE_RECEIVE = "RECEIVE";
    public static final String TYPE_MANUAL = "MANUAL";
    public static final String TYPE_SCRIPT = "SCRIPT";
    public static final String TYPE_RULE = "RULE";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_REFERENCE = "Reference";
    /** Loop-Property: None */
    public static final String LOOP_NONE = "NONE";
    /** Loop-Property: Standard */
    public static final String LOOP_STANDARD = "STANDARD";
    /** Loop-Property: Multi Instance Sequence */
    public static final String LOOP_MULTI_SEQUENCE = "SEQUENCE";
    /** Loop-Property: Mutli Instance Parallel */
    public static final String LOOP_MULTI_PARALLEL = "PARALLEL";
    /** The size of the marker icons */
    public static final int MARKER_ICON_SIZE = 10;
    /** The size of the stereotype icons */
    public static final int STEREOTYPE_ICON_SIZE = 20;

    public Activity() {
        super();
        setSize(100, 60);
    }

    public static void drawParallelMultiInstance(Graphics2D g2, int x, int y) {
        g2.setStroke(BPMNUtils.defaultStroke);
        // Draw 3 small lines
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, x - MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x, y - MARKER_ICON_SIZE / 2, x, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x + MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
    }

    public static void drawSequentialMultiInstance(Graphics2D g2, int x, int y) {
        g2.setStroke(BPMNUtils.defaultStroke);
        // Draw 3 small lines
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2);
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y, x + MARKER_ICON_SIZE / 2, y);
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
    }

    public static void drawStandardLoop(Graphics2D g2, int x, int y) {
        g2.setStroke(BPMNUtils.defaultStroke);
        g2.drawArc(x - MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, MARKER_ICON_SIZE, MARKER_ICON_SIZE, 315, 270);

        g2.drawLine(x, y, x, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x, y + MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
    }

    /**
     * set plus to false, to draw a minus sign
     * @param g2
     * @param x
     * @param y
     * @param plus
     */
    public static void drawSubProcessMarker(Graphics2D g2, int x, int y,boolean plus) {
        g2.setStroke(BPMNUtils.defaultStroke);
        g2.drawRect(x - MARKER_ICON_SIZE, y - MARKER_ICON_SIZE, MARKER_ICON_SIZE * 2, MARKER_ICON_SIZE * 2);
        //if(plus)
        	g2.drawLine(x, y - MARKER_ICON_SIZE / 3, x, y + MARKER_ICON_SIZE / 3);
        g2.drawLine(x - MARKER_ICON_SIZE / 3, y, x + MARKER_ICON_SIZE / 3, y);
    }
    
    public static void drawSubProcessMarker(Graphics2D g2, int x, int y) {
       drawSubProcessMarker(g2, x, y, true);
    }

    public static void drawAdHoc(Graphics2D g2, int x, int y) {
        Font oldFont = g2.getFont();
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setPaint(Color.BLACK);
        g2.drawString("~", x - MARKER_ICON_SIZE, y + MARKER_ICON_SIZE);
        g2.setFont(oldFont);
    }

    public static void drawCompensation(Graphics2D g2, int x, int y) {
        g2.setPaint(Color.BLACK);
        g2.setStroke(BPMNUtils.defaultStroke);
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y, x, y - MARKER_ICON_SIZE / 2);
        g2.drawLine(x - MARKER_ICON_SIZE / 2, y, x, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x, y - MARKER_ICON_SIZE / 2, x, y + MARKER_ICON_SIZE / 2);

        g2.drawLine(x, y, x + MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2);
        g2.drawLine(x, y, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
        g2.drawLine(x + MARKER_ICON_SIZE / 2, y - MARKER_ICON_SIZE / 2, x + MARKER_ICON_SIZE / 2, y + MARKER_ICON_SIZE / 2);
    }

    public static void drawMessage(Graphics2D g2, int x, int y, Color fill, Color draw) {
        g2.setStroke(BPMNUtils.defaultStroke);
        Rectangle2D.Double letter = new Rectangle2D.Double(
                x - STEREOTYPE_ICON_SIZE / 2,
                y - STEREOTYPE_ICON_SIZE / 3,
                STEREOTYPE_ICON_SIZE,
                STEREOTYPE_ICON_SIZE / 3 * 2.0);

        int[] trix = new int[]{
            Math.round((float) letter.x),
            Math.round((float) (letter.x + (letter.width / 2))),
            Math.round((float) (letter.x + letter.width))
        };
        int[] triy = new int[]{
            Math.round((float) letter.y),
            Math.round((float) (letter.y + (letter.height / 2.1))),
            Math.round((float) letter.y)
        };
        Polygon triangle = new Polygon(trix, triy, trix.length);
        g2.setColor(fill);
        g2.fill(letter);
        g2.fill(triangle);
        g2.setColor(draw);
        g2.draw(letter);
        g2.draw(triangle);
    }

    public static void drawService(Graphics2D g2, int x, int y) {
        g2.setPaint(Color.BLACK);
        g2.setStroke(BPMNUtils.extraThinStroke);

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

        // Draw outer shape
        at.scale(Activity.STEREOTYPE_ICON_SIZE * 0.07, Activity.STEREOTYPE_ICON_SIZE * 0.07);
        Shape shape = at.createTransformedShape(path);
        at.setToTranslation(x, y);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw inner shape
        at = new AffineTransform();
        at.scale(Activity.STEREOTYPE_ICON_SIZE * 0.035, Activity.STEREOTYPE_ICON_SIZE * 0.035);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x, y);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw second outer shape
        at = new AffineTransform();
        at.scale(Activity.STEREOTYPE_ICON_SIZE * 0.07, Activity.STEREOTYPE_ICON_SIZE * 0.07);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x + STEREOTYPE_ICON_SIZE / 6, y + STEREOTYPE_ICON_SIZE / 6);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);

        // Draw second inner shape
        at = new AffineTransform();
        at.scale(Activity.STEREOTYPE_ICON_SIZE * 0.035, Activity.STEREOTYPE_ICON_SIZE * 0.035);
        shape = at.createTransformedShape(path);
        at.setToTranslation(x + STEREOTYPE_ICON_SIZE / 6, y + STEREOTYPE_ICON_SIZE / 6);
        shape = at.createTransformedShape(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.draw(shape);
    }

    public static void drawStereotype(Graphics2D g2, ProcessNode n) {
        if (n.getStereotype().length() > 0) {
            g2.setFont(BPMNUtils.defaultFont.deriveFont(Font.ITALIC));

            // Check if stereotype is "SERVICE"
            if (n.getStereotype().toLowerCase().equals(Activity.TYPE_SERVICE.toLowerCase())) {
                Activity.drawService(g2,
                        n.getPos().x - n.getSize().width / 2 + Activity.STEREOTYPE_ICON_SIZE / 2,
                        n.getPos().y - n.getSize().height / 2 + Activity.STEREOTYPE_ICON_SIZE / 2);
            } else {
                // "SEND"
                if (n.getStereotype().toLowerCase().equals(Activity.TYPE_SEND.toLowerCase())) {
                    Activity.drawMessage(g2,
                            n.getPos().x - n.getSize().width / 2 + Activity.STEREOTYPE_ICON_SIZE / 2 + 5,
                            n.getPos().y - n.getSize().height / 2 + Activity.STEREOTYPE_ICON_SIZE / 2,
                            Color.BLACK, Color.WHITE);
                } else {
                    // "RECEIVE"
                    if (n.getStereotype().toLowerCase().equals(Activity.TYPE_RECEIVE.toLowerCase())) {
                        Activity.drawMessage(g2,
                                n.getPos().x - n.getSize().width / 2 + Activity.STEREOTYPE_ICON_SIZE / 2 + 5,
                                n.getPos().y - n.getSize().height / 2 + Activity.STEREOTYPE_ICON_SIZE / 2,
                                Color.WHITE, Color.BLACK);
                    } else {
                        // "USER"
                        if (n.getStereotype().toLowerCase().equals(Activity.TYPE_USER.toLowerCase())) {
                            BPMNUtils.drawImage("/bpmn/user_task.png", g2,
                                    n.getTopLeftPos().x + 3, n.getTopLeftPos().y + 2);
                        } else {
                            // "MANUAL"
                            if (n.getStereotype().toLowerCase().equals(Activity.TYPE_MANUAL.toLowerCase())) {
                                BPMNUtils.drawImage("/bpmn/manual_task.png", g2,
                                        n.getTopLeftPos().x + 3, n.getTopLeftPos().y + 4);
                            } else {
                                // "RULE"
                                if (n.getStereotype().toLowerCase().equals(Activity.TYPE_RULE.toLowerCase())) {
                                    BPMNUtils.drawImage("/bpmn/rule_task.png", g2,
                                            n.getTopLeftPos().x + 3, n.getTopLeftPos().y + 4);
                                } else {
                                    // "SCRIPT"
                                    if (n.getStereotype().toLowerCase().equals(Activity.TYPE_SCRIPT.toLowerCase())) {
                                        BPMNUtils.drawImage("/bpmn/script_task.png", g2,
                                                n.getTopLeftPos().x + 3, n.getTopLeftPos().y + 4);
                                    } else {
                                        // Draw Stereotype as text
                                        g2.setPaint(Color.DARK_GRAY);
                                        BPMNUtils.drawText(g2, n.getPos().x, n.getPos().y - (n.getSize().height / 2),
                                                n.getSize().width - 8, "<<" + n.getStereotype() + ">>", BPMNUtils.Orientation.TOP);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    public void paintInternal(Graphics g) {
        // Do nothing here
    }

    @Override
    protected Shape getOutlineShape() {
        return null;
    }

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Task.class);
        result.add(SubProcess.class);
        result.add(CallActivity.class);
        return result;
    }
    
    public void pack() {
    	BufferedImage _bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
    	Graphics g = _bi.getGraphics();
    	Rectangle _bounds = ProcessUtils.drawText((Graphics2D) g, 0, 0, this.getSize().width, this.getText(), Orientation.CENTER);
    	this.setSize(this.getSize().width, _bounds.height+10);
    }

    public String toString() {
        return "BPMN Activity (" + getText() + ")";
    }
}
