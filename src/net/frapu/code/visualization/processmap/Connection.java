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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author frank
 */
public class Connection extends ProcessEdge {
    
    private final static int xArrowPoints[] = {0, -10, -10};
    private final static int yArrowPoints[] = {0, 6, -6};
    private final static Polygon edgeArrow = new Polygon(xArrowPoints, yArrowPoints, 3);

    public Connection() {
        super();
    }
    
    public Connection(ProcessNode source, ProcessNode target) {
            super(source, target);
        }   

    @Override
    public Shape getSourceShape() {
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return edgeArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return ProcessMapUtils.defaultStroke;
    }
    
}
