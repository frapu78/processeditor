/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008-2017 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import net.frapu.code.visualization.editors.MultiLinePropertyEditor;
import org.w3c.dom.Node;

/**
 *
 * This class provides an abstract representation of a process node.
 * 
 * @todo: Insert generic calculation of default connection points! Called each 
 * time getConnectPoint is called!
 * @todo: Refactor connection points to refer to relative coordinates according
 * to the corresponding graphical schema of the node
 * 
 * @author frank
 */
public abstract class ProcessNode extends ProcessObject implements Dragable {

    public final static String TAG_NODE = "node";

    public final static String PROP_TEXT = "text";
    public final static String PROP_LABEL = "label";
    public final static String PROP_STEREOTYPE = "stereotype";
    public final static String PROP_XPOS = "x";
    public final static String PROP_YPOS = "y";
    public final static String PROP_WIDTH = "width";
    public final static String PROP_HEIGHT = "height";
    public final static String PROP_SHADOW = "shadow";
    /** Optional property that might be used to reference from one node to one or more others */
    public final static String PROP_REF = "ref";
    /** The background color (see java.awt.Color) for values */
    public final static String PROP_BACKGROUND = "color_background";

    //when imported from IS
    public final static String PROP_MODULEID = "ModuleID";
    public final static String PROP_ANNOTATION = "SemanticAnnotations";    
    /** Flags if this ProcessNode is visible or not */
    private boolean visible = true;
    /** Stores a user definable Object that will not be serialized by default */
    protected Object userObject;    
   /** Holds the connection points of the process node */
    private HashSet<Point> connectionPoints = new HashSet<Point>();
    
    public ProcessNode() {
    	super();
        setProperty(PROP_TEXT, "");
        setPropertyEditor(PROP_TEXT, new MultiLinePropertyEditor());
        setProperty(PROP_STEREOTYPE, "");
        setProperty(PROP_XPOS, "0");
        setProperty(PROP_YPOS, "0");
        setProperty(PROP_WIDTH, "100");
        setProperty(PROP_HEIGHT, "60");
        setProperty(PROP_SHADOW, FALSE);
        setPropertyEditor(PROP_SHADOW, new BooleanPropertyEditor());
        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
        //Enable this if you add new Types to check whether their default connection points are really contained
        checkDefaultConnectionPoints();
    }

