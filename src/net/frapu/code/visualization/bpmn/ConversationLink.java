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

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.*;
import java.awt.*;

/**
 *
 * @author fpu
 */
public class ConversationLink extends ProcessEdge {

    /** The fork status of this edge (0,1) */
    public final String PROP_FORK = "fork";

    protected final static int xArrowTargetPoints[] = {0, 0, 20, 0, 0, 18};
    protected final static int yArrowTargetPoints[] = {8, 10, 0, -10, -8, 0};
    protected final static Polygon forkArrowTarget = new Polygon(xArrowTargetPoints, yArrowTargetPoints, 6);

    public ConversationLink() {
        super();
        initializeProperties();
    }

    public ConversationLink(ProcessNode source, ProcessNode target) {
        super(source, target);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_FORK, FALSE);
        setPropertyEditor(PROP_FORK, new BooleanPropertyEditor());
    }

    @Override
    public Shape getSourceShape() {
        // Fork is not contained in recent BPMN 2.0 draft
//        String forkStatus = getProperty(PROP_FORK).toLowerCase();
//        if (forkStatus.equals(TRUE)) {
//            return forkArrowTarget;
//        }
        return null;
    }

    @Override
    public Shape getTargetShape() {
        return null;
    }

    @Override
    public Stroke getLineStroke() {
        return BPMNUtils.doubleLinedStroke;
    }
}
