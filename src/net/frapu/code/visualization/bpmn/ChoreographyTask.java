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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.DynamicListSelectionPropertyEditor;
import net.frapu.code.visualization.editors.ListDataSource;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author fpu
 */
public class ChoreographyTask extends ChoreographyActivity {

    /** The property for the name of the upper participant */
    public final static String PROP_UPPER_PARTICIPANT = "upper_participant";
    /** The property for the name of the lower participant */
    public final static String PROP_LOWER_PARTICIPANT = "lower_participant";
    /** The property for the active participant */
    public final static String PROP_ACTIVE_PARTICIPANT = "active_participant";
    /** The property for the loop type: "NONE, STANDARD, MULTIINSTANCE" */
    public static final String PROP_LOOP_TYPE = "loop_type";
    /** The property for upper multi instance participants (=FALSE,1=TRUE)*/
    public final static String PROP_UPPER_PARTICIPANT_MULTI = "upper_mi";
    /** The property for  lower multi instance participants (=FALSE,1=TRUE) */
    public final static String PROP_LOWER_PARTICIPANT_MULTI = "lower_mi";
    public static final String LOOP_NONE = "NONE";
    public static final String LOOP_STANDARD = "STANDARD";
    public static final String LOOP_MULTI_INSTANCE = "MULTIINSTANCE";

    private DynamicListSelectionPropertyEditor activeParticipantsEditor;
    private DynamicListSelectionPropertyEditor upperParticipantsEditor;
    private DynamicListSelectionPropertyEditor lowerParticipantsEditor;

    public ChoreographyTask() {
        super();
        initializeProps();
    }

    public ChoreographyTask(int x, int y, String label) {
        super();
        initializeProps();
        setPos(x, y);
        setText(label);
    }

    private void initializeProps() {
        int w = 140;
        int h = 100;
        setSize(w, h);
        setProperty(PROP_UPPER_PARTICIPANT, "Participant A");
        upperParticipantsEditor = new DynamicListSelectionPropertyEditor(new ParticipantDataSource(), true);
        setPropertyEditor(PROP_UPPER_PARTICIPANT, upperParticipantsEditor);
        setProperty(PROP_LOWER_PARTICIPANT, "Participant B");
        lowerParticipantsEditor = new DynamicListSelectionPropertyEditor(new ParticipantDataSource(), true);
        setPropertyEditor(PROP_LOWER_PARTICIPANT, lowerParticipantsEditor);
        setProperty(PROP_ACTIVE_PARTICIPANT, "Participant A");
        activeParticipantsEditor = new DynamicListSelectionPropertyEditor(new ActiveParticipantDataSource(), false);
        setPropertyEditor(PROP_ACTIVE_PARTICIPANT, activeParticipantsEditor);
        setProperty(PROP_LOOP_TYPE, LOOP_NONE);
        String[] loop = {LOOP_NONE, LOOP_STANDARD, LOOP_MULTI_INSTANCE};
        setPropertyEditor(PROP_LOOP_TYPE, new ListSelectionPropertyEditor(loop));

        setProperty(PROP_UPPER_PARTICIPANT_MULTI, FALSE);
        setPropertyEditor(PROP_UPPER_PARTICIPANT_MULTI, new BooleanPropertyEditor());

        setProperty(PROP_LOWER_PARTICIPANT_MULTI, FALSE);
        setPropertyEditor(PROP_LOWER_PARTICIPANT_MULTI, new BooleanPropertyEditor());
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        // Update active participant selection
        if (key.equals(PROP_ACTIVE_PARTICIPANT) |
                key.equals(PROP_LOWER_PARTICIPANT) |
                key.equals(PROP_UPPER_PARTICIPANT)) {
            if (activeParticipantsEditor!=null) activeParticipantsEditor.update();
            if (lowerParticipantsEditor!=null) lowerParticipantsEditor.update();
            if (upperParticipantsEditor!=null) upperParticipantsEditor.update();
        }
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
        HashSet<Point> cp = new HashSet<Point>();
        // Set default connection points
        cp.add(new Point(0 - (getSize().width / 2), 0));
        cp.add(new Point(0, 0 - (getSize().height / 2)));
        cp.add(new Point(0 - (getSize().width / 2) + 30, 0 - (getSize().height / 2)));
        cp.add(new Point((getSize().width / 2) - 30, 0 - (getSize().height / 2)));
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
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
    }

    private void drawTask(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setFont(BPMNUtils.defaultFont);

        g2.setPaint(getBackground());
        g2.fill(outline);

        // Draw upper participant
        int upperHeight = 20;
        if (getProperty(PROP_UPPER_PARTICIPANT_MULTI).equals(TRUE)) {
            // Increase upper height if mi for upper participant
            upperHeight += Activity.MARKER_ICON_SIZE + 3;
        }

        if (!getProperty(PROP_ACTIVE_PARTICIPANT).equals(
                getProperty(PROP_UPPER_PARTICIPANT))) {
            // Highlight background
            RoundRectangle2D partOutline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                    getPos().y - (getSize().height / 2), getSize().width, upperHeight, 10, 10);
            g2.setPaint(Color.LIGHT_GRAY);
            g2.fill(partOutline);
        }

