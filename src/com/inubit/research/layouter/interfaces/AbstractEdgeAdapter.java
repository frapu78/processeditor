/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.interfaces;

import java.awt.Point;
import java.util.List;

import com.inubit.research.layouter.preprocessor.DummyEdgeDocker;


/**
 * An Implementation of EdgeInterface which looks after
 * source and target declarations correctly.
 * @author ff
 *
 */
public abstract class AbstractEdgeAdapter implements EdgeInterface{


	private NodeInterface f_target = null;
	private NodeInterface f_source = null;
	
	private boolean f_switched = false; //can be set to true to treat target as source and source as target
	
	/**
	 * returns the node where this Edge starts
	 * @return
	 */
	public ObjectInterface getSource() {
		if(f_switched) {
			if(f_target == null) {
				
				return getTargetInternal();
			}
			return f_target;
		}//else {
		if(f_source == null) {
			return getSourceInternal();
		}
		return f_source;
	}
	
	/**
	 * returns the node where this Edge starts at
	 * @return
	 */
	protected abstract ObjectInterface getSourceInternal();
	
	/**
	 * sets the source of this edge.
	 * This change is only temporarily and will be corrected 
	 * at the end of the layouting process!
	 * @param source
	 */
	public void setSource(ObjectInterface source) {
		if(!(source instanceof DummyEdgeDocker)) {
			setSourceInternal((NodeInterface)source);
		}else {
			f_source  = (NodeInterface) source;
		}
	}
	/**
	 * sets the source of this edge.
	 * This change is only temporarily and will be corrected 
	 * at the end of the layouting process!
	 * @param source
	 */
	protected abstract void setSourceInternal(ObjectInterface target);
	

	public ObjectInterface getTarget() {
		if(f_switched) {
			if(f_source == null) {
				return getSourceInternal();
			}
			return f_source;
		}//else {
		if(f_target == null) {
			
			return getTargetInternal();
		}
		return f_target;
	}
	/**
	 * returns the node where this Edge ends
	 */
	protected abstract ObjectInterface getTargetInternal();
	
	/**
	 * sets the target of this edge.
	 * This change is only temporarily and will be corrected 
	 * at the end of the layouting process!
	 * @param source
	 */
	public void setTarget(ObjectInterface target) {
		if(!(target instanceof DummyEdgeDocker)) {
			setTargetInternal((NodeInterface)target);
		}else {
			f_target  = (NodeInterface) target;
		}
	}
	
	
	protected abstract void setTargetInternal(ObjectInterface target);
	/**
	 * removes all points that are used for the routing of this edge
	 * and resets it to the default
	 */
	public abstract void clearRoutingPoints();
	/**
	 * shall return a list of all routing points.
	 * the connection point with the source and target node have 
	 * to be included even if no other routing points were set before.
	 * <b>Thus the List will always have at least a size of 2!</b>
	 * @return
	 */
	public abstract List<Point> getRoutingPoints();
	
	/**
	 * sets the list of new routing points.
	 * Excludes the start and end point!
	 */
	public abstract void setRoutingPoints(List<Point> routingPoints);

	/**
	 * sets the switched value.
	 * If true target and source will be treated vice versa.
	 * This is necessary, e.g. for some preprocessing steps (UMLPreProcessor)
	 * @param switched
	 */
	public void setSwitched(boolean switched) {
		this.f_switched = switched;
	}

	/**
	 * retrieves the switched value.
	 * If true target and source will be treated vice versa.
	 * This is necessary, e.g. for some preprocessing steps (UMLPreProcessor)
	 * @param switched
	 */
	public boolean isSwitched() {
		return f_switched;
	}
	
}
