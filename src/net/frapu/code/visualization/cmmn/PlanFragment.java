/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author Stephan
 * @version 14.10.2014
 */
public class PlanFragment  extends Cluster {

    public static final int MARKER_ICON_SIZE = 10;

    public PlanFragment() {
        super();
        setSize(300, 100);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, "" + Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
        
        setProperty(PROP_COLLAPSED, FALSE);
        setPropertyEditor(PROP_COLLAPSED, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int currentMarker = 1;

        Shape fragmentShape = getOutlineShape();
        
        g2.setPaint(getBackground());
        g2.fill(fragmentShape);
        g2.setPaint(Color.BLACK);
        g2.setStroke(CMMNUtils.longDashedStroke);
        g2.draw(fragmentShape);

        drawStageMarker(g2,
                getPos().x,
                getPos().y + getSize().height / 2 - MARKER_ICON_SIZE,
                getProperty(PROP_COLLAPSED).equalsIgnoreCase(TRUE));

    }

    private void drawStageMarker(Graphics2D g2, int x, int y, boolean plus) {
        g2.setStroke(CMMNUtils.defaultStroke);
        g2.drawRect(x - MARKER_ICON_SIZE, y - MARKER_ICON_SIZE, MARKER_ICON_SIZE * 2, MARKER_ICON_SIZE * 2);
        if (plus) {
            g2.drawLine(x, y - MARKER_ICON_SIZE / 3, x, y + MARKER_ICON_SIZE / 3);
        }
        g2.drawLine(x - MARKER_ICON_SIZE / 3, y, x + MARKER_ICON_SIZE / 3, y);
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D result = new RoundRectangle2D.Double();
        result.setRoundRect(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2),
                getSize().width,
                getSize().height,
                5,
                5
        );
        return result;
    }
}
