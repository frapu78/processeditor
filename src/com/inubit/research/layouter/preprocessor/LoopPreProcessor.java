/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;

/**
 * removes loops
 * @author ff
 *
 */
public class LoopPreProcessor implements IPreProcessor {

	private List<EdgeInterface> f_loopedEdges = new ArrayList<EdgeInterface>();
	
	@Override
	public void process(AbstractModelAdapter model) {
		f_loopedEdges = removeLoopEdges(model);
	}

	public static List<EdgeInterface> removeLoopEdges(AbstractModelAdapter model) {
		return removeLoopEdges(model, model.getEdges());
	}
	
	public static List<EdgeInterface> removeLoopEdges(AbstractModelAdapter model,Collection<EdgeInterface> edgesToScan) {
		ArrayList<EdgeInterface> _loopedEdges = new ArrayList<EdgeInterface>();
		for(EdgeInterface e:edgesToScan) {
			if(e.getSource().equals(e.getTarget())) {
				_loopedEdges.add(e);
				model.removeEdge(e);
			}
		}
		return _loopedEdges;
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return true; //support everything
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		unprocess(model, f_loopedEdges, true);
	}
	
	public static void unprocess(AbstractModelAdapter model,List<EdgeInterface> loopedEdges,boolean routeEdges) {
		for(EdgeInterface e:loopedEdges) {
			model.addEdge(e);
			if(routeEdges)
				LayoutHelper.routeSelfEdge(e);
		}
		loopedEdges.clear();
	}
}
