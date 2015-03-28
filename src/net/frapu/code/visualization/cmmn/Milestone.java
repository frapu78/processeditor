/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @version 14.10.2014
 * @author Stephan
 */
public class Milestone extends ProcessNode {


    /** Properties for decorators */
    public static final String PROP_REPETITION = "repetition";
    public static final String PROP_REQUIRED = "required";

    public static final int MARKER_ICON_SIZE = 10;

    public Milestone() {
        super();
        int w = 100;
        int h = 30;
        setSize(w, h);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());

        setProperty(PROP_REPETITION, FALSE);
        setPropertyEditor(PROP_REPETITION, new BooleanPropertyEditor());

        setProperty(PROP_REQUIRED, FALSE);
        setPropertyEditor(PROP_REQUIRED, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {Graphics2D g2 = (Graphics2D) g;
        int currentMarker = 0;

        RoundRectangle2D milestoneShape = new RoundRectangle2D.Double();
        milestoneShape.setRoundRect(getPos().x - (getSize().width/2),
                getPos().y - (getSize().height/2),
                getSize().width, getSize().height, 45, 90);

        g2.setPaint(getBackground());
        g2.fill(milestoneShape);
        g2.setPaint(Color.BLACK);
        g2.setStroke(CMMNUtils.defaultStroke);
        g2.setColor(getBackground());
        g2.fill(milestoneShape);
        g2.setPaint(Color.BLACK);
        g2.draw(milestoneShape);

        g2.setFont(CMMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        CMMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), CMMNUtils.Orientation.CENTER);

        if (getProperty(PROP_REPETITION).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawRepetitionMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
            currentMarker = CMMNModel.markerPosMap.get(new Integer(currentMarker));
        }

        if (getProperty(PROP_REQUIRED).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawRequiredMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
            currentMarker = CMMNModel.markerPosMap.get(new Integer(currentMarker));
        }
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D result = new RoundRectangle2D.Double();
        result.setRoundRect(getPos().x - (getSize().width/2),
                getPos().y - (getSize().height/2),
                getSize().width, getSize().height, 45, 90);
        return result;
    }

    @Override
    public String toString() {
        return "CMMN Milestone ("+getText()+")";
    }
}
