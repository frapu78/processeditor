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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.helper.LabelHelper;
import org.w3c.dom.Node;

/**
 *
 * This class provides an abstract representation of a process edge.
 * 
 * @author fpu
 */
public abstract class ProcessEdge extends ProcessObject {

    /**
     * Serialization properties
     */
    public final static String TAG_EDGE = "edge";
    public final static String PROP_SOURCENODE = "#sourceNode";
    public final static String PROP_TARGETNODE = "#targetNode";
    /** The list of intermediate routing points in format x1,y1+x2,y2+x3,y3 */
    public final static String PROP_POINTS = "points";
    /** The color of the arc in jawa.awt.Color */
    public final static String PROP_COLOR_ARC = "color_arc";
    /** The label of the edge */
    public final static String PROP_LABEL = "label";
    /** The offset of the label from the source (in percent as double) */
    public final static String PROP_LABELOFFSET = "#label_offset";
    /** The source docking point (offset from source node) */
    public final static String PROP_SOURCE_DOCKPOINT = "#source_dockpoint";
    /** The target docking point (offset from target node) */
    public final static String PROP_TARGET_DOCKPOINT = "#target_dockpoint";
    private final static int BRIDGE_SIZE = 8;
    private ProcessNode source;
    private ProcessNode target;    
    /** Used for caching the source and target arrows */
    private double sourceArrowAngle = Double.POSITIVE_INFINITY;
    private double targetArrowAngle = Double.POSITIVE_INFINITY;
    private int sourceArrowX = Integer.MAX_VALUE;
    private int sourceArrowY = Integer.MAX_VALUE;
    private int targetArrowX = Integer.MAX_VALUE;
    private int targetArrowY = Integer.MAX_VALUE;
    private Shape sourceArrow = null;
    private Shape targetArrow = null;
    protected transient List<Point> routingPointCache = null;
    
    /** A Helper Object which takes the label of this edge 
     * we do not have to care about this helper, after the edge was drawn,
     * the corresponding ProcessEditor adds it to his Helper list
     */
    private LabelHelper label = new LabelHelper(this);

    public ProcessEdge() {
        this(null, null);
        initializeProperties();
    }

    public ProcessEdge(ProcessNode source, ProcessNode target) {
        super();
        setSource(source);
        setTarget(target);
        initializeProperties();
    }

    public static ProcessEdge newInstanceFromSerialization(Node XMLnode, HashMap<String, ProcessNode> nodesInModel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            // Create flows according to type
            HashMap<String, String> props = ProcessUtils.readProperties(xpath, XMLnode);
            // Try to instantiate corresponding process edge
            Object o = Class.forName(props.get(ProcessObject.PROP_CLASS_TYPE)).newInstance();
            if (!(o instanceof ProcessEdge)) {
                throw new Exception("Illegal node found!");
            }
            ProcessEdge pn = (ProcessEdge) o;
            // Add properties to node
            for (String key : props.keySet()) {
                pn.setProperty(key, props.get(key));
            }
            // Set source and target
            pn.setSource(nodesInModel.get(props.get(ProcessEdge.PROP_SOURCENODE)));
            pn.setTarget(nodesInModel.get(props.get(ProcessEdge.PROP_TARGETNODE)));
            return pn;
    }

    private void initializeProperties() {
        setProperty(PROP_SOURCE_DOCKPOINT, "");
        setProperty(PROP_TARGET_DOCKPOINT, "");
        setProperty(PROP_POINTS, "");
        setProperty(PROP_COLOR_ARC, "" + Color.BLACK.getRGB());
        setPropertyEditor(PROP_COLOR_ARC, new ColorPropertyEditor());
        setProperty(PROP_LABEL, "");
        setProperty(PROP_LABELOFFSET, "0.5");
    }

    public ProcessNode getSource() {
        return source;
    }

    public void setSource(ProcessNode source) {
        if (source != null) {
            setProperty(PROP_SOURCENODE, source.getId());
        }
        this.source = source;
    }

    public ProcessNode getTarget() {
        return target;
    }

    public void setTarget(ProcessNode target) {
        if (target != null) {
            setProperty(PROP_TARGETNODE, target.getId());
        }
        this.target = target;
    }

    public void setLabel(String label) {
        setProperty(PROP_LABEL, label);
    }

    public String getLabel() {
        return getProperty(PROP_LABEL);
    }
    

