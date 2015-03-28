/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.Linkable;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Artifact;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 *
 * @author stephan
 */
public class CaseFileItem extends ProcessNode {


    public static final int DEFAULT_WIDTH = 20;
    public static final int DEFAULT_HEIGHT = 30;
    private static final int CURVESIZE = 5;
    private static final double EDGEVALUE = 0.3;

    public static ReferenceChooserRestriction restrictions;

    public CaseFileItem() {
        super();
        initializeProperties();
    }

    public CaseFileItem(int xPos, int yPos, String text) {
        super();
        setPos(xPos, yPos);
        setText(text);
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
        g2.setFont(CMMNUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           String text = getText();
           textBounds = CMMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, text, CMMNUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    private void drawDataObject(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(CMMNUtils.defaultStroke);

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

        String text = getText();
        
        g2.setFont(CMMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        CMMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2), getSize().width + 100, text, CMMNUtils.Orientation.TOP);
    }

    @Override
    public String toString() {
        return "CMMN Case File Item ("+getText()+")";
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
