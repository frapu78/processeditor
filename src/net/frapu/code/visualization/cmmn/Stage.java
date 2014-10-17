package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

import java.awt.*;

/**
 * @author Stephan
 * @version 13.10.2014.
 */
public class Stage extends Cluster {

    public static final String PROP_DISCRETIONARY = "discretionary";
    /** The size of the marker icons */
    public static final int MARKER_ICON_SIZE = 10;
    /** Property for the autoComplete Marker */
    public static final String PROP_AUTOCOMPLETE = "autocomplete";
    public static final String PROP_REPETITION = "repetition";
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_MANUAL_ACTIVATION = "manual activation";
    public static final String PROP_PLANNING_TABLE = "planning table";
    public static final String PROP_VISUALIZED_SHAPES = "visualized shapes";

    public Stage() {
        super();
        setSize(240, 100);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_DISCRETIONARY, FALSE);
        setPropertyEditor(PROP_DISCRETIONARY, new BooleanPropertyEditor());

        setProperty(PROP_AUTOCOMPLETE, FALSE);
        setPropertyEditor(PROP_AUTOCOMPLETE, new BooleanPropertyEditor());

        setProperty(PROP_REPETITION, FALSE);
        setPropertyEditor(PROP_REPETITION, new BooleanPropertyEditor());

        setProperty(PROP_REQUIRED, FALSE);
        setPropertyEditor(PROP_REQUIRED, new BooleanPropertyEditor());

        setProperty(PROP_MANUAL_ACTIVATION, FALSE);
        setPropertyEditor(PROP_MANUAL_ACTIVATION, new BooleanPropertyEditor());

        setProperty(PROP_PLANNING_TABLE, FALSE);
        setPropertyEditor(PROP_PLANNING_TABLE, new BooleanPropertyEditor());

        setProperty(PROP_VISUALIZED_SHAPES, FALSE);
        setPropertyEditor(PROP_VISUALIZED_SHAPES, new BooleanPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        int currentMarker = 1;
        Graphics2D g2 = (Graphics2D) g;

        Polygon stageShape = new Polygon();
        stageShape.addPoint(getPos().x - (getSize().width / 2), getPos().y - (getSize().height) * 7 / 16);
        stageShape.addPoint(getPos().x - (getSize().width * 7 / 16), getPos().y - (getSize().height / 2));
        stageShape.addPoint(getPos().x + (getSize().width * 7 / 16), getPos().y - (getSize().height / 2));
        stageShape.addPoint(getPos().x + (getSize().width / 2), getPos().y - (getSize().height) * 7 / 16);
        stageShape.addPoint(getPos().x + (getSize().width / 2), getPos().y + (getSize().height) * 7 / 16);
        stageShape.addPoint(getPos().x + (getSize().width * 7 / 16), getPos().y + (getSize().height / 2));
        stageShape.addPoint(getPos().x - (getSize().width * 7 / 16), getPos().y + (getSize().height / 2));
        stageShape.addPoint(getPos().x - (getSize().width / 2), getPos().y + (getSize().height) * 7 / 16);
        g2.setPaint(getBackground());
        g2.fill(stageShape);
        g2.setPaint(Color.BLACK);
        if (getProperty(PROP_DISCRETIONARY).equalsIgnoreCase(TRUE)) {
            g2.setStroke(CMMNUtils.dashedStroke);
        } else {
            g2.setStroke(CMMNUtils.defaultStroke);
        }
        g2.draw(stageShape);

        g2.setStroke(CMMNUtils.defaultStroke);
        g2.setFont(CMMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        if (getProperty(PROP_COLLAPSED).equals(TRUE)) {
            CMMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), CMMNUtils.Orientation.CENTER);
        } else {
            CMMNUtils.drawFitText(g2,
                    getPos().x,
                    getPos().y - (getSize().height / 2 - 5),
                    getSize().width * 7 / 8 - 8,
                    20,
                    getText());
        }

        drawStageMarker(g2, getPos().x, getPos().y + getSize().height / 2 - MARKER_ICON_SIZE, getProperty(PROP_COLLAPSED).equalsIgnoreCase(TRUE));


        if (getProperty(PROP_AUTOCOMPLETE).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawAutoCompleteMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
            currentMarker = CMMNModel.markerPosMap.get(new Integer(currentMarker));
        }

        if (getProperty(PROP_REPETITION).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawRepetitionMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
            currentMarker = CMMNModel.markerPosMap.get(new Integer(currentMarker));
        }

        if (getProperty(PROP_REQUIRED).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawRequiredMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
            currentMarker = CMMNModel.markerPosMap.get(new Integer(currentMarker));
        }

        if (getProperty(PROP_MANUAL_ACTIVATION).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawManualActivationMarker(
                    g2,
                    getPos().x - MARKER_ICON_SIZE * 2 * currentMarker,
                    getPos().y + (getSize().height / 2) - MARKER_ICON_SIZE,
                    MARKER_ICON_SIZE
            );
        }

        if (getProperty(PROP_PLANNING_TABLE).equalsIgnoreCase(TRUE)) {
            CMMNUtils.drawPlanningTable(
                    g2,
                    getPos().x - getSize().width / 8,
                    getPos().y - getSize().height / 2,
                    2 * MARKER_ICON_SIZE,
                    getProperty(PROP_VISUALIZED_SHAPES).equalsIgnoreCase(TRUE)
            );
        }
    }

    private void drawStageMarker(Graphics2D g2, int x, int y, boolean plus) {
        g2.setStroke(CMMNUtils.defaultStroke);
        g2.drawRect(x - MARKER_ICON_SIZE, y - MARKER_ICON_SIZE, MARKER_ICON_SIZE * 2, MARKER_ICON_SIZE * 2);
        if (plus) {
            g2.drawLine(x, y - MARKER_ICON_SIZE / 3, x, y + MARKER_ICON_SIZE / 3);
        }
        g2.drawLine(x - MARKER_ICON_SIZE / 3, y, x + MARKER_ICON_SIZE / 3, y);
    }

    @Override
    protected Shape getOutlineShape() {
        Polygon result = new Polygon();

        result.addPoint(getPos().x - (getSize().width / 2), getPos().y - (getSize().height) * 7 / 16);
        result.addPoint(getPos().x - (getSize().width * 7 / 16), getPos().y - (getSize().height / 2));
        result.addPoint(getPos().x + (getSize().width * 7 / 16), getPos().y - (getSize().height / 2));
        result.addPoint(getPos().x + (getSize().width / 2), getPos().y - (getSize().height) * 7 / 16);
        result.addPoint(getPos().x + (getSize().width / 2), getPos().y + (getSize().height) * 7 / 16);
        result.addPoint(getPos().x + (getSize().width * 7 / 16), getPos().y + (getSize().height / 2));
        result.addPoint(getPos().x - (getSize().width * 7 / 16), getPos().y + (getSize().height / 2));
        result.addPoint(getPos().x - (getSize().width / 2), getPos().y + (getSize().height) * 7 / 16);

        return result;
    }

    @Override
    public String toString() {
        return "CMMN Stage ("+getText()+")";
    }
}
