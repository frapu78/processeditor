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
import java.awt.Shape;

/**
 *
 * @author fpu
 */
public class RoutingPointDragable extends ProcessHelper implements Dragable {

    private ProcessEdge edge;
    private int orderPos;

    public RoutingPointDragable(ProcessEdge edge, int orderPos) {
        this.edge = edge;
        this.orderPos = orderPos;
    }

    @Override
    public Object clone() {
        // @todo: Implement clone()
        return null;
    }

    public ProcessEdge getEdge() {
        return edge;
    }

    /**
     * Returns the ordering position of this RoutingPoint.
     * @return
     */
    public int getOrderPosition() {
        return orderPos;
    }

    @Override
    public void setPos(Point p) {
        edge.moveRoutingPoint(orderPos, p);
    }

    @Override
    public Point getPos() {
        Shape s = edge.getRoutingPointShapes().get(orderPos);
        return new Point(s.getBounds().x+(s.getBounds().width/2),
                s.getBounds().y+(s.getBounds().height/2));
    }

    @Override
    public void paint(Graphics g) {
        ProcessUtils.drawSelectionPoint(g, getPos());
    }

    @Override
    public boolean isSelectable() {
        // @todo: Fix to make it selectable here instead of special handling.
        return false;
    }

}
