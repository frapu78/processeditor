/**
 *
 * Process Editor - Lifecycle Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.lifecycle;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class Transition extends ProcessEdge {

    private Path2D targetShape;

    public Transition(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    public Transition() {
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
        return ProcessUtils.defaultStroke;
    }

}
