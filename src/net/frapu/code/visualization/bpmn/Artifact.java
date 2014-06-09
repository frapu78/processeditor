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
 * @author fpu
 */
public class Artifact extends FlowObject {

    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(DataObject.class);
        result.add(DataStore.class);
        result.add(TextAnnotation.class);
        result.add(Group.class);
        result.add(UserArtifact.class);
        return result;
    }

    @Override
    protected void paintInternal(Graphics g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Shape getOutlineShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
