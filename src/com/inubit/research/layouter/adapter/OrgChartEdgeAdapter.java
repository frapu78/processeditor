/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;


import net.frapu.code.visualization.ProcessEdge;

import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.OrgChartEdgeInterface;

/**
 * @author ff
 *
 */
public class OrgChartEdgeAdapter extends ProcessEdgeAdapter implements OrgChartEdgeInterface{

	/**
	 * @param e
	 */
	public OrgChartEdgeAdapter(ProcessEdge e) {
		super(e);
	}
	
	@Override
	public NodeInterface getSourceInternal() {
		return new OrgChartNodeAdapter(getEdge().getSource());
	}
	
	@Override
	public NodeInterface getTargetInternal() {
		return new OrgChartNodeAdapter(getEdge().getTarget());
	}

	

}
