/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.xForms;

import java.awt.Shape;
import java.awt.Stroke;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public class VirtualXFormsEdge extends ProcessEdge {
	
	/**
	 * @param node
	 * @param node2
	 */
	public VirtualXFormsEdge(ProcessNode start, ProcessNode end) {
		setSource(start);
		setTarget(end);

	}

	//these methods should never get called!
	
	@Override
	public Stroke getLineStroke() {
		
		return null;
	}

	@Override
	public Shape getSourceShape() {
		return null;
	}

	@Override
	public Shape getTargetShape() {
		return null;
	}

}
