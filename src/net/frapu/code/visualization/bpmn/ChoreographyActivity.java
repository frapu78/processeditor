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

import java.awt.Graphics;
import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessNode;

/**
 *
 * Provides an abstraction for Choreography Activities.
 *
 * @author frank
 */
public class ChoreographyActivity extends FlowObject {

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(ChoreographyTask.class);
        result.add(ChoreographySubProcess.class);
        return result;
    }

    @Override
    protected void paintInternal(Graphics g) {
        // Do nothing here
    }

    @Override
    protected Shape getOutlineShape() {
        return null;
    }

}
