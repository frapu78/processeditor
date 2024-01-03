/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 inubit AG
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author frank
 */
public class DomainClass extends ProcessNode {

    public final static String PROP_ABSTRACT = "abstract";
    public final static String PROP_ATTRIBUTES = "#attributes";
    public final static String PROP_KEY = "key";
    public final static int FONTSIZE = 11;
    public final static String ELEMENT_DELIMITER = ";";
    public static final String STEREOTPYE_ROOT_INSTANCE = "root_instance";
    public static final String STEREOTPYE_PROCESS_BO = "process_bo";
    public static final String STEREOTPYE_THING_BO = "thing_bo";
    public static final String STEREOTPYE_EVENT = "event";
    private static int ROUNDED_EDGE_VALUE = 5;
    private final static Color ROOT_COLOR = new Color(243, 134, 3);
    private final static Color DEFAULT_COLOR = new Color(174, 206, 61);
    private Map<String, Attribute> attributes;
    private Map<String, DomainClass> parents;
    private Map<String, DomainClass> aggregatedClasses;
    private LinkedHashMap<String, DomainClass> assoziatedClasses;
    
    

    public DomainClass() {   
        super();
        initializeProperties();
    }

    public DomainClass(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    protected void initializeProperties() {
        setSize(100, 80);
        setText("Class");
        setProperty(PROP_ABSTRACT, FALSE);
        setPropertyEditor(PROP_ABSTRACT, new BooleanPropertyEditor());
        setProperty(PROP_ATTRIBUTES, "");
        setBackground(DEFAULT_COLOR);

        setProperty(PROP_KEY, "");

        String[] atype = {"", STEREOTPYE_ROOT_INSTANCE, STEREOTPYE_PROCESS_BO, STEREOTPYE_THING_BO,
            STEREOTPYE_EVENT};
        setPropertyEditor(PROP_STEREOTYPE, new ListSelectionPropertyEditor(atype));

    }

    public Map<String, Attribute> getAttributesByIDs() {
        if (attributes == null) {
            this.setProperty(PROP_ATTRIBUTES, this.getProperty(PROP_ATTRIBUTES));
        }
        return attributes;
    }
    
    public Map<String, Attribute> getAttributesByNames() {
        Map<String, Attribute> byIDs = getAttributesByIDs();
        HashMap<String, Attribute> result = new HashMap<String, Attribute>();
        for (Attribute attr : byIDs.values()) {
            result.put(attr.getName(), attr);
        }
        return result;
    }

    public Attribute getAttributeByName(String name) {
        return getAttributesByNames().get(name);
    }

    public void addAttribute(String name) {
        String oldAttributes = getProperty(DomainClass.PROP_ATTRIBUTES);
        String newAttribute = "{" + getUnusedAttributeId() + "}+" + name + "[0..1]:text";
        setProperty(DomainClass.PROP_ATTRIBUTES,
                oldAttributes + (oldAttributes.isEmpty() ? "" : ELEMENT_DELIMITER) + newAttribute);
    }

    public String getKey() {
        String key = getProperty(PROP_KEY);
        if (key == null) {
            return null;
        }
        if (key.length() == 0) {
            return null;
        }
        return key;
    }

    @Override
    protected Shape getOutlineShape() {
        /*
        Shape outline = new RoundRectangle2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);
         */
        Shape outline = new Rectangle2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);

        return outline;
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);

