/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.Comment;

import com.inubit.research.layouter.interfaces.PetriNetNodeInterface;

/**
 * @author ff
 *
 */
public class PetriNetNodeAdapter extends ProcessNodeAdapter implements PetriNetNodeInterface {

	/**
	 * @param n
	 */
	public PetriNetNodeAdapter(ProcessNode n) {
		super(n);
	}

	@Override
	public boolean isComment() {
		return getNode() instanceof Comment;
	}

}
