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
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author frank
 */
public class UMLClass extends ProcessNode {

    public final static String PROP_ABSTRACT = "abstract";
    public final static String PROP_ATTRIBUTES = "#attributes";
    public final static String PROP_CONSTRAINTS = "constraints";
    public final static String PROP_METHODS = "methods";
    public final static int FONTSIZE = 11;
    public final static String ELEMENT_DELIMITER = ";";

    private static int ROUNDED_EDGE_VALUE = 0;

    private List<UMLAttribute> attributes;

    public UMLClass() {
        super();
        initializeProperties();
    }

    public UMLClass(String name) {
        super();
        initializeProperties();
        setText(name);
    }


    protected void initializeProperties() {
        setSize(80, 80);
        setText("Class");
        setProperty(PROP_ABSTRACT, FALSE);
        setPropertyEditor(PROP_ABSTRACT, new BooleanPropertyEditor());
        setProperty(PROP_ATTRIBUTES, "");
        setProperty(PROP_METHODS, "");
        setBackground(new Color(245,245,245));
    }

    public void addAttribute( String name ) {
        String oldAttributes = getProperty(UMLClass.PROP_ATTRIBUTES);
        setProperty(UMLClass.PROP_ATTRIBUTES,
                    oldAttributes + (oldAttributes.isEmpty() ? "" : ELEMENT_DELIMITER) + name);

//        attributes.add( new UMLAttribute(name) );
    }

    public void addAttribute( String name , String type ) {
        attributes.add( new UMLAttribute(name, type) );
    }

    public void addAttribute( UMLAttribute attr ) {
        this.attributes.add(attr);
    }

    public List<UMLAttribute> getAttributes() {
        return this.attributes;
    }

    @Override
    protected Shape getOutlineShape() {
        Shape outline = new RoundRectangle2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);
        return outline;
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        if ( PROP_ATTRIBUTES.equals( key ) ) {
            this.attributes = new LinkedList<UMLAttribute>();
            String[] atts = value.split(UMLClass.ELEMENT_DELIMITER);

            for ( String attribute : atts ) {
                if(!attribute.isEmpty()) {
				int open = attribute.indexOf('[');
				int sep = attribute.indexOf(':');

                                if ( sep < 0 )
                                    sep = attribute.length() - 1;

				String attName = (String) ((open<0) ? attribute.substring(1,sep) : attribute.subSequence(1, open));
                                String attType = sep < attribute.length() ? attribute.substring( sep + 1) : "";

                                UMLAttribute a = new UMLAttribute(attName, attType);

				String multi = (String) ((open>0) ? attribute.substring(open+1, attribute.indexOf(']')) : "1");
                                a.setProperty( UMLAttribute.PROP_MULTIPLICITY, multi );

                                Visibility v = Visibility.forUMLString( String.valueOf(attribute.charAt(0)));

                                if ( v != null )
                                    a.setProperty( UMLAttribute.PROP_VISIBILITY, v.toString() );

				this.attributes.add( a );
			}
            }
        }
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
        if (!this.attributes.isEmpty()) {
//            StringTokenizer attributes = new StringTokenizer(getProperty(PROP_ATTRIBUTES), ELEMENT_DELIMITER);
            height += attributes.size() * 15;
        }
        height += 20; // Spacing
        if (!getProperty(PROP_METHODS).isEmpty()) {
            StringTokenizer methods = new StringTokenizer(getProperty(PROP_METHODS), ELEMENT_DELIMITER);
            height += methods.countTokens() * 15;
        }

        setSize(getSize().width, height);

    }

//    @Override
//    public Element getSerialization(Document xmlDoc) {
//        Element el = super.getSerialization(xmlDoc);
//        Element attributesElement = xmlDoc.createElement("attributes");
//
//        el.appendChild(attributesElement);
//
//        for ( UMLAttribute attribute : this.attributes ) {
//            attributesElement.appendChild( attribute.getSerialization(xmlDoc) );
//        }
//
//        return el;
//    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(UMLUtils.defaultStroke);
        Shape outline = getOutlineShape();

        // #1: Complete background in white
        g2.setPaint(Color.WHITE);
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
        g2.setPaint(Color.WHITE);
        Shape fillshape2 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2)+topsize,
                getSize().width,
                ROUNDED_EDGE_VALUE
                );
        g2.fill(fillshape2);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

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
//        if (!getProperty(PROP_ATTRIBUTES).isEmpty()) {
//            StringTokenizer attributes = new StringTokenizer(getProperty(PROP_ATTRIBUTES),ELEMENT_DELIMITER);
//            while (attributes.hasMoreTokens()) {
//                String attribut = attributes.nextToken().trim();
//                if (yPos < getPos().y + getSize().height / 2 - 15) {
//                    ProcessUtils.drawText(g2, getPos().x-getSize().width/2+4, yPos,
//                            getSize().width - 8, attribut,
//                            ProcessUtils.Orientation.LEFT, false, false);
//                }
//                yPos += g2.getFont().getSize()+3;
//
//            }
//        }

        if (!this.attributes.isEmpty()) {
            for ( UMLAttribute attribute : this.attributes ) {

                if (yPos < getPos().y + getSize().height / 2 - 15) {
                    ProcessUtils.drawText(g2, getPos().x-getSize().width/2+4, yPos,
                            getSize().width - 8, attribute.toString(),
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
}
