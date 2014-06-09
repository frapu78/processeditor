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
import java.util.HashSet;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.UMLEdgeInterface;
import com.inubit.research.layouter.interfaces.UMLModelInterface;

/**
 * @author ff
 *
 */
public class HierarchyExtractor implements IPreProcessor{

	private ArrayList<EdgeInterface> f_switchedEdges = new ArrayList<EdgeInterface>();
	private ArrayList<NodeInterface> f_removedNodes = new ArrayList<NodeInterface>();
	private ArrayList<EdgeInterface> f_removedEdges = new ArrayList<EdgeInterface>();
	private HashSet<NodeInterface> f_selectedNodes;

	
	@Override
	public void process(AbstractModelAdapter model) {
		f_selectedNodes = new HashSet<NodeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e instanceof UMLEdgeInterface) {
				UMLEdgeInterface _umledge = (UMLEdgeInterface) e;
				if(_umledge.isGeneralization()|| _umledge.isComposition() || _umledge.isAggregation()) {
					f_selectedNodes.add((NodeInterface) e.getSource());
					f_selectedNodes.add((NodeInterface)e.getTarget());
					if(_umledge.isGeneralization()) {
						f_switchedEdges.add(e);
						LayoutHelper.switchEdge(e);
					}
				}else {
					f_removedEdges.add(e);
					model.removeEdge(e);
				}
			}
		}
		for(NodeInterface n:model.getNodes()) {
			if(!(f_selectedNodes.contains(n))) {
				f_removedNodes.add(n);
				model.removeNode(n);
			}
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof UMLModelInterface;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		for(NodeInterface n:f_removedNodes) {
			model.addNode(n);
		}
		for(EdgeInterface e:f_switchedEdges) {
			LayoutHelper.switchEdge(e);
		}
		for(EdgeInterface e:f_removedEdges) {
			model.addEdge(e);
		}
	}
	
	public HashSet<NodeInterface> getHierarchyNodes(){
		return f_selectedNodes;
	}

}
