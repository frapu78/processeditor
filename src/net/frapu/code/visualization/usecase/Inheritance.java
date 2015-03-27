/**
 *
 * Process Editor - Use Case Package
 *
 * (C) 2015 Frank Puhlmann
 *
 * http://frapu.de
 *
 */
package net.frapu.code.visualization.usecase;

import net.frapu.code.visualization.ProcessEdge;


import java.awt.*;

/**
 *
 * UML Use Case Inheritance.
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
    
    public Inheritance(UseCase source, UseCase target) {
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
        return UseCaseUtils.defaultStroke;
    }

    @Override
    public boolean isOutlineTargetArrow() {
        return true;
    }

}
