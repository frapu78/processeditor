/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class InhibitorEdge extends ProcessEdge {

    private final static int xArrowPoints[] = {2, -15, -15};
    private final static int yArrowPoints[] = {0, 8, -8};
    private final static Polygon edgeArrow = new Polygon(xArrowPoints, yArrowPoints, 3);

    public InhibitorEdge() {
        super();
        initializeProperties();
    }

    public InhibitorEdge(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();        
    }

    private void initializeProperties() {
        setColor(Color.BLUE);
    }

    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return edgeArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return PetriNetUtils.extraBoldStroke;
    }
}