        if (PROP_ATTRIBUTES.equals(key)) {
            this.attributes = new LinkedHashMap<String, Attribute>();
            List<Attribute> noId = new LinkedList<Attribute>();
            String[] atts = value.split(DomainClass.ELEMENT_DELIMITER);

            for (String attribute : atts) {
                if (!attribute.isEmpty()) {
                    int open = attribute.indexOf('[');
                    int idOpen = attribute.indexOf('{');
                    int defOpen = attribute.indexOf('(');
                    int sep = attribute.indexOf(':');

                    if (sep < 0) {
                        sep = attribute.length() - 1;
                    }

                    if (defOpen < 0) {
                        defOpen = attribute.length();
                    }
                    int visIndex = 0;
                    int nameStart = 1;
                    if (idOpen >= 0) {
                        visIndex = attribute.indexOf('}') + 1;
                        nameStart = visIndex + 1;
                    }

                    String attName = (String) ((open < 0) ? attribute.substring(nameStart, sep) : attribute.subSequence(nameStart, open));
                    String attType = sep < attribute.length() ? attribute.substring(sep + 1, defOpen) : "";

                    Attribute a = new Attribute(attName, attType);

                    String multi = (String) ((open > 0) ? attribute.substring(open + 1, attribute.indexOf(']')) : "1");
                    a.setProperty(Attribute.PROP_MULTIPLICITY, multi);

                    Visibility v = Visibility.forUMLString(String.valueOf(attribute.charAt(visIndex)));

                    if (v != null) {
                        a.setProperty(Attribute.PROP_VISIBILITY, v.toString());
                    }

                    if (defOpen > -1 && defOpen < attribute.length()) {
                        int close = attribute.indexOf(')', defOpen);
                        if (close > -1) {
                            a.setProperty(Attribute.PROP_DEFAULT_VALUE, attribute.substring(defOpen + 1, close));
                        }
                    }

                    if (idOpen < 0) {
                        noId.add(a);
                    } else {
                        a.setProperty(Attribute.PROP_ID, attribute.substring(idOpen + 1, attribute.indexOf('}')));
                        this.attributes.put(a.getId(), a);
                    }
                }
            }

            if (noId.size() > 0) {
                for (Attribute a : noId) {
                    a.setProperty(Attribute.PROP_ID, this.getUnusedAttributeId());
                    this.attributes.put(a.getId(), a);
                }

                //build new property string
                StringBuffer sb = new StringBuffer();
                int i = 0;
                for (Attribute a : this.attributes.values()) {
                    i++;
                    sb.append(a);
                    if (i < this.attributes.values().size()) {
                        sb.append(ELEMENT_DELIMITER);
                    }
                }
                super.setProperty(PROP_ATTRIBUTES, sb.toString());
            }
        } else if (key.equalsIgnoreCase(PROP_STEREOTYPE)) {
            // Set BACKGROUND COLOR if set to root_instance otherwise reset color
            if (value.equalsIgnoreCase(STEREOTPYE_ROOT_INSTANCE)) {
                setBackground(ROOT_COLOR);
            } else {
                setBackground(DEFAULT_COLOR);
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

        setSize(getSize().width, height);

    }

    private void paintInternalOld(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(DomainUtils.defaultStroke);
        Shape outline = getOutlineShape();

        // #1: Complete background in white
        g2.setPaint(Color.WHITE);
        g2.fill(outline);

        // #2: Top in background
        int topsize = 20;
        if (!getStereotype().isEmpty()) {
            topsize += 20;
        }
        g2.setPaint(getBackground());
        Shape fillshape1 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2),
                getSize().width,
                topsize);
        g2.fill(fillshape1);

