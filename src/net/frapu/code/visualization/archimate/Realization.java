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
import java.awt.geom.Path2D;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class Realization extends ProcessEdge {

    private Path2D dotShapeTarget;

    public Realization() {
        super();
    }

    public Realization(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        if (dotShapeTarget == null) {
            dotShapeTarget = new Path2D.Double();
            dotShapeTarget.moveTo(0.0, 0.0);
            dotShapeTarget.lineTo(-12.0, -8.0);
            dotShapeTarget.lineTo(-12.0, 8.0);
            dotShapeTarget.closePath();
        }
        return dotShapeTarget;
    }

    @Override
    public boolean isOutlineTargetArrow() {
        return true;
    }

    @Override
    public Stroke getLineStroke() {
        return ArchimateUtils.dottedDashedStroke;
    }
}
