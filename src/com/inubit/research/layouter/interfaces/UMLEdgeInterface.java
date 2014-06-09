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
 * special implementation of the edge interface to provide
 * further information about edges in UMl Diagrams. 
 * @author ff
 *
 */
public interface UMLEdgeInterface extends EdgeInterface {

	public boolean isGeneralization();
	public boolean isAggregation();
	public boolean isComposition();
}
