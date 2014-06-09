/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import com.inubit.research.layouter.interfaces.EdgeInterface;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public class UMLNodeAdapter extends ProcessNodeAdapter{

	/**
	 * @param n
	 */
	public UMLNodeAdapter(ProcessNode n) {
		super(n);
	}
	
	@Override
	public EdgeInterface getDockedTo() {
		if(getNode() instanceof EdgeDocker) {
			return new UMLEdgeAdapter(((EdgeDocker)getNode()).getDockedEdge());
		}
		return null;
	}

}
