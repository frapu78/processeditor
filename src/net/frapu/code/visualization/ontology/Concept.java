/**
 *
 * Process Editor - Ontology Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.ontology;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.petrinets.PetriNetUtils;

/**
 *
 * @author frank
 */
public class Concept extends OntologyNode {

    public Concept() {
        super();
        initializeProperties();
    }

    public Concept(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    protected void initializeProperties() {
        setSize(80,50);
        setText("Concept");
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

        g2.setStroke(PetriNetUtils.defaultStroke);
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
