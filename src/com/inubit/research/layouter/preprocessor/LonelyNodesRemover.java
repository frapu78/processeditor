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
import java.util.LinkedList;
import java.util.List;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class LonelyNodesRemover implements IPreProcessor{
	
	private ArrayList<NodeInterface> f_removedNodes = new ArrayList<NodeInterface>();

	@Override
	public void process(AbstractModelAdapter model) {
		//removing lonely floating objects for the layout process
		List<NodeInterface> _nodes = new LinkedList<NodeInterface>(model.getNodes());
		for(EdgeInterface e:model.getEdges()) {
			_nodes.remove(e.getSource());
			_nodes.remove(e.getTarget());
		}
		for(NodeInterface n:_nodes) {
			model.removeNode(n);
			f_removedNodes.add(n);
		}
	}

	/**
	 * no standard application!
	 * has to be explicitly called by an algorithm or preprocessor
	 */
	@Override
	public boolean supports(AbstractModelAdapter model) {
		return false;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		for(NodeInterface n: f_removedNodes) {
			model.addNode(n);
		}
	}


}
