/**
 *
 * Process Editor - UML Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.uml;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.Reference;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.RoutingPointLayouter;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import net.frapu.code.visualization.editors.ReferencePropertyEditor;

/**
 *
 * @author frank
 */
public class UMLClassReference extends ProcessNode {

    public final static String PROP_ABSTRACT = "abstract";
    public final static String PROP_ATTRIBUTES = "attributes";
    public final static String PROP_CONSTRAINTS = "constraints";
    public final static String PROP_METHODS = "methods";
    public final static int FONTSIZE = 11;
    public final static String ELEMENT_DELIMITER = ";";

    private final Color FILLCOLOR = new Color(255,255,255);
    private final Color TEXTCOLOR = new Color(180,180,180);

    private static int ROUNDED_EDGE_VALUE = 0;

    public static ReferenceChooserRestriction restrictions;

    public UMLClassReference() {
        super();
        initializeProperties();
    }

    public UMLClassReference(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    @Override
    public void setProperty(String key, String value) {
        //
        // Just a hack for testing: Override setting of x,y properties
        //
        if (key.equals(PROP_XPOS) | key.equals(PROP_YPOS) |
            key.equals(PROP_WIDTH) | key.equals(PROP_HEIGHT)) {

            // Get contexts
            for (ProcessModel m: getContexts()) {
                if (m.getUtils()!=null) {
                    if (m.getUtils().getRoutingPointLayouter()!=null) {
                        RoutingPointLayouter rp = m.getUtils().getRoutingPointLayouter();
                        // Layout corresponding edges
                        for (ProcessEdge edge: m.getEdges()) {
                            if (edge.getSource()==this || edge.getTarget()==this) {
                                rp.optimizeRoutingPoints(edge,this);
                            }
                        }
                    }
                }
            }

        }

        super.setProperty(key, value);
    }

    public static ReferenceChooserRestriction getReferenceRestrictions() {
        if (restrictions == null) {
            LinkedList<Class> classes = new LinkedList<Class>();
            classes.add(UMLClass.class);
            restrictions = new ReferenceChooserRestriction(null, classes);
        }
        return restrictions;
    }

    protected final void initializeProperties() {
        setSize(80, 80);
        setText("Reference");
        setProperty(PROP_ABSTRACT, FALSE);
        setPropertyEditor(PROP_ABSTRACT, new BooleanPropertyEditor());
        setProperty(PROP_ATTRIBUTES, "");
        setProperty(PROP_METHODS, "");
        setBackground(new Color(245,245,245));
        setProperty(PROP_REF,"");
        setPropertyEditor(PROP_REF, new ReferencePropertyEditor(getReferenceRestrictions()));
    }

    public void addAttribute(String attr) {
        String oldAttributes = getProperty(UMLClassReference.PROP_ATTRIBUTES);
        setProperty(UMLClassReference.PROP_ATTRIBUTES,
                    oldAttributes + (oldAttributes.isEmpty() ? "" : ELEMENT_DELIMITER) + attr);
    }

    @Override
    protected Shape getOutlineShape() {
        Shape outline = new RoundRectangle2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);
        return outline;
    }

    @Override
    public void setSize(int w, int h) {
        if (w < 50 | h < 50) {
            return;
        }
        super.setSize(w, h);
    }

