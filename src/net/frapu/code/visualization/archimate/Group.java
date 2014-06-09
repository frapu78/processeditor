/**
 *
 * Process Editor - Archimate Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.archimate;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class Group extends Cluster {

    public Group() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(300,200);
        setText("Group");
    }

    @Override
    protected void paintInternal(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ArchimateUtils.longDashedStroke);

        int yPos = 8;
        int spacing = 0;

        if (!getStereotype().isEmpty()) spacing = 15;

        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2)+20+spacing,
                getSize().width, getSize().height-20-spacing);

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        Rectangle2D titleBox = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2),
                getSize().width/2, 20+spacing);

        g2.setPaint(getBackground());
        g2.fill(titleBox);

        g2.setPaint(Color.BLACK);
        g2.draw(titleBox);
        
        g2.setPaint(Color.BLACK);
        if (!getStereotype().isEmpty()) {
            // Draw Stereotype
            g2.setFont(ArchimateUtils.defaultFont.deriveFont(Font.PLAIN));
            ProcessUtils.drawText(g2, getPos().x-(getSize().width/4), getPos().y - getSize().height/2+yPos,
                getSize().width / 2, "<<"+getStereotype()+">>", ProcessUtils.Orientation.CENTER);
            yPos+=15;
        }

        g2.setFont(ArchimateUtils.defaultFont);
        ProcessUtils.drawText(g2, getPos().x-(getSize().width/4), getPos().y - getSize().height/2+yPos,
                getSize().width / 2, getText(), ProcessUtils.Orientation.CENTER);
    }

    @Override
    protected Shape getOutlineShape() {

        int spacing = 0;
        if (!getStereotype().isEmpty()) spacing = 15;

        Path2D path = new Path2D.Double();

        path.moveTo(getPos().x - (getSize().width / 2), getPos().y - (getSize().height / 2));

        path.lineTo(getPos().x - (getSize().width / 2)+getSize().width/2, getPos().y - (getSize().height / 2));
        path.lineTo(getPos().x - (getSize().width / 2)+getSize().width/2, getPos().y - (getSize().height / 2)+20+spacing);
        path.lineTo(getPos().x +getSize().width/2, getPos().y - (getSize().height / 2)+20+spacing);
        path.lineTo(getPos().x +getSize().width/2, getPos().y +getSize().height/2);
        path.lineTo(getPos().x -getSize().width/2, getPos().y +getSize().height/2);

        path.closePath();
        return path;
    }

}
