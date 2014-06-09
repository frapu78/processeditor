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


/**
 * Default Interface for all edges that are to be layouted.
 * @author ff
 *
 */
public interface EdgeInterface extends ObjectInterface{

	/**
	 * returns the node where this Edge starts at
	 * @return
	 */
	public ObjectInterface getSource();
	/**
	 * sets the source of this edge.
	 * This change is only temporarily and will be corrected 
	 * at the end of the layouting process!
	 * @param source
	 */
	public void setSource(ObjectInterface source);

	/**
	 * returns the node where this Edge ends
	 */
	public ObjectInterface getTarget();
	/**
	 * sets the target of this edge.
	 * This change is only temporarily and will be corrected 
	 * at the end of the layouting process!
	 * @param source
	 */
	public void setTarget(ObjectInterface target);
	/**
	 * removes all points that are used for the routing of this edge
	 * and resets it to the default
	 */
	public void clearRoutingPoints();
	/**
	 * shall return a list of all routing points.
	 * the connection point with the source and target node have 
	 * to be included even if no other routing points were set before.
	 * <b>Thus the List will always have at least a size of 2!</b>
	 * @return
	 */
	public List<Point> getRoutingPoints();
	
	/**
	 * sets the list of new routing points.
	 * The list excludes the start and end point!
	 * 
	 */
	public void setRoutingPoints(List<Point> routingPoints);
	
	/**
	 * sets the switched value.
	 * If true target and source will be treated vice versa.
	 * This is necessary, e.g. for some preprocessing steps (UMLPreProcessor)
	 * @param switched
	 */
	public void setSwitched(boolean switched);

	/**
	 * retrieves the switched value.
	 * If true target and source will be treated vice versa.
	 * This is necessary, e.g. for some preprocessing steps (UMLPreProcessor)
	 * @param switched
	 */
	public boolean isSwitched();
	
}
