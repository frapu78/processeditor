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

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 *
 * UML Use Case.
 *
 * @author fpu
 */
public class UseCase extends ProcessNode {

    public UseCase() {
        super();
        initializeProperties();
    }

    public UseCase(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    protected void initializeProperties() {
        setSize(160,80);
        setText("Use Case");
    }

    @Override
    protected Shape getOutlineShape() {
        Ellipse2D outline = new Ellipse2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(UseCaseUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        ProcessUtils.drawText(g2, getPos().x, getPos().y-3,
                getSize().width-8, getText(),
                ProcessUtils.Orientation.CENTER);
    }
}
