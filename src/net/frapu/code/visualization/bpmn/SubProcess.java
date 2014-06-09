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
import net.frapu.code.visualization.Cluster;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class SubProcess extends Cluster {

    /** Loop-Type: "NONE, STANDARD, SEQUENCE, PARALLEL" */
    public static final String PROP_LOOP_TYPE = "loop_type";
    /** Transaction: 0=false 1=true */
    public static final String PROP_TRANSACTION = "transaction";
    /** Event-SubProcess: 0=false 1=true */
    public static final String PROP_EVENT_SUBPROCESS = "triggered_by_event";
    /** The property for ad-hoc sub-processes (0=FALSE,1=TRUE) */
    public final static String PROP_AD_HOC = "adhoc";
    //** The property for compensation (0=FALSE;1=TRUE) */
    public final static String PROP_COMPENSATION = "compensation";

    public SubProcess() {
        super();
        initializeProperties();
    }

    public SubProcess(int x, int y, String label) {
        super();
        setPos(x, y);
        setText(label);
        initializeProperties();
    }

    @Override
        public void setProperty(String key, String value) {
        if (key.equals(PROP_HEIGHT)) {
            try {
                int height = Integer.parseInt(value);
                if (height<30) value = "30";
            } catch (Exception e) {
                value = "120";
            }
        }
        if (key.equals(PROP_WIDTH)) {
            try {
                int width = Integer.parseInt(value);
                if (width<50) value = "50";
            } catch (Exception e) {
                value = "200";
            }
        }
        super.setProperty(key, value);
    }

    /**
     *
     */
    public void setAdHoc(){
        setProperty(PROP_AD_HOC, TRUE);
    }

    /**
     *
      */
     public void setTransaction(){
        setProperty(PROP_TRANSACTION, TRUE);
    }

    private void initializeProperties() {
        setProperty(PROP_LOOP_TYPE, Activity.LOOP_NONE);
        String[] type = { Activity.LOOP_NONE, Activity.LOOP_STANDARD, Activity.LOOP_MULTI_SEQUENCE, Activity.LOOP_MULTI_PARALLEL };
        setPropertyEditor(PROP_LOOP_TYPE, new ListSelectionPropertyEditor(type));

        setProperty(PROP_TRANSACTION, FALSE);
        setPropertyEditor(PROP_TRANSACTION, new BooleanPropertyEditor());

        setProperty(PROP_EVENT_SUBPROCESS, FALSE);
        setPropertyEditor(PROP_EVENT_SUBPROCESS, new BooleanPropertyEditor());

        setProperty(PROP_AD_HOC, FALSE);
        setPropertyEditor(PROP_AD_HOC, new BooleanPropertyEditor());

        setProperty(PROP_COMPENSATION, FALSE);
        setPropertyEditor(PROP_COMPENSATION, new BooleanPropertyEditor());

        setProperty(Activity.PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(Activity.PROP_BACKGROUND, new ColorPropertyEditor());

        int w = 200;
        int h = 120;		
        setSize(w, h);
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width/2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
    }

    @Override
    public void paintInternal(Graphics g) {
        drawSubProcess(g);
    }

    private void drawSubProcess(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Check if event sub process
        if (getProperty(PROP_EVENT_SUBPROCESS).equals(TRUE)) {
            g2.setStroke(BPMNUtils.longDashedStroke);
        } else {
            g2.setStroke(BPMNUtils.defaultStroke);
        }
        Shape outline = getOutlineShape();

        g2.setPaint(FlowObject.getBackground(getProperty(Activity.PROP_BACKGROUND)));
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Draw stereotype
        //Activity.drawStereotype(g2, this);

        // Check if Collapsed Event Sub Process
        if (getProperty(PROP_COLLAPSED).equals(TRUE) &&
                getProperty(PROP_EVENT_SUBPROCESS).equals(TRUE)) {
            // Show marker for first start event
            for (ProcessNode node: getProcessNodes()) {
                if (node instanceof MessageStartEvent |
                        node instanceof TimerStartEvent |
                        node instanceof EscalationStartEvent |
                        node instanceof ConditionalStartEvent |
                        node instanceof SignalStartEvent |
                        node instanceof MultipleStartEvent |
                        node instanceof ParallelMultipleStartEvent) {
                    // Check if is non-interrupting
                    if (node.getProperty(StartEvent.PROP_NON_INTERRUPTING).equals(TRUE)) {
                        StartEvent dummy = (StartEvent)node.clone();
                        dummy.setPos(getPos().x-getSize().width/2+dummy.getSize().width/2+5,
                                getPos().y-getSize().height/2+dummy.getSize().height/2+5);
                        dummy.paintInternal(g2);
                        break;
                    }
                }
            }
        }

        // Check if Transaction
        if (getProperty(PROP_TRANSACTION).equals(TRUE)) {
            RoundRectangle2D innerOutline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2) + 5,
                    getPos().y - (getSize().height / 2) + 5, getSize().width - 10, getSize().height - 10, 10, 10);
            g2.draw(innerOutline);
        }

        g2.setStroke(BPMNUtils.defaultStroke);
        g2.setFont(BPMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);

        int spacing = 0;
        if (getProperty(PROP_TRANSACTION).equals(TRUE)) spacing=5;

        if (getProperty(PROP_COLLAPSED).equals(FALSE)) {
            BPMNUtils.drawFitText(g2, getPos().x, getPos().y-getSize().height/2+spacing, getSize().width-10-spacing*2, 20, getText());
        } else {
            BPMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), BPMNUtils.Orientation.CENTER);
        }

        int lowerY = getPos().y+getSize().height/2;
        int markerPos = getPos().x;

        // Draw SubProcess Marker
        if (getProperty(PROP_COLLAPSED).equals(TRUE)) {
            Activity.drawSubProcessMarker(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE,getProperty(PROP_COLLAPSED).equals(TRUE));
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Standard Loop-Marker
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(Activity.LOOP_STANDARD.toLowerCase())) {
            Activity.drawStandardLoop(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Multi Instance Loop-Marker (Parallel)
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(Activity.LOOP_MULTI_PARALLEL.toLowerCase())) {
            Activity.drawParallelMultiInstance(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Multi Instance Loop-Marker (Sequence)
        if (getProperty(PROP_LOOP_TYPE).toLowerCase().equals(Activity.LOOP_MULTI_SEQUENCE.toLowerCase())) {
            Activity.drawSequentialMultiInstance(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Ad-Hoc-Marker
        if (getProperty(PROP_AD_HOC).equals(TRUE)) {
            Activity.drawAdHoc(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

        // Draw Compensation-Marker
        if (getProperty(PROP_COMPENSATION).equals(TRUE)) {
            Activity.drawCompensation(g2, markerPos, lowerY - Activity.MARKER_ICON_SIZE);
            markerPos += Activity.MARKER_ICON_SIZE * 2;
        }

    }

    @Override
    public void addProcessNode(ProcessNode n) {
        super.addProcessNode(n);
        // Check if StartEvent, if so change non_interrupting to true
        if (((n instanceof MessageStartEvent) ||
                (n instanceof TimerStartEvent) ||
                (n instanceof EscalationStartEvent) ||
                (n instanceof ConditionalStartEvent) ||
                (n instanceof SignalStartEvent) ||
                (n instanceof MultipleStartEvent) ||
                (n instanceof ParallelMultipleStartEvent)) &&
                (getProperty(PROP_EVENT_SUBPROCESS).equals(TRUE))){
            n.setProperty(StartEvent.PROP_NON_INTERRUPTING, TRUE);
        }

    }

    @Override
    public void removeProcessNode(ProcessNode n) {
        if (this.isContained(n)) {
            super.removeProcessNode(n);
            // Check if StartEvent, if so change non_interrupting to false
            if (((n instanceof MessageStartEvent) ||
                    (n instanceof TimerStartEvent) ||
                    (n instanceof EscalationStartEvent) ||
                    (n instanceof ConditionalStartEvent) ||
                    (n instanceof SignalStartEvent) ||
                    (n instanceof MultipleStartEvent) ||
                    (n instanceof ParallelMultipleStartEvent)) &&
                    (getProperty(PROP_EVENT_SUBPROCESS).equals(TRUE))){
                n.setProperty(StartEvent.PROP_NON_INTERRUPTING, FALSE);
            }
        }
    }

    @Override
    public boolean isCollapsed() {
        return getProperty(PROP_COLLAPSED).equals(TRUE);
    }



    /**
     * Code replicated from Activity, since SubProcess does not inherit Activity.
     * @return
     */
    @Override
    public List<Class<? extends ProcessNode>> getVariants() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Task.class);
        result.add(SubProcess.class);
        result.add(CallActivity.class);
        return result;
    }

}
