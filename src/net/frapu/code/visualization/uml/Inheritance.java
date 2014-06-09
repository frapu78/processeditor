/**
 *
 * Process Editor - UML Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.uml;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;

/**
 *
 * @author fpu
 */
public class Inheritance extends ProcessEdge {
    
    protected final static int xArrowPoints[] = {0, -15, -15};
    protected final static int yArrowPoints[] = {0, 10, -10};
    protected final static Polygon sequenceFlowArrow = new Polygon(xArrowPoints, yArrowPoints, 3);

    public Inheritance() {
        super();
    }
    
    public Inheritance(UMLClass source, UMLClass target) {
            super(source, target);
    }

    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return sequenceFlowArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return UMLUtils.thinStroke;
    }

    @Override
    public boolean isOutlineTargetArrow() {
        return true;
    }

}
