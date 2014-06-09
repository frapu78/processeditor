/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.interfaces;

import java.awt.Dimension;
import java.awt.Point;

/**
 * An interface for nodes, so they can be used in layout algorithms.
 * If an Adapter is used,
 * it has to be ensured that equals() and hashCode() can be used as if this interface
 * was directly implemented
 * @author ff
 *
 */
public interface NodeInterface extends ObjectInterface {

	
	/**
	 * if this node is directly connected to an edge (like an edge docker)
	 * preprocessors can take that into account.
	 * @return
	 */
	public EdgeInterface getDockedTo();
	
	/**
	 * Should returns the current position of the <b>center</b> of node
	 * @return
	 */
	public Point getPos();
	
	/**
	 * the current size of the node (width,height)
	 * @return
	 */
	public Dimension getSize();

	/**
	 * Gets the text or label present on the node
	 * @return
	 */
	public String getText();

	/**
	 * virtual nodes are nodes not visible in a diagram (e.g. an Edge Docker)
	 * virtual nodes will usually be ignored! in the layouting process!
	 * @return
	 */
	public boolean isVirtualNode();
	
	/**
	 * sets the new position for this Node.
	 * The position refers to the <b>center</b> of the node!
	 * @param i
	 * @param _y
	 */
	public void setPos(int x, int y);
	
	/**
	 * The value returned by this method adds a padding on the bottom/top (half of the value) of a node.
	 * It can be used e.g. to avoid that the nodes contained in a Choreography SubProcess
	 * interfere with the participants at the bottom/top. In this case a Padding which is equal to 
	 * twice the height of the participants should be returned here.
	 * @return
	 */
	public int getPaddingY();
	/**
	 * The value returned by this method adds a padding on the left/right (half of the value) of a node.
	 * It can be used e.g. to avoid that the nodes contained in a Choreography SubProcess
	 * interfere with the participants at the left/right. In this case a Padding which is equal to 
	 * twice the height of the participants should be returned here.
	 * @return
	 */
	public int getPaddingX();

}
