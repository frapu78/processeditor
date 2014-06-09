/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class ResetEdge extends ProcessEdge {

    private final static int xArrowPoints[] = {0, -10, -10, -2, -10, -10};
    private final static int yArrowPoints[] = {0, 6, 4, 0, -4, -6};
    private final static Polygon edgeArrow = new Polygon(xArrowPoints, yArrowPoints, xArrowPoints.length);

    public ResetEdge() {
        initializeProperties();
    }

    /**
	 * @param source  
     * @param target 
	 */
    public ResetEdge(ProcessNode source, ProcessNode target) {
        initializeProperties();
    }

    private void initializeProperties() {
        setColor(Color.RED);
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
        return PetriNetUtils.dashedStroke;
    }
}
