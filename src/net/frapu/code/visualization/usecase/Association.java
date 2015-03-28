/**
 *
 * Process Editor - Use Case Package
 *
 * (C) 2015 the authors
 *
 * http://frapu.de
 *
 */
package net.frapu.code.visualization.usecase;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

import java.awt.*;

/**
 * UML Use Case Association
 * @author fpu
 */
public class Association extends ProcessEdge {

    public Association() {
        super();
        initializeProperties();
    }

    public Association(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {

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
        return UseCaseUtils.defaultStroke;
    }

    @Override
    protected boolean isDockingSupported() {
        return true;
    }


}
