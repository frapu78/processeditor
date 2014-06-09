/**
 *
 * Process Editor - Core Package
 *
 * (C) 2010 inubit AG
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

/**
 *
 * @author fpu
 */
public class ProcessEditorMath {


    /**
     * Returns the point of a line segment.
     * @param g2 - Used for graphical debugging (null=no debugging)
     * @param rp - The list of Points
     * @param offset - The offset in pixel
     * @return
     */
    public static Point getPointOnLineSequenceFromOffset(List<Point> rp, int offset, Graphics2D g2) {
        Point lastPoint = rp.get(0);
        int currPos = 0;
        for (int i=1; i<rp.size(); i++) {
            // Check if offset is in current segment
            // Get length of current segment
            Point currPoint = rp.get(i);
            double currLength = Math.sqrt(
                    (currPoint.x-lastPoint.x)*(currPoint.x-lastPoint.x)+
                    (currPoint.y-lastPoint.y)*(currPoint.y-lastPoint.y));
            if (offset<(currPos+currLength)) {
                // found segment; detect point
                double a = Math.abs(currPoint.y-lastPoint.y);
                double b = Math.abs(currPoint.x-lastPoint.x);
                //double c = currLength;

                double alpha = (a<b)?Math.tanh(a/b):Math.tanh(b/a);

                double c_new = offset - currPos;
                double a_new = (a<b)?Math.sin(alpha)*c_new:Math.cos(alpha)*c_new;
                double b_new = (a<b)?Math.cos(alpha)*c_new:Math.sin(alpha)*c_new;

                if (currPoint.x<lastPoint.x) {
                    // Swap x
                    b = b*-1.0;
                    b_new = b_new*-1.0;
                }
                if (currPoint.y<lastPoint.y) {
                    // Swap y
                    a = a*-1.0;
                    a_new = a_new*-1.0;
                }

                if (g2!=null) {
                    g2.setStroke(ProcessUtils.dashedStroke);
                    g2.setPaint(Color.GREEN);
                    g2.drawLine(lastPoint.x, lastPoint.y, (int)(lastPoint.x),(int)( lastPoint.y+a));
                    g2.setPaint(Color.RED);
                    g2.drawLine(lastPoint.x, (int)(lastPoint.y+a), (int)(lastPoint.x+b), (int)(lastPoint.y+a));
                    //System.out.println(""+this.getId()+": a="+a+", b="+b+", c="+currLength+", alpha="+alpha+", a_new="+a_new+", b_new="+b_new+", c_new="+c_new);
                }

                // Return new point
                return new Point((int)(lastPoint.x+b_new), (int)(lastPoint.y+a_new));
            }
            // Increase segment
            lastPoint = currPoint;
            currPos += currLength;
        }
        // Return last point if offset outside
        return lastPoint;
    }

    /**
     * Returns the absolute length of a line sequence
     * @param rp
     * @return
     */
    public static double getLineSequenceLength(List<Point> rp) {
        int result = 0;
        Point lastPoint = rp.get(0);
        for (int i=1; i<rp.size(); i++) {
            // Check if offset is in current segment
            // Get length of current segment
            Point currPoint = rp.get(i);
            double currLength = Math.sqrt(
                    (currPoint.x-lastPoint.x)*(currPoint.x-lastPoint.x)+
                    (currPoint.y-lastPoint.y)*(currPoint.y-lastPoint.y));
            // Increase segment
            lastPoint = currPoint;
            result += currLength;
        }
        return result;
    }

    /**
     * Returns the length of a line.
     * @param p1
     * @param p2
     * @return
     */
    public static double getLineLength(Point p1, Point p2) {
        return Math.sqrt(
                    (p1.x-p2.x)*(p1.x-p2.x)+
                    (p1.y-p2.y)*(p1.y-p2.y));
    }

}
