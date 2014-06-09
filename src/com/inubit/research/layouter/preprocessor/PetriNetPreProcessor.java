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

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.PetriNetModelInterface;
import com.inubit.research.layouter.interfaces.PetriNetNodeInterface;

/**
 * @author ff
 *
 */
public class PetriNetPreProcessor implements IPreProcessor {

	private ArrayList<EdgeInterface> f_switchedEdges = new ArrayList<EdgeInterface>();
	private LonelyNodesRemover f_lnr = new LonelyNodesRemover();
	
	@Override
	public void process(AbstractModelAdapter model) {
		//making sure no Comment is a source
		for(EdgeInterface e:model.getEdges()) {
			if(e instanceof PetriNetNodeInterface) {
				PetriNetNodeInterface _n = (PetriNetNodeInterface) e;
				if(_n.isComment()) {
					LayoutHelper.switchEdge(e);
					f_switchedEdges.add(e);
				}
			}
			
		}
		f_lnr.process(model);
		
			
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof PetriNetModelInterface;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		f_lnr.unprocess(model);
		for(EdgeInterface e: f_switchedEdges) {
			LayoutHelper.switchEdge(e);
		}
		
	}
	
	
	

}
