/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.editors.DefaultPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author fpu
 */
public class Task extends Activity {

    //** The property for compensation (0=FALSE;1=TRUE) */
    public final static String PROP_COMPENSATION = "compensation";
    public final static String PROP_IMPLEMENTATION = "implementation";

    public Task() {
        super();
        initializeProperties();
    }

    /**
     * Creates a new BPMN Task at the given location.
     * @param x Location x-axis
     * @param y Location y-axis
     * @param label The initial label of the Task.
     */
    public Task(int x, int y, String label) {
        super();
        int w = 100;
        int h = 60;
        setSize(w, h);
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {
        setProperty(PROP_LOOP_TYPE, LOOP_NONE);
        String[] type = { Activity.LOOP_NONE, Activity.LOOP_STANDARD, Activity.LOOP_MULTI_SEQUENCE, Activity.LOOP_MULTI_PARALLEL };
        setPropertyEditor(PROP_LOOP_TYPE, new ListSelectionPropertyEditor(type));

        String[] atype = { "", Activity.TYPE_SEND, Activity.TYPE_RECEIVE, Activity.TYPE_SERVICE,
            Activity.TYPE_USER, Activity.TYPE_MANUAL, Activity.TYPE_RULE, Activity.TYPE_SCRIPT
        };
        setPropertyEditor(PROP_STEREOTYPE, new ListSelectionPropertyEditor(atype));
        
        setProperty(PROP_COMPENSATION, FALSE);
        setPropertyEditor(PROP_COMPENSATION, new BooleanPropertyEditor());

        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
        
        setProperty(PROP_IMPLEMENTATION, "");
    }

    @Override
    public void setProperty(String key, String value) {
        if (key.equals(PROP_HEIGHT)) {
            try {
                int height = Integer.parseInt(value);
                if (height<30) value = "30";
            } catch (Exception e) {
                value = "60";
            }
        }
        if (key.equals(PROP_WIDTH)) {
            try {
                int width = Integer.parseInt(value);
                if (width<50) value = "50";
            } catch (Exception e) {
                value = "100";
            }
        }
        super.setProperty(key, value);
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
        HashSet<Point> cp = new HashSet<Point>();
        // Set default connection points
        int spacing = 30;
        if ((getSize().width/2)<30) spacing = getSize().width/2;

        cp.add(new Point(0 - (getSize().width / 2), 0));
        cp.add(new Point(0, 0 - (getSize().height / 2)));
        cp.add(new Point(0 - (getSize().width / 2) + spacing, 0 - (getSize().height / 2)));
        cp.add(new Point((getSize().width / 2) - spacing, 0 - (getSize().height / 2)));
        cp.add(new Point(0 - (getSize().width / 2) + spacing, (getSize().height / 2)));
        cp.add(new Point((getSize().width / 2) - spacing, (getSize().height / 2)));
        cp.add(new Point((getSize().width / 2), 0));
        cp.add(new Point(0, (getSize().height / 2)));
        return cp;
    }

    @Override
    public void paintInternal(Graphics g) {
        drawTask(g);
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 15, 15);
        return outline;
    }

    private void drawTask(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw Stereotype
        Activity.drawStereotype(g2, this);

        // @todo: Break text lines automatically to match
        // Count text lines
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        BPMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), BPMNUtils.Orientation.CENTER);
        int lowerY = getPos().y + getSize().height / 2;
        int markerPos = getPos().x;

        if(getProperty(PROP_LOOP_TYPE) != null) {
	        // Draw Standard Loop-Marker
	        if (LOOP_STANDARD.toLowerCase().equals(getProperty(PROP_LOOP_TYPE).toLowerCase())) {
	            Activity.drawStandardLoop(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
	            markerPos += Activity.MARKER_ICON_SIZE * 2;
	        }
	
	        // Draw Multi Instance Loop-Marker (Parallel)
	        if (LOOP_MULTI_PARALLEL.toLowerCase().equals(getProperty(PROP_LOOP_TYPE).toLowerCase())) {
	            Activity.drawParallelMultiInstance(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
	            markerPos += Activity.MARKER_ICON_SIZE * 2;
	        }
	
	        // Draw Multi Instance Loop-Marker (Sequence)
	        if (LOOP_MULTI_SEQUENCE.toLowerCase().equals(getProperty(PROP_LOOP_TYPE).toLowerCase())) {
	            Activity.drawSequentialMultiInstance(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
	            markerPos += Activity.MARKER_ICON_SIZE * 2;
	        }
        }
        // Draw Compensation-Marker
        if (TRUE.equals(getProperty(PROP_COMPENSATION))) {
            Activity.drawCompensation(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

    }
}
