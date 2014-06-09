/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.interfaces;

/**
 * special implementation for Petri Net nodes
 * @author ff
 *
 */
public interface PetriNetNodeInterface extends NodeInterface {

	/**
	 * return true if this node is a comment
	 * @return
	 */
	public boolean isComment();

}
