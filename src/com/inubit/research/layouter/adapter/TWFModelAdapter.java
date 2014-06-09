/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.twf.TWFModel;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFModelInterface;

/**
 * @author ff
 *
 */
public class TWFModelAdapter extends ProcessModelAdapter implements TWFModelInterface {

	/**
	 * @param p
	 */
	public TWFModelAdapter(TWFModel p) {
		super(p);
	}
	
	@Override
	public List<NodeInterface> getNodesInternal() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode e : getModel().getNodes()) {
			_list.add(new TWFNodeAdapter(e));
		}
		return _list;
	}
	
	@Override
	public List<EdgeInterface> getEdgesInternal() {
		ArrayList<EdgeInterface> _list = new ArrayList<EdgeInterface>();
		for(ProcessEdge e : getModel().getEdges()) {
			_list.add(new TWFEdgeAdapter(e));
		}
		return _list;
	}

}
