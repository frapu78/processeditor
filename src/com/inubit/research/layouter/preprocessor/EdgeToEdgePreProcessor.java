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

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.ObjectInterface;

import com.inubit.research.layouter.preprocessor.IPreProcessor;

/**
 * handles edges that have an edge as target
 * @author ff
 *
 */
public class EdgeToEdgePreProcessor implements IPreProcessor {
	
	private ArrayList<EdgeInterface> f_edges = new ArrayList<EdgeInterface>();
	private ArrayList<DummyEdgeDocker> f_dummyDockers = new ArrayList<DummyEdgeDocker>();
	private ArrayList<EdgeInterface> f_edgeTargets = new ArrayList<EdgeInterface>();

	@Override
	public void process(AbstractModelAdapter model) {
		f_edges.clear();
		f_dummyDockers.clear();
		f_edgeTargets.clear();
		
		for(EdgeInterface e: model.getEdges()) {
			ObjectInterface _n;
			boolean _target = true;
			if(e.getTarget() instanceof EdgeInterface) {
				_n = (ObjectInterface) e.getTarget();								
			}else if(e.getSource() instanceof EdgeInterface) {
				_n = (ObjectInterface) e.getSource();
				_target = false;
			}else {
				continue;
			}
			
			f_edges.add(e);
			DummyEdgeDocker _d = new DummyEdgeDocker((EdgeInterface)_n);
			f_dummyDockers.add(_d);
			if(_target)
				e.setTarget(_d);
			else 
				e.setSource(_d);
			model.addDummyNode(_d);
			f_edgeTargets.add((EdgeInterface)_n);
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return true; //support all model types
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		for(int i = 0;i<f_edges.size();i++) {
			if(f_edges.get(i).getTarget() instanceof DummyEdgeDocker) {
				f_edges.get(i).setTarget(f_edgeTargets.get(i));
				(model).removeDummyNode(f_dummyDockers.get(i));		
			}else {
				f_edges.get(i).setSource(f_edgeTargets.get(i));
				(model).removeDummyNode(f_dummyDockers.get(i));		
			}
		}
	}

}
