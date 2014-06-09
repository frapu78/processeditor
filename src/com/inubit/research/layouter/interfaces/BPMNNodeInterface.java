/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.interfaces;

import java.util.List;

/**
 * @author ff
 *
 */
public interface BPMNNodeInterface extends NodeInterface{

	
	/**
	 * returns true if this Node is a BPMN gateway
	 * @return
	 */
	public boolean isGateway();
	/**
	 * returns true if this Node is a BPMN data object
	 * @return
	 */
	public boolean isDataObject();
	/**
	 * this method is called to decide whether this dataObject
	 * should be placed on top (true) or below (false) it's connected Task
	 * @return
	 */
	public boolean placeDataObjectUpwards();
	
	/**
	 * if this node is e.g. an attached intermediate event, this method
	 * returns the task node it is attached to.
	 * @param model
	 * @return
	 */
	public NodeInterface isAttatchedTo(AbstractModelAdapter model);
	
	/**
	 * if this node is e.g. an attached intermediate event and was deleted during the layouting
	 * process, the connection with its task can be reestablished here.
	 * @param model
	 * @return
	 */
	public void setAttatchedTo(AbstractModelAdapter model,NodeInterface node);
	
	
	
	/**
	 * returns true if this Node is a BPMN Annotation Object
	 * @return
	 */
	public boolean isAnnotation();
	/**
	 * returns true if this Node is a BPMN Pool
	 * @return
	 */
	public boolean isPool();
	/**
	 * Applies to Pools and Lanes and
	 * returns true if this Node in "Vertical" orientation
	 * and false if it is "Horizontal"
	 * @return
	 */
	public boolean isVertical();
	/**
	 * returns true if this Node is a BPMN Lane
	 * @return
	 */
	public boolean isLane();
	/**
	 * returns true if this Node is a BPMN SubProcess
	 * @return
	 */
	public boolean isSubProcess();
	/**
	 * if this node is a Pool, a SubProcess or e.g. a TWF-Frame, this method has to be implemented as well and
	 * shall return all nodes that are currently residing within this Cluster
	 * @return
	 */
	public List<NodeInterface> getContainedNodes();
	/**
	 * modifies the size of the node.
	 * This is important for Pools and subprocesses.
	 * @param i
	 * @param j
	 */
	public void setSize(int i, int j);
	

}
