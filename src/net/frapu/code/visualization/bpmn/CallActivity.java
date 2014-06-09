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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedList;
import net.frapu.code.visualization.Linkable;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import net.frapu.code.visualization.editors.ReferencePropertyEditor;

/**
 *
 * @author fpu
 */
public class CallActivity extends Activity implements Linkable {

    //** The property for the call type (TASK, PROCESS) */
    public final static String PROP_CALL_TYPE = "call_type";

    public final static String CALL_TASK = "TASK";
    public final static String CALL_PROCESS = "PROCESS";

    public static ReferenceChooserRestriction restrictions;

    public ReferenceChooserRestriction getReferenceRestrictions() {
        if (restrictions == null) {
            LinkedList<Class> classes = new LinkedList<Class>();
            classes.add(StartEvent.class);
            classes.add(Pool.class);
            classes.add(Task.class);
            restrictions = new ReferenceChooserRestriction(null, classes);
        }
        return restrictions;
    }

    public CallActivity() {
        super();
        initializeProperties();
    }

    public CallActivity(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    protected void initializeProperties() {        
        setSize(100, 60);
        setProperty(PROP_CALL_TYPE, CALL_PROCESS);
        String[] type = { CALL_TASK, CALL_PROCESS };
        setPropertyEditor( PROP_CALL_TYPE, new ListSelectionPropertyEditor(type));
        setProperty(PROP_REF,"");
        setPropertyEditor(PROP_REF, new ReferencePropertyEditor(getReferenceRestrictions()));
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w,h);
    }

    @Override
    public void paintInternal(Graphics g) {
        drawCallActivity(g);
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x-(getSize().width/2),
                getPos().y-(getSize().height/2), getSize().width, getSize().height, 10, 10);
        return outline;
    }

    private void drawCallActivity(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(BPMNUtils.boldStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        g2.setStroke(BPMNUtils.defaultStroke);

        // Draw Stereotype
        if (getStereotype().length() > 0) {
            g2.setFont(BPMNUtils.defaultFont.deriveFont(Font.ITALIC));
            g2.setPaint(Color.DARK_GRAY);
            BPMNUtils.drawText(g2, getPos().x, getPos().y - (getSize().height / 2),
                    getSize().width-8, "<<" + getStereotype() + ">>", BPMNUtils.Orientation.TOP);
        }

        // Count text lines
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        BPMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width-8, getText(), BPMNUtils.Orientation.CENTER);
        int lowerY = getPos().y+getSize().height/2;
        int markerPos = getPos().x;

        // Draw SubProcess-Marker
        if (getProperty(PROP_CALL_TYPE).equalsIgnoreCase(CALL_PROCESS)) {
            Activity.drawSubProcessMarker(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE,getProperty(PROP_CALL_TYPE).toLowerCase().equals(CALL_PROCESS.toLowerCase()));
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

    }

}
