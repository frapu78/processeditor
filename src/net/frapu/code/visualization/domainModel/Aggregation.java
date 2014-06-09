/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 inubit AG
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author ff
 */
public class Aggregation extends ProcessEdge {

    protected final static int xArrowPoints[] = {0, 7, 14, 7};
    protected final static int yArrowPoints[] = {0, 4, 0, -4};
    protected final static Polygon aggArrow = new Polygon(xArrowPoints, yArrowPoints, xArrowPoints.length);
    protected final static int xNavAggArrowPoints[] = {0, 10, 17, 27, 29, 19, 29, 27, 17, 10, 0};
    protected final static int yNavAggArrowPoints[] = {0, 7, 0, 6, 6, 0, -6, -6, 0, -7, 0};
    protected final static Polygon navAggArrow = new Polygon(xNavAggArrowPoints, yNavAggArrowPoints, yNavAggArrowPoints.length);
    protected final static int xArrowTargetPoints[] = {0, -10, -12, -2, -12, -10};
    protected final static int yArrowTargetPoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon arrowTarget = new Polygon(xArrowTargetPoints, yArrowTargetPoints, 6);
    public final static String PROP_SOURCE_NAME = "sourceName";
    public final static String PROP_SOURCE_MULTIPLICITY = "sourceMultiplicity";
    public final static String PROP_SOURCE_VISIBILITY = "sourceVisibility";
    public final static String PROP_TARGET_NAME = "targetName";
    public final static String PROP_TARGET_MULTIPLICITY = "targetMultiplicity";
    public final static String PROP_TARGET_VISIBILITY = "targetVisibility";
    public final static String PROP_COMPOSITION = "composition";
    public final static String PROP_DIRECTION = "direction";
    public final static String DIRECTION_SOURCE = "SOURCE";
    public final static String DIRECTION_TARGET = "TARGET";
    public final static String DIRECTION_BOTH = "BOTH";
    public final static String DIRECTION_NONE = "NONE";

    public Aggregation() {
        super();
        initializeProperties();
    }

    public Aggregation(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_COMPOSITION, FALSE);
        setProperty(PROP_SOURCE_NAME, "");
        setProperty(PROP_TARGET_NAME, "");
        setProperty(PROP_SOURCE_VISIBILITY, "");
        setProperty(PROP_TARGET_VISIBILITY, "");
        setProperty(PROP_SOURCE_MULTIPLICITY, "1..*");
        setProperty(PROP_TARGET_MULTIPLICITY, "1..*");
        setProperty(PROP_DIRECTION, DIRECTION_NONE);
        setPropertyEditor(PROP_COMPOSITION, new BooleanPropertyEditor());
        setPropertyEditor(PROP_DIRECTION, new ListSelectionPropertyEditor(new String[]{DIRECTION_SOURCE, DIRECTION_TARGET, DIRECTION_NONE, DIRECTION_BOTH}));
        setPropertyEditor(PROP_SOURCE_VISIBILITY, new ListSelectionPropertyEditor(Visibility.stringValues()));
        setPropertyEditor(PROP_TARGET_VISIBILITY, new ListSelectionPropertyEditor(Visibility.stringValues()));
    }

    @Override
    public Shape getSourceShape() {
        if (this.getProperty(PROP_DIRECTION).equals(DIRECTION_SOURCE)
                || this.getProperty(PROP_DIRECTION).equals(DIRECTION_BOTH)) {
            return navAggArrow;
        }

        return aggArrow;
    }

    @Override
    public Shape getTargetShape() {
        if (this.getProperty(PROP_DIRECTION).equals(DIRECTION_TARGET)
                || this.getProperty(PROP_DIRECTION).equals(DIRECTION_BOTH)) {
            return arrowTarget;
        }

        return null;
    }

    @Override
    public Stroke getLineStroke() {
        return DomainUtils.thinStroke;
    }

    @Override
    public boolean isOutlineSourceArrow() {
        return getProperty(PROP_COMPOSITION).equals(FALSE);
    }
}
