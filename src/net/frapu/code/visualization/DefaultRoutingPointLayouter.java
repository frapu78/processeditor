/**
 *
 * Process Editor - UML Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.adapter.ProcessEdgeAdapter;
import com.inubit.research.layouter.preprocessor.LoopPreProcessor;
import com.inubit.research.testUtils.TestUtils;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static java.lang.Math.*;

/**
 *
 * @author fpu
 */
public class DefaultRoutingPointLayouter implements RoutingPointLayouter {

    public int minDistance = 10; //>0 The minimal distance of an edge to a node
    protected int iterationsLeft = 3;

    public DefaultRoutingPointLayouter() {
    }

    public static double AbsSlope(Point a, Point b) {
        double diffx = a.x - b.x;
        double diffy = a.y - b.y;
        if (diffx == 0) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.abs(diffy / diffx);
    }

    public static boolean horizontallyContained(ProcessNode node, Point p, int delta) {
        return (p.x < node.getBounds().x + node.getBounds().width + delta)
                && (p.x > node.getBounds().x - delta);
    }

    public static boolean verticallyConatained(ProcessNode node, Point p, int delta) {
        return (p.y < node.getBounds().y + node.getBounds().height + delta)
                && (p.y > node.getBounds().y - delta);
    }

    private boolean deltaContained(ProcessNode node, Point p, int delta) {
        return verticallyConatained(node, p, delta) && horizontallyContained(node, p, delta);
    }

    public static boolean isHorizontalDockingPoint(Point p, ProcessNode n) {
        Point outer = n.getTopLeftPos();
        Point middle = n.getPos();
        return AbsSlope(middle, p) <= AbsSlope(middle, outer);
    }

    public static boolean isVerticalDockingPoint(Point p, ProcessNode n) {
        Point outer = n.getTopLeftPos();
        Point middle = n.getPos();
        return AbsSlope(middle, p) >= AbsSlope(middle, outer);
    }

    public static boolean isNorthDockingPoint(Point p, ProcessNode n) {
        return isVerticalDockingPoint(p, n) && n.getPos().y >= p.y;
    }

    public static boolean isSouthDockingPoint(Point p, ProcessNode n) {
        return isVerticalDockingPoint(p, n) && n.getPos().y <= p.y;
    }

    public static boolean isEastDockingPoint(Point p, ProcessNode n) {
        return isHorizontalDockingPoint(p, n) && n.getPos().x <= p.x;
    }

    public static boolean isWestDockingPoint(Point p, ProcessNode n) {
        return isHorizontalDockingPoint(p, n) && n.getPos().x >= p.x;
    }

    /*
     * gets rid of Points where a line with 4 points is not possible without intersecting one of the Keep out zones
     */
    private ArrayList<Point> reachableDockingPoints(ArrayList<Point> adjustedNode1Points, ProcessNode node1, Point adjustedNode2Point, ProcessNode node2) {
        ArrayList<Point> filteredDockingPoints = new ArrayList<Point>();
        for (Point p : adjustedNode1Points) {

            if (dockingPointsInSight(node1, p, node2, adjustedNode2Point)) {
                filteredDockingPoints.add(p);
            }
        }
        return filteredDockingPoints;
    }

    private Point getClosestDP(ArrayList<Point> filteredTargetPoints, Point sourceDP) {
        Point result = null;
        double minDist = Double.MAX_VALUE;
        assert minDist > 100;
        for (Point p : filteredTargetPoints) {
            double currDist = p.distance(sourceDP);
            assert currDist > 0;
            if (currDist < minDist) {
                minDist = currDist;
                result = p;
            }
        }
        return result;
    }

    private Point adjustDockPoint(Point p, ProcessNode node) {
        Point newPoint = new Point(p);
        if (isNorthDockingPoint(p, node)) {
            newPoint.y -= minDistance;
        } else if (isSouthDockingPoint(p, node)) {
            newPoint.y += minDistance;
        } else if (isEastDockingPoint(p, node)) {
            newPoint.x += minDistance;
        } else if (isWestDockingPoint(p, node)) {
            newPoint.x -= minDistance;
        } else {
            assert false;
        }
        return newPoint;
    }

    private ArrayList<Point> getKeepOutZoneAdjustedDockingPoints(ProcessNode target) {
        // Check if connectionPoints are set use default ones
        // If no connection points are set, create default connection points
        ArrayList<Point> targetPoints = new ArrayList<Point>();
        //shift points to ensure a minimum distance
        Point newPoint;
        for (Point p : target.getDefaultConnectionPoints()) {
            p.translate(target.getPos().x, target.getPos().y);
            newPoint = adjustDockPoint(p, target);
            assert !target.getBounds().contains(newPoint);
            targetPoints.add(newPoint);
        }
        return targetPoints;
    }

    private boolean isSameDockingPointDirection(Point p1, Point p2, ProcessNode n) {
        return isNorthDockingPoint(p1, n) == isNorthDockingPoint(p2, n)
                && isSouthDockingPoint(p1, n) == isSouthDockingPoint(p2, n)
                && isEastDockingPoint(p1, n) == isEastDockingPoint(p2, n)
                && isWestDockingPoint(p1, n) == isWestDockingPoint(p2, n);
    }

    public static boolean isHorizontal(Point p1, Point p2) {
        return p1.y == p2.y;
    }

    public static boolean isVertical(Point p1, Point p2) {
        return p1.x == p2.x;
    }

    public static boolean isStraight(Point p1, Point p2) {
        return isVertical(p1, p2) || isHorizontal(p1, p2);
    }

    public void addStair(ProcessEdge edge) {
        LinkedList<Point> rp = (LinkedList<Point>) edge.getRoutingPoints();
        assert rp.size() == 2;
        Point start = rp.getFirst();
        Point end = rp.getLast();


        Point firstRoutingPoint;
        Point lastRoutingPoint;
        if (isHorizontalDockingPoint(start, edge.getSource())) {
            int meetX = middle(start.x, end.x);
            firstRoutingPoint = new Point(meetX, start.y);
            lastRoutingPoint = new Point(meetX, end.y);
        } else {
            int meetY = middle(start.y, end.y);
            firstRoutingPoint = new Point(start.x, meetY);
            lastRoutingPoint = new Point(end.x, meetY);
        }
        edge.addRoutingPoint(1, firstRoutingPoint);
        edge.addRoutingPoint(rp.size() - 1, lastRoutingPoint);

        assert edge.getRoutingPoints().size() == 4;
    }