	 /**
    * Returns the center position of the label.
    * @return
    */
   public Point getLabelPosition() {
       double labelOffset = 0.5;
       try {
           labelOffset = getLabelOffset();
       } catch (Exception ex) {
       }
       // Calculate absolute value
       List<Point> rp = getRoutingPoints();
       int offsetPixel = (int) (labelOffset * ProcessEditorMath.getLineSequenceLength(rp));
       return ProcessEditorMath.getPointOnLineSequenceFromOffset(rp, offsetPixel, null);
   }

	public double getLabelOffset() {
		return Double.parseDouble(getProperty(ProcessEdge.PROP_LABELOFFSET));
	}
	
	public void setLabelOffset(double offset) {
		if(offset > 1.0) {
			offset = 1.0;
		}else if(offset < 0.0) {
			offset = 0.0;
		}
		setProperty(ProcessEdge.PROP_LABELOFFSET, Double.toString(offset));
	}

    /**
     * Sets the Color of the Edge.
     * @param color
     */
    public void setColor(Color color) {
        setProperty(PROP_COLOR_ARC, "" + color.getRGB());
    }

    /**
     * Returns the Color of the Edge.
     * @return
     */
    public Color getColor() {
        Color color = Color.BLACK;
        try {
            color = new Color(Integer.parseInt(getProperty(PROP_COLOR_ARC)));
        } catch (Exception e) {
        }
        return color;
    }

    /**
     * Returns if this edge supports docking (might be overwritten by
     * subclasses).
     * @return
     */
    protected boolean isDockingSupported() {
        return false;
    }

    /**
     * Adds a routing point (detects the position automatically).
     * @param p
     */
    public void addRoutingPoint(Point p) {
        int distance = Integer.MAX_VALUE;
        int currPos = 0;
        int pos = 0;
        List<Point> points = getRoutingPoints();

        for (int i = 0; i < points.size() - 1; i++) {
            // Calculate distance to line
            int currDist = (int) Line2D.ptLineDist(points.get(i).x, points.get(i).y,
                    points.get(i + 1).x, points.get(i + 1).y,
                    p.x, p.y);
            // Save if closer than recent distance
            if (currDist < distance) {
                distance = currDist;
                currPos = pos;
            }
            pos++;
        }
        addRoutingPoint(currPos, p);
    }

    /**
     * Adds a new routing point at a specific location (0=first index)
     * @param p
     * @param pos
     */
    public synchronized void addRoutingPoint(int pos, Point p) {
        String points = "";
        // Tokenize points
        StringTokenizer st = new StringTokenizer(getProperty(PROP_POINTS), "+");
        int ipos = 0;
        while (st.hasMoreTokens()) {
            if (ipos == pos) {
                points += "" + p.x + "," + p.y + "+";
            }
            points += st.nextToken() + "+";
            ipos++;
        }
        if (pos >= ipos) {
            points += "" + p.x + "," + p.y + "+";
        }
        points = points.substring(0, points.length() - 1);
        // Set changed property
        setProperty(PROP_POINTS, points);
        // Update cache
        updateCache();
    }

    /** 
     * sets the coordiantes of the point with index pos to the position p.
     * 
     * @param pos
     * @param p
     */
    public void moveRoutingPoint(int pos, Point p) {
        String points = "";
        // Tokenize points
        StringTokenizer st = new StringTokenizer(getProperty(PROP_POINTS), "+");
        // Check if initial point
        if (pos == 0) {
            // Set property
            Point xp = source.getPos();
            xp.x = p.x - xp.x;
            xp.y = p.y - xp.y;
            // Keep only if inside source node
            if (source instanceof ProcessNode) {
                // ProcessNode node = (ProcessNode)source;
                if (source.getOutlineShape() != null) {
                    if (source.contains(p)) {
                        setProperty(PROP_SOURCE_DOCKPOINT, xp.x + "," + xp.y);
                    } else {
                        //xp = getCorrectedDockingPoint(xp, source);
                        //setProperty(PROP_SOURCE_DOCKPOINT, xp.x + "," + xp.y);
                    }
                }
            }
        }

        // Check if final point
        if (pos > st.countTokens()) {
            // Set property
            Point xp = target.getPos();
            xp.x = p.x - xp.x;
            xp.y = p.y - xp.y;
            // Keep only if inside target node
            if (target instanceof ProcessNode) {
                // ProcessNode node = (ProcessNode)target;
                if (target.getOutlineShape() != null) {
                    if (target.contains(p)) {
                        setProperty(PROP_TARGET_DOCKPOINT, xp.x + "," + xp.y);
                    } else {
                        //xp = getCorrectedDockingPoint(xp, target);
                        //setProperty(PROP_TARGET_DOCKPOINT, xp.x + "," + xp.y);
                    }
                }
            }
        }

        int ipos = 1;
        while (st.hasMoreTokens()) {
            if (ipos == pos) {
                points += "" + p.x + "," + p.y + "+";
                // Ignore next token
                st.nextToken();
            } else {
                points += st.nextToken() + "+";
            }
            ipos++;
        }
        if (!points.isEmpty()) {
            points = points.substring(0, points.length() - 1);
        }
        // Set changed property
        setProperty(PROP_POINTS, points);
        // Update cache
        updateCache();
    }

