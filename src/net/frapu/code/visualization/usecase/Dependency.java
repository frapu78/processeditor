/**
 * Process Editor - Use Case Package
 *
 * (C) 2015 the authors
 *
 * http://frapu.de
 */
package net.frapu.code.visualization.usecase;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

import java.awt.*;

/**
 *
 * UML Use Case Dependency.
 *
 * @author fpu
 */
public class Dependency extends ProcessEdge {

    protected final static int xArrowSourcePoints[] = {0, 10, 12, 2, 12, 10};
    protected final static int yArrowSourcePoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon arrowSource = new Polygon(xArrowSourcePoints, yArrowSourcePoints, 6);

    protected final static int xArrowTargetPoints[] = {0, -10, -12, -2, -12, -10};
    protected final static int yArrowTargetPoints[] = {0, 6, 6, 0, -6, -6};
    protected final static Polygon arrowTarget = new Polygon(xArrowTargetPoints, yArrowTargetPoints, 6);

    public final static String PROP_DIRECTION = "direction";
    public final static String DIRECTION_SOURCE = "SOURCE";
    public final static String DIRECTION_TARGET = "TARGET";
    public final static String DIRECTION_BOTH = "BOTH";
    public final static String DIRECTION_NONE = "NONE";

    public Dependency() {
        super();
        initializeProperties();
    }

    public Dependency(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_DIRECTION, DIRECTION_NONE);
        setPropertyEditor(PROP_DIRECTION, new ListSelectionPropertyEditor(new String[]{DIRECTION_SOURCE, DIRECTION_TARGET, DIRECTION_NONE, DIRECTION_BOTH}));
    }

    @Override
    public Shape getSourceShape() {
        if (this.getProperty(PROP_DIRECTION).equals(DIRECTION_SOURCE) ||
                this.getProperty(PROP_DIRECTION).equals(DIRECTION_BOTH))
            return arrowSource;

        return null;
    }

    @Override
    public Shape getTargetShape() {
        if (this.getProperty(PROP_DIRECTION).equals(DIRECTION_TARGET) ||
                this.getProperty(PROP_DIRECTION).equals(DIRECTION_BOTH))
            return arrowTarget;

        return null;
    }

    @Override
    public Stroke getLineStroke() {
        return UseCaseUtils.dashedStroke;
    }

    @Override
    public boolean isOutlineSourceArrow() {
        return false;
    }

}
