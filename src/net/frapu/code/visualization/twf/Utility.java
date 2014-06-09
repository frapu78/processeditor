/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.twf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class Utility extends Module{

	public Utility() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setText("Utility");
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(new Color(230,230,230));
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.setFont(TWFUtils.defaultFont);

        ProcessUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2),
                getSize().width+50, getText(),
                ProcessUtils.Orientation.TOP);

    }

    @Override
    protected Shape getOutlineShape() {
        Path2D path = new Path2D.Double();

        path.moveTo(getPos().x, getPos().y-getSize().height/2);

        path.lineTo(getPos().x+getSize().width/2, getPos().y-getSize().height/8);
        path.lineTo(getPos().x+getSize().width/2, getPos().y+getSize().height/8);
        path.lineTo(getPos().x, getPos().y+getSize().height/2);
        path.lineTo(getPos().x-getSize().width/2, getPos().y+getSize().height/8);
        path.lineTo(getPos().x-getSize().width/2, getPos().y-getSize().height/8);

        path.closePath();

        return path;
    }

}
