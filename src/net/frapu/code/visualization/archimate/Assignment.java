/**
 *
 * Process Editor - Archimate Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.archimate;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class Assignment extends ProcessEdge {

    private final Shape dotShapeSource = new Ellipse2D.Double(0, -2.5, 5.0, 5.0);
    private final Shape dotShapeTarget = new Ellipse2D.Double(-5.0, -2.5, 5.0, 5.0);

    public Assignment(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    public Assignment() {
        super();
    }
   
    @Override
    public Shape getSourceShape() {
        return dotShapeSource;
    }

    @Override
    public Shape getTargetShape() {
        return dotShapeTarget;
    }

    @Override
    public Stroke getLineStroke() {
        return ArchimateUtils.defaultStroke;
    }

}
