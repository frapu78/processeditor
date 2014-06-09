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
import net.frapu.code.visualization.bpmn.BPMNModel;

import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class BPMNModelAdapter extends ProcessModelAdapter implements BPMNModelInterface{

	

	/**
	 * @param p
	 */
	public BPMNModelAdapter(BPMNModel p) {
		super(p);
	}
	
	@Override
	public List<NodeInterface> getNodesInternal() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode e : getModel().getNodes()) {
			_list.add(new BPMNNodeAdapter(e));
		}
		return _list;
	}
	
	@Override
	public List<EdgeInterface> getEdgesInternal() {
		ArrayList<EdgeInterface> _list = new ArrayList<EdgeInterface>();
		for(ProcessEdge e : getModel().getEdges()) {
			_list.add(new BPMNEdgeAdapter(e));
		}
		return _list;
	}
}
