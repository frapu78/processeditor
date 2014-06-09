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
 * This is a special extension of the edge interface for
 * BPMN models.
 * @author ff
 *
 */
public interface BPMNEdgeInterface extends EdgeInterface{
	
	/**
	 * The source as BPMNNodeinterface
	 */
	@Override
	public BPMNNodeInterface getSource();

	/**
	 * The target as BPMNNodeinterface
	 */
	@Override
	public BPMNNodeInterface getTarget();
	
	/**
	 * Message flows will receive a special routing
	 * and are an important factor for layouting
	 * as they will be treated differently to Sequence Flows.
	 * @return
	 */
	public boolean isMessageFlow();

	/**
	 * is this a connections to a dataobject (or similar)
	 * @return
	 */
	public boolean isAssociation();
	
	

}
