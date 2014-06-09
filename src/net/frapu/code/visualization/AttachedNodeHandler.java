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

import java.util.List;

/**
 *
 * This class provides an handler for the ProcessEditor to support
 * AttachedNodes.
 *
 * @author fpu
 */
public abstract class AttachedNodeHandler {

    public final static int RESIZE_NORTH = 0;
    public final static int RESIZE_SOUTH = 1;
    public final static int RESIZE_EAST = 2;
    public final static int RESIZE_WEST = 3;

    /**
     * Returns true if the target ProcessNode can be attached to the source
     * ProcessNode at their current positions.
     * @param source
     * @param target
     * @return
     */
    public abstract boolean isAttachable(ProcessNode source, AttachedNode target);

    /**
     * Attaches the target AttachedNode to the source ProcessNode. Might be
     * overwritten by subclasses to refine the attachement.
     * @param source
     * @param target
     */
    public void attach(ProcessNode source, AttachedNode target) {
        target.setParentNode(source);
    }

    /**
     * Detaches the target AttachedNode from any source ProcessNode.
     * @param target
     */
    public void detach(AttachedNode target) {
        target.setParentNode(null);
    }

    /**
     * Is called each time the source ProcessNode is moved. Subclasses
     * might also move the AttachedNodes.
     * @param source - can be used by subclasses
     */
    public void sourceMoved(ProcessNode source, List<AttachedNode> attachedNodes, int xDiff, int yDiff) {
        for (AttachedNode node : attachedNodes) {
            node.setPos(node.getPos().x + xDiff, node.getPos().y + yDiff);
        }
    }

    /**
     * Is called each time the source ProcessNode is resized. Subclasses
     * might also resize/move the AttachedNodes.
     * @param oldSource
     * @param newSource
     * @param attachedNodes
     * @param direction One of RESIZE_{NORTH,SOUTH,EAST,WEST}
     */
    public void sourceResized(ProcessNode oldSource, ProcessNode newSource, List<AttachedNode> attachedNodes, int direction) {
        // Do nothing here...
    }
}
