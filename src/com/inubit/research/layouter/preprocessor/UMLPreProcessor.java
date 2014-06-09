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
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.UMLEdgeInterface;
import com.inubit.research.layouter.interfaces.UMLModelInterface;

/**
 * @author ff
 *
 */
public class UMLPreProcessor implements IPreProcessor {

	private ArrayList<EdgeInterface> f_switchedEdges = new ArrayList<EdgeInterface>();
	
	@Override
	public void process(AbstractModelAdapter model) {
		f_switchedEdges.clear();
		for(EdgeInterface e:model.getEdges()) {
			if(e instanceof UMLEdgeInterface) {
				UMLEdgeInterface _ue = (UMLEdgeInterface) e;
				if(_ue.isGeneralization()) {
					LayoutHelper.switchEdge(_ue);
					f_switchedEdges.add(_ue);
				}
			}
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof UMLModelInterface;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		for(EdgeInterface e:f_switchedEdges) {
			LayoutHelper.invertRoutingPoints(e);
			LayoutHelper.switchEdge(e);
		}
	}

}
