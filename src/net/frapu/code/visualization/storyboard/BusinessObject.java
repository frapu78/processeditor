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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import net.frapu.code.converter.XSDImporter;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import net.frapu.code.visualization.editors.ReferencePropertyEditor;

/**
 *
 * @author fpu
 */
public class BusinessObject extends ProcessNode {

    public final static String PROP_ELEMENT_REF = "element_ref";

    private final static int xDataObjectPoints[] = {5, -20, -20, 20, 20};
    private final static int yDataObjectPoints[] = { -30, -30, 30, 30, -10};
    private final static Polygon dataObjectPoly = new Polygon(xDataObjectPoints, yDataObjectPoints, xDataObjectPoints.length);
   // private final static int xLinePoints[] = {5, 5, 20};
   // private final static int yLinePoints[] = {-30, -10, -10};

    public static ReferenceChooserRestriction restrictions;

    public static ReferenceChooserRestriction getReferenceRestrictions() {
        if (restrictions == null) {
            LinkedList<String> stereotypes = new LinkedList<String>();
            stereotypes.add(XSDImporter.STEREOTYPE_ELEMENT);
            stereotypes.add(XSDImporter.STEREOTYPE_COMPLEXTYPE);
            restrictions = new ReferenceChooserRestriction(stereotypes, null);
            restrictions.setReturnNameOnly(true);
        }
        return restrictions;
    }

    public BusinessObject() {
        super();
        init();
    }

    private void init() {
        setSize(40,60);
        setBackground(new Color(230,230,240));
        setProperty(PROP_ELEMENT_REF, "");
        setPropertyEditor(PROP_ELEMENT_REF, new ReferencePropertyEditor(getReferenceRestrictions()));
    }

    @Override
    protected Shape getOutlineShape() {
        // Clone dataObject
        Polygon currentDataObject = new Polygon(
                dataObjectPoly.xpoints, dataObjectPoly.ypoints, dataObjectPoly.npoints);
        StoryboardUtils.scalePolygon(currentDataObject, getSize().width, getSize().height);
        StoryboardUtils.movePolygon(currentDataObject, getPos().x, getPos().y);
        return currentDataObject;
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(StoryboardUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
           String text = getText();
           textBounds = StoryboardUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                   getSize().width + 100, text, StoryboardUtils.Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    @Override
        protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(StoryboardUtils.defaultStroke);

        Shape currentDataObject = getOutlineShape();

        // @todo: Correct fill for data object
        g2.setPaint(getBackground());
        g2.fill(currentDataObject);
        g2.setPaint(Color.BLACK);
        g2.draw(currentDataObject);

        // Draw inner lines
        int x1 = getTopLeftPos().x;
        int y1 = getTopLeftPos().y;
        g2.drawLine((int)(x1+getSize().width*0.6333), y1, (int)(x1+getSize().width*0.6333),
                (int)(y1+getSize().height*0.33333));
        g2.drawLine((int)(x1+getSize().width*0.6333), (int)(y1+getSize().height*0.33333),
                (int)(x1+getSize().width), (int)(y1+getSize().height*0.33333));

        // Check if Link found
        if (!getProperty(PROP_ELEMENT_REF).isEmpty()) {
            // Show Link Bubble
            g2.setPaint(Color.WHITE);
            Shape s = new Ellipse2D.Double(x1+getSize().width-8, y1+getSize().height-8, 16, 16);
            g2.fill(s);
            g2.setPaint(Color.BLACK);
            g2.draw(s);
            g2.setFont(new Font("Arial Bold",Font.BOLD, 12));
            StoryboardUtils.drawText(g2, x1+getSize().width-1, y1+getSize().height-10, 15, "R", Orientation.TOP);
        }

        String text = getText();
        g2.setFont(StoryboardUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        StoryboardUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2), getSize().width + 100, text, StoryboardUtils.Orientation.TOP);
    }

    @Override
    public String toString() {
        return getText()+" (Business Object)";
    }

}
