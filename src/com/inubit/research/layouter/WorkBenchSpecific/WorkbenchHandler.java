/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.WorkBenchSpecific;

import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.uml.ClassModel;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.adapter.ProcessEdgeAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

/**
 * 
 * encapsulates special handling required after layouting a model,
 * which is only applicable for the inubit Workbench and is not useful for the IS.
 * @author ff
 *
 */
public class WorkbenchHandler {
	
	public static void postProcess(ProcessLayouter layouter,ProcessModel model) {
		// special handling for certain model types
        if(layouter instanceof SugiyamaLayoutAlgorithm && model instanceof ClassModel) {
        	SugiyamaLayoutAlgorithm _layouter = (SugiyamaLayoutAlgorithm) layouter;
        	setDockingPoints(model,_layouter);
        }
        List<EdgeInterface> _edgesToRoute = layouter.getUnroutedEdges();
        if(_edgesToRoute != null) {
        	for(EdgeInterface edge :_edgesToRoute) {
                        if (edge instanceof ProcessEdgeAdapter) {
                            ProcessEdge _e = ((ProcessEdgeAdapter)edge).getEdge();
                            model.getUtils().getRoutingPointLayouter().optimizeRoutingPoints(_e, _e.getSource());
                    }
        	}
        }
		
	}

	/**
	 * @param model 
	 * @param layouter 
	 * 
	 */
	private static void setDockingPoints(ProcessModel model, SugiyamaLayoutAlgorithm layouter) {		
    	if("1".equals(Configuration.getInstance().getProperty(LayoutHelper.CONF_SET_CONNECTION_POINTS))) {
    		//correcting points directly at node
    		for(ProcessEdge edge:model.getEdges()) {
    			LayoutHelper.setDockingPointOffset(edge,layouter.getTopToBottom());
    		}    	
    	}    	
	}

}
