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
public class UsedBy extends ProcessEdge {

    private Path2D targetShape;

    public UsedBy(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    public UsedBy() {
        super();
    }
   
    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        if (targetShape==null) {
            targetShape = new Path2D.Double();
            targetShape.moveTo(-5.0, -5.0);
            targetShape.lineTo(0.0, 0.0);
            targetShape.lineTo(-5.0, 5.0);
            targetShape.lineTo(-6.0, 5.0);
            targetShape.lineTo(-1.0, 0.0);
            targetShape.lineTo(-6.0, -5.0);
            targetShape.closePath();
        }
        return targetShape;
    }

    @Override
    public Stroke getLineStroke() {
        return ArchimateUtils.defaultStroke;
    }

}
