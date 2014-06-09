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
import net.frapu.code.visualization.orgChart.OrgChartModel;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.OrgChartModelInterface;

/**
 * @author ff
 *
 */
public class OrgChartModelAdapter extends ProcessModelAdapter implements OrgChartModelInterface{

	/**
	 * @param p
	 */
	public OrgChartModelAdapter(OrgChartModel p) {
		super(p);
	}

	
	@Override
	public List<EdgeInterface> getEdgesInternal() {
		ArrayList<EdgeInterface> _list = new ArrayList<EdgeInterface>();
		for(ProcessEdge e : getModel().getEdges()) {
			_list.add(new OrgChartEdgeAdapter(e));
		}
		return _list;
	}

	@Override
	public List<NodeInterface> getNodesInternal() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode n : getModel().getNodes()) {
			_list.add(new OrgChartNodeAdapter(n));
		}
		return _list;
	}


}
