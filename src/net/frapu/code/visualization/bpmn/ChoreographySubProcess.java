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
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author fpu
 */
public class ChoreographySubProcess extends Cluster {

    /** The property for the name of the upper participant */
    public final static String PROP_UPPER_PARTICIPANTS = "upper_participants";
    /** The property for the name of the lower participant */
    public final static String PROP_LOWER_PARTICIPANTS = "lower_participants";
    /** The property for the active participant */
    public final static String PROP_ACTIVE_PARTICIPANTS = "active_participants";
    /** The property for the loop type: "NONE, STANDARD, MULTIINSTANCE" */
    public static final String PROP_LOOP_TYPE = "loop_type";
    /** The property for upper multi instance participants (=FALSE,1=TRUE)*/
    public final static String PROP_UPPER_PARTICIPANTS_MULTI = "upper_mi";
    /** The property for lower multi instance participants (=FALSE,1=TRUE) */
    public final static String PROP_LOWER_PARTICIPANTS_MULTI = "lower_mi";
    public static final String LOOP_NONE = "NONE";
    public static final String LOOP_STANDARD = "STANDARD";
    public static final String LOOP_MULTI_INSTANCE = "MULTIINSTANCE";

    public ChoreographySubProcess() {
        super();
        initializeProps();
    }

    public ChoreographySubProcess(int x, int y, String label) {
        super();
        initializeProps();
        setPos(x, y);
        setText(label);
    }

    private void initializeProps() {
        setSize(140, 140);
        setProperty(PROP_UPPER_PARTICIPANTS, "Participant A");
        setProperty(PROP_LOWER_PARTICIPANTS, "Participant B");
        setProperty(PROP_ACTIVE_PARTICIPANTS, "Participant A");
        setProperty(PROP_LOOP_TYPE, LOOP_NONE);
        String[] loop = { LOOP_NONE , LOOP_STANDARD, LOOP_MULTI_INSTANCE };
        setPropertyEditor(PROP_LOOP_TYPE, new ListSelectionPropertyEditor(loop));

        setProperty(PROP_UPPER_PARTICIPANTS_MULTI, "0");
        setProperty(PROP_LOWER_PARTICIPANTS_MULTI, "0");

    }

    @Override
    public void paintInternal(Graphics g) {
        drawChoreographySubProcess(g);
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
    }
    
    public List<String> getUpperParticipants(){
    	return Arrays.asList(getProperty(PROP_UPPER_PARTICIPANTS).split(";"));
    }
    
    public List<String> getLowerParticipants(){
    	return Arrays.asList(getProperty(PROP_LOWER_PARTICIPANTS).split(";"));
    }

    private void drawChoreographySubProcess(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setFont(BPMNUtils.defaultFont);

        g2.setPaint(getBackground());
        g2.fill(outline);

        // Draw upper participants
        int upperY = drawParticipantBands(g2,
                getProperty(PROP_UPPER_PARTICIPANTS),
                getProperty(PROP_UPPER_PARTICIPANTS_MULTI),
                getPos().y - getSize().height / 2,
                false);

        // Draw lower participant
        int lowerY = drawParticipantBands(g2,
                getProperty(PROP_LOWER_PARTICIPANTS),
                getProperty(PROP_LOWER_PARTICIPANTS_MULTI),
                getPos().y + getSize().height / 2,
                true);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw text
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        if (getProperty(PROP_COLLAPSED).equals(FALSE)) {
            BPMNUtils.drawFitText(g2, getPos().x, upperY, getSize().width - 10, 20, getText());
        } else {
            BPMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), BPMNUtils.Orientation.CENTER);
        }

        int markerPos = getPos().x;

        // Draw SubProcess Marker
        if (getProperty(PROP_COLLAPSED).toLowerCase().equals(TRUE.toLowerCase())) {
            Activity.drawSubProcessMarker(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE,getProperty(PROP_COLLAPSED).equals(TRUE));
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Standard Loop-Marker
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(LOOP_STANDARD.toLowerCase())) {
            Activity.drawStandardLoop(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
        }

        // Draw Multi Instance Loop-Marker
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(LOOP_MULTI_INSTANCE.toLowerCase())) {
            Activity.drawParallelMultiInstance(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
        }
    }

    private int drawParticipantBands(Graphics2D g2, String participants, String participants_multi, int yStart, boolean bottomUp) {
        // The distance between the participant bands
        final int DISTANCE = 20;

        // Detect upper mi participants
        int upperHeight = 0;
        StringTokenizer st = new StringTokenizer(participants_multi, ";");
        List<Boolean> upperMIStatus = new LinkedList<Boolean>();
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals(ProcessNode.TRUE)) {
                upperMIStatus.add(tok.equals(ProcessNode.TRUE));
                upperHeight += Activity.MARKER_ICON_SIZE + 3;
            } else {
                upperMIStatus.add(false);
            }
        }
        st = new StringTokenizer(participants, ";");
        upperHeight += st.countTokens() * DISTANCE;
        // Modify yStart if bottomUp
        if (bottomUp) {
            yStart -= upperHeight;
        }

        // List with FALSE
        while (upperMIStatus.size() < st.countTokens()) {
            upperMIStatus.add(false);
        }
        g2.setPaint(Color.BLACK);
        //int upperY = yStart + upperHeight;
        int currentY = yStart;
        // Iterate over all Participants: Figure draw band
        st = new StringTokenizer(participants, ";");
        int pos = 0;
        while (st.hasMoreTokens()) {
            String currentParticipant = st.nextToken();
            // Gray out background if not active
            StringTokenizer stActive = new StringTokenizer(getProperty(PROP_ACTIVE_PARTICIPANTS), ";");
            boolean match = false;
            while (stActive.hasMoreTokens()) {
                if (stActive.nextToken().equals(currentParticipant)) {
                    match = true;
                }
            }
            // Gray out current background
            if (!match) {
                RoundRectangle2D partOutline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2), currentY + 2, getSize().width, DISTANCE + (upperMIStatus.get(pos) ? Activity.MARKER_ICON_SIZE + 3 : 0), 5, 5);
                g2.setPaint(Color.LIGHT_GRAY);
                g2.fill(partOutline);
            }

            g2.setPaint(Color.BLACK);
            if (bottomUp) {
                g2.drawLine(getPos().x - (getSize().width / 2), currentY, getPos().x + (getSize().width / 2), currentY);
            }

            BPMNUtils.drawText(g2, getPos().x, currentY, getSize().width - 8, currentParticipant, BPMNUtils.Orientation.TOP);
            if (upperMIStatus.get(pos)) {
                // Draw mi marker for upper participant
                currentY += Activity.MARKER_ICON_SIZE + 3;
                Activity.drawParallelMultiInstance(g2, getPos().x, currentY + Activity.MARKER_ICON_SIZE);
            }
            if (!bottomUp) {
                g2.drawLine(getPos().x - (getSize().width / 2), currentY + DISTANCE, getPos().x + (getSize().width / 2), currentY + DISTANCE);
            }
            currentY += DISTANCE;
            pos++;
        }

        if (bottomUp) {
            return yStart;
        }
        return currentY;
    }

    /**
     * Code replicated from ChoreographyActivity, since ChoreographySubProcess
     * does not inherit ChoreographyActivity.
     * @return
     */
    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(ChoreographyTask.class);
        result.add(ChoreographySubProcess.class);
        return result;
    }

    @Override
    public boolean isCollapsed() {
        return getProperty(PROP_COLLAPSED).equalsIgnoreCase(TRUE);
    }

    @Override
    public String toString() {
        return "BPMN Choreography Sub Process";
    }
}
