/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditorMath;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class LabelHelper extends ProcessHelper implements Dragable {

    private ProcessEdge f_parent;
    //bounds and pos cache, update at each paint call
    private Rectangle f_bounds = new Rectangle(0, 0, 0, 0);
    private Point f_labelPos = new Point(0, 0);
    private static final int BOUNDS_EXTENSION = 4; //should be an even number!

    /**
     *
     */
    public LabelHelper(ProcessEdge parent) {
        f_parent = parent;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Draw label at offset from starting point
        String label = f_parent.getLabel();
        if (label != null && label.length() > 0) {
            f_labelPos = f_parent.getLabelPosition();
            g2.setFont(ProcessUtils.defaultFont);
            f_bounds = ProcessUtils.drawText(g2, f_labelPos.x, f_labelPos.y - 4, 100, label, ProcessUtils.Orientation.CENTER, true, true, 0.6f);
            f_bounds.x -= BOUNDS_EXTENSION / 2;
            f_bounds.width += BOUNDS_EXTENSION;
            f_bounds.y -= BOUNDS_EXTENSION / 2;
            f_bounds.height += BOUNDS_EXTENSION;

            // Fill background
            g2.setPaint(Color.WHITE);
            g2.fillRoundRect(f_bounds.x, f_bounds.y, f_bounds.width, f_bounds.height, BOUNDS_EXTENSION / 2, BOUNDS_EXTENSION / 2);

            g2.setPaint(Color.BLACK);
            g2.setStroke(ProcessUtils.defaultStroke);
            f_bounds = ProcessUtils.drawText(g2, f_labelPos.x, f_labelPos.y - 4, 100, label, ProcessUtils.Orientation.CENTER, true, true, 0.6f);

            g2.setColor(Color.RED);

            if (this.isSelected()) {
                g2.setColor(ProcessUtils.selectionColor);
                g2.setStroke(ProcessUtils.selectionStroke);
                g2.drawRoundRect(f_bounds.x, f_bounds.y, f_bounds.width, f_bounds.height, BOUNDS_EXTENSION / 2, BOUNDS_EXTENSION / 2);
            }
        }
    }

    @Override
    public Point getPos() {
        return f_labelPos;
    }

    @Override
    public boolean contains(Point p) {
        return f_bounds.contains(p);
    }

    //------------------------------------------------------------------------------
    //PORTING OF THE JS CODE IN THE PROCESSEDITOR
    //------------------------------------------------------------------------------
    @Override
    public void setPos(Point p) {
        ArrayList<Point> segments = getSegmentsForPoint(p);
        Point closestPoint = null;
        double distance = Double.MAX_VALUE;
        for (int i = 1; i < segments.size(); i += 2) {
            Point p1 = segments.get(i - 1);
            Point p2 = segments.get(i);
            double d = Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
            if (d < distance) {
                distance = d;
                closestPoint = getFootPoint(p1, p2, p);
            }
        }
        for (Point rp : f_parent.getRoutingPoints()) {
            double d = Math.sqrt((rp.x - p.x) * (rp.x - p.x) + (rp.y - p.y) * (rp.y - p.y));
            if (d < distance) {
                distance = d;
                closestPoint = rp;
            }
        }
        f_parent.setLabelOffset(getOffsetAtEdge(closestPoint));
    }

    /**
     * p has to be a point on the line
     * @param p
     * @return
     */
    private double getOffsetAtEdge(Point p) {
        double length = ProcessEditorMath.getLineSequenceLength(f_parent.getRoutingPoints());
        double curr = 0;
        List<Point> _rps = f_parent.getRoutingPoints();
        for (int i = 1; i < _rps.size(); i++) {
            Point p1 = _rps.get(i - 1);
            Point p2 = _rps.get(i);

            if (p.x <= Math.max(p1.x, p2.x) && p.x >= Math.min(p1.x, p2.x)
                    && p.y <= Math.max(p1.y, p2.y) && p.y >= Math.min(p1.y, p2.y)) {
                curr += Math.sqrt((p1.x - p.x) * (p1.x - p.x) + (p1.y - p.y) * (p1.y - p.y));
                break;
            } else {
                curr += Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
            }
        }
        double _off = (curr / length);
        return _off;
    }

    private ArrayList<Point> getSegmentsForPoint(Point p) {
        ArrayList<Point> segments = new ArrayList<Point>();
        List<Point> _rps = f_parent.getRoutingPoints();
        for (int i = 1; i < _rps.size(); i++) {
            Point p1 = _rps.get(i - 1);
            Point p2 = _rps.get(i);

            if (p1.x == p2.x) {
                if (p.y <= Math.max(p1.y, p2.y) && p.y >= Math.min(p1.y, p2.y)) {
                    segments.add(p1);
                    segments.add(p2);
                }
            } else if (p1.y == p2.y) {
                if ((p.x) <= Math.max(p1.x, p2.x) && (p.x) >= Math.min(p1.x, p2.x)) {
                    segments.add(p1);
                    segments.add(p2);
                }
            } else {
                if (p.y <= Math.max(p1.y, p2.y) && p.y >= Math.min(p1.y, p2.y)) {
                    if (p.x <= Math.max(p1.x, p2.x) && p.x >= Math.min(p1.x, p2.x)) {
                        segments.add(p1);
                        segments.add(p2);
                    }
                }
            }
        }

        return segments;
    }

    private Point getFootPoint(Point l1, Point l2, Point p) {
        Point line = new Point(l1.x - l2.x, l1.y - l2.y); //Vector
        double line_len = Math.sqrt(line.x * line.x + line.y * line.y);
        line.x /= line_len; // unit vector
        line.y /= line_len;
        // orthogonal Projection
        double lambda = (p.x - l2.x) * line.x + (p.y - l2.y) * line.y;
        return new Point((int) (l2.x + lambda * line.x), (int) (l2.y + lambda * line.y));
    }
}
