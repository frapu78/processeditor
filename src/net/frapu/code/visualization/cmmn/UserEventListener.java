package net.frapu.code.visualization.cmmn;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.round;

/**
 * @author Stephan
 * @version 13.10.2014
 */
public class UserEventListener extends EventListener {

    @Override
    protected void drawMarker(Graphics2D g2) {
        g2.setPaint(Color.BLACK);
        g2.setStroke(CMMNUtils.defaultStroke);

        drawBody(g2);
        drawHead(g2);



    }

    private void drawHead(Graphics2D g2) {
        double expansion = getSize().width / 1.5;
        double radius = expansion * 3 / 16;

        Ellipse2D head = new Ellipse2D.Double();
        head.setFrame(getPos().x - radius, getPos().y - (expansion / 2), 2 * radius, 2 * radius);

        g2.draw(head);
    }

    private void drawBody(Graphics2D g2) {
        double expansion = getSize().width / 1.5;

        Shape shirt = getShirt();
        Shape cleavage = getCleavage();

        g2.draw(shirt);
        g2.draw(cleavage);

        g2.drawLine(getPos().x - (int)round((expansion / 5)),
                getPos().y + (int)round(expansion / 2),
                getPos().x - (int)round(expansion / 5),
                getPos().y + (int)round(expansion/3));

        g2.drawLine(getPos().x + (int)round((expansion / 5)),
                getPos().y + (int)round(expansion / 2),
                getPos().x + (int)round(expansion / 5),
                getPos().y + (int)round(expansion/3));
    }

    private Shape getCleavage() {
        double expansion = getSize().width / 1.5;
        Rectangle2D result = new Rectangle.Double();

        result.setRect(getPos().x - (int)round(expansion / 8.0), getPos().y - (int)round(expansion / 8.0),
                round(expansion / 4), round(expansion / 8));

        return result;
    }

    private Shape getShirt() {
        Polygon result = new Polygon();
        double expansion = getSize().width / 1.5;

        result.addPoint(getPos().x - (int) round(expansion / 2.75), getPos().y + (int) round(expansion / 2.0));
        result.addPoint(getPos().x - (int) round(expansion / 2.75), getPos().y + (int) round(expansion / 2.25));
        result.addPoint(getPos().x - (int) round(expansion / 2.25), getPos().y + (int) round(expansion / 2.25));
        result.addPoint(getPos().x - (int) round(expansion / 2.25), getPos().y + (int) round(expansion / 8.0));
        result.addPoint(getPos().x - (int) round(expansion / 2.75), getPos().y);
        result.addPoint(getPos().x - (int) round(expansion / 2.75), getPos().y - (int) round(expansion / 8.0));
        result.addPoint(getPos().x + (int) round(expansion / 2.75), getPos().y - (int) round(expansion / 8.0));
        result.addPoint(getPos().x + (int) round(expansion / 2.75), getPos().y);
        result.addPoint(getPos().x + (int) round(expansion / 2.25), getPos().y + (int) round(expansion / 8.0));
        result.addPoint(getPos().x + (int) round(expansion / 2.25), getPos().y + (int) round(expansion / 2.25));
        result.addPoint(getPos().x + (int) round(expansion / 2.75), getPos().y + (int) round(expansion / 2.25));
        result.addPoint(getPos().x + (int) round(expansion / 2.75), getPos().y + (int) round(expansion / 2.0));

        return result;
    }

    @Override
    public String toString() { return "CMMN User Event Listener ("+getText()+")"; }
}
