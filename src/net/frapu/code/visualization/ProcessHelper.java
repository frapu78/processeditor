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
import java.awt.Point;

/**
 *
 * A ProcessObject representing a temporary helper object (not serialized).
 *
 * @author frank
 */
public abstract class ProcessHelper extends ProcessObject {

    public final static String TAG_HELPER = "helper";

    /**
     * Paints this ProcessHelper on a given Graphics.
     * @param g
     */
    public abstract void paint(Graphics g);

    /**
     * Returns whether this ProcessHelper can be selected in the ProcessEditor or not.
     * @return
     */
    public abstract boolean isSelectable();

    @Override
    public Object clone() {
        // @todo: Implement clone()
        return null;
    }

    @Override
    protected String getXmlTag() {
        return TAG_HELPER;
    }

	/**
	 * hook which can be used for selection detection in sub classes
	 * is only called when isSelectable() return true.
	 * @param p
	 * @return
	 */
	public boolean contains(Point p) {
		return false;
	}


}