    public void addFourthRoutingPoint(ProcessEdge edge) {
        Point p1 = edge.getRoutingPoints().get(0);
        Point p2 = edge.getRoutingPoints().get(1);
        Point p3 = edge.getRoutingPoints().get(2);
        assert edge.getRoutingPoints().size() == 3;
        edge.addRoutingPoint(1, new Point(p2));
        assert edge.getRoutingPoints().size() == 4;
    }

    @Override
    public void optimizeRoutingPoints(ProcessEdge edge, ProcessNode updatedNode) {
        this.iterationsLeft = 3;
        layoutRoutingPoints(edge, updatedNode);
        clearSuperfluousPoints(edge);
    }

    private void layoutRoutingPoints(ProcessEdge edge, ProcessNode updatedNode) {
        // Calculate all routing points the simple way
        if (edge.getSource() == null || edge.getTarget() == null) {
            return;
        }

        if ( edge.getSource() == edge.getTarget() ) {
            LayoutHelper.routeSelfEdge( new ProcessEdgeAdapter(edge) );
            LayoutHelper.setDockingPointOffset(edge);
            return;
        }

        //do not layout edges from edge dockers e.g. to data objects
        if (edge.getSource() instanceof EdgeDocker || edge.getTarget() instanceof EdgeDocker) {
            return;
        }

        if (endDirectionsCorrect(edge, true) && nodesEmpty(edge) && isStraight(edge, 0, 0)) {
            return;
        }
        synchronized (edge) {
            if (edge.getRoutingPoints().size() < 2) {
                return;
            } else if (edge.getRoutingPoints().size() == 2) {
                //add 2 additional routing Points
                addStair(edge);
            } else if (edge.getRoutingPoints().size() == 3) {
                //add 1 additional routing Point
                addFourthRoutingPoint(edge);
            }
            if (edge.getRoutingPoints().size() >= 4) {
                if (edge.getSource() == edge.getTarget() && edge.getRoutingPoints().size() == 4) {
                    //special case of self reference
                    Point helper = new Point((int) edge.getSource().getBounds().getMaxX() + minDistance, (int) edge.getSource().getBounds().getMinX() - minDistance);
                    edge.addRoutingPoint(1, helper);
                }
                Point orginalSourceDockingPoint = edge.getSourceDockPointOffset();
                if (orginalSourceDockingPoint != null) {
                    orginalSourceDockingPoint.translate(edge.getSource().getPos().x, edge.getSource().getPos().y);
                }
                Point orginalTargetDockingPoint = edge.getTargetDockPointOffset();
                if (orginalTargetDockingPoint != null) {
                    orginalTargetDockingPoint.translate(edge.getSource().getPos().x, edge.getSource().getPos().y);
                }

                if (orginalSourceDockingPoint == null && orginalTargetDockingPoint == null) {
                    resetBothDockingPoints(edge);
                } else if (orginalSourceDockingPoint == null) {
                    resetDockingPoint(edge, edge.getRoutingPoints().get(3), true);
                }
                if (orginalTargetDockingPoint == null) {
                    resetDockingPoint(edge, edge.getRoutingPoints().get(edge.getRoutingPoints().size() - 4), false);
                }



                setFirstRoutingPoint(edge.getSource(), edge, edge.getRoutingPoint(0), true);
                setFirstRoutingPoint(edge.getTarget(), edge, edge.getRoutingPoint(-1), false);
                assert isStraight(edge.getRoutingPoint(0), edge.getRoutingPoint(1));
                assert isStraight(edge.getRoutingPoint(-1), edge.getRoutingPoint(-2));
                if (!deltaIntersect(edge)) {
                    assert endDirectionsCorrect(edge, true) : TestUtils.getLayoutDebugMessage(edge);
                }
                rectifyEdge(edge);
                if (!deltaIntersect(edge)) {
                    assert isStraight(edge, 0, 0);
                }
                if (!deltaIntersect(edge)) {
                    assert endDirectionsCorrect(edge, false) : TestUtils.getLayoutDebugMessage(edge);
                }
                ensureMinDistance(edge, true, updatedNode);
                ensureMinDistance(edge, false, updatedNode);               
                if (!deltaIntersect(edge)) {
                    assert isStraight(edge, 0, 0);
                    assert endDirectionsCorrect(edge, true);
                    assert nodesEmpty(edge): TestUtils.getLayoutDebugMessage(edge);
                }
                if (orginalSourceDockingPoint == null) {
                    edge.clearSourceDockPointOffset();
                }
                if (orginalTargetDockingPoint == null) {
                    edge.clearTargetDockPointOffset();
                }
                if (deltaIntersect(edge) && !isStraight(edge, 0, 0)) {
                        setFirstRoutingPoint(edge.getSource(), edge, edge.getRoutingPoint(0), true);
                        setFirstRoutingPoint(edge.getTarget(), edge, edge.getRoutingPoint(-1), false);
                        rectifyEdge(edge);
                        assert isStraight(edge, 0, 0);
                }
                if (!deltaIntersect(edge)) {
                    assert endDirectionsCorrect(edge, true);
                    assert nodesEmpty(edge);
                }
                assert isStraight(edge, 0, 0);
            }
        }
    }

    public boolean endDirectionsCorrect(ProcessEdge edge, boolean strict) {
        boolean b = true;
        Point p1 = edge.getRoutingPoint(0);
        Point p2 = edge.getRoutingPoint(1);
        b &= endHasRightDirection(p1, p2, edge.getSource(), strict);
        p1 = edge.getRoutingPoint(-1);
        p2 = edge.getRoutingPoint(-2);
        b &= endHasRightDirection(p1, p2, edge.getTarget(), strict);
        return b;
    }

