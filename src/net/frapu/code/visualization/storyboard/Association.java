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

import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class Association extends ProcessEdge {

    public Association() {
        super();
    }

    public Association(ProcessNode source, ProcessNode target) {
        super(source, target);
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
        return StoryboardUtils.dashedStroke;
    }



}
