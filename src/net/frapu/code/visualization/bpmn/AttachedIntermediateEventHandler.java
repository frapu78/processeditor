/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.util.List;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.AttachedNodeHandler;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class AttachedIntermediateEventHandler extends AttachedNodeHandler {

    public final static int POSITION_OUT = -1;
    public final static int POSITION_TOP = 0;
    public final static int POSITION_RIGHT = 1;
    public final static int POSITION_BOTTOM = 2;
    public final static int POSITION_LEFT = 3;
    /** The distance to the border for a node to be attached */
    final int BORDER_DISTANCE = 5;

    private int getPosition(ProcessNode source, AttachedNode target) {
        // Check if at boundary position
        int x = target.getPos().x;
        int y = target.getPos().y;        
        // Bottom
        if ( (x>source.getPos().x-source.getSize().width/2) &&
                (x<source.getPos().x+source.getSize().width/2) &&
                (y>source.getPos().y+source.getSize().height/2-BORDER_DISTANCE) &&
                (y<source.getPos().y+source.getSize().height/2+BORDER_DISTANCE))
                return POSITION_BOTTOM;
        // Top
        if ( (x>source.getPos().x-source.getSize().width/2) &&
                (x<source.getPos().x+source.getSize().width/2) &&
                (y>source.getPos().y-source.getSize().height/2-BORDER_DISTANCE) &&
                (y<source.getPos().y-source.getSize().height/2+BORDER_DISTANCE))
                return POSITION_TOP;
        // Right
        if ( (x>source.getPos().x+source.getSize().width/2-BORDER_DISTANCE) &&
                (x<source.getPos().x+source.getSize().width/2+BORDER_DISTANCE) &&
                (y>source.getPos().y-source.getSize().height/2) &&
                (y<source.getPos().y+source.getSize().height/2))
                return POSITION_RIGHT;
        // Left
        if ( (x>source.getPos().x-source.getSize().width/2-BORDER_DISTANCE) &&
                (x<source.getPos().x-source.getSize().width/2+BORDER_DISTANCE) &&
                (y>source.getPos().y-source.getSize().height/2) &&
                (y<source.getPos().y+source.getSize().height/2))
                return POSITION_LEFT;
        return POSITION_OUT;
    }

    @Override
    public boolean isAttachable(ProcessNode source, AttachedNode target) {
        // Check if types are either Task or SubProcess
        if (!(source instanceof Task || source instanceof SubProcess ||
              source instanceof ChoreographyActivity || source instanceof ChoreographySubProcess ||
              source instanceof CallActivity)) return false;
        // Check if target is correct type
        if (!(target instanceof MessageIntermediateEvent ||
                target instanceof TimerIntermediateEvent ||
                target instanceof EscalationIntermediateEvent ||
                target instanceof ConditionalIntermediateEvent ||
                target instanceof CancelIntermediateEvent ||
                target instanceof SignalIntermediateEvent ||
                target instanceof MultipleIntermediateEvent ||
                target instanceof ParallelMultipleIntermediateEvent ||
                target instanceof CompensationIntermediateEvent ||
                target instanceof ErrorIntermediateEvent)) return false;
               
        return getPosition(source, target)!=POSITION_OUT;
    }
    
    @Override
    public void sourceResized(ProcessNode oldSource, ProcessNode newSource, List<AttachedNode> attachedNodes, int direction) {
        // Precheck
        if (newSource==null || oldSource==null) return;
        if (attachedNodes==null) return;
        if (attachedNodes.size()==0) return;
        // Check directions
        for (AttachedNode node: attachedNodes) {
            
            // Get original position of node
            int orgPos = getPosition(oldSource, node);

            switch (direction) {
                case RESIZE_NORTH:
                case RESIZE_SOUTH:
                    switch (orgPos) {
                        case POSITION_TOP:
                            node.setPos(node.getPos().x, newSource.getPos().y-newSource.getSize().height/2);
                            break;
                        case POSITION_BOTTOM:
                            node.setPos(node.getPos().x, newSource.getPos().y+newSource.getSize().height/2);
                            break;
                        case POSITION_LEFT:
                        case POSITION_RIGHT:
                            // Check if movement required
                            if (getPosition(newSource, node)==POSITION_OUT) {
                                // Node is outside of boundary
                                if (node.getPos().y<newSource.getPos().y) {
                                    node.setPos(node.getPos().x, newSource.getPos().y-newSource.getSize().height/2+BORDER_DISTANCE);
                                } else {
                                    node.setPos(node.getPos().x, newSource.getPos().y+newSource.getSize().height/2-BORDER_DISTANCE);
                                }
                            }
                            break;
                    }
                    break;
                    
                case RESIZE_WEST:
                case RESIZE_EAST:
                    switch (orgPos) {
                        case POSITION_LEFT:
                            node.setPos(newSource.getPos().x-newSource.getSize().width/2, node.getPos().y);
                            break;
                        case POSITION_RIGHT:
                            node.setPos(newSource.getPos().x+newSource.getSize().width/2, node.getPos().y);
                            break;
                        case POSITION_BOTTOM:
                        case POSITION_TOP:
                            // Check if movement required
                            if (getPosition(newSource, node)==POSITION_OUT) {
                                // Node is outside of boundary
                                if (node.getPos().x<newSource.getPos().x) {
                                    node.setPos(newSource.getPos().x-newSource.getSize().width/2+BORDER_DISTANCE, node.getPos().y);
                                } else {
                                    node.setPos(newSource.getPos().x+newSource.getSize().width/2-BORDER_DISTANCE, node.getPos().y);
                                }
                            }
                            break;
                    }

                    break;
            }
        }
    }

}