    private boolean endHasRightDirection(Point p1, Point p2, ProcessNode node, boolean strict) {
        boolean result;
        if (strict) {
            result = ((p1.x < p2.x && isEastDockingPoint(p1, node))
                    || (p2.x < p1.x && isWestDockingPoint(p1, node))
                    || (p2.y < p1.y && isNorthDockingPoint(p1, node))
                    || (p1.y < p2.y && isSouthDockingPoint(p1, node)));
        } else {
            result = ((p1.x <= p2.x && isEastDockingPoint(p1, node))
                    || (p2.x <= p1.x && isWestDockingPoint(p1, node))
                    || (p2.y <= p1.y && isNorthDockingPoint(p1, node))
                    || (p1.y <= p2.y && isSouthDockingPoint(p1, node)));
        }
        /*
        assert result : p1 + " -- " + p2 + isEastDockingPoint(p1, node) + " " + isWestDockingPoint(p1, node)
        + " " + isNorthDockingPoint(p1, node) + " " + isSouthDockingPoint(p1, node);
         */
        result &= isStraight(p1, p2);
        return result;
    }

    public void rectifyEdge(ProcessEdge edge) {
        assert isStraight(edge.getRoutingPoints().get(0), edge.getRoutingPoints().get(1));
        assert isStraight(edge.getRoutingPoint(-1), edge.getRoutingPoint(-2));
        assert endDirectionsCorrect(edge, true);
        int i = 1;
        int limit = edge.getRoutingPoints().size() - 3;
        while (i <= limit) {
            Point first = edge.getRoutingPoints().get(i);
            Point second = edge.getRoutingPoints().get(i + 1);
            if (!isStraight(first, second)) {
                rectify(edge.getRoutingPoints().get(i - 1), first, second, edge.getRoutingPoints().get(i + 2));
                assert isStraight(edge.getRoutingPoints().get(i - 1), first);
                assert isStraight(first, second);
                if (i == 1) {
                    assert endHasRightDirection(edge.getRoutingPoints().get(i - 1), first, edge.getSource(), false) : TestUtils.getLayoutDebugMessage(edge);
                }
                if (i == limit) {
                    assert isStraight(second, edge.getRoutingPoint(i + 2));
                    assert endHasRightDirection(edge.getRoutingPoints().get(i + 2), second, edge.getTarget(), false) : TestUtils.getLayoutDebugMessage(edge);
                }
                edge.moveRoutingPoint(i, first);
                edge.moveRoutingPoint(i + 1, second);
                if (i == limit) {
                    assert isStraight(edge.getRoutingPoint(i + 1), edge.getRoutingPoint(i + 2)) : edge.getRoutingPoints() + "second: " + second;
                    assert endHasRightDirection(edge.getRoutingPoints().get(i + 2), second, edge.getTarget(), false) : TestUtils.getLayoutDebugMessage(edge);
                }
            }
            i++;
        }
        assert isStraight(edge, 0, 0) : edge.getRoutingPoints();
        assert endDirectionsCorrect(edge, false);

    }

