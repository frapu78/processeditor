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
import net.frapu.code.visualization.xforms.XFormsModel;

import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class XFormsModelAdapter extends ProcessModelAdapter implements BPMNModelInterface {


	/**
	 * 
	 */
	public XFormsModelAdapter(XFormsModel model) {
		super(model);
	}
	
	@Override
	public void addEdge(EdgeInterface e) {
		getModel().addEdge(((BPMNEdgeAdapter)e).getEdge());
	}

	@Override
	public void addNode(NodeInterface n) {
		getModel().addNode(((XFormsNodeAdapter)n).getNode());
	}

	
	@Override
	public List<NodeInterface> getNodesInternal() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode e : getModel().getNodes()) {
			_list.add(new XFormsNodeAdapter(e));
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
	
	@Override
	public void removeEdge(EdgeInterface e) {
		getModel().removeEdge(((BPMNEdgeAdapter)e).getEdge());
	}

	@Override
	public void removeNode(NodeInterface n) {
		getModel().removeNode(((XFormsNodeAdapter)n).getNode());
	}

}
