/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization;

import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.List;
import static net.frapu.code.visualization.DefaultRoutingPointLayouter.*;

/**
 *
 * @author uha
 */
public class ProcessEdgeDragHelper extends ProcessHelper implements Dragable {

    private Point dragPoint;
    private ProcessEdge edge;
    private int minDistance = 15;
    private int p1Index;
    private int p2Index;
    private boolean targetAdjusted = false;
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public ProcessEdgeDragHelper(ProcessEdge edge, Point dragPoint) {
        this.edge = edge;
        this.dragPoint = dragPoint;
        this.detectClosestRoutingPoints();
    }

    public boolean isHorizontal() {
        return DefaultRoutingPointLayouter.isHorizontal(getRoutingPoint1(), getRoutingPoint2());
    }

    public boolean isVertical() {
        return DefaultRoutingPointLayouter.isVertical(getRoutingPoint1(), getRoutingPoint2());
    }

    public void detectClosestRoutingPoints() {
        // Iterate over all edges and return the closest distance
        int distance = Integer.MAX_VALUE;
        int resultIndex = 0;
        List<Point> points = edge.getRoutingPoints();

        for (int i = 0; i < points.size() - 1; i++) {
            // Calculate distance to line
            int currDist = (int) Line2D.ptSegDist(points.get(i).x, points.get(i).y,
                    points.get(i + 1).x, points.get(i + 1).y,
                    dragPoint.x, dragPoint.y);
            // Save if closer than recent distance
            if (currDist < distance) {
                resultIndex = i;
                distance = currDist;
            }
        }
        this.p1Index = resultIndex;
        this.p2Index = resultIndex + 1;
    }

    public Point getRoutingPoint1() {
        detectClosestRoutingPoints();
        return edge.getRoutingPoint(p1Index);
    }

    public Point getRoutingPoint2() {
        detectClosestRoutingPoints();
        return edge.getRoutingPoint(p2Index);
    }

    private void dragInsideNode(ProcessEdge edge, ProcessNode node, Point target, Point dockPoint, Point p2, boolean isSource, boolean isHorizontal) {
        //try setting docking Point
        Point nodePos = node.getPos();
        Rectangle2D nodeBounds = node.getBounds();
        Point newDockPoint = new Point(dockPoint);
        moveToTarget(newDockPoint, target, isHorizontal);
        if (isHorizontal) {
            newDockPoint.x = (int) ((p2.x < nodePos.x) ? nodeBounds.getMinX() : nodeBounds.getMaxX());
        } else {
            newDockPoint.y = (int) ((p2.y < nodePos.y) ? nodeBounds.getMinY() : nodeBounds.getMaxY());
        }
        newDockPoint.translate(-nodePos.x, -nodePos.y);
        boolean settingDPSucceeded = trySetDockPointOffset(newDockPoint, isHorizontal, isSource);
        if (settingDPSucceeded) {
            int removeIndex = isSource ? p1Index-1 : p2Index - 1;
            edge.removeRoutingPoint(removeIndex);
            if (isSource) {
                p1Index--;
                p2Index--;
            }  else {
                p2Index--;
            }
            moveSimpleRP(p1Index, target, isHorizontal);
            moveSimpleRP(p2Index, target, isHorizontal);
        } else {
            //assert false;
            
            moveSimpleRP(p1Index, target, isHorizontal);
            moveSimpleRP(p2Index, target, isHorizontal);
            
            int firstPointIndex = isSource ? p1Index : p2Index;
            Point firstPoint = edge.getRoutingPoint(firstPointIndex);
            Point connectionPoint = node.getConnectionPoint(firstPoint);
            connectionPoint.translate(-nodePos.x, -nodePos.y);
            edge.setDockPointOffset(connectionPoint, isSource);
            if (isHorizontal) {
                firstPoint.x = edge.getDockPointOffset(isSource).x;
            } else {
                firstPoint.y = edge.getDockPointOffset(isSource).y;
            }
            moveSimpleRP(firstPointIndex, target, isHorizontal); 
        } 
    }
    
    private boolean isDockingPoint(int index) {
        return index==0 || index==edge.getRoutingPoints().size()-1;
    }
    
    private ProcessNode getAffectedNode(boolean isSource) {
        return isSource ? edge.getSource() : edge.getTarget();
    }
    
    private boolean nodeDeepContains(Point p, boolean isSource) {
        return getAffectedNode(isSource).containsDeepInside(p);
    }
    
