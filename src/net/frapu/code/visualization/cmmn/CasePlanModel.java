package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.Cluster;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephan
 * @version 12.10.2014.
 */
public class CasePlanModel extends Cluster {

    public CasePlanModel() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(300, 200);
        setText("Case Plan Model");
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(CMMNUtils.defaultStroke);

        int yPos = 10;
        int spacing = 0;

        if (!getStereotype().isEmpty()) spacing = 20;

        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2)+20+spacing,
                getSize().width, getSize().height-20-spacing);

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        Polygon titleBox = new Polygon();
        titleBox.addPoint(getPos().x - (getSize().width / 2) + 15, getPos().y - (getSize().height / 2));
        titleBox.addPoint(getPos().x - 15, getPos().y - (getSize().height / 2));
        titleBox.addPoint(getPos().x, getPos().y - (getSize().height / 2 - 20 - spacing));
        titleBox.addPoint(getPos().x - (getSize().width / 2), getPos().y - (getSize().height / 2 - 20 - spacing));

        g2.setPaint(getBackground());
        g2.fill(titleBox);

        g2.setPaint(Color.BLACK);
        g2.draw(titleBox);
        g2.setFont(CMMNUtils.defaultFont.deriveFont(Font.BOLD));
        CMMNUtils.drawText(g2, getPos().x-(getSize().width/4), getPos().y - getSize().height/2+yPos,
                getSize().width / 2, getText(), CMMNUtils.Orientation.CENTER);
    }

    @Override
    protected Shape getOutlineShape() {
       int spacing = 0;
       if (!getStereotype().isEmpty()) spacing = 20;

       Path2D path = new Path2D.Double();

        path.moveTo(getPos().x - (getSize().width / 2), getPos().y - (getSize().height / 2 - 20 - spacing));

        path.lineTo(getPos().x - (getSize().width / 2) + 15, getPos().y - (getSize().height / 2));
        path.lineTo(getPos().x - 15, getPos().y - (getSize().height / 2));
        path.lineTo(getPos().x, getPos().y - (getSize().height / 2 - 20 - spacing));
        path.lineTo(getPos().x + (getSize().width / 2), getPos().y - (getSize().height / 2 - 20 - spacing));
        path.lineTo(getPos().x + (getSize().width / 2), getPos().y + (getSize().height / 2));
        path.lineTo(getPos().x - (getSize().width / 2), getPos().y + (getSize().height / 2));
        path.lineTo(getPos().x - (getSize().width / 2), getPos().y - (getSize().height / 2 - 20 - spacing));

        path.closePath();
        return path;
    }

    @Override
     public String toString() {
        return "CMMN Case Plan Model ("+getText()+")";
    }
}
