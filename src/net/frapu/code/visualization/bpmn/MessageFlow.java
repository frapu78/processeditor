/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import net.frapu.code.visualization.*;

/**
 *
 * @author fpu
 */
public class MessageFlow extends ProcessEdge {

    protected final static int xArrowPoints[] = {0, -10, -10};
    protected final static int yArrowPoints[] = {0, 6, -6};
    protected final static Polygon messageFlowArrow = new Polygon(xArrowPoints, yArrowPoints, 3);

    public MessageFlow() {
        super();
        initializeProperties();
    }

    public MessageFlow(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        // empty yet
    }

    @Override
    public Shape getSourceShape() {
        return new Ellipse2D.Double(0.0, -5.0, 10.0, 10.0);
    }

    @Override
    public Shape getTargetShape() {
        return messageFlowArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return BPMNUtils.longDashedStroke;
    }

    @Override
    public boolean isOutlineSourceArrow() {
        return true;
    }

    @Override
    public boolean isOutlineTargetArrow() {
        return true;
    }

    @Override
    protected boolean isDockingSupported() {
        return true;
    }
}
