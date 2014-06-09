/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import com.inubit.research.layouter.interfaces.AbstractEdgeAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.ObjectInterface;

/**
 * @author ff
 *
 */
public class DummyEdge extends AbstractEdgeAdapter{

	private NodeInterface f_src;
	private NodeInterface f_tgt;

	/**
	 * 
	 */
	public DummyEdge(NodeInterface source, NodeInterface target) {
		f_src = source;
		f_tgt = target;
	}

	@Override
	public void clearRoutingPoints() {
	}

	@Override
	public List<Point> getRoutingPoints() {
		return new LinkedList<Point>();
	}


	@Override
	public void setRoutingPoints(List<Point> routingPoints) {
		//not our business
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+ "("+getSource().toString()+" -> "+getTarget().toString()+")";
	}

	@Override
	protected ObjectInterface getSourceInternal() {
		return  f_src;
	}

	@Override
	protected ObjectInterface getTargetInternal() {
		return  f_tgt;
	}

	@Override
	protected void setSourceInternal(ObjectInterface source) {
		f_src = (NodeInterface)source;
	}

	@Override
	protected void setTargetInternal(ObjectInterface target) {
		f_tgt = (NodeInterface)target;
	}

}