    public static boolean isStraight(ProcessEdge edge, int start, int minusEnd) {
        int i = start;
        List<Point> rps = edge.getRoutingPoints();
        while (i < rps.size() - minusEnd - 1) {
            Point first = rps.get(i);
            Point second = rps.get(i + 1);           
            if (!isStraight(first, second)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public boolean nodesEmpty(ProcessEdge edge) {
        if (edge.getRoutingPoints().size() < 4) {
            return true;
        }
        if (deltaIntersect(edge)) {
            return true;
        }
        Point p1 = edge.getRoutingPoints().get(1);
        Point p2 = edge.getRoutingPoints().get(2);
        Point p3 = edge.getRoutingPoints().get(edge.getRoutingPoints().size() - 2);
        Point p4 = edge.getRoutingPoints().get(edge.getRoutingPoints().size() - 3);

        boolean result = !deltaContained(edge.getSource(), p1, minDistance);
        result &= !deltaContained(edge.getSource(), p2, minDistance);
        result &= !deltaContained(edge.getTarget(), p3, minDistance);
        result &= !deltaContained(edge.getTarget(), p4, minDistance);

        return result;
    }



    public static Point getRelativePosition(Point base, Point p) {
        return new Point(p.x - base.x, p.y - base.y);
    }

  
    private int getContained(int limit1, int limit2, int toSet, int alternative) {
        if (isInBetween(toSet, limit1, limit2)) {
            return toSet;
        } else if (isInBetween(alternative, limit1, limit2)) {
            return alternative;
        } else {
            return middle(limit1, limit2);
        }
    }

    public static Point reversedPoint(Point p) {
        Point newPoint = new Point(p);
        newPoint.x = p.y;
        newPoint.y = p.x;
        return newPoint;
    }

    public static void reverse(Point p1) {
        p1.setLocation(reversedPoint(p1));
    }

    public static void reverse(Point p1, Point p2) {
        reverse(p1);
        reverse(p2);
    }

    public static void reverse(Point p1, Point p2, Point p3) {
        reverse(p1, p2);
        reverse(p3);
    }

    public static void reverse(Point p1, Point p2, Point p3, Point p4) {
        reverse(p1, p2);
        reverse(p3, p4);
    }

    private void straightAndInBetween(Point p1, Point p2, Point p3, Point p4) {
        assert isInBetween(p2.x, p1.x, p4.x);
        assert isInBetween(p3.x, p1.x, p4.x);
        assert isInBetween(p2.x, p1.x, p4.x);
        assert isInBetween(p3.x, p1.x, p4.x);
        assert isStraight(p2, p3);
    }

    private void rectifySameDirectionHorizontally(Point p1, Point p2, Point p3, Point p4, boolean p1p2p3p4PositiveDirection) {
        if (p1p2p3p4PositiveDirection ^ p4.x <= p1.x) {
            p3.x = p2.x = getContained(p1.x, p4.x, p2.x, p3.x);
        } else {
            p2.x = p1.x;
            p3.x = p4.x;
            p2.y = p3.y = middle(p1.y, p4.y);
        }
    }

    private void rectifyDifferentDirectionsHorizontally(Point p1, Point p2, Point p3, Point p4, boolean p1p2PositiveDirection) {
        p2.x = p3.x = p1p2PositiveDirection ? Math.max(p2.x, p3.x) : Math.min(p2.x, p3.x);
    }

    private void rectifyDifferentDirectionHV(Point p1, Point p2, Point p3, Point p4, boolean p1p2PositiveDirection) {
        if (p1p2PositiveDirection ^ p3.x <= p1.x) {
            p2.x = p3.x;
        } else {
            p2.x = p1.x;
            p2.y = p3.y;
        }
    }

    private void rectify(Point p1, Point p2, Point p3, Point p4) {
        assert !isStraight(p2, p3);
        boolean b1 = isStraight(p1, p2);
        boolean b2 = isStraight(p3, p4);
        Point p1o = new Point(p1);
        Point p2o = new Point(p2);
        Point p3o = new Point(p3);
        Point p4o = new Point(p4);

        if ((isHorizontal(p1, p2) && isVertical(p3, p4)) || (isVertical(p1, p2) && isHorizontal(p3, p4))) {
            boolean reverse = isVertical(p1, p2);
            if (reverse) {
                reverse(p1, p2, p3, p4);
            }
            rectifyDifferentDirectionHV(p1, p2, p3, p4, p1.x <= p2.x);
            if (reverse) {
                reverse(p1, p2, p3, p4);
            }
        } else if (isVertical(p1, p2) || isVertical(p3, p4)) {
            reverse(p1, p2, p3, p4);
            if ((p1.x >= p2.x && p3.x >= p4.x) || (p1.x <= p2.x && p3.x <= p4.x)) {
                rectifySameDirectionHorizontally(p1, p2, p3, p4, p1.x <= p2.x);
            } else {
                rectifyDifferentDirectionsHorizontally(p1, p2, p3, p4, p1.x <= p2.x);
            }
            reverse(p1, p2, p3, p4);
        } else {
            //if (isHorizontal(p1, p2) || isHorizontal(p3, p4)) {
            if ((p1.x >= p2.x && p3.x >= p4.x) || (p1.x <= p2.x && p3.x <= p4.x)) {
                rectifySameDirectionHorizontally(p1, p2, p3, p4, p1.x <= p2.x);
            } else {
                rectifyDifferentDirectionsHorizontally(p1, p2, p3, p4, p1.x <= p2.x);
            }
            //} else
            //    assert false;
        }
        if (!p1o.equals(p2o) && !p3o.equals(p4o)) {
            if (isRight(p1o, p2o)) {
                if (!(p2.x >= p1.x)) {
                    rectify(p1o, p2o, p3o, p4o);
                    assert p2.x >= p1.x : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
                }
            }
            if (isLeft(p1o, p2o)) {
                assert p2.x <= p1.x : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
            }
            if (isUp(p1o, p2o)) {
                if (!(p2.y <= p1.y)) {
                    rectify(p1o, p2o, p3o, p4o);
                    assert p2.y <= p1.y : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
                }
            }
            if (isDown(p1o, p2o)) {
                assert p2.y >= p1.y : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
            }
            if (isRight(p3o, p4o)) {
                if (!(p4.x >= p3.x)) {
                    rectify(p1o, p2o, p3o, p4o);
                    assert p4.x >= p3.x : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
                }
            }
            if (isLeft(p3o, p4o)) {
                assert p4.x <= p3.x : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
            }
            if (isUp(p3o, p4o)) {
                if (!(p4.y <= p3.y)) {
                    rectify(p1o, p2o, p3o, p4o);
                    assert p4.y <= p3.y : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
                }
            }
            if (isDown(p3o, p4o)) {
                assert p4.y >= p3.y : p1o + "/" + p2o + "/" + p3o + "/" + p4o;
            }
            if (b1) {
                assert isStraight(p1, p2);
            }
            assert isStraight(p2, p3);
            if (b2) {
                assert isStraight(p3, p4);
            }
        }

    }

    private void correctOverlapingEdges(Point p1, Point p2, Point p3) {
        if (isLeft(p1, p2) && isRight(p2, p3)) {
            p2.x = Math.min(p1.x, p3.x);
        }
        if (isRight(p1, p2) && isLeft(p2, p3)) {
            p2.x = Math.max(p1.x, p3.x);
        }
        if (isUp(p1, p2) && isDown(p2, p3)) {
            p2.y = Math.min(p1.y, p3.y);
        }
        if (isDown(p1, p2) && isUp(p2, p3)) {
            p2.y = Math.max(p1.y, p3.y);
        }
    }

    public static int middle(int a, int b) {
        return (a + b) / 2;
    }

    public static boolean isInBetween(int inBetween, int a, int b) {
        return (a <= inBetween && inBetween <= b) || (a >= inBetween && inBetween >= b);
    }

    private void clearSuperfluousPoints(ProcessEdge edge) {
        int i = 1;
        while (i < edge.getRoutingPoints().size() - 1) {
            if ((isHorizontal(edge.getRoutingPoints().get(i - 1), edge.getRoutingPoints().get(i)) && isHorizontal(edge.getRoutingPoints().get(i), edge.getRoutingPoints().get(i + 1)))
                    || (isVertical(edge.getRoutingPoints().get(i - 1), edge.getRoutingPoints().get(i)) && isVertical(edge.getRoutingPoints().get(i), edge.getRoutingPoints().get(i + 1)))) {
                edge.removeRoutingPoint(i);
            } else {
                i++;
            }
        }

    }

    private boolean isWithinMinDistance(ProcessNode node, Point p) {
        Rectangle rec = new Rectangle(node.getBounds().x - minDistance, node.getBounds().y - minDistance,
                (int) node.getBounds().getWidth() + 2 * minDistance, (int) node.getBounds().getHeight() + 2 * minDistance);
        return rec.contains(p);
    }

    public boolean deltaIntersect(ProcessEdge edge) {
        Rectangle rec1 = new Rectangle((int) (edge.getSource().getBounds().getMinX() - minDistance), (int) (edge.getSource().getBounds().getMinY() - minDistance),
                (int) edge.getSource().getBounds().getWidth() + 2 * minDistance + 2, (int) edge.getSource().getBounds().getHeight() + 2 * minDistance + 2);
        Rectangle rec2 = new Rectangle((int) (edge.getTarget().getBounds().getMinX() - minDistance), (int) (edge.getTarget().getBounds().getMinY() - minDistance),
                (int) edge.getTarget().getBounds().getWidth() + 2 * minDistance + 2, (int) edge.getTarget().getBounds().getHeight() + 2 * minDistance + 2);
        Rectangle sourceBounds = edge.getSource().getBounds();
        Rectangle targetBounds = edge.getTarget().getBounds();
        int maxX1 = (int) sourceBounds.getMaxX();
        int maxX2 = (int) targetBounds.getMaxX();
        int minX1 = (int) sourceBounds.getMinX();
        int minX2 = (int) targetBounds.getMinX();
        int maxY1 = (int) sourceBounds.getMaxY();
        int maxY2 = (int) targetBounds.getMaxY();
        int minY1 = (int) sourceBounds.getMinY();
        int minY2 = (int) targetBounds.getMinY();
        boolean result = false;
        result |= min(abs(maxX1 - minX2), abs(maxX2 - minX1)) <= 2 * minDistance + 2;
        result |= min(abs(maxY2 - minY1), abs(maxY1 - minY2)) <= 2 * minDistance + 2;
        return rec1.intersects(rec2) || rec1.contains(rec2) || rec2.contains(rec1) || rec2.intersects(rec1);
    }

    private int getClosestRPIndex(ProcessNode sourceNode, ProcessEdge edge, boolean source) {
        if (source) {
            return 1;
        } else {
            return edge.getRoutingPoints().size() - 2;
        }
    }

    private void setFirstRoutingPoint(ProcessNode sourceNode, ProcessEdge edge, Point dockingPoint, boolean source) {
        Point firstPoint = edge.getRoutingPoint(getClosestRPIndex(sourceNode, edge, source));
        if (isHorizontalDockingPoint(dockingPoint, sourceNode)) {
            firstPoint.y = dockingPoint.y;
            if (dockingPoint.x > sourceNode.getPos().x) {
                firstPoint.x = (firstPoint.x <= dockingPoint.x) ? dockingPoint.x + minDistance : firstPoint.x;
            } else {
                firstPoint.x = (firstPoint.x >= dockingPoint.x) ? dockingPoint.x - minDistance : firstPoint.x;
            }
        } else {
            firstPoint.x = dockingPoint.x;
            if (dockingPoint.y > sourceNode.getPos().y) {
                firstPoint.y = (firstPoint.y <= dockingPoint.y) ? dockingPoint.y + minDistance : firstPoint.y;
            } else {
                firstPoint.y = (firstPoint.y >= dockingPoint.y) ? dockingPoint.y - minDistance : firstPoint.y;
            }
        }
        edge.moveRoutingPoint(getClosestRPIndex(sourceNode, edge, source), firstPoint);
        assert isStraight(dockingPoint, firstPoint);
        assert endHasRightDirection(dockingPoint, firstPoint, sourceNode, false);
    }

    private boolean isInVerticalKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        int keepout = edge.getRoutingPoints().size() > 4 ? minDistance : 2 * minDistance;
        return isInBetween(p.x, (int) node.getBounds().getMaxX(), (int) node.getBounds().getMaxX() + keepout)
                || isInBetween(p.x, (int) node.getBounds().getMinX(), (int) node.getBounds().getMinX() - keepout);
    }

    private boolean isInHorizontalKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        int keepout = edge.getRoutingPoints().size() > 4 ? minDistance : 2 * minDistance;
        return isInBetween(p.y, (int) node.getBounds().getMaxY(), (int) node.getBounds().getMaxY() + keepout)
                || isInBetween(p.y, (int) node.getBounds().getMinY(), (int) node.getBounds().getMinY() - keepout);
    }

    private boolean isInKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        return isInVerticalKeepOutZone(node, p, edge) || isInHorizontalKeepOutZone(node, p, edge);
    }

