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
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.MessageFlow;

import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class BPMNEdgeAdapter extends ProcessEdgeAdapter implements BPMNEdgeInterface {

	/**
	 * @param e
	 */
	public BPMNEdgeAdapter(ProcessEdge e) {
		super(e);
	}

	@Override
	public BPMNNodeInterface getSource() {
		return (BPMNNodeInterface)super.getSource();
	}

	@Override
	public BPMNNodeInterface getTarget() {
		return (BPMNNodeInterface)super.getTarget();
	}
	
	@Override
	public NodeInterface getSourceInternal() {
		return new BPMNNodeAdapter(getEdge().getSource());
	}
	
	@Override
	public NodeInterface getTargetInternal() {
		return new BPMNNodeAdapter(getEdge().getTarget());
	}
	
	@Override
	public boolean isMessageFlow() {
		return f_edge instanceof MessageFlow;
	}

	@Override
	public boolean isAssociation() {
		return f_edge instanceof Association;
	}

}
