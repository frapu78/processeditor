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

        // Determine the new location of the window
        int w = frame.getSize().width;
        int h = frame.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        frame.setLocation(x, y);
    }

}

