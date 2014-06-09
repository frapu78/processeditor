/**
 *
 * Process Editor - Petri Net Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * Wraps a predecessor for a Place/Transition.
 *
 * @author fpu
 */
public class Predecessor {

    private ProcessNode node;
    private ProcessEdge edgeFromNode;

    public Predecessor() {
    }

    public Predecessor(ProcessNode node, ProcessEdge edgeFromNode) {
        this.node = node;
        this.edgeFromNode = edgeFromNode;
    }

    public void setEdgeFromNode(ProcessEdge edgeFromNode) {
        this.edgeFromNode = edgeFromNode;
    }

    public void setNode(ProcessNode node) {
        this.node = node;
    }

    public ProcessEdge getEdgeFromNode() {
        return edgeFromNode;
    }

    public ProcessNode getNode() {
        return node;
    }

}