    /**
     * removes all routing points of that edge
     */
    public void clearRoutingPoints() {
        setProperty(PROP_POINTS, "");
        updateCache();
    }

    /**
     * Removes a routing point at a specific location (0=first index)
     * @param p
     * @param pos
     */
    public void removeRoutingPoint(int pos) {
        String points = "";

        // Tokenize points
        StringTokenizer st = new StringTokenizer(getProperty(PROP_POINTS), "+");
        int ipos = 0;
        while (st.hasMoreTokens()) {
            if (ipos != pos-1) {
                points += st.nextToken() + "+";
            } else {
                st.nextToken();
            }
            ipos++;
        }
        if (points.length() > 0) {
            points = points.substring(0, points.length() - 1);
        }
        // Set changed property
        setProperty(PROP_POINTS, points);
        // Update cache
        updateCache();
    }

    /**
     * Returns the list of Points the edge goes through. The initial and
     * final Points are always contained (gathered from the source and target
     * nodes via ProcessNode.getConnectionPoint().
     */
    public synchronized List<Point> getRoutingPoints() {
        // Always update cache, since we do not know (yet) whether the source
        // or target node has been moved.
        updateCache();
        return routingPointCache;
    }

    /**
     * Returns the routing Point with the given Index. Negative Numbers can be used to count from the end. -1 Is the Index of the last element.
     * @param index
     * @return getRoutingPoints().get(index);
     */
    public synchronized Point getRoutingPoint(int index) {
        if (index >= 0) {
            return getRoutingPoints().get(index);
        } else {
            return getRoutingPoints().get(getRoutingPoints().size() + index);
        }
    }

    /**
     * Returns a list of Shapes representing the routing points.
     * @return
     */
    public List<Shape> getRoutingPointShapes() {
        List<Shape> result = new LinkedList<Shape>();

        for (Point p : getRoutingPoints()) {
            Shape s = new Ellipse2D.Double(p.x - 5, p.y - 5, 10, 10);
            result.add(s);
        }

        return result;
    }

    protected synchronized void updateCache() {
        routingPointCache = new LinkedList<Point>();

        // Parse all points property
        StringTokenizer st = new StringTokenizer(getProperty(PROP_POINTS), "+");
        while (st.hasMoreTokens()) {
            String pos = st.nextToken();
            try {
                StringTokenizer st1 = new StringTokenizer(pos, ",");
                int x = Integer.parseInt(st1.nextToken());
                int y = Integer.parseInt(st1.nextToken());
                routingPointCache.add(new Point(x, y));
            } catch (Exception e) {
            }
        }

        if (target != null && source != null) {
            Point sourcePosition = this.source.getPos();
            Point targetPosition = this.target.getPos();
            Point targetPos = new Point(targetPosition);
            Point sourcePos = new Point(sourcePosition);
            Point initialPoint;
            Point finalPoint;

            if (routingPointCache.size() > 0) {
                //if we have a routing point, only this point has to be considered
                targetPos = routingPointCache.get(0).getLocation();
                sourcePos = routingPointCache.get(routingPointCache.size() - 1).getLocation();
                initialPoint = this.source.getConnectionPoint(targetPos);
                finalPoint = this.target.getConnectionPoint(sourcePos);
            } else {
                // if it is the default connection line, all connection points of the source
                // have to be considered at the target
                initialPoint = this.getSource().getConnectionPoint(this.target.getDefaultConnectionPoints(), targetPos);
                finalPoint = this.getTarget().getConnectionPoint(initialPoint);
            }

            // Check if source dockpoint is defined
            if (!getProperty(PROP_SOURCE_DOCKPOINT).isEmpty()) {
                initialPoint = getSourceDockPointOffset();
                initialPoint.translate(sourcePosition.x, sourcePosition.y);
            }

            // Check if target dockpoint is defined
            if (!getProperty(PROP_TARGET_DOCKPOINT).isEmpty()) {
                finalPoint = getTargetDockPointOffset();
                finalPoint.translate(targetPosition.x, targetPosition.y);
            }

            routingPointCache.add(0, initialPoint);
            routingPointCache.add(finalPoint);
        }
    }

