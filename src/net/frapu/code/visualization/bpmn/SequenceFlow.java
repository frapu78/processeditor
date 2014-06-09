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
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.*;

/**
 *
 * @author fpu
 */
public class SequenceFlow extends ProcessEdge {
	
	protected final static Polygon sequenceFlowArrow = ProcessUtils.standardArrowFilled;   
    
    protected final static int xDefaultArrowPoints[] = {6, 8, 12, 10};
    protected final static int yDefaultArrowPoints[] = {-8, -8, 8, 8};
    protected final static Polygon defaultFlowArrow = new Polygon(xDefaultArrowPoints, yDefaultArrowPoints, 4);
    protected final static int xCondArrowPoints[] = {0, 10, 20, 10};
    protected final static int yCondArrowPoints[] = {0, -6, 0, 6};
    protected final static Polygon condFlowArrow = new Polygon(xCondArrowPoints, yCondArrowPoints, 4);
    /** The direction of this SequenceFlow. Possible values are "STANDARD, DEFAULT, CONDITIONAL" */
    public final static String PROP_SEQUENCETYPE = "sequence_type";

    public final static String TYPE_STANDARD = "STANDARD";
    public final static String TYPE_DEFAULT = "DEFAULT";
    public final static String TYPE_CONDITIONAL = "CONDITIONAL";

    public SequenceFlow() {
        super();
        initializeProperties();
    }

    public SequenceFlow(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_SEQUENCETYPE, "TYPE_STANDARD");
        String[] type = { TYPE_STANDARD , TYPE_DEFAULT, TYPE_CONDITIONAL };
        setPropertyEditor(PROP_SEQUENCETYPE, new ListSelectionPropertyEditor(type));

    }

    @Override
    public Shape getSourceShape() {
        String type = getProperty(PROP_SEQUENCETYPE);
        if (type.equals(TYPE_DEFAULT)) {
            return defaultFlowArrow;
        }
        if (type.equals(TYPE_CONDITIONAL)) {
            return condFlowArrow;
        }
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return sequenceFlowArrow;
    }

    @Override
    public Stroke getLineStroke() {
        return BPMNUtils.defaultStroke;
    }

    @Override
    public boolean isOutlineSourceArrow() {
        String type = getProperty(PROP_SEQUENCETYPE).toLowerCase();
        if (type.equals(TYPE_CONDITIONAL)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isDockingSupported() {
        return true;
    }
}
