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
public interface TWFNodeInterface extends BPMNNodeInterface {

	/**
	 * return true, if this node is a tool error connector
	 * (the small rectangle on the lower left which is used 
	 * in case of an error/escalation etc.)
	 * @return
	 */
	public boolean isToolErrorConnector();
	/**
	 * return true, if this node is a tool docker
	 * (the small circle which is used to connect
	 * nodes outside of a Tool/Scope etc. with the inside)
	 * @return
	 */
	public boolean isToolDocker();
	/**
	 * used to retrieve the parent of this ToolDocker
	 * or ToolErrorConnector
	 * @return
	 */
	public TWFNodeInterface getParent();	
	
}
