/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author frank
 */
public class Edge extends ProcessEdge {
    
    private final static int xArrowPoints[] = {0, -10, -10};
    private final static int yArrowPoints[] = {0, 6, -6};
    private final static Polygon edgeArrow = new Polygon(xArrowPoints, yArrowPoints, 3);

    public Edge() {
        super();
    }
    
    public Edge(ProcessNode source, ProcessNode target) {
            super(source, target);
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
        return PetriNetUtils.defaultStroke;
    }
    
}
