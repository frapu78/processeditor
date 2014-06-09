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

import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;
import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;

/**
 *
 * @author frank
 */
public class Association extends ProcessEdge {

    protected final static int xArrowTargetPoints[] = {0, -10, -12, -2, -12, -10};
    protected final static int yArrowTargetPoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon associationFlowArrowTarget = new Polygon(xArrowTargetPoints, yArrowTargetPoints, 6);

    protected final static int xArrowSourcePoints[] = {0, 10, 12, 2, 12, 10};
    protected final static int yArrowSourcePoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon associationFlowArrowSource = new Polygon(xArrowSourcePoints, yArrowSourcePoints, 6);


    private final static float dash1[] = {2.0f};
    public final static BasicStroke annotationStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            1.0f, dash1, 0.0f);

    /** The direction of this Association. Possible values are "SOURCE, TARGET, BOTH, NONE" */
    public final static String PROP_DIRECTION = "direction";

    public final static String DIRECTION_SOURCE = "SOURCE";
    public final static String DIRECTION_TARGET = "TARGET";
    public final static String DIRECTION_BOTH = "BOTH";
    public final static String DIRECTION_NONE = "NONE";

    public Association() {
        super();
        initializeProperties();
    }
    
    public Association(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_DIRECTION, DIRECTION_TARGET);
        String[] direction = { DIRECTION_NONE, DIRECTION_SOURCE, DIRECTION_TARGET, DIRECTION_BOTH };
        setPropertyEditor(PROP_DIRECTION, new ListSelectionPropertyEditor(direction));
    }

    @Override
    public Shape getSourceShape() {
        String direction = getProperty(PROP_DIRECTION).toUpperCase();
        if (direction.equals(DIRECTION_SOURCE) || direction.equals(DIRECTION_BOTH)) {
            return associationFlowArrowSource;
        }
        return null;
    }

    @Override
    public Shape getTargetShape() {
        String direction = getProperty(PROP_DIRECTION).toUpperCase();
        if (direction.equals(DIRECTION_TARGET) || direction.equals(DIRECTION_BOTH)) {
            return associationFlowArrowTarget;
        }
        return null;
    }

    @Override
    public Stroke getLineStroke() {
        return annotationStroke;
    }
    
}
