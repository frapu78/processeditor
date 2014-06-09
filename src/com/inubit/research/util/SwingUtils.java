/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.util;

import java.awt.Point;


/**
 *
 * @author fpu
 */
public class SwingUtils {

    /**
     * Centers the Frame on the screen
     */
    public static void center(java.awt.Window frame) {
        // Get the size of the screen
        java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Point _loc = center(dim, frame);
     // Move the window
        frame.setLocation(_loc);
    }

	/**
	 * @param owner
	 * @param newConfigDialog
	 */
	public static void centerRelative(java.awt.Window main,java.awt.Window child) {
		java.awt.Dimension dim = main.getSize();
		Point _loc = center(dim,child);
		// Move the window
		_loc.translate(main.getX(), main.getY());
		child.setLocation(_loc);
	}
	
	/**
	 * gets the center point for "what" relative to the given Dimension dim 
	 * @param dim
	 * @param what
	 */
	private static Point center(java.awt.Dimension dim,java.awt.Window what){
		 // Determine the new location of the window
        int w = what.getSize().width;
        int h = what.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        return new Point(x,y);
	}
}
