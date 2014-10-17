package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

import java.awt.*;

/**
 *  @version 13.10.2014.
 *  @author Stephan
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
        // nothing to do
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
        return CMMNUtils.dashedStroke;
    }

    @Override
    protected boolean isDockingSupported() {
        return true;
    }
}
