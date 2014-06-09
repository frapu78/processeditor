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
public interface OrgChartNodeInterface extends NodeInterface{	
	
	
	/**
	 * has to return true if this node is a node representing a person
	 * (i.e. not an org unit or a role)
	 * @return
	 */
	public boolean isPerson();
	/**
	 * returns true if this Node is a Cluster (e.g. a Frame Object)
	 * @return
	 */
	public boolean isCluster();
	/**
	 * if this node is a Cluster this method 
	 * shall return all nodes that are currently residing within this Cluster
	 * @return
	 */
	public List<NodeInterface> getContainedNodes();

	/**
	 * modifies the size of the node.
	 * This is important for Clusters
	 * @param i
	 * @param j
	 */
	public void setSize(int i, int j);
}
