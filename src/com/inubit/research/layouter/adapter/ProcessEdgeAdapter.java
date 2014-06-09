/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import java.awt.Point;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;

import com.inubit.research.layouter.interfaces.AbstractEdgeAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.ObjectInterface;

/**
 * @author ff
 *
 */
public class ProcessEdgeAdapter extends AbstractEdgeAdapter implements EdgeInterface{

	protected ProcessEdge f_edge;
	
	/**
	 * @param e
	 */
	public ProcessEdgeAdapter(ProcessEdge e) {
		f_edge = e;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ProcessEdgeAdapter) {
		return f_edge.equals(((ProcessEdgeAdapter)obj).getEdge());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return f_edge.hashCode();
	}
	
	@Override
	public void clearRoutingPoints() {
		f_edge.clearRoutingPoints();
		f_edge.setProperty(ProcessEdge.PROP_TARGET_DOCKPOINT, "");
		f_edge.setProperty(ProcessEdge.PROP_SOURCE_DOCKPOINT, "");
	}
	
	public ProcessEdge getEdge() {
		return f_edge;
	}

	@Override
	public NodeInterface getSourceInternal() {
		return new ProcessNodeAdapter(f_edge.getSource());
	}

	@Override
	public NodeInterface getTargetInternal() {
		return new ProcessNodeAdapter(f_edge.getTarget());
	}

	@Override
	public void setSourceInternal(ObjectInterface source) {
		ProcessNodeAdapter _n = (ProcessNodeAdapter) source;
		f_edge.setSource(_n.getNode());
	}

	@Override
	public void setTargetInternal(ObjectInterface target) {
		ProcessNodeAdapter _n = (ProcessNodeAdapter) target;
		f_edge.setTarget(_n.getNode());	
	}

	
	@Override
	public void setRoutingPoints(List<Point> routingPoints) {
		f_edge.clearRoutingPoints();
		for(int i=0;i<routingPoints.size();i++) {
			f_edge.addRoutingPoint(i+1, routingPoints.get(i));
		}
	}

	@Override
	public List<Point> getRoutingPoints() {
		return f_edge.getRoutingPoints();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + getSource() + " -> " + getTarget() + ")";
	}

	

}
