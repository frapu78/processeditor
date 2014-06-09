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
import net.frapu.code.visualization.twf.ErrorConnection;

import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFEdgeInterface;
import com.inubit.research.layouter.interfaces.TWFNodeInterface;

/**
 * @author ff
 *
 */
public class TWFEdgeAdapter extends BPMNEdgeAdapter implements TWFEdgeInterface{

	/**
	 * @param e
	 */
	public TWFEdgeAdapter(ProcessEdge e) {
		super(e);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public TWFNodeInterface getSource() {
		return (TWFNodeInterface)super.getSource();
	}

	@Override
	public TWFNodeInterface getTarget() {
		return (TWFNodeInterface)super.getTarget();
	}
	
	@Override
	public NodeInterface getSourceInternal() {
		return new TWFNodeAdapter(getEdge().getSource());
	}
	
	@Override
	public NodeInterface getTargetInternal() {
		return new TWFNodeAdapter(getEdge().getTarget());
	}
	
	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public boolean isMessageFlow() {
		return false;
	}

	@Override
	public boolean isErrorConnection() {
		return (getEdge() instanceof ErrorConnection);
	}
}