    private boolean isInNorthKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        return isInVerticalKeepOutZone(node, p, edge) && node.getPos().y < p.y;
    }

    private boolean isInSouthKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        return isInVerticalKeepOutZone(node, p, edge) && node.getPos().y > p.y;
    }

    private boolean isInEastKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        return isInHorizontalKeepOutZone(node, p, edge) && node.getPos().y < p.y;
    }

    private boolean isInWestKeepOutZone(ProcessNode node, Point p, ProcessEdge edge) {
        return isInHorizontalKeepOutZone(node, p, edge) && node.getPos().y > p.y;
    }

    private void clearKeepOutZone(ProcessEdge edge, ProcessNode node, Point p, boolean isSource) {
        if (isInEastKeepOutZone(node, p, edge)) {
            setEastDockingPoint(edge, isSource);
        } else if (isInWestKeepOutZone(node, p, edge)) {
            setWestDockingPoint(edge, isSource);
        } else if (isInNorthKeepOutZone(node, p, edge)) {
            setNorthDockingPoint(edge, isSource);
        } else if (isInSouthKeepOutZone(node, p, edge)) {
            setSouthDockingPoint(edge, isSource);
        }
    }

    private Point getConnectionPoint(Point target, ProcessNode node) {
        ProcessNode copy = node.copy();
        int w = node.getBounds().getSize().width + 4 * minDistance;
        int h = node.getBounds().getSize().height + 4 * minDistance;
        copy.setSize(max(w, 1), max(h, 1));

        return node.getConnectionPoint(copy.getConnectionPoint(target));
    }

    private Rectangle getKeepOutZone(ProcessNode node) {
        Point pos = node.getTopLeftPos();
        int m = minDistance - 1;
        pos.x -= m;
        pos.y -= m;
        Rectangle keepOutZone = new Rectangle(node.getBounds().getSize().width + 2 * m, node.getBounds().getSize().height + 2 * m);
        keepOutZone.setLocation(pos);
        assert keepOutZone.contains(node.getBounds());
        return keepOutZone;
    }

    private boolean isValidTarget(ProcessNode startNode, Point startDockPoint, Point target) {
        if (isNorthDockingPoint(startDockPoint, startNode)) {
            return startDockPoint.y >= target.y;
        } else if (isSouthDockingPoint(startDockPoint, startNode)) {
            return startDockPoint.y <= target.y;
        } else if (isEastDockingPoint(startDockPoint, startNode)) {
            return startDockPoint.x <= target.x;
        } else if (isWestDockingPoint(startDockPoint, startNode)) {
            return startDockPoint.x >= target.x;
        } else {
            assert false;
            return false;
        }
    }

    private boolean dockingPointsInSight(ProcessNode node1, Point p1, ProcessNode node2, Point p2) {
        return isValidTarget(node1, p1, p2) && isValidTarget(node2, p2, p1);
    }

    private class NodeTupel {

        public Point bestSource = null;
        public Point bestTarget = null;

        public Point getBestSource() {
            return bestSource;
        }

        public void setBestSource(Point bestSource) {
            this.bestSource = bestSource;
        }

        public Point getBestTarget() {
            return bestTarget;
        }

        public void setBestTarget(Point bestTarget) {
            this.bestTarget = bestTarget;
        }
    }

    private NodeTupel getConnectionPointForSourceNode(ProcessEdge edge, ProcessNode source, ProcessNode target) {
        // Search for the connection point that is closest
        NodeTupel result = new NodeTupel();
        if (deltaIntersect(edge)) {
            result.bestSource = source.getConnectionPoint(target.getPos());
            return result;
        }

        ArrayList<Point> sourceDockingPoints = getKeepOutZoneAdjustedDockingPoints(source);
        assert edge.getSource() == source || edge.getTarget() == source;
        assert edge.getSource() == target || edge.getTarget() == target;
        //try existing target Point first
        Point targetDP = edge.getSource() == source ? edge.getRoutingPoint(-1) : edge.getRoutingPoint(0);
        targetDP = adjustDockPoint(targetDP, source);
        ArrayList<Point> filteredSourcePoints = reachableDockingPoints(sourceDockingPoints, source, targetDP, target);
        result.bestSource = getClosestDP(filteredSourcePoints, targetDP);
        if (result.bestSource == null) {
            // no valid line was found!
            //reset both
            Point res = null;
            ArrayList<Point> targetDockingPoints = getKeepOutZoneAdjustedDockingPoints(target);
            double shortestDistance = Double.MAX_VALUE;
            for (Point tar : targetDockingPoints) {
                //dp.translate(source.getPos().x, source.getPos().y);
                filteredSourcePoints = reachableDockingPoints(sourceDockingPoints, source, tar, target);
                res = getClosestDP(filteredSourcePoints, tar);
                if (res != null) {
                    double dis = tar.distance(res);
                    if (dis < shortestDistance) {
                        shortestDistance = dis;
                        result.bestSource = res;
                        result.bestTarget = tar;
                    }
                }
            }
        }
        assert result.bestSource != null || result.bestTarget != null;
        return result;
    }

    private void resetDockingPoint(ProcessEdge edge, Point target, boolean isSource) {
        //assert edge.getSourceDockPointOffset()==null && edge.getTargetDockPointOffset()==null;
        ProcessNode sourceNode = isSource ? edge.getSource() : edge.getTarget();
        ProcessNode otherNode = isSource ? edge.getTarget() : edge.getSource();
        int s = edge.getRoutingPoints().size();
        if (s < 4) {
            assert false;
            return;
        } else if (s == 4) {
            NodeTupel dps = getConnectionPointForSourceNode(edge, sourceNode, otherNode);
            assert dps.bestSource != null || dps.bestTarget != null;
            if (dps.bestSource != null && isSource) {
                edge.setSourceDockPointOffset(getRelativePosition(sourceNode.getPos(), dps.bestSource));
            }
            if (dps.bestTarget != null && isSource) {
                edge.setTargetDockPointOffset(getRelativePosition(otherNode.getPos(), dps.bestTarget));
            }
            if (dps.bestSource != null && !isSource) {
                edge.setTargetDockPointOffset(getRelativePosition(sourceNode.getPos(), dps.bestSource));
            }
            if (dps.bestTarget != null && !isSource) {
                edge.setSourceDockPointOffset(getRelativePosition(otherNode.getPos(), dps.bestTarget));
            }
        } else {
            Point dockingPoint = getConnectionPoint(target, sourceNode);
            if (isSource) {
                edge.setSourceDockPointOffset(getRelativePosition(sourceNode.getPos(), dockingPoint));
            } else {
                edge.setTargetDockPointOffset(getRelativePosition(sourceNode.getPos(), dockingPoint));
            }
        }

    }

    private void resetBothDockingPoints(ProcessEdge edge) {
        resetDockingPoint(edge, edge.getRoutingPoint(3), true);
        resetDockingPoint(edge, edge.getRoutingPoint(-4), false);
    }

    private void forceRepositionDockingPoint(ProcessEdge edge, ProcessNode sourceNode, Point Target) {
        assert edge.getSource() == sourceNode || edge.getTarget() == sourceNode;
        if (edge.getSource() == sourceNode) {
            edge.clearSourceDockPointOffset();
        }
        if (edge.getTarget() == sourceNode) {
            int s = edge.getRoutingPoints().size();
            edge.clearTargetDockPointOffset();
        }
        if (this.iterationsLeft <= 2) {
            edge.clearSourceDockPointOffset();
            edge.clearTargetDockPointOffset();
        }

    }

    private boolean distanceAbsoluteBigger(int a, int b, int common, boolean positive) {
        return (positive) ? a - common >= b - common : common - a >= common - b;
    }

    private boolean setMinX(ProcessNode node, ProcessEdge edge, Point dockingPoint, Point p1, Point p2, Point limit, Point target) {
        assert isStraight(dockingPoint, p1);
        assert isStraight(p1, p2);
        assert isStraight(p2, limit);
        assert deltaContained(node, dockingPoint, minDistance);
        assert isHorizontalDockingPoint(dockingPoint, node);
        assert isEastDockingPoint(dockingPoint, node) || isWestDockingPoint(dockingPoint, node);
        assert edge.getSource() == node || edge.getTarget() == node;
        if (!isHorizontal(dockingPoint, p1)) {            
            return false;
        }
        /* if (limit.x < dockingPoint.x && isEastDockingPoint(dockingPoint, node)) {
        dockingPoint.setLocation(node.getConnectionPoint((int) node.getBounds().getMinX() - minDistance, dockingPoint.y));
        clearDockingPoint(node, edge);
        }
        if (limit.x > dockingPoint.x && isWestDockingPoint(dockingPoint, node)) {
        dockingPoint.setLocation(node.getConnectionPoint((int) node.getBounds().getMaxX() + minDistance, dockingPoint.y));
        clearDockingPoint(node, edge);
        }*/
        int minX = isEastDockingPoint(dockingPoint, node) ? (int) node.getBounds().getMaxX() + minDistance : (int) node.getBounds().getMinX() - minDistance;
        boolean acceptable = isEastDockingPoint(dockingPoint, node) ? minX <= limit.x : minX >= limit.x;
        acceptable &= endHasRightDirection(dockingPoint, p1, node, true);
        if (acceptable) {
            p1.x = minX;
            p2.x = distanceAbsoluteBigger(p1.x, p2.x, dockingPoint.x, isEastDockingPoint(dockingPoint, node)) ? p1.x : p2.x;
            if (deltaContained(node, p1, minDistance)) {
                assert !deltaContained(node, p1, minDistance);
            }
            if (deltaContained(node, p2, minDistance)) {
                assert !deltaContained(node, p2, minDistance);
            }
            if (!isStraight(dockingPoint, p1)) {
                assert isStraight(dockingPoint, p1);
            }
            if (!isStraight(p1, p2)) {
                assert isStraight(p1, p2);
            }
            if (!isStraight(p2, limit)) {
                assert isStraight(p2, limit);
            }
            assert isStraight(dockingPoint, p1);
            assert isStraight(p1, p2);
            assert isStraight(p2, limit);
            assert isHorizontal(dockingPoint, p1);
        } else {
            //System.out.println("No solution posssible. Reseting Docking Point");
        }
        assert isStraight(edge, 0, 0);
        return acceptable;
    }

    private void clearDockingPoint(ProcessNode node, ProcessEdge edge) {
        if (edge.getSource() == node) {
            edge.clearSourceDockPointOffset();
        } else if (edge.getTarget() == node) {
            edge.clearTargetDockPointOffset();
        } else {
            edge.clearSourceDockPointOffset();
            edge.clearTargetDockPointOffset();
        }
    }

    private boolean setMinY(ProcessNode node, ProcessEdge edge, Point dockingPoint, Point p1, Point p2, Point limit, Point target) {
        assert isStraight(dockingPoint, p1);
        assert isStraight(p1, p2);
        assert isStraight(p2, limit);
        assert deltaContained(node, dockingPoint, minDistance);
        assert isVerticalDockingPoint(dockingPoint, node);
        assert isNorthDockingPoint(dockingPoint, node) || isSouthDockingPoint(dockingPoint, node);
        assert edge.getSource() == node || edge.getTarget() == node;
        if (!isVertical(dockingPoint, p1)) {            
            return false;
        }
        Point p1b = new Point(p1);
        Point p2b = new Point(p2);
        int minY = isSouthDockingPoint(dockingPoint, node) ? (int) node.getBounds().getMaxY() + minDistance : (int) node.getBounds().getMinY() - minDistance;
        boolean acceptable = isSouthDockingPoint(dockingPoint, node) ? minY + minDistance <= limit.y : minY - minDistance >= limit.y;
        acceptable &= endHasRightDirection(dockingPoint, p1, node, true);
        if (acceptable) {
            assert isStraight(p1, p2) : p1 + " / " + p2;
            p1.y = minY;
            p2.y = distanceAbsoluteBigger(p1.y, p2.y, dockingPoint.y, isSouthDockingPoint(dockingPoint, node)) ? p1.y : p2.y;
            if (deltaContained(node, p1, minDistance)) {
                assert !deltaContained(node, p1, minDistance);
            }
            if (deltaContained(node, p2, minDistance)) {
                assert !deltaContained(node, p2, minDistance);
            }
            if (!isStraight(dockingPoint, p1)) {
                assert isStraight(dockingPoint, p1);
            }
            if (!isStraight(p1, p2)) {
                assert isStraight(p1, p2) : p1 + " / " + p2;
            }
            if (!isStraight(p2, limit)) {
                assert isStraight(p2, limit);
            }
            assert isVertical(dockingPoint, p1);
        } else {
            //System.out.println("No solution posssible. Reseting Docking Point");
        }
        assert isStraight(edge, 0, 0);
        return acceptable;
    }

    private boolean setMinDistance(ProcessNode node, ProcessEdge edge, Point dockingPoint, Point p1, Point p2, Point limit, Point target) {
        assert isStraight(edge, 0, 0);
        if (isHorizontalDockingPoint(dockingPoint, node)) {
            return setMinX(node, edge, dockingPoint, p1, p2, limit, target);
        } else {
            return setMinY(node, edge, dockingPoint, p1, p2, limit, target);
        }
    }

    private void getBorderPoints(ProcessEdge edge, boolean source, Point p1, Point p2, Point p3, Point p4) {
        assert edge.getRoutingPoints().size() >= 4 : edge.getRoutingPoints().size();
        if (source) {
            p1.setLocation(edge.getRoutingPoints().get(0));
            p2.setLocation(edge.getRoutingPoints().get(1));
            p3.setLocation(edge.getRoutingPoints().get(2));
            p4.setLocation(edge.getRoutingPoints().get(3));
        } else {
            int s = edge.getRoutingPoints().size();
            p4.setLocation(edge.getRoutingPoints().get(s - 4));
            p3.setLocation(edge.getRoutingPoints().get(s - 3));
            p2.setLocation(edge.getRoutingPoints().get(s - 2));
            p1.setLocation(edge.getRoutingPoints().get(s - 1));
        }
    }

    private boolean ensureMinDistance(ProcessEdge edge, boolean source, ProcessNode updatedNode) {
        if (deltaIntersect(edge)) {
            return false;
        }
        if (edge.getRoutingPoints().size() < 4) {
            return false;
        }
        Point p1 = new Point();
        Point p2 = new Point();
        Point p3 = new Point();
        Point p4 = new Point();
        getBorderPoints(edge, source, p1, p2, p3, p4);
        assert isStraight(p1, p2);
        assert isStraight(p2, p3);
        assert isStraight(p3, p4);
        ProcessNode node = source ? edge.getSource() : edge.getTarget();
        if (deltaContained(node, p2, minDistance) || deltaContained(node, p3, minDistance)) {
            boolean acceptable = setMinDistance(node, edge, p1, p2, p3, p4, p4);
            int s = edge.getRoutingPoints().size();
            edge.moveRoutingPoint(source ? 1 : s - 2, p2);
            edge.moveRoutingPoint(source ? 2 : s - 3, p3);
            if (acceptable) {
                assert !deltaContained(node, p2, minDistance);
                assert !deltaContained(node, p3, minDistance);
            } else {
                //if ((source ? edge.getSourceDockPointOffset() : edge.getTargetDockPointOffset()) != null) {
                if (this.iterationsLeft > 0) {
                    this.iterationsLeft--;
                    //System.out.println("Iteration: " + this.iterationsLeft);
                    forceRepositionDockingPoint(edge, node, p4);
                    this.layoutRoutingPoints(edge, updatedNode);
                } else {
                    if (!deltaIntersect(edge)) {
                        System.out.println("Failed routing a rectified edge from source to target node: " + TestUtils.getLayoutDebugMessage(edge));
                        assert isCorrectlyLayouted(edge) : TestUtils.getLayoutDebugMessage(edge);
                        if (!isCorrectlyLayouted(edge)) {
                            isCorrectlyLayouted(edge);
                            //forceRepositionDockingPoint(edge, node, p4);
                            //optimizeRoutingPoints(edge, updatedNode);
                        }
                    }
                }
                //}
            }
            getBorderPoints(edge, source, p1, p2, p3, p4);
            assert isStraight(p1, p2);
            assert isStraight(p2, p3);
            assert isStraight(p3, p4);
            return acceptable;
        }
        return true;
    }

    private boolean isAboutMiddle(int p1, int p2, int p3) {
        int exactMiddle = middle(p1, p3);
        return exactMiddle - minDistance < p2 && p2 < exactMiddle + minDistance;
    }

    private void ensureRoutingPointOrder(Point p1, Point p2, Point p3) {
        //p2 is ensured
        if (isHorizontal(p1, p2) && isHorizontal(p2, p3) && !isInBetween(p2.x, p1.x, p3.x)) {
            p2.x = middle(p1.x, p3.x);
        }
        if (isVertical(p1, p2) && isVertical(p2, p3) && !isInBetween(p2.y, p1.y, p3.y)) {
            p2.y = middle(p1.y, p3.y);
        }
    }

    public static boolean isLeft(Point p1, Point p2) {
        return isHorizontal(p1, p2) && p1.x >= p2.x;


    }

    public static boolean isRight(Point p1, Point p2) {
        return isHorizontal(p1, p2) && p1.x <= p2.x;


    }

    public static boolean isUp(Point p1, Point p2) {
        return isVertical(p1, p2) && p1.y >= p2.y;


    }

    public static boolean isDown(Point p1, Point p2) {
        return isVertical(p1, p2) && p1.y <= p2.y;

    }

    public void optimizeAllEdges(ProcessNode changedNode) {
        for (ProcessModel context : changedNode.getContexts()) {
            for (ProcessEdge edge : context.getEdges()) {
                if (edge.getSource() == changedNode || edge.getTarget() == changedNode) {
                    this.optimizeRoutingPoints(edge, changedNode);
                }
            }
        }
    }

    public void setSouthDockingPoint(ProcessEdge edge, boolean isSource) {
        ProcessNode node = isSource ? edge.getSource() : edge.getTarget();
        Point pos = node.getPos();
        Point target = new Point(node.getPos());
        target.y = (int) (node.getBounds().getMaxY() + minDistance);
        target = node.getConnectionPoint(target);
        if (isSource) {
            edge.setSourceDockPointOffset(getRelativePosition(pos, target));
        } else {
            edge.setTargetDockPointOffset(getRelativePosition(pos, target));
        }
    }

    public void setNorthDockingPoint(ProcessEdge edge, boolean isSource) {
        ProcessNode node = isSource ? edge.getSource() : edge.getTarget();
        Point pos = node.getPos();
        Point target = new Point(node.getPos());
        target.y = (int) (node.getBounds().getMinY() - minDistance);
        target = node.getConnectionPoint(target);
        if (isSource) {
            edge.setSourceDockPointOffset(getRelativePosition(pos, target));
        } else {
            edge.setTargetDockPointOffset(getRelativePosition(pos, target));
        }
    }

    public void setWestDockingPoint(ProcessEdge edge, boolean isSource) {
        ProcessNode node = isSource ? edge.getSource() : edge.getTarget();
        Point pos = node.getPos();
        Point target = new Point(node.getPos());
        target.x = (int) (node.getBounds().getMinX() - minDistance);
        target = node.getConnectionPoint(target);
        if (isSource) {
            edge.setSourceDockPointOffset(getRelativePosition(pos, target));
        } else {
            edge.setTargetDockPointOffset(getRelativePosition(pos, target));
        }
    }

    public void setEastDockingPoint(ProcessEdge edge, boolean isSource) {
        ProcessNode node = isSource ? edge.getSource() : edge.getTarget();
        Point pos = node.getPos();
        Point target = new Point(node.getPos());
        target.x = (int) (node.getBounds().getMaxX() + minDistance);
        target = node.getConnectionPoint(target);
        if (isSource) {
            edge.setSourceDockPointOffset(getRelativePosition(pos, target));
        } else {
            edge.setTargetDockPointOffset(getRelativePosition(pos, target));
        }
    }

    //Listeners
    @Override
    public void processNodeAdded(ProcessNode newNode) {
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
        if (edge != null && edge.getSource() != null && edge.getTarget() != null) {
            this.optimizeRoutingPoints(edge, null);
        }
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        if (obj instanceof ProcessNode && name != null) {
            if (name.equals(ProcessNode.PROP_XPOS) || name.equals(ProcessNode.PROP_YPOS)
                    || name.equals(ProcessNode.PROP_WIDTH) || name.equals(ProcessNode.PROP_HEIGHT)) {
                ProcessNode n = (ProcessNode) obj;
                optimizeAllEdges(n);
            }
        }
    }

    @Override
    public boolean isCorrectlyLayouted(ProcessEdge edge) {
        if (deltaIntersect(edge)) {
            return true;
        }
        return endDirectionsCorrect(edge, true) && nodesEmpty(edge) && isStraight(edge, 0, 0);
    }
}