    public static ProcessNode newInstanceFromSerialization(Node XMLnode) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        HashMap<String, String> props = ProcessUtils.readProperties(xpath, XMLnode);
        // Hack to make old TWFs work (fpu)
        if (props.get(ProcessObject.PROP_CLASS_TYPE).equals("net.frapu.code.visualization.twf.ColoredFrame")) {
            props.put(ProcessObject.PROP_CLASS_TYPE, "net.frapu.code.visualization.general.ColoredFrame");
        }
        // Try to instantiate corresponding process node
        Object o = Class.forName(props.get(ProcessObject.PROP_CLASS_TYPE)).newInstance();
        if (!(o instanceof ProcessNode)) {
            throw new Exception("Illegal node found!");
        }
        ProcessNode pn = (ProcessNode) o;
        // Add properties to node
        for (String key : props.keySet()) {
            String newKey = key;
            // Convert old "#attributes" to "attributes"
            if (key.equals("#attributes")) {
                newKey = "attributes";
            }            
            pn.setProperty(key, props.get(key));
        }
        return pn;
    }

    @Override
    /**
     * Creates a deep copy of this ProcessNode (inkl. Properties without id!).
     */
    public ProcessNode clone() {
        ProcessNode copy = (ProcessNode) super.clone();
        copy.connectionPoints = (HashSet<Point>) connectionPoints.clone();
        return copy;
    }



    /**
     * Creates a deep copy of this ProcessNode (inkl. Properties without id!).     
     */
    @Deprecated
    public ProcessNode copy() {
        return clone();
    }

    
    /**
     * Draws the flow object.
     * @param g
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

         // Draw shadow for Process Nodes
        if (isShadowEnabled()) {
            g.setColor(Color.LIGHT_GRAY);
            AffineTransform at = new AffineTransform();
            at.translate(5, 5);
            Shape shape = at.createTransformedShape(getOutlineShape());
            if (shape!=null) g2.fill(shape);
        }

        // Draw shape
        paintInternal(g);
        // Check if selected
        if (isSelected()) {
            // Draw selection
            paintSelection(g);
        }
        if (isHighlighted()) {
            // Draw highlight @todo: Animate!
            paintHighlight(g);
        }
    }
                
    /**
     * Paints the flow object on the given graphics. Needs to be implemented
     * by a subclass.
     * @param g
     */
    protected abstract void paintInternal(Graphics g);
    
    /**
     * Returns the external shape of the given process node. The shape is
     * already scaled and moved to the corresponding position!
     * @return
     */
    protected abstract Shape getOutlineShape();
    
    /**
     * Paints the selection around the node (if selected). Might be
     * implemented by a subclass.
     * @param g
     */
    protected void paintSelection(Graphics g) {
       ProcessUtils.drawSelectionBorder(g, getOutlineShape()); 
    }
    
    /**
     * Paints the highlight around the flow object.
     * @param g
     */
    protected void paintHighlight(Graphics g) {
        ProcessUtils.drawHighlight(g, getOutlineShape());
    }
    
    public Point getConnectionPoint(int x, int y) {
        return getConnectionPoint(new Point(x,y));
    }

    public Point getSelectionOffset() {
        return new Point(0, 0);
    }
    
    /**
     * Returns the nearest absolute(!) connection point for a line from the 
     * flow object
     * to (toX, toY)
     * @param toX
     * @param toY
     */
    public Point getConnectionPoint(Point target) {
        // Search for the connection point that is closest
        Point result = new Point(getPos());
        double minDist = Double.MAX_VALUE;
        
        // Check if connectionPoints are set use default ones        
        // If no connection points are set, create default connection points
        Point pos = getPos();
        for (Point p: connectionPoints.size()>0?connectionPoints:getDefaultConnectionPoints()) {
        	Point check = new Point(p.x+pos.x,p.y+pos.y);
            double currDist = check.distance(target);
            if (currDist<minDist) {
                minDist = currDist;                
                result=check;
            }
        }
        return result;
    }
    
        /**
	 * Finds the best Connection points respecting all possible connection Point of another node
	 * @param defaultConnectionPoints
	 * @return
	 */
	public Point getConnectionPoint(Set<Point> defaultConnectionPoints,Point targetPos) {
		Point result = getPos();
		double distance = Double.MAX_VALUE;
		for(Point target:defaultConnectionPoints) {
			Point targetPointCoords = new Point(target.x + targetPos.x, target.y + targetPos.y);
			Point found = getConnectionPoint(targetPointCoords);
			double dist = found.distance(targetPointCoords);
			if(dist < distance) {
				result = found;
				distance = dist;
			}						
		}
		return result;
	}
    
    /**
     * Returns the default connection points for this node. Can be overridden
     * by sub-classes to implement specific behavior. The default connection 
     * points are only used if no additional connection points have been
     * specified!
     * @return
     */
    public Set<Point> getDefaultConnectionPoints() {
        HashSet<Point> cp = new HashSet<Point>();
        // Calculate default connection points
        cp.add(new Point(0-(getSize().width/2), 0));
        cp.add(new Point(0, 0-(getSize().height/2)));
        cp.add(new Point((getSize().width/2) - 1, 0));
        cp.add(new Point(0, (getSize().height/2)-1));
        return cp;
    }
    /**
     * Do not use getOutlineShape().contains() to check whether a Point is contained.
     * Use this method instead. The reason is that points that are located exactly on
     * the outer left or lower line are not considered to be contained, yet this is import
     * for e.g. connection points. This methods also returns true if the point lies on a delimeter line.
     * @param p as absolute value
     * @return
     */
    public boolean contains(Point p) {
        if (getOutlineShape()==null) return false;
        Point test = new Point(p);
        for (int i=-1;i<=1;i++) {
            for (int j=-1;j<=1;j++) {
                test.setLocation(p.x+i, p.y+j);
                if (getOutlineShape().contains(test)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This methods returns true if the given Point and all 8 directly surrounding
     * points are contained in this node. Thus it does not - in contrast to contains() - lie
     * on the delimeter of this node
     * @param p as absolute value
     * @return
     */
    public boolean containsDeepInside(Point p) {
        if (getOutlineShape()==null) return false;
        Point test = new Point(p);
        for (int i=-2;i<=2;i++) {
            for (int j=-2;j<=2;j++) {
                test.setLocation(p.x+i, p.y+j);
                if (!getBounds().contains(test)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean connectionPointContained(Point connectionPoint) {
            if (connectionPoint==null) return false;
            Point absolute = new Point(connectionPoint);
            absolute.translate(getPos().x, getPos().y);
            try {
                return getOutlineShape()!=null ? contains(absolute) : true;
            } catch (UnsupportedOperationException e) {
                return true;
            }            
    }

    public final void checkDefaultConnectionPoints() {
        for (Point p : getDefaultConnectionPoints()) {
            if (!connectionPointContained(p)) {               
                /*throw new IllegalStateException*/System.err.println("The default connection point '" + p.toString() +
                        "' of " + this.getClass().getSimpleName() + " is invalid because it is not contained in the process node.");
            }
        }
    }
    
    
    
    /**
     * Adds a relative(!) connection point for the flow object.
     * @param x
     * @param y
     */
    public void addConnectionPoint(int x, int y) {
        Point p = new Point(x,y);
        //System.out.println("Added "+p);
        connectionPoints.add(p);
    }
    
    public void removeAllConnectionPoints() {
        connectionPoints = new HashSet<Point>();
    }

    /**
     * Returns the size of this ProcessNode. Also consider getBoundingBox().
     * @return
     */
    public Dimension getSize() {
        return new Dimension(Integer.parseInt(getProperty(PROP_WIDTH)),
                Integer.parseInt(getProperty(PROP_HEIGHT)));
    }

    /**
     * Returns the bounding box for this ProcessNode. Default is the same
     * as getOutlineShape. Might be overwritten by subclasses.
     * @return
     */
    public Rectangle getBoundingBox() {
        Rectangle rect=null;
        if (getOutlineShape()!=null) {
            rect = getOutlineShape().getBounds();
        }
        if (rect==null) {
            rect = new Rectangle(getTopLeftPos().x, getTopLeftPos().y, getSize().width, getSize().height);
        }
        // Check if shadow is on
        if (isShadowEnabled()) {
            rect.width += 5;
            rect.height += 5;
        }
        return rect;
    }

    public void setSize(int w, int h) {
        if (w<0 | h<0) return;
        this.setProperty(PROP_WIDTH, ""+w);
        this.setProperty(PROP_HEIGHT, ""+h);
    }

    private void resetdockingPoints() {
        for (ProcessModel context : this.getContexts()) {
            List<ProcessEdge> incommingEdges = context.getIncomingEdges(ProcessEdge.class, this);
            List<ProcessEdge> outgoingEdges = context.getOutgoingEdges(ProcessEdge.class, this);
            resetEdgeDockingPoint(outgoingEdges, true);
            resetEdgeDockingPoint(incommingEdges, false);
        }
    }

    /**
     * Corrects DP position, if size changes
     */
    private void resetEdgeDockingPoint(List<ProcessEdge> edges, boolean isSource) {
        for (ProcessEdge edge : edges) {
            if (isSource) {
                Point oldDP = edge.getSourceDockPointOffset();
                edge.setSourceDockPointOffset(oldDP);
            } else {
                Point oldDP = edge.getTargetDockPointOffset();
                edge.setTargetDockPointOffset(oldDP);
            }
        }
    }
    
    public void setSize(Dimension d) {
        this.setSize(d.width, d.height);
    }

    public String getText() {
        return this.getProperty(PROP_TEXT);
    }

    public void setText(String text) {
        this.setProperty(PROP_TEXT, text);
    }

    public String getStereotype() {
        return getProperty(PROP_STEREOTYPE);
    }

    public void setStereotype(String stereotype) {
        setProperty(PROP_STEREOTYPE, stereotype);
    }

    /**
     * Sets the center position of this ProcessNode.
     * @param p
     */
    public void setPos(Point p) {
        setPos(p.x,p.y);
    }

    /**
     * Sets the center position of this ProcessNode.
     * @param x
     * @param y
     */
    public void setPos(int x, int y) {
        setProperty(PROP_XPOS, ""+x);
        setProperty(PROP_YPOS, ""+y);
    }

    /**
     * Returns the center position of this ProcessNode.
     * @return
     */
    public Point getPos() {
        return new Point(Integer.parseInt(getProperty(PROP_XPOS)),
                Integer.parseInt(getProperty(PROP_YPOS)));
    }

    /**
     * Returns the top left position of this ProcessNode.
     * @return
     */
    public Point getTopLeftPos() {
        Point c = getPos();
        Dimension d = getSize();
        return new Point(c.x-d.width/2, c.y-d.height/2);
    }

    public void setShadowEnabled(boolean shadow) {
        setProperty(PROP_SHADOW, shadow?TRUE:FALSE) ;
    }

    public boolean isShadowEnabled() {
        return TRUE.equals(getProperty(PROP_SHADOW));
    }

    public void setBackground(Color c) {
        setProperty(PROP_BACKGROUND, ""+c.getRGB());
    }

    public Color getBackground() {
        return getBackground(getProperty(PROP_BACKGROUND));
    }

    public static Color getBackground(String colorPropertyValue) {
        Color c = Color.WHITE;
        try {
            c = new Color(Integer.parseInt(colorPropertyValue));
        } catch (Exception e) {}

        return c;
    }

    public Rectangle getBounds() {
        return new Rectangle(getPos().x-(getSize().width/2),getPos().y-
                (getSize().height/2),getSize().width,getSize().height); 
       
    }

    /**
     * Returns if this ProcessNode should be rendered by the ProcessEditor
     * or not.
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Defines whether this ProcessNode is visible or not (this selection
     * is not serialized).
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
   
    /**
     * Sets the user object for the process node. This object will not (yet) 
     * be serialized.
     * @return
     */
    public Object getUserObject() {
        return userObject;
    }
    
    @Override
    protected String getXmlTag() {
    	return TAG_NODE;
    }

    /**
     * Returns the user object for the process node.
     * @param userObject
     */
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    /**
     * Returns the list of variants for this node, e.g. the node types
     * that can be used to refactor this node.
     * @return
     */
    public List<Class<? extends ProcessNode>> getVariants() {
        // Default is empty list
        return new LinkedList<Class<? extends ProcessNode>>();
    }

    /**
     * Returns if this node is an instance of a subclass of Cluster
     * @return
     */
    public boolean isCluster() {
        return false;
    }

    @Override
    public String getName() {
        return getText();
    }


           
    public String toString() {
        return "ProcessNode ("+getText()+")";
    }

	/**
	 * hook so new classes can take special actions after cloning
	 * @param localIdMap 
	 * 
	 */
	protected void handleCloning(Map<String, String> localIdMap) {
		
	}


     /*
     * returns all clusters in which this node is containded
     */
    public Set<Cluster> getParentClusters() {
        Set<Cluster> result = new HashSet<Cluster>(1);
        Cluster c;
        for (ProcessModel m: getContexts()) {
            c = m.getClusterForNode(this);
            if (c!=null) result.add(c);
        }
        return result;
    }

    /**
     * Might be implemented by sub classes. Is called each time the references
     * are updated. This method should change the properties of this node
     * if applicable.
     * @param references 
     */
    public void updateReferences(List<Reference> references) {
        // Do nothing per default
    }

    @Override
    public void setProperty(String key, String value) {
        if (key.equals(ProcessNode.PROP_WIDTH) || key.equals(ProcessNode.PROP_HEIGHT)) {
            if (this.getContexts().size()>0) {
                resetdockingPoints();                
            }
        }
        super.setProperty(key, value);
    }



    
}
