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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import net.frapu.code.visualization.Linkable;
import net.frapu.code.visualization.domainModel.DomainClass;

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;

/**
 *
 * @author frank
 */
public class DataObject extends Artifact implements Linkable {


    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 60;
    private static final int LOOP_SPACE_TO_BORDER = 3;
    private static final int LOOP_ICON_SIZE = 10;
    private static final int CURVESIZE = 5;
    private static final double EDGEVALUE = 0.3;

    /** Property if this Data Object is a collection (0=FALSE, 1=TRUE) */
    public final static String PROP_COLLECTION = "collection";
    /** Property if this Data Object is Input or Output) */
    public final static String PROP_DATA = "data";
    /** Property to hold the state of the DataObject */
    public final static String PROP_STATE = "state";
    /** DataObject type is not input/output */
    public final static String DATA_NONE = "";
    /** DataObject type is Input */
    public final static String DATA_INPUT = "INPUT";
    /** DataObject type is Output */
    public final static String DATA_OUTPUT = "OUTPUT";

    public static ReferenceChooserRestriction restrictions;

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
        setProperty(PROP_COLLECTION, FALSE);
        setPropertyEditor(PROP_COLLECTION, new BooleanPropertyEditor());

        setProperty(PROP_DATA, DATA_NONE);
        String[] data = { DATA_NONE, DATA_INPUT, DATA_OUTPUT };
        setPropertyEditor(PROP_DATA, new ListSelectionPropertyEditor( data));

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
        g2.setFont(BPMNUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           String text = getText();
           if (!getState().isEmpty()) text += "\\n["+getState()+"]";
           textBounds = BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, text, BPMNUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    private void drawDataObject(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(BPMNUtils.defaultStroke);

        Shape currentDataObject = getOutlineShape();

        // @todo: Correct fill for data object
        g2.setPaint(getBackground());
        g2.fill(currentDataObject);
        g2.setPaint(Color.gray);
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

        if (getProperty(PROP_COLLECTION).equals(TRUE)) {
            drawMultiInstance(g2);
        }
        if (getProperty(PROP_DATA).toUpperCase().equals(DATA_INPUT)) {
            drawDataArrow(g2, false);
        }
        if (getProperty(PROP_DATA).toUpperCase().equals(DATA_OUTPUT)) {
            drawDataArrow(g2, true);
        }

        String text = getText();
        if (!getState().isEmpty()) text += "\n["+getState()+"]";
        
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.GRAY);
        BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2), getSize().width + 100, text, BPMNUtils.Orientation.TOP);
    }

    /**
     * @param g2
     */
    protected void drawMultiInstance(Graphics2D g2) {
        g2.setStroke(BPMNUtils.extraBoldStroke);
        Point pos = this.getPos();
        pos.y += this.getSize().height / 2 - LOOP_SPACE_TO_BORDER - LOOP_ICON_SIZE;
        pos.x -= LOOP_ICON_SIZE / 2;
        //drawing 3 small lines
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
        pos.x += LOOP_ICON_SIZE / 2;
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
        pos.x += LOOP_ICON_SIZE / 2;
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
    }

    /**
     * Draws INPUT/OUTPUT arrow
     * @param g2
     * @param fill
     */
    protected void drawDataArrow(Graphics2D g2, boolean fill) {
        g2.setStroke(BPMNUtils.defaultStroke);

        Path2D path = new Path2D.Double();

        path.moveTo(3.0,-6.0);
        path.lineTo(6.0,0.0);
        path.lineTo(3.0,6.0);
        path.lineTo(3.0,2.0);

        path.lineTo(-6.0,2.0);
        path.lineTo(-6.0,-2.0);
        path.lineTo(3.0,-2.0);
        path.lineTo(3.0,-6.0);

        path.closePath();

        // Tranform and scale to target
        AffineTransform at = new AffineTransform();

        // Draw outer shape
        at.scale(1.5, 1.2);
        Shape shape = at.createTransformedShape(path);
        at.setToTranslation(getPos().x-getSize().width/2+12, getPos().y-getSize().height/2+12);
        shape = at.createTransformedShape(shape);
        if (fill) g2.fill(shape);
        g2.draw(shape);
    }

    @Override
    public String toString() {
        return "BPMN Data Object ("+getText()+")";
    }

    public ReferenceChooserRestriction getReferenceRestrictions() {
        if (restrictions == null) {
            LinkedList<Class> classes = new LinkedList<Class>();
            classes.add(DomainClass.class);
            restrictions = new ReferenceChooserRestriction(null, classes);
        }
        return restrictions;
    }
}
