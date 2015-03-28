/**
 *
 * Process Editor - Use Case Package
 *
 * (C) 2015 the authors
 *
 * http://frapu.de
 *
 */
package net.frapu.code.visualization.usecase;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 *
 * UML Use Case System Boundary.
 *
 * @author fpu
 */
public class SystemBoundary extends Cluster {

    /** Label-Position-Type: "TOPLEFT, TOPCENTER, TOPRIGHT, BOTTOMLEFT, BOTTOMCENTER, BOTTOMRIGHT" */
    public static final String PROP_LABEL_POS = "label_pos";
    public static final String LABEL_TOPLEFT = "TOPLEFT";
    public static final String LABEL_TOPCENTER = "TOPCENTER";
    public static final String LABEL_TOPRIGHT = "TOPRIGHT";
    public static final String LABEL_BOTTOMLEFT = "BOTTOMLEFT";
    public static final String LABEL_BOTTOMCENTER = "BOTTOMCENTER";
    public static final String LABEL_BOTTOMRIGHT = "BOTTOMRIGHT";

    public SystemBoundary() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(300,200);
        setText("System");
        setProperty(PROP_LABEL_POS, LABEL_TOPCENTER);
        String[] type = { LABEL_TOPLEFT, LABEL_TOPCENTER, LABEL_TOPRIGHT, LABEL_BOTTOMLEFT, LABEL_BOTTOMCENTER,
                LABEL_BOTTOMRIGHT };
        setPropertyEditor(PROP_LABEL_POS, new ListSelectionPropertyEditor(type));
    }

    @Override
    protected void paintInternal(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(UseCaseUtils.defaultStroke);


        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw Label according to LABEL_POS
        g2.setFont(UseCaseUtils.defaultFont);
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_TOPLEFT)) {
            ProcessUtils.drawText(g2, getPos().x-(getSize().width/2)+5, getPos().y - getSize().height/2+15,
                    getSize().width -10, getText(), ProcessUtils.Orientation.LEFT);
        }
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_TOPCENTER)) {
            ProcessUtils.drawText(g2, getPos().x, getPos().y - getSize().height/2+15,
                    getSize().width -10, getText(), ProcessUtils.Orientation.CENTER);
        }
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_TOPRIGHT)) {
            ProcessUtils.drawText(g2, getPos().x+(getSize().width/2)-5, getPos().y - getSize().height/2+15,
                    getSize().width -10, getText(), ProcessUtils.Orientation.RIGHT);
        }
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_BOTTOMLEFT)) {
            ProcessUtils.drawText(g2, getPos().x-(getSize().width/2)+5, getPos().y + getSize().height/2-20,
                    getSize().width -10, getText(), ProcessUtils.Orientation.LEFT);
        }
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_BOTTOMCENTER)) {
            ProcessUtils.drawText(g2, getPos().x, getPos().y + getSize().height/2-20,
                    getSize().width -10, getText(), ProcessUtils.Orientation.CENTER);
        }
        if (getProperty(PROP_LABEL_POS).equalsIgnoreCase(LABEL_BOTTOMRIGHT)) {
            ProcessUtils.drawText(g2, getPos().x+(getSize().width/2)-5, getPos().y + getSize().height/2-20,
                    getSize().width -10, getText(), ProcessUtils.Orientation.RIGHT);
        }

    }

    @Override
    protected Shape getOutlineShape() {

        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

}
