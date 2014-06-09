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
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @see http://www.opengroup.org/archimate/doc/ts_archimate/
 * @author fpu
 */
public class Association extends ProcessEdge {

    public Association(ProcessNode source, ProcessNode target) {
        super(source, target);
    }

    public Association() {
        super();
    }
   
    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return null;
    }

    @Override
    public Stroke getLineStroke() {
        return ArchimateUtils.defaultStroke;
    }

}
