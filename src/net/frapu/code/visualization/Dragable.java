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

import java.awt.Point;

/**
 * This interface is a wrapper for everything that can be dragged in the
 * editor.
 *
 * @author fpu
 */
public interface Dragable {

    /** Sets the position for this Dragable */
    public void setPos(Point p);

    /* Returns the position for this Dragable */
    public Point getPos();

}