    private void adjustDeepContainedDockingPointToBeOnTheRightSide(Point newPoint, int delimiterIndex,  Point otherDelimiter, boolean isSource, boolean isHorizontal) {
        assert nodeDeepContains(newPoint, isSource);
        ProcessNode node = getAffectedNode(isSource);
        if (isHorizontal) {
            if (otherDelimiter.x<node.getPos().x) {
                newPoint.x = (int) node.getBounds().getMinX();
            } else {
                newPoint.x = (int) node.getBounds().getMaxX();
            }
        } else {
            if (otherDelimiter.y<node.getPos().y) {
                newPoint.y = (int) node.getBounds().getMinY();
            } else {
                newPoint.y = (int) node.getBounds().getMaxY();
            }            
        }
        edge.setDockPointOffset(getRelativePosition(node.getPos(), newPoint), isSource);
        int removeIndex = isSource ? p1Index : p2Index;
        removeRoutingPoint(removeIndex);
    }

    private boolean dockingPointIsOnWrongSide(Point dp, Point target, boolean isSource) {
        ProcessNode node = getAffectedNode(isSource);
        boolean result = isNorthDockingPoint(dp, node) && node.getPos().y<target.y;
        result |= isSouthDockingPoint(dp, node) && node.getPos().y>target.y;
        result |= isEastDockingPoint(dp, node) && node.getPos().x>target.x;
        result |= isWestDockingPoint(dp, node) && node.getPos().x<target.x;
        return result;
    }

    private void moveDPToRightSide(Point dockPoint, Point target, boolean isHorizontal, boolean isSource) {
        ProcessNode node = getAffectedNode(isSource);
        Point newPoint = new Point(dockPoint);
        if (!isHorizontal) {
            if (target.x<node.getPos().x) {
                newPoint.x = (int) node.getBounds().getMinX();
            } else {
                newPoint.x = (int) node.getBounds().getMaxX();
            }
        } else {
            if (target.y<node.getPos().y) {
                newPoint.y = (int) node.getBounds().getMinY();
            } else {
                newPoint.y = (int) node.getBounds().getMaxY();
            }
        }
        edge.setDockPointOffset(getRelativePosition(node.getPos(), newPoint), isSource);
    }

    private void removeRoutingPoint(int index) {
        if (p1Index>index) p1Index--;
        if (p2Index>index) p2Index--;
        edge.removeRoutingPoint(index);
    }

    private void addRoutingPoint(Point p, int index) {
        if (p1Index>index) p1Index++;
        if (p2Index>index) p2Index++;
        edge.addRoutingPoint(index, p);
    }

    private void moveDelimiter(Point delimiter, int delimeterIndex, Point otherDelimiter, Point target, boolean sectionIsHorizontal, boolean isSource) {
        Point newPoint = delimiter.getLocation();
        moveToTarget(newPoint, target, sectionIsHorizontal);
        if (((delimeterIndex==1 && isSource) || (delimeterIndex==edge.getRoutingPoints().size()-2 && !isSource))) {
            Point dock = isSource ? edge.getRoutingPoint(0) : edge.getRoutingPoint(-1);
            if(nodeDeepContains(newPoint, isSource)) {
                adjustDeepContainedDockingPointToBeOnTheRightSide(newPoint,delimeterIndex, otherDelimiter, isSource, sectionIsHorizontal);
            } else if (dockingPointIsOnWrongSide(dock, newPoint, isSource)) {
                moveDPToRightSide(dock, newPoint, sectionIsHorizontal, isSource);
            }
        }
        if (!isDockingPoint(delimeterIndex)) {
            moveSimpleRP(delimeterIndex, target, sectionIsHorizontal);
        } else {
            boolean successful = trySetDockPointOffset(target, sectionIsHorizontal, isSource);
            if (!successful) {
                insertHelperPointAndMoveDP(target, getAffectedNode(isSource), edge, getRelativePosition(getAffectedNode(isSource).getPos(), newPoint), sectionIsHorizontal, isSource);
            }
        }
    }

