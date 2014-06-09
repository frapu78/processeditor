/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter;

import java.util.ArrayList;
import java.util.List;


import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * This class handles layouting of BPMN Subprocesses.
 * It removes all nodes that are contained in a subprocess from the model
 * and layouts them independently using the GridLayouter.
 * Afterwards the nodes get reinserted into the model again
 * @author ff
 *
 */
public class SubModel extends AbstractModelAdapter{
	
	private ArrayList<NodeInterface> f_nodes = new ArrayList<NodeInterface>();
	private ArrayList<EdgeInterface> f_edges = new ArrayList<EdgeInterface>();	

	public SubModel(AbstractModelAdapter baseModel) {
		this.setEdgeLayoutSizeStore(baseModel.getEdgeLayoutSizeStore());
		
	}
	@Override
	public void addEdge(EdgeInterface e) {
		if(getRemovedEdges().contains(e)) {
			super.addEdge(e);
		}else {
			f_edges.add(e);
		}
	}

	@Override
	public void addNode(NodeInterface n) {
		if(getRemovedNodes().contains(n)) {
			super.addNode(n);
		}else {
			f_nodes.add(n);
		}
	}

	@Override
	public void removeEdge(EdgeInterface e) {
		if(f_edges.contains(e)) {
			f_edges.remove(e);
		}else {
			super.removeEdge(e);
		}
	}

	@Override
	public void removeNode(NodeInterface o) {
		if(f_nodes.contains(o)) {
			f_nodes.remove(o);
		}else {
			super.removeNode(o);
		}
	}
	
	@Override
	/**
	 * as the SubModel allows the addition of edges, no caching is required
	 */
	protected List<EdgeInterface> getEdgeList() {
		return new ArrayList<EdgeInterface>(getEdgesInternal());
	}

	@Override
	public List<EdgeInterface> getEdgesInternal() {
		return f_edges;
	}

	@Override
	public List<NodeInterface> getNodesInternal() {
		return f_nodes;
	}
}
