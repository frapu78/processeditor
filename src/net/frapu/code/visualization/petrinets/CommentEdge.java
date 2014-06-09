/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author frank
 */
public class CommentEdge extends ProcessEdge {
    
    public CommentEdge() {
        super();
    }
    
    public CommentEdge(ProcessNode source, ProcessNode target) {
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
        return PetriNetUtils.thinDashedStroke;
    }
    
}
