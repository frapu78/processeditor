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

import java.awt.Polygon;
import net.frapu.code.visualization.uml.*;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;

/**
 *
 * @author ff
 */
public class ContainmentEdge extends ProcessEdge {

    protected final static int xArrowTargetPoints[] = {0, -10, -12, -2, -12, -10};
    protected final static int yArrowTargetPoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon containmentFlowArrow = new Polygon(xArrowTargetPoints, yArrowTargetPoints, 6);
    
    public ContainmentEdge() {
        super();
    }
    
    public ContainmentEdge(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return containmentFlowArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return UMLUtils.dashedStroke;
    }

}