        g2.setPaint(Color.BLACK);
        int upperY = getPos().y - (getSize().height / 2) + upperHeight;
        g2.drawLine(getPos().x - (getSize().width / 2),
                upperY,
                getPos().x + (getSize().width / 2),
                upperY);
        BPMNUtils.drawText(g2, getPos().x, getPos().y - (getSize().height / 2),
                getSize().width - 8, getProperty(PROP_UPPER_PARTICIPANT),
                BPMNUtils.Orientation.TOP);
        if (getProperty(PROP_UPPER_PARTICIPANT_MULTI).equals(TRUE)) {
            // Draw mi marker for upper participant
            Activity.drawParallelMultiInstance(g2, getPos().x, upperY - Activity.MARKER_ICON_SIZE);
        }

        // Draw lower participant
        int lowerHeight = 20;
        if (getProperty(PROP_LOWER_PARTICIPANT_MULTI).equals(TRUE)) {
            // Increase lower height if mi for upper participant
            lowerHeight += Activity.MARKER_ICON_SIZE + 3;
        }
        if (!getProperty(PROP_ACTIVE_PARTICIPANT).equals(
                getProperty(PROP_LOWER_PARTICIPANT))) {
            // Highlight background
            RoundRectangle2D partOutline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                    getPos().y + (getSize().height / 2) - lowerHeight, getSize().width, lowerHeight, 10, 10);
            g2.setPaint(Color.LIGHT_GRAY);
            g2.fill(partOutline);
        }
        g2.setPaint(Color.BLACK);
        int lowerY = getPos().y + (getSize().height / 2) - lowerHeight;
        g2.drawLine(getPos().x - (getSize().width / 2),
                lowerY,
                getPos().x + (getSize().width / 2),
                lowerY);
        BPMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().height / 2) - lowerHeight,
                getSize().width - 8, getProperty(PROP_LOWER_PARTICIPANT),
                BPMNUtils.Orientation.TOP);
        if (getProperty(PROP_LOWER_PARTICIPANT_MULTI).equals(TRUE)) {
            // Draw mi marker for lower participant
            Activity.drawParallelMultiInstance(g2, getPos().x, getPos().y + getSize().height / 2 - Activity.MARKER_ICON_SIZE);
        }

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw Stereotype
        if (getStereotype().length() > 0) {
            g2.setFont(BPMNUtils.defaultFont.deriveFont(Font.ITALIC));
            g2.setPaint(Color.DARK_GRAY);
            BPMNUtils.drawText(g2, getPos().x, getPos().y - (getSize().height / 2) + 20,
                    getSize().width - 8, "<<" + getStereotype() + ">>", BPMNUtils.Orientation.TOP);
        }

        // Draw text
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        BPMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), BPMNUtils.Orientation.CENTER);

        // Draw Standard Loop-Marker
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(LOOP_STANDARD.toLowerCase())) {
            Activity.drawStandardLoop(g2, getPos().x, lowerY - Activity.MARKER_ICON_SIZE);
        }

        // Draw Multi Instance Loop-Marker
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(LOOP_MULTI_INSTANCE.toLowerCase())) {
            Activity.drawParallelMultiInstance(g2, getPos().x, lowerY - Activity.MARKER_ICON_SIZE);
        }
    }

    @Override
    public String toString() {
        return "BPMN Choreography Task";
    }

    class ParticipantDataSource implements ListDataSource {

        @Override
        public List<String> getListData() {
            List<String> result = new LinkedList<String>();
            // Detect current participants of the models
            for (ProcessModel model : getContexts()) {
                for (ProcessNode node : model.getNodes()) {
                    if (node instanceof ChoreographyTask) {
                        // Collect all participants in the model
                        String upperParticipant = node.getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT);
                        String lowerParticipant = node.getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT);
                        // Check if already contained, if not add
                        if (!result.contains(upperParticipant)) {
                            result.add(upperParticipant);
                        }
                        if (!result.contains(lowerParticipant)) {
                            result.add(lowerParticipant);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public String getData() {
            String result = "";
            return result;
        }
    }

    class ActiveParticipantDataSource implements ListDataSource {

        @Override
        public List<String> getListData() {
            List<String> result = new LinkedList<String>();
            // Detect current participants 
            String upperParticipant = getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT);
            String lowerParticipant = getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT);
            result.add(upperParticipant);
            result.add(lowerParticipant);
            return result;
        }

        @Override
        public String getData() {
            String result = "";
            return result;
        }
    }
}