    public abstract Shape getSourceShape();

    public abstract Shape getTargetShape();

    public abstract Stroke getLineStroke();

    /**
     * Draws the edge.
     * @param g
     */
    protected void paint(Graphics g) {
        paintInternal(g);
        // Check if selected
        if (isSelected()) {
            // Draw selection
            paintSelection(g);
        }
    }

    /**
     * Paints the selection around the edge (if selected). Might be
     * implemented by a subclass.
     * @param g
     */
    protected void paintSelection(Graphics g) {
        // Draw all points as circles
        g.setColor(Color.blue);

        for (Point p : getRoutingPoints()) {
            ProcessUtils.drawSelectionPoint(g, p);
        }
    }

    /**
     * Paints the flow object on the given graphics. Might be overridden
     * by a subclass for special purposes.
     * @param g
     */
    protected synchronized void paintInternal(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getColor());
        g2.setStroke(getLineStroke());

        // Draw highlight
        if (isHighlighted()) {
            g2.setColor(Color.RED);
            g2.setStroke(ProcessUtils.selectionStroke);
        }

        List<Point> rp = this.getRoutingPoints();

        // Curved edges are disabled by default
        if (false) {
            List<Point> newRP = new LinkedList<Point>();

            // Prepare lines between all points
            for (int i = 0; i < rp.size() - 1; i++) {
                Point sourceP = rp.get(i);
                Point targetP = rp.get(i + 1);
                // Shorten line (move points) if length > 10
                double length = ProcessEditorMath.getLineLength(sourceP, targetP);
                if (length > BRIDGE_SIZE) {
                    if (i > 0) {
                        sourceP = ProcessEditorMath.getPointOnLineSequenceFromOffset(rp.subList(i, i + 2), BRIDGE_SIZE / 2, null);
                    }
                    if (i < rp.size() - 2) {
                        targetP = ProcessEditorMath.getPointOnLineSequenceFromOffset(rp.subList(i, i + 2), (int) (length - BRIDGE_SIZE), null);
                    }
                } else {
                    // Merge source and target points
                    targetP = ProcessEditorMath.getPointOnLineSequenceFromOffset(rp.subList(i, i + 2), (int) (length / 2), null);
                    sourceP = targetP;
                }

                newRP.add(sourceP);
                newRP.add(targetP);
            }

            // Draw lines
            for (int i = 0; i < newRP.size() - 1; i += 2) {
                g2.drawLine(newRP.get(i).x, newRP.get(i).y, newRP.get(i + 1).x, newRP.get(i + 1).y);
            }

            // Draw arcs
            for (int i = 1; i < newRP.size() - 2; i += 2) {
                Point p1 = newRP.get(i);
                Point p2 = newRP.get(i + 1);

                Point p3 = newRP.get(i - 1);
                Point p4 = newRP.get(i + 2);

                boolean dir = true;
                // NE right
                if (p3.x < p2.x & p4.y < p2.y) {
                    dir = false;
                }
                // SW left
                if (p3.x > p2.x & p4.y < p2.y) {
                    dir = false;
                }
                // SW right
                if (p3.x > p2.x & p4.y > p2.y) {
                    dir = false;
                }
                // Se left
                if (p3.x < p2.x & p4.y > p2.y) {
                    dir = false;
                }

                g2.draw(getCurve(p1, p2, dir));
            }
        } else {
            // Draw lines
            for (int i = 0; i < rp.size() - 1; i += 1) {
                g2.drawLine(rp.get(i).x, rp.get(i).y, rp.get(i + 1).x, rp.get(i + 1).y);
            }
        }
        // Draw the source arrow
        drawSourceArrow(g2);
        // Draw the target arrow
        drawTargetArrow(g2);

