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

import java.awt.Graphics2D;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

/**
 *
 * @author fpu
 */
public class EventBasedGateway extends Gateway {

    /** Defines if this Gateway is instantiating: NONE, EXCLUSIVE, PARALLEL */
    public final static String PROP_INSTANTIATE = "instantiate";

    public final static String TYPE_INSTANTIATE_NONE = "NONE";
    public final static String TYPE_INSTANTIATE_EXCLUSIVE = "EXCLUSIVE";
    public final static String TYPE_INSTANTIATE_PARALLEL = "PARALLEL";

    public EventBasedGateway() {
        super();
        initializeProperties();
    }

    public void initializeProperties() {
        setProperty(PROP_INSTANTIATE, TYPE_INSTANTIATE_NONE);
        String[] inst = { TYPE_INSTANTIATE_NONE , TYPE_INSTANTIATE_EXCLUSIVE, TYPE_INSTANTIATE_PARALLEL };
        setPropertyEditor(PROP_INSTANTIATE, new ListSelectionPropertyEditor(inst));
    }

    @Override
    protected void drawMarker(Graphics2D g2) {
        // Check if exclusive instantiating
        if (getProperty(PROP_INSTANTIATE).toLowerCase().equals(TYPE_INSTANTIATE_EXCLUSIVE.toLowerCase())) {
            drawInitiatingXOREventGateway(g2);
            return;
        }
        // Check if parallel instantiating
        if (getProperty(PROP_INSTANTIATE).toLowerCase().equals(TYPE_INSTANTIATE_PARALLEL.toLowerCase())) {
            drawInitiatingParallelEventGateway(g2);
            return;
        }
        
        drawXOREventGateway(g2);
    }
    
}
