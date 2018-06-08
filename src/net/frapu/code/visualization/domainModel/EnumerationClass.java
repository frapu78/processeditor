/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2018 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.DefaultPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

import java.awt.*;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 *
 * @author frank
 */
public class EnumerationClass extends ProcessNode {

    public final static String PROP_TYPES = "typeValues";
    public final static int FONTSIZE = 11;
    public final static String ELEMENT_DELIMITER = ";";
    public final static String STEREOTYPE_ENUMERATION = "enumeration";

    private static int ROUNDED_EDGE_VALUE = 5;
    private final static Color DEFAULT_COLOR = new Color(115, 184, 206);

    public EnumerationClass() {
        super();
        initializeProperties();
    }

    public EnumerationClass(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    protected void initializeProperties() {
        setSize(128, 80);
        setText("Type");
        setProperty(PROP_TYPES, "");
        setPropertyEditor(PROP_TYPES, new DefaultPropertyEditor());
        setProperty(PROP_STEREOTYPE, STEREOTYPE_ENUMERATION);
        setBackground(DEFAULT_COLOR);

        String[] atype = {STEREOTYPE_ENUMERATION};
        setPropertyEditor(PROP_STEREOTYPE, new ListSelectionPropertyEditor(atype));
    }

    @Override
    protected Shape getOutlineShape() {
        Shape outline = new Rectangle2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);

        return outline;
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
    }

    @Override
    public void setSize(int w, int h) {
        if (w < 50 | h < 50) {
            return;
        }
        super.setSize(w, h);
    }

    /**
     * Optimizes the size of this ProcessNode to fit all its current content.
     */
    public void pack() {
        int height = 0;

        if (!getStereotype().isEmpty()) {
            height += 20;
        }
        height += 20; // Name
        String[] types = getTypes();

        // For each type +15
        if (types.length>0) {
//            StringTokenizer attributes = new StringTokenizer(getProperty(PROP_ATTRIBUTES), ELEMENT_DELIMITER);
            height += types.length * 15;
        }
        height += 20; // Spacing

        setSize(getSize().width, height);

    }

    private void paintInternalOld(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(DomainUtils.defaultStroke);
        Shape outline = getOutlineShape();

        // #1: Complete background in white
        g2.setPaint(Color.WHITE);
        g2.fill(outline);

        // #2: Top in background
        int topsize = 20;
        if (!getStereotype().isEmpty()) {
            topsize += 20;
        }
        g2.setPaint(getBackground());
        Shape fillshape1 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2),
                getSize().width,
                topsize);
        g2.fill(fillshape1);

        // #3: Fill middle in white
        g2.setPaint(Color.WHITE);
        Shape fillshape2 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2) + topsize,
                getSize().width,
                ROUNDED_EDGE_VALUE);
        g2.fill(fillshape2);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        int yPos = getPos().y - (getSize().height / 2);

        // Stereotype
        if (!getStereotype().isEmpty()) {
            g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) FONTSIZE));
            ProcessUtils.drawText(g2, getPos().x, yPos,
                    getSize().width - 8, "<<" + getStereotype() + ">>",
                    ProcessUtils.Orientation.TOP,
                    false);
            yPos += 20;
        }

        // Name
        g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) FONTSIZE));
        ProcessUtils.drawText(g2, getPos().x, yPos,
                getSize().width - 8, getText(),
                ProcessUtils.Orientation.TOP,
                false, false);
        yPos += 20;
        drawLine(g2, yPos);

        g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) 10));

        String[] types = getTypes();

        if (types.length>0) {
            for (String t: types) {

                if (yPos < getPos().y + getSize().height / 2 - 15) {
                    ProcessUtils.drawText(g2, getPos().x - getSize().width / 2 + 4, yPos,
                            getSize().width - 8, t,
                            ProcessUtils.Orientation.LEFT, false, false);
                }
                yPos += g2.getFont().getSize() + 3;

            }
        }

        yPos += 10;

    }

    public String[] getTypes() {
        if (getProperty(PROP_TYPES)==null) {
            return new String[0];
        }
        return getProperty(PROP_TYPES).split(ELEMENT_DELIMITER);
    }

    @Override
    protected void paintInternal(Graphics g) {
        paintInternalOld(g);
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
        Set<Point> points = super.getDefaultConnectionPoints();
        return points;
    }

    private void drawLine(Graphics2D g2, int yPos) {
        g2.drawLine(getPos().x - (getSize().width / 2),
                yPos,
                getPos().x + (getSize().width / 2),
                yPos);
    }

}
