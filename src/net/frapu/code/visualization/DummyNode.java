/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 *
 * Represents a ProcessNode with no visible shape. Used for docking edges
 * that are not connect to a ProcessNode.
 *
 * @author fpu
 */
public class DummyNode extends ProcessNode {

    @Override
    protected void paintInternal(Graphics g) {
        // Do nothing here
    }

    @Override
    protected Shape getOutlineShape() {
        return new Ellipse2D.Double(getPos().x-5, getPos().y-5,10,10);
    }

}
