package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @version 13.10.2014.
 * @author Stephan
 */
public class Task extends ProcessNode {

    /** The different stereotypes */
    public static final String TYPE_USER = "USER";
    public static final String TYPE_BLOCKING_USER = "BLOCKING_USER";
    public static final String TYPE_PROCESS = "PROCESS";
    public static final String TYPE_CASE = "CASE";

    public static final String PROP_DISCRETIONARY = "discretionary";
    /** The size of the stereotype icons */
    public static final int STEREOTYPE_ICON_SIZE = 20;
    /** Properties for decorators */
    public static final String PROP_REPETITION = "repetition";
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_MANUAL_ACTIVATION = "manual activation";
    public static final String PROP_PLANNING_TABLE = "planning table";
    public static final String PROP_VISUALIZED_SHAPES = "visualized shapes";

    public static final int MARKER_ICON_SIZE = 10;

    public Task() {
        super();
        int w = 100;
        int h = 60;
        setSize(w, h);
        initializeProperties();
    }

    private void initializeProperties() {
        String[] ttype = { "", Task.TYPE_BLOCKING_USER, Task.TYPE_USER,
            Task.TYPE_PROCESS, Task.TYPE_CASE
        };
        setPropertyEditor(PROP_STEREOTYPE, new ListSelectionPropertyEditor(ttype));

        setProperty(PROP_BACKGROUND, ""+Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());

        setProperty(PROP_DISCRETIONARY, FALSE);
        setPropertyEditor(PROP_DISCRETIONARY, new BooleanPropertyEditor());

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

    public static void drawUser(Graphics2D g2, int x, int y) {
        CMMNUtils.drawImage("/cmmn/user.png", g2, x + 3, y + 4);
    }

    public static void drawBlockingUser(Graphics2D g2, int x, int y) {
        CMMNUtils.drawImage("/cmmn/blocking_user.png", g2, x + 3, y + 4);
    }

    public static void drawProcess(Graphics2D g2, int x, int y) {
        g2.setStroke(CMMNUtils.defaultStroke);
        Polygon p = new Polygon();

        p.addPoint(x + STEREOTYPE_ICON_SIZE / 2, y);
        p.addPoint(x + (STEREOTYPE_ICON_SIZE / 6), y - STEREOTYPE_ICON_SIZE / 4);
        p.addPoint(x - (STEREOTYPE_ICON_SIZE / 2), y - STEREOTYPE_ICON_SIZE / 4);
        p.addPoint(x - (STEREOTYPE_ICON_SIZE / 6), y);
        p.addPoint(x - (STEREOTYPE_ICON_SIZE / 2), y + STEREOTYPE_ICON_SIZE / 4);
        p.addPoint(x + (STEREOTYPE_ICON_SIZE / 6), y + STEREOTYPE_ICON_SIZE / 4);

        g2.setPaint(Color.BLACK);
        g2.draw(p);
    }

    public static void drawCase(Graphics2D g2, int x, int y) {
        g2.setStroke(CMMNUtils.defaultStroke);
        g2.setPaint(Color.BLACK);

        Polygon body = new Polygon();
        body.addPoint(x - STEREOTYPE_ICON_SIZE / 2, y - (STEREOTYPE_ICON_SIZE / 2 - STEREOTYPE_ICON_SIZE / 5));
        body.addPoint(x + STEREOTYPE_ICON_SIZE / 2, y - (STEREOTYPE_ICON_SIZE / 2 - STEREOTYPE_ICON_SIZE / 5));
        body.addPoint(x + STEREOTYPE_ICON_SIZE / 2, y + (STEREOTYPE_ICON_SIZE / 2));
        body.addPoint(x - STEREOTYPE_ICON_SIZE / 2, y + (STEREOTYPE_ICON_SIZE / 2));

        Polygon title = new Polygon();
        title.addPoint(x - STEREOTYPE_ICON_SIZE / 2 + STEREOTYPE_ICON_SIZE / 5, y - (STEREOTYPE_ICON_SIZE / 2 - STEREOTYPE_ICON_SIZE / 5));
        title.addPoint(x - STEREOTYPE_ICON_SIZE / 2 + STEREOTYPE_ICON_SIZE / 5, y - (STEREOTYPE_ICON_SIZE / 2));
        title.addPoint(x, y - (STEREOTYPE_ICON_SIZE / 2));
        title.addPoint(x, y - (STEREOTYPE_ICON_SIZE / 2 - STEREOTYPE_ICON_SIZE / 5));

        g2.draw(body);
        g2.draw(title);
    }

    public static void drawStereotype(Graphics2D g2, ProcessNode n) {
        String stereotype = n.getStereotype();
        if (stereotype.length() > 0) {
           if (stereotype.equalsIgnoreCase(TYPE_PROCESS)) {
               Task.drawProcess(g2,
                       n.getPos().x - n.getSize().width / 2 + STEREOTYPE_ICON_SIZE / 2 + 5,
                       n.getPos().y - n.getSize().height / 2 + STEREOTYPE_ICON_SIZE / 2);
           } else if (stereotype.equalsIgnoreCase(TYPE_CASE)) {
               Task.drawCase(g2,
                       n.getPos().x - n.getSize().width / 2 + STEREOTYPE_ICON_SIZE / 2 + 5,
                       n.getPos().y - n.getSize().height / 2 + STEREOTYPE_ICON_SIZE / 2);
           } else if (stereotype.equalsIgnoreCase(TYPE_USER)) {
               Task.drawUser(g2,
                       n.getTopLeftPos().x,
                       n.getTopLeftPos().y);
           } else if (stereotype.equalsIgnoreCase(TYPE_BLOCKING_USER)) {
               Task.drawBlockingUser(g2,
                       n.getTopLeftPos().x,
                       n.getTopLeftPos().y);

           }
        }
    }

    @Override
    protected void paintInternal(Graphics g) {
        int currentMarker = 0;

        Graphics2D g2 = (Graphics2D) g;
        RoundRectangle2D taskShape = new RoundRectangle2D.Double();
        taskShape.setRoundRect(getPos().x - getSize().width / 2,
                getPos().y - getSize().height / 2,
                getSize().width, getSize().height,
                10,
                10
        );

        g2.setPaint(getBackground());
        g2.fill(taskShape);
        g2.setPaint(Color.BLACK);
        if (getProperty(PROP_DISCRETIONARY).equalsIgnoreCase(TRUE)) {
            g2.setStroke(CMMNUtils.dashedStroke);
        } else {
            g2.setStroke(CMMNUtils.defaultStroke);
        }
        g2.draw(taskShape);
        Task.drawStereotype(g2, this);

        g2.setFont(CMMNUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        CMMNUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(), CMMNUtils.Orientation.CENTER);

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

        if (getProperty(PROP_PLANNING_TABLE).equalsIgnoreCase(TRUE)
                && (getProperty(PROP_STEREOTYPE).equalsIgnoreCase(TYPE_BLOCKING_USER)
                || getProperty(PROP_STEREOTYPE).equalsIgnoreCase(TYPE_USER))) {
            CMMNUtils.drawPlanningTable(
                    g2,
                    getPos().x - getSize().width / 8,
                    getPos().y - getSize().height / 2,
                    2 * MARKER_ICON_SIZE,
                    getProperty(PROP_VISUALIZED_SHAPES).equalsIgnoreCase(TRUE)
            );
        }
    }

    @Override
    protected Shape getOutlineShape() {
        RoundRectangle2D result = new RoundRectangle2D.Double();
        result.setRoundRect(getPos().x - getSize().width / 2,
                getPos().y - getSize().height / 2,
                getSize().width, getSize().height,
                10,
                10
        );
        return result;
    }

    @Override
    public String toString() {
        return "CMMN Task ("+getText()+")";
    }
}
