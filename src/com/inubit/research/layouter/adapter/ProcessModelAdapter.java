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
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class ProcessModelAdapter extends AbstractModelAdapter {

	private ProcessModel f_model;
	
	/**
	 * 
	 */
	public ProcessModelAdapter(ProcessModel p) {
		f_model = p;
	}

	@Override
	public List<EdgeInterface> getEdgesInternal() {
		ArrayList<EdgeInterface> _list = new ArrayList<EdgeInterface>();
		for(ProcessEdge e : f_model.getEdges()) {
			_list.add(new ProcessEdgeAdapter(e));
		}
		return _list;
	}

	@Override
	public List<NodeInterface> getNodesInternal() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode e : f_model.getNodes()) {
			_list.add(new ProcessNodeAdapter(e));
		}
		return _list;
	}

	
	public ProcessModel getModel() {
		return f_model;
	}
}
