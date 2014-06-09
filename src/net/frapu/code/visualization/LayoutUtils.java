/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Point;

import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.orgChart.OrgChartModel;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.twf.TWFModel;
import net.frapu.code.visualization.uml.ClassModel;
import net.frapu.code.visualization.xforms.XFormsModel;

import com.inubit.research.layouter.adapter.BPMNModelAdapter;
import com.inubit.research.layouter.adapter.OrgChartModelAdapter;
import com.inubit.research.layouter.adapter.PetriNetModelAdapter;
import com.inubit.research.layouter.adapter.ProcessModelAdapter;
import com.inubit.research.layouter.adapter.TWFModelAdapter;
import com.inubit.research.layouter.adapter.UMLModelAdapter;
import com.inubit.research.layouter.adapter.XFormsModelAdapter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;

/**
 * @author ff
 *
 */
public class LayoutUtils {

    /**
     * Moves all nodes inside a process model inside positive coordinations,
     * starting from (10,10).
     * @param model
     */
    public static void alignProcessModel(ProcessModel model) {
        int lowX = Integer.MAX_VALUE;
        int lowY = Integer.MAX_VALUE;
        // Find lowest positions
        for (ProcessNode node : model.getNodes()) {
            int startX = node.getPos().x - (node.getSize().width / 2);
            int startY = node.getPos().y - (node.getSize().height / 2);
            if (startX < lowX) {
                lowX = startX;
            }
            if (startY < lowY) {
                lowY = startY;
            }
        }
        // Move all nodes to positive coordinations
        int diffX = 10 - lowX;
        int diffY = 10 - lowY;
        for (ProcessNode node : model.getNodes()) {
        	//do it this way so nodes within a Cluster do not get moved twice
        	node.setProperty(ProcessNode.PROP_XPOS, ""+(node.getPos().x + diffX));
        	node.setProperty(ProcessNode.PROP_YPOS, ""+(node.getPos().y + diffY));
        }
        // Move all routing points
        for (ProcessEdge edge : model.getEdges()) {
            if (edge.getRoutingPoints().size() > 2) {
                for (int i = 1; i < edge.getRoutingPoints().size() - 1; i++) {
                    Point p = edge.getRoutingPoints().get(i);
                    p.setLocation(p.x + diffX, p.y + diffY);
                    edge.moveRoutingPoint(i-1, p);
                }
            }
        }
    }   
	

	/**
	 * @param model
	 * @return
	 */
	public static AbstractModelAdapter getAdapter(ProcessModel model) {
		if(model instanceof BPMNModel) {
			return new BPMNModelAdapter((BPMNModel)model);
		}else if(model instanceof PetriNetModel) {
			return new PetriNetModelAdapter((PetriNetModel)model);
		}else if(model instanceof ClassModel) {
			return new UMLModelAdapter((ClassModel)model);
		}else if(model instanceof TWFModel) {
			return new TWFModelAdapter((TWFModel)model);
		}else if(model instanceof XFormsModel) {
			return new XFormsModelAdapter((XFormsModel)model);
		}else if(model instanceof OrgChartModel) {
			return new OrgChartModelAdapter((OrgChartModel)model);
		}
		return new ProcessModelAdapter(model);
	}
}