    /**
     * Optimizes the size of this ProcessNode to fit all its current content.
     */
    public void pack() {
        int height = 0;

        if (!getStereotype().isEmpty()) {
            height += 20;
        }
        height += 20; // Name
        // For each attribute +15
        if (!getProperty(PROP_ATTRIBUTES).isEmpty()) {
            StringTokenizer attributes = new StringTokenizer(getProperty(PROP_ATTRIBUTES), ELEMENT_DELIMITER);
            height += attributes.countTokens() * 15;
        }
        height += 20; // Spacing
        if (!getProperty(PROP_METHODS).isEmpty()) {
            StringTokenizer methods = new StringTokenizer(getProperty(PROP_METHODS), ELEMENT_DELIMITER);
            height += methods.countTokens() * 15;
        }

        setSize(getSize().width, height);

    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(UMLUtils.boldStroke);
        Shape outline = getOutlineShape();

        // #1: Complete background in white
        g2.setPaint(FILLCOLOR);
        g2.fill(outline);

        // #2: Top in background
        int topsize = 20;
        if (!getStereotype().isEmpty()) topsize += 20;
        g2.setPaint(getBackground());
        Shape fillshape1 = new RoundRectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2),
                getSize().width,
                topsize + ROUNDED_EDGE_VALUE,
                ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);
        g2.fill(fillshape1);

        // #3: Fill middle in white
        g2.setPaint(FILLCOLOR);
        Shape fillshape2 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2)+topsize,
                getSize().width,
                ROUNDED_EDGE_VALUE
                );
        g2.fill(fillshape2);

        g2.setPaint(TEXTCOLOR);
        g2.draw(outline);

        g2.setStroke(UMLUtils.defaultStroke);

        int yPos = getPos().y - (getSize().height / 2);

        // Stereotype
        if (!getStereotype().isEmpty()) {
            g2.setFont(UMLUtils.defaultFont.deriveFont(Font.PLAIN, (float)FONTSIZE));
            ProcessUtils.drawText(g2, getPos().x, yPos,
                    getSize().width - 8, "<<" + getStereotype() + ">>",
                    ProcessUtils.Orientation.TOP,
                    false);
            yPos += 20;
        }

        // Name
        g2.setFont(UMLUtils.defaultFont.deriveFont(getProperty(PROP_ABSTRACT).equals(FALSE) ? Font.BOLD + Font.PLAIN : Font.BOLD + Font.ITALIC, (float)FONTSIZE));
        ProcessUtils.drawText(g2, getPos().x, yPos,
                getSize().width - 8, getText(),
                ProcessUtils.Orientation.TOP,
                false,false);
        yPos += 20;
        drawLine(g2, yPos);

        g2.setFont(UMLUtils.defaultFont.deriveFont(Font.PLAIN, (float)10));

        // Attributes
        if (!getProperty(PROP_ATTRIBUTES).isEmpty()) {
            StringTokenizer attributes = new StringTokenizer(getProperty(PROP_ATTRIBUTES),ELEMENT_DELIMITER);
            while (attributes.hasMoreTokens()) {
                String attribut = attributes.nextToken().trim();
                if (yPos < getPos().y + getSize().height / 2 - 15) {
                    ProcessUtils.drawText(g2, getPos().x-getSize().width/2+4, yPos,
                            getSize().width - 8, attribut,
                            ProcessUtils.Orientation.LEFT, false, false);
                } 
                yPos += g2.getFont().getSize()+3;

            }
        }
        yPos += 10;
        if (yPos < getPos().y + getSize().height / 2 - 5) {
            drawLine(g2, yPos);
        }

        // Methods
        if (!getProperty(PROP_METHODS).isEmpty()) {
            StringTokenizer methods = new StringTokenizer(getProperty(PROP_METHODS), ELEMENT_DELIMITER);
            while (methods.hasMoreTokens()) {
                String method = methods.nextToken().trim();
                if (yPos < getPos().y + getSize().height / 2 - 15) {
                    ProcessUtils.drawText(g2, getPos().x-getSize().width/2+4, yPos,
                            getSize().width - 8, method,
                            ProcessUtils.Orientation.LEFT);
                }
                yPos += 15;
            }
        }
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
    	Set<Point> points = super.getDefaultConnectionPoints();
    	/*points.add(new Point(10, 0-(getSize().height/2)));
    	points.add(new Point(-10, 0-(getSize().height/2)));
    	points.add(new Point(20, 0-(getSize().height/2)));
    	points.add(new Point(-20, 0-(getSize().height/2)));
    	
    	points.add(new Point(10, (getSize().height/2)));
    	points.add(new Point(-10, (getSize().height/2)));
    	points.add(new Point(20, (getSize().height/2)));
    	points.add(new Point(-20, (getSize().height/2)));*/
    	
    	return points;
    }
    
    private void drawLine(Graphics2D g2, int yPos) {
        g2.drawLine(getPos().x - (getSize().width / 2),
                yPos,
                getPos().x + (getSize().width / 2),
                yPos);
    }

    @Override
    public void updateReferences(List<Reference> references) {
        // Check if size > 0
        if (references!=null) {
            if (references.size()>0) {
                Reference ref = references.get(0);
                if (ref.getRefObject() instanceof UMLClass) {
                    UMLClass refClass = (UMLClass)ref.getRefObject();
                    // Copy NAME, STEREOTYPE, ATTRIBUTES, METHODS, BACKGROUND, ABSTRACT
                    this.setProperty(PROP_TEXT, refClass.getProperty(PROP_TEXT));
                    this.setProperty(PROP_STEREOTYPE, refClass.getProperty(PROP_STEREOTYPE));
                    this.setProperty(PROP_ATTRIBUTES, refClass.getProperty(PROP_ATTRIBUTES));
                    this.setProperty(PROP_METHODS, refClass.getProperty(PROP_METHODS));
                    this.setProperty(PROP_ABSTRACT, refClass.getProperty(PROP_ABSTRACT));
                    this.setProperty(PROP_BACKGROUND, refClass.getProperty(PROP_BACKGROUND));
                }

            }
        }
    }

}
