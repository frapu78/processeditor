/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class Sequence extends ProcessEdge {

    protected final static int xArrowPoints[] = {0, -14, -14};
    protected final static int yArrowPoints[] = {0, 10, -10};
    protected final static Polygon sequenceFlowArrow = new Polygon(xArrowPoints, yArrowPoints, 3);


    public Sequence() {
    }

    public Sequence(ProcessNode source, ProcessNode target) {
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
        return StoryboardUtils.defaultStroke;
    }

    @Override
    protected boolean isDockingSupported() {
        return true;
    }



}
