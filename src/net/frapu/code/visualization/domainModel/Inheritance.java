/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 Frank Puhlmann
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

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
    
    public Inheritance(DomainClass source, DomainClass target) {
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
        return DomainUtils.defaultStroke;
    }

    @Override
    public boolean isOutlineTargetArrow() {
        return true;
    }

}