        // #3: Fill middle in white
        g2.setPaint(Color.WHITE);
        Shape fillshape2 = new Rectangle2D.Double(
                getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2) + topsize,
                getSize().width,
                ROUNDED_EDGE_VALUE);
        g2.fill(fillshape2);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        int yPos = getPos().y - (getSize().height / 2);

        // Stereotype
        if (!getStereotype().isEmpty()) {
            g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) FONTSIZE));
            ProcessUtils.drawText(g2, getPos().x, yPos,
                    getSize().width - 8, "<<" + getStereotype() + ">>",
                    ProcessUtils.Orientation.TOP,
                    false);
            yPos += 20;
        }

        // Name
        g2.setFont(DomainUtils.defaultFont.deriveFont(getProperty(PROP_ABSTRACT).equals(FALSE) ? Font.BOLD + Font.PLAIN : Font.BOLD + Font.ITALIC, (float) FONTSIZE));
        ProcessUtils.drawText(g2, getPos().x, yPos,
                getSize().width - 8, getText(),
                ProcessUtils.Orientation.TOP,
                false, false);
        yPos += 20;
        drawLine(g2, yPos);

        g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) 10));

        if (!this.attributes.isEmpty()) {
            for (Attribute attribute : this.attributes.values()) {

                if (yPos < getPos().y + getSize().height / 2 - 15) {
                    ProcessUtils.drawText(g2, getPos().x - getSize().width / 2 + 4, yPos,
                            getSize().width - 8, attribute.toUMLAttributeString(),
                            ProcessUtils.Orientation.LEFT, false, false);
                }
                yPos += g2.getFont().getSize() + 3;

            }
        }

        yPos += 10;

    }

    private void paintInternalNew(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(DomainUtils.defaultStroke);
        Shape outline = getOutlineShape();

        final int OFFSET = 2;
        Shape innerOutline = new RoundRectangle2D.Double(getPos().x + OFFSET - (getSize().width / 2),
                getPos().y + OFFSET - (getSize().height / 2),
                getSize().width - OFFSET * 2,
                getSize().height - OFFSET * 2, ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);

        // #0 Fill shadow
        g2.setPaint(DomainUtils.OUTLINE_COLOR);
        g2.fill(outline);

        // #1: Complete background in white (radial)
        Point2D center = getPos();
        float radius = getSize().width > getSize().height ? getSize().width : getSize().height;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {Color.WHITE, new Color(230, 230, 230)};
        RadialGradientPaint rp =
                new RadialGradientPaint(center, radius, dist, colors);
        g2.setPaint(rp);
        g2.fill(innerOutline);

        // #2: Top in background
        int topsize = 20;
        if (!getStereotype().isEmpty()) {
            topsize += 17;
        }

        //Point2D center2 = new Point(getPos().x-getSize().width/4, getPos().y);
        Point2D center2 = new Point(getPos().x + getSize().width / 4, getTopLeftPos().y + topsize / 2);
        float radius2 = getSize().width > topsize ? getSize().width : topsize;
        Color dimmedColor = new Color(
                (int) (getBackground().getRed() * 0.8),
                (int) (getBackground().getGreen() * 0.8),
                (int) (getBackground().getBlue() * 0.8));
        float[] dist2 = {0.0f, 0.35f, 1.0f};
        Color[] colors2 = {getBackground(), dimmedColor, dimmedColor};
        /** REMOVED SINCE NOT SUPPORTED BY CURRENT PDF/SVG EXPORT
        RadialGradientPaint rp2 =
                new RadialGradientPaint(center2, radius2, dist2, colors2);
         g2.setPaint(rp2);
        */
        g2.setColor(dimmedColor);

        Shape fillshape1 = new RoundRectangle2D.Double(
                getPos().x - (getSize().width / 2) + OFFSET,
                getPos().y - (getSize().height / 2) + OFFSET,
                getSize().width - OFFSET * 2,
                topsize + ROUNDED_EDGE_VALUE - OFFSET,
                ROUNDED_EDGE_VALUE, ROUNDED_EDGE_VALUE);
        g2.fill(fillshape1);

        int yPos = getPos().y - (getSize().height / 2);

        // Header Color
        g2.setPaint(Color.WHITE);

        // Stereotype
        if (!getStereotype().isEmpty()) {
            g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) FONTSIZE));
            ProcessUtils.drawText(g2, getTopLeftPos().x + OFFSET * 3, yPos + OFFSET,
                    getSize().width - OFFSET * 6, getStereotype(),
                    ProcessUtils.Orientation.LEFT,
                    false);
            yPos += 17;
        }

        // Name
        g2.setFont(DomainUtils.defaultFont.deriveFont(getProperty(PROP_ABSTRACT).equals(FALSE) ? Font.BOLD + Font.PLAIN : Font.BOLD + Font.ITALIC, (float) FONTSIZE));
        ProcessUtils.drawText(g2, getTopLeftPos().x + OFFSET * 3, yPos + OFFSET,
                getSize().width - OFFSET * 6, getText(),
                ProcessUtils.Orientation.LEFT,
                false, false);
        yPos += 25;

        //drawLine(g2, yPos);

        g2.setPaint(Color.BLACK);
        g2.setFont(DomainUtils.defaultFont.deriveFont(Font.PLAIN, (float) 10));

        if (!this.attributes.isEmpty()) {
            for (Attribute attribute : this.getAttributesByIDs().values()) {

                if (yPos < getPos().y + getSize().height / 2 - 20) {
                    ProcessUtils.drawText(g2, getTopLeftPos().x + OFFSET * 3, yPos,
                            getSize().width - OFFSET * 6, attribute.toUMLAttributeString(),
                            ProcessUtils.Orientation.LEFT, false, false);
                }
                yPos += g2.getFont().getSize() + 3;

            }
        }

        yPos += 10;
    }

    @Override
    protected void paintInternal(Graphics g) {
        //paintInternalNew(g);
        paintInternalOld(g);
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
        Set<Point> points = super.getDefaultConnectionPoints();
        return points;
    }

    private String getUnusedAttributeId() {
        int id = 0;
        while (this.attributes.containsKey(Integer.toString(id))) {
            ++id;
        }
        return Integer.toString(id);
    }

    private void drawLine(Graphics2D g2, int yPos) {
        g2.drawLine(getPos().x - (getSize().width / 2),
                yPos,
                getPos().x + (getSize().width / 2),
                yPos);
    }

    /**
     * Returns a list of all outgoing Aggregations, incl. those from super classes
     * @return
     */
    public List<Aggregation> getAggregations() {
        return DomainUtils.getAggregations(this, (DomainModel)this.getContexts().toArray()[0]);
    }
    
    public List<DomainClass> getAggregatedClasses() {
        LinkedList<DomainClass> result = new LinkedList<DomainClass>();
        for (Aggregation aggr : getAggregations()) {
            result.add((DomainClass)aggr.getTarget());
        }
        return result;
    }

    /**
     * Returns a list of all outgoing Associations, incl. those from super classes.
     *
     * The list contains all Associations attached to one of dc's classes that
     * have either DIRECTION_NONE, DIRECTION_BOTH, OR DIRECTION_TARGET as DIRECTION.
     *
     * DIRECTION.SOURCE is not included, since it only points to dc.
     *
     * @return
     */
    public List<Association> getAssociations() {
        return DomainUtils.getAssociations(this, (DomainModel)this.getContexts().toArray()[0]);
    }
    
    public List<DomainClass> getAssociatedClasses() {
        LinkedList<DomainClass> result = new LinkedList<DomainClass>();
        for (Association assoc : getAssociations()) {
            result.add((DomainClass)assoc.getTarget());
        }
        return result;
    }

    /**
     * Returns a list of all parent classes, incl. the recent one
     * @return
     */
    public List<DomainClass> getParents() {
        return DomainUtils.getParents(this, (DomainModel)this.getContexts().toArray()[0]);
    }

    /**
     * Returns a list of all child classes, incl. the recent one.
     * @return
     */
    public List<DomainClass> getChildren() {
        return DomainUtils.getChildren(this, (DomainModel)this.getContexts().toArray()[0]);
    }
}
