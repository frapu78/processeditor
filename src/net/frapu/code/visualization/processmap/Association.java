/**
 *
 * Process Editor - Process Map Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.processmap;

import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author frank
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
        return ProcessMapUtils.dashedStroke;
    }
    
}
