/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * This interface provides the ability to attach a node to another node.
 *
 * @author fpu
 */
public interface AttachedNode {

    /**
     * Returns the ProcessNode this AttachedNode belongs to.
     * @return
     */
    public ProcessNode getParentNode(ProcessModel model);

    /**
     * Returns the id of the parent node.
     * @return
     */
    public String getParentNodeId();

    /**
     * Sets the parent node.
     * @param node
     * @return
     */
    public void setParentNode(ProcessNode node);

    /**
     * Returns the position of this attached node.
     * @return
     */
    public Point getPos();

    /**
     * Sets the position of this attached node.
     * @param x
     * @param y
     */
    public void setPos(int x, int y);

    public Dimension getSize();

    public void setSize(int width, int height);

}
