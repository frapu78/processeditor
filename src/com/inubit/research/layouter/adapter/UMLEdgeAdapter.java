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
import net.frapu.code.visualization.uml.Aggregation;
import net.frapu.code.visualization.uml.Inheritance;

import com.inubit.research.layouter.interfaces.UMLEdgeInterface;

/**
 * @author ff
 *
 */
public class UMLEdgeAdapter extends ProcessEdgeAdapter implements UMLEdgeInterface{

	/**
	 * @param e
	 */
	public UMLEdgeAdapter(ProcessEdge e) {
		super(e);
	}

	@Override
	public boolean isAggregation() {
		return getEdge() instanceof Aggregation;
	}

	@Override
	public boolean isComposition() {
		return false;
	}

	@Override
	public boolean isGeneralization() {
		return getEdge() instanceof Inheritance;
	}

}