    private void insertHelperPointAndMoveDP(Point target, ProcessNode node, ProcessEdge edge, Point newDockPointOffset, boolean isHorizontal, boolean isSource) {
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        if (isSource) {
            edge.clearSourceDockPointOffset();
        } else {
            edge.clearTargetDockPointOffset();
        }
        int dis = minDistance;
        if (targetAdjusted) dis = 0;
        Point helperRP;
        if (isHorizontal) {
            if (newDockPointOffset.y < 0) {
                l.setNorthDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x, node.getPos().y - dis);
                helperRP.y = Math.min(helperRP.y, target.y);
            } else {
                l.setSouthDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x, node.getPos().y + dis);
                helperRP.y = Math.max(helperRP.y, target.y);
            }
            if (!targetAdjusted) target.y = helperRP.y;
        } else {
            if (newDockPointOffset.x > 0) {
                l.setEastDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x + dis, node.getPos().y);
                helperRP.x = Math.max(helperRP.x, target.x);
            } else {
                l.setWestDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x - dis, node.getPos().y);
                helperRP.x = Math.min(helperRP.x, target.x);
            }
            if (!targetAdjusted) target.x = helperRP.x;
        }
        if (targetAdjusted) moveToTarget(helperRP, target, isHorizontal);
        targetAdjusted = true;
        if (isSource) {
            addRoutingPoint(helperRP,0);
            assert edge.getRoutingPoint(1).equals(helperRP);
        } else {
            addRoutingPoint(helperRP, edge.getRoutingPoints().size()-2);
            assert edge.getRoutingPoint(-2).equals(helperRP);
        }
    }

    @Override
    public void setPos(Point p) {
        synchronized (edge) {
            targetAdjusted = false;
            detectClosestRoutingPoints();
            Point p1 = getRoutingPoint1();
            Point p2 = getRoutingPoint2();
            Point target = new Point(getPos());
            boolean isHorizontal = true;
            if (isHorizontal()) {
                target.y = p.y;
            } else if (isVertical()) {
                target.x = p.x;
                isHorizontal = false;
            } else {
                return;
            }

            if (!isStraight(edge, 0, 0)) return;
            moveDelimiter(edge.getRoutingPoints().get(p2Index), p2Index,edge.getRoutingPoints().get(p1Index), target, isHorizontal, false);
            moveDelimiter(edge.getRoutingPoints().get(p1Index), p1Index,edge.getRoutingPoints().get(p2Index), target, isHorizontal, true);
            if (!isStraight(edge, 0, 0)) {
               moveDelimiter(edge.getRoutingPoints().get(p2Index), p2Index,edge.getRoutingPoints().get(p1Index), target, isHorizontal, false);
            }
            assert isStraight(edge, 0, 0);
            assert isStraight(edge, 0, 0);
            this.dragPoint.setLocation(target);
            assert isStraight(getRoutingPoint1(), getRoutingPoint2());
            //layoutRoutingPoints();
        }

    }


    private void changeDockingPointSide(ProcessNode affectedNode, Point p1, Point p2, Point helperRP, boolean isSource, boolean yChanged) {
        if (yChanged) {
            //y changed
            helperRP.setLocation(affectedNode.getPos().x, p1.y > affectedNode.getPos().y ? Math.max(p1.y, p2.y) + minDistance : Math.min(p1.y, p2.y) - minDistance);
            p2.y = helperRP.y;
        } else {
            //x changed
            helperRP.setLocation(p1.x > affectedNode.getPos().x ? Math.max(p1.x, p2.x) + minDistance : Math.min(p1.x, p2.x) - minDistance, affectedNode.getPos().y);
            p2.x = helperRP.x;
        }
        //edge.moveRoutingPoint(otherEndIndex, p2);
        p1.setLocation(affectedNode.getConnectionPoint(helperRP));
        if (yChanged) {
            helperRP.x = p1.x;
        } else {
            helperRP.y = p1.y;
        }
    }

    private void insertHelperRoutingPoint(Point target, ProcessNode node, ProcessEdge edge, Point newDockPointOffset, boolean isHorizontal, boolean isSource) {
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        Point helperRP;
        if (isHorizontal) {
            if (newDockPointOffset.y < 0) {
                l.setNorthDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x, node.getPos().y - minDistance);
            } else {
                l.setSouthDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x, node.getPos().y + minDistance);
            }
            target.y = helperRP.y;
        } else {
            if (newDockPointOffset.x > 0) {
                l.setEastDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x + minDistance, node.getPos().y);
            } else {
                l.setWestDockingPoint(edge, isSource);
                helperRP = edge.getDockPointOffset(isSource);
                helperRP.translate(node.getPos().x - minDistance, node.getPos().y);
            }
            target.x = helperRP.x;
        }
        if (isSource) {
            edge.addRoutingPoint(0, helperRP);
            p2Index++;
        } else {
            edge.addRoutingPoint(p2Index, helperRP);
        }

    }

    private void moveSimpleRP(int index, Point target, boolean isHorizontal) {
        Point newPoint = edge.getRoutingPoint(index);
        moveToTarget(newPoint, target, isHorizontal);
        edge.moveRoutingPoint(index, newPoint);
    }

    private void moveToTarget(Point p, Point target, boolean isHorizontal) {
        if (isHorizontal) {
            p.y = target.y;
        } else {
            p.x = target.x;
        }
    }

    private boolean trySetDockPointOffset(Point newPoint, boolean isHorizontal, boolean isSource) {
        ProcessNode parentNode = getAffectedNode(isSource);
        Point dpOffset = getRelativePosition(parentNode.getPos(), newPoint);
        Point oldOffSet = edge.getDockPointOffset(isSource);
        edge.setDockPointOffset(dpOffset, isSource);
        boolean succeeded = isHorizontal ? edge.getDockPointOffset(isSource).y == dpOffset.y
                : edge.getDockPointOffset(isSource).x == dpOffset.x;
        if (!succeeded) edge.setDockPointOffset(oldOffSet, isSource);
        return succeeded;
    }

    @Override
    public Point getPos() {
        Point p1 = getRoutingPoint1();
        Point p2 = getRoutingPoint2();
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    @Override
    public void paint(Graphics g) {
    }

    @Override
    public boolean isSelectable() {
        return false;
    }
}