        //label.paint(g2); he should not paint his own label, should be done by PE 
    }

    protected QuadCurve2D getCurve(Point p1, Point p2, boolean direction) {
        //List<Point> rp = this.getRoutingPoints();
        return new QuadCurve2D.Double(
                p1.x, p1.y,
                direction ? p1.x : p2.x, direction ? p2.y : p1.y,
                p2.x, p2.y);
    }

    /**
     * This method draws the source arrow. Might be overwriten.
     * @param g
     */
    protected void drawSourceArrow(Graphics2D g2) {
        List<Point> rp = this.getRoutingPoints();
        // Draw source shape if existing
        if (getSourceShape() != null) {
            int x1 = rp.get(0).x;
            int y1 = rp.get(0).y;
            int x2 = rp.get(1).x;
            int y2 = rp.get(1).y;

            // Calculate degree
            double alpha = Math.PI / 2;
            if (y2 < y1) {
                alpha = -alpha;
            }
            if ((x2 - x1) != 0) {
                alpha = Math.atan(((double) (y2 - y1)) / ((double) (x2 - x1)));
            }
            if (x2 < x1) {
                alpha += Math.PI;
            }

            // Update arrow cache if required
            if ((alpha != sourceArrowAngle) || (x1 != sourceArrowX) || (y1 != sourceArrowY)) {
                AffineTransform tx = new AffineTransform();
                tx.translate(x1, y1);
                tx.rotate(alpha);
                sourceArrow = tx.createTransformedShape(getSourceShape());
                sourceArrowAngle = alpha;
                sourceArrowX = x1;
                sourceArrowY = y1;
            }

            // Check if outline required
            if (isOutlineSourceArrow()) {
                g2.setColor(Color.white);
                g2.fill(sourceArrow);
                g2.setStroke(ProcessUtils.defaultStroke);
                Color arcColor = Color.BLACK;
                try {
                    arcColor = new Color(Integer.parseInt(getProperty(PROP_COLOR_ARC)));
                } catch (Exception e) {
                }
                g2.setColor(arcColor);
                g2.draw(sourceArrow);
            } else {
                g2.fill(sourceArrow);
            }

        }
    }

    /**
     * This method draws the target arrow. Might be overwriten.
     * @param g
     */
    protected void drawTargetArrow(Graphics2D g2) {
        List<Point> rp = this.getRoutingPoints();
        // Draw source shape if existing
        if (getTargetShape() != null) {
            int x1 = rp.get(rp.size() - 2).x;
            int y1 = rp.get(rp.size() - 2).y;
            int x2 = rp.get(rp.size() - 1).x;
            int y2 = rp.get(rp.size() - 1).y;

            // Calculate degree
            double alpha = Math.PI / 2;
            if (y2 < y1) {
                alpha = -alpha;
            }
            if ((x2 - x1) != 0) {
                alpha = Math.atan(((double) (y2 - y1)) / ((double) (x2 - x1)));
            }
            if (x2 < x1) {
                alpha += Math.PI;
            }

            // Update arrow cache if required
            if ((alpha != targetArrowAngle) || (x2 != targetArrowX) || (y2 != targetArrowY)) {
                AffineTransform tx = new AffineTransform();
                tx.translate(x2, y2);
                tx.rotate(alpha);
                targetArrow = tx.createTransformedShape(getTargetShape());
                targetArrowAngle = alpha;
                targetArrowX = x2;
                targetArrowY = y2;
            }

            // Check if outline required
            if (isOutlineTargetArrow()) {
                g2.setColor(Color.white);
                g2.fill(targetArrow);
                g2.setStroke(ProcessUtils.defaultStroke);
                Color arcColor = Color.BLACK;
                try {
                    arcColor = new Color(Integer.parseInt(getProperty(PROP_COLOR_ARC)));
                } catch (Exception e) {
                }
                g2.setColor(arcColor);
                g2.draw(targetArrow);
            } else {
                g2.fill(targetArrow);
            }
        }
    }

    /**
     * Returns the closest distance of a certain point to the lines of this
     * edge.
     * @param p
     * @return
     */
    public int distanceToEdge(Point p) {
        // Iterate over all edges and return the closest distance
        int distance = Integer.MAX_VALUE;
        List<Point> points = getRoutingPoints();

        for (int i = 0; i < points.size() - 1; i++) {
            // Calculate distance to line
            int currDist = (int) Line2D.ptSegDist(points.get(i).x, points.get(i).y,
                    points.get(i + 1).x, points.get(i + 1).y,
                    p.x, p.y);
            // Save if closer than recent distance
            if (currDist < distance) {
                distance = currDist;
            }
        }
        // Return closest distance
        return distance;
    }

    private Point findContainedPoint(Point start, ProcessNode node, int addX, int addY) {
        Point run = new Point(start);
        if (addX != 0 && addY != 0) {
            throw new IllegalArgumentException("One Argument must not be zero");
        }
        assert addX != 0 || addY != 0;
        assert node.getBounds().contains(start) : start;
        assert !node.containsDeepInside(start) : start;
        int i = 10000;
        while (!node.contains(run)) {
            if (!node.getBounds().contains(run)) {
                if (addX == 0) {
                    run.x = (run.x > node.getPos().x) ? run.x-- : run.x++;
                    run.y = start.y; // +1 should avoid infinite loops in corners
                } else if (addY == 0) {
                    run.y = (run.y > node.getPos().y) ? run.y-- : run.y++;
                    run.x = start.x; // +1 should avoid infinite loops in corners
                }
            }
            i--;
            run.x += addX;
            run.y += addY;
            if (i == 0) {
                assert false;
                return node.getPos();
            }
        }
        return run;
    }

    private void movePointInsideNodeBounds(Point abs, ProcessNode node) {
       Rectangle bounds = node.getBounds();
       if (abs.x >= bounds.getMaxX()) {
            abs.x = (int) bounds.getMaxX();
        }
        if (abs.x <= bounds.getMinX()) {
            abs.x = (int) bounds.getMinX();
        }
        if (abs.y >= bounds.getMaxY()) {
            abs.y = (int) bounds.getMaxY();
        }
        if (abs.y <= bounds.getMinY()) {
            abs.y = (int) bounds.getMinY();
        }

        //correct the one pixel error in most shapes

        if (!bounds.contains(abs) && abs.x == bounds.getMaxX()) {
            abs.x--;
        }
        if (!bounds.contains(abs) && abs.x == bounds.getMinX()) {
            abs.x++;
        }
        if (!bounds.contains(abs) && abs.y == bounds.getMaxY()) {
            abs.y--;
        }
        if (!bounds.contains(abs) && abs.y == bounds.getMinY()) {
            abs.y++;
        }
       assert node.getBounds().contains(abs) : abs;
    }

    Point correctedDockPointOffset(Point p, ProcessNode node) {
        Point abs = new Point(p);
        abs.translate(node.getPos().x, node.getPos().y);
        boolean contained = node.contains(abs);
        boolean deepContained = node.containsDeepInside(abs);
        if (contained && !deepContained) {
            return p;
        }       
        if (DefaultRoutingPointLayouter.isEastDockingPoint(abs, node)) {
            if (deepContained) {
                abs.x = (int) node.getBounds().getMaxX();
            }
            movePointInsideNodeBounds(abs, node);
            abs = findContainedPoint(abs, node, -1, 0);
        } else if (DefaultRoutingPointLayouter.isWestDockingPoint(abs, node)) {
            if (deepContained) {
                abs.x = (int) node.getBounds().getMinX();
            }
            movePointInsideNodeBounds(abs, node);
            abs = findContainedPoint(abs, node, 1, 0);
        } else if (DefaultRoutingPointLayouter.isNorthDockingPoint(abs, node)) {
            if (deepContained) {
                abs.y = (int) node.getBounds().getMinY();
            }
            movePointInsideNodeBounds(abs, node);
            abs = findContainedPoint(abs, node, 0, 1);
        } else if (DefaultRoutingPointLayouter.isSouthDockingPoint(abs, node)) {
            if (deepContained) {
                abs.y = (int) node.getBounds().getMaxY();
            }
            movePointInsideNodeBounds(abs, node);
            abs = findContainedPoint(abs, node, 0, -1);
        }
        return DefaultRoutingPointLayouter.getRelativePosition(node.getPos(), abs);
    }

    /**
     * Sets the relative dock point for the source node, based on the source
     * node's position.
     * @param p
     */
    public void setSourceDockPointOffset(Point p) {
        if (p == null) {
            return;
        }
        Point oldValue = getSourceDockPointOffset();
        Point corrected = correctedDockPointOffset(p, getSource());
        if (corrected.equals(oldValue)) {
            return;
        }
        setProperty(PROP_SOURCE_DOCKPOINT, corrected.x + "," + corrected.y);
    }

    /**
     * Sets source or target dock point offset depending on forSource.
     * @param p
     * @param forSource
     */
    public void setDockPointOffset(Point p, boolean forSource) {
        if (forSource) {
            setSourceDockPointOffset(p);
        } else {
            setTargetDockPointOffset(p);
        }
    }

    /**
     * Gets the relative dock point for the source node, based on the source
     * node's position. If it was not specified, null will be returned.
     * @param p
     */
    public Point getSourceDockPointOffset() {
        String prop = getProperty(PROP_SOURCE_DOCKPOINT);
        return toPoint(prop);
    }

    /**
     * resets the target docking point offset and returns to the usage 
     * of automatic determination
     */
    public void clearSourceDockPointOffset() {
        setProperty(PROP_SOURCE_DOCKPOINT, "");
    }

    /**
     * Sets the relative dock point for the target node, based on the target
     * node's position.
     * @param p
     */
    public void setTargetDockPointOffset(Point p) {
        if (p == null) {
            return;
        }
        Point oldValue = getTargetDockPointOffset();
        Point corrected = correctedDockPointOffset(p, target);
        if (corrected.equals(oldValue)) {
            return;
        }
        setProperty(PROP_TARGET_DOCKPOINT, corrected.x + "," + corrected.y);

    }

    /**
     * Gets the relative dock point for the target node, based on the target
     * node's position. If it was not specified, null will be returned.
     * @param p
     */
    public Point getTargetDockPointOffset() {
        String prop = getProperty(PROP_TARGET_DOCKPOINT);
        return toPoint(prop);
    }

    public Point getDockPointOffset(boolean fromSource) {
        if (fromSource) {
            return getSourceDockPointOffset();
        } else {
            return getTargetDockPointOffset();
        }
    }

    /**
     * resets the target docking point offset and returns to the usage 
     * of automatic determination
     */
    public void clearTargetDockPointOffset() {
        setProperty(PROP_TARGET_DOCKPOINT, "");
    }

    /**
     * internal helper method.
     * Converts a string with comma delimited coordinates into a point
     * (e.g. "100,50").
     * If parsing fails, null is returned.
     * @param prop
     * @return
     */
    private Point toPoint(String prop) {
        if (prop.isEmpty()) {
            return null;
        }
        try {
            String[] coords = prop.split(",");
            Point p = new Point();
            p.x = Integer.parseInt(coords[0]);
            p.y = Integer.parseInt(coords[1]);
            return p;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Determines whether the source arrow should be outlined or not. Might
     * be overwritten by subclasses.
     * @return
     */
    public boolean isOutlineSourceArrow() {
        return false;
    }

    /**
     * Determines whether the target arrow should be outlined or not. Might
     * be overwritten by subclasses.
     * @return
     */
    public boolean isOutlineTargetArrow() {
        return false;
    }

    /**
     * Copies the properties from this ProcessEdge, without ID and Type.
     * @see clonePropertiesFrom()
     * @param edge
     */
    public void copyPropertiesFrom(ProcessEdge edge) {
        for (String key : edge.getPropertyKeys()) {
            if (key.equals(PROP_ID) || key.equals(PROP_CLASS_TYPE)) {
                continue;
            }
            this.setProperty(key, edge.getProperty(key));
        }
    }

    /**
     * Clones all properties from this ProcessEdge, including ID and TYPE.
     * @see copyPropertiesFrom()
     * @param edge
     */
    public void clonePropertiesFrom(ProcessEdge edge) {
        for (String key : edge.getPropertyKeys()) {
            this.setProperty(key, edge.getProperty(key));
        }
        this.setAlpha(edge.getAlpha());
    }

    @Override
    public ProcessEdge clone() {
        ProcessEdge newEdge = (ProcessEdge) super.clone();
        //copy properties
        newEdge.setSource(this.getSource());
        newEdge.setTarget(this.getTarget());
        return newEdge;
    }

    @Override
    protected String getXmlTag() {
        return TAG_EDGE;
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " (" + getSource() + " -> " + getTarget() + ")";
    }

	/**
	 * @return
	 */
	public ProcessHelper getLabelHelper() {
		return label;
	}

	/**
	 * @param _rps
	 */
	public void setRoutingPoints(List<Point> _rps) {
		//setting new routing points
		clearRoutingPoints();
		for(int i=0;i<_rps.size();i++) {
			addRoutingPoint(i+1, _rps.get(i));
		}
	}
}
