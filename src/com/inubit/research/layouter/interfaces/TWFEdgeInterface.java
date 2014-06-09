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
 * @author ff
 *
 */
public interface TWFEdgeInterface extends BPMNEdgeInterface {
	
	/**
	 * The source as TWFNodeinterface
	 */
	@Override
	public TWFNodeInterface getSource();

	/**
	 * The target as TWFNodeinterface
	 */
	@Override
	public TWFNodeInterface getTarget();
	
	/**
	 * should return true if this edge is connected to the error 
	 * boxes (in the lower left corner of a Tool/Scope etc.)
	 * @return
	 */
	public boolean isErrorConnection();

}
