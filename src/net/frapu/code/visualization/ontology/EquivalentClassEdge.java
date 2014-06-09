/**
 *
 * Process Editor - Ontology Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.ontology;

import net.frapu.code.visualization.uml.*;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;

/**
 *
 * @author ff
 */
public class EquivalentClassEdge extends ProcessEdge {
    
    
    public EquivalentClassEdge() {
        super();
    }
    
    public EquivalentClassEdge(ProcessNode source, ProcessNode target) {
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
        return UMLUtils.boldStroke;
    }

}
