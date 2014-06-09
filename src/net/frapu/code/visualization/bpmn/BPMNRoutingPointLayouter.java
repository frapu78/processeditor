/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Point;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.DefaultRoutingPointLayouter;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author uha
 */
public class BPMNRoutingPointLayouter extends DefaultRoutingPointLayouter {

    private void optimizeSourceGatewayDockingPoint(ProcessEdge edge) {
        ProcessNode node = edge.getSource();
        int maxIndex = Math.min(3, edge.getRoutingPoints().size() - 1);
        Point target = edge.getRoutingPoint(maxIndex);
        if (node.getBounds().getMinY() - minDistance > target.y) {
            setNorthDockingPoint(edge, true);
        } else if (node.getBounds().getMaxY() + minDistance < target.y) {
            setSouthDockingPoint(edge, true);
        }
    }

    private void optimizeTargetGatewayDockingPoint(ProcessEdge edge) {
        ProcessNode node = edge.getTarget();
        int maxIndex = Math.max(edge.getRoutingPoints().size() - 4, 0);
        Point target = edge.getRoutingPoint(maxIndex);
        if (node.getBounds().getMinY() - minDistance > target.y) {
            setNorthDockingPoint(edge, false);
        } else if (node.getBounds().getMaxY() + minDistance < target.y) {
            setSouthDockingPoint(edge, false);
        }
    }

    private void optimizeAttachedNodeDockingPoint(ProcessEdge edge, boolean isSource) {
        AttachedNode node = (AttachedNode) (isSource ? edge.getSource() : edge.getTarget());
        ProcessNode parent;
        Point p = node.getPos();
        for (ProcessModel m : edge.getContexts()) {
            parent = node.getParentNode(m);
            if (parent==null) continue;
            if (isSouthDockingPoint(p, parent)) {
                setSouthDockingPoint(edge, isSource);
            } else if (isNorthDockingPoint(p, parent)) {
                setNorthDockingPoint(edge, isSource);
            } else if (isEastDockingPoint(p, parent)) {
                setEastDockingPoint(edge, isSource);
            } else if (isWestDockingPoint(p, parent)) {
                setWestDockingPoint(edge, isSource);
            }
        }

    }

    @Override
    public void optimizeRoutingPoints(ProcessEdge edge, ProcessNode updatedNode) {
        if (edge.getSource() instanceof Gateway) {
            optimizeSourceGatewayDockingPoint(edge);
        } else if (edge.getSource() instanceof AttachedNode) {
            optimizeAttachedNodeDockingPoint(edge,true);
        }
        if (edge.getTarget() instanceof Gateway) {
            optimizeTargetGatewayDockingPoint(edge);
        } else if (edge.getTarget() instanceof AttachedNode) {
            optimizeAttachedNodeDockingPoint(edge,false);
        }
        super.optimizeRoutingPoints(edge, updatedNode);
    }
}
