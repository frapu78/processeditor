/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.soundness;

import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tmi
 */
public class ReachabilityGraphNode {

    private ProcessState state;
    private List<ReachabilityGraphEdge> incomingEdges, outgoingEdges;

    public ReachabilityGraphNode(ProcessState state) {
        this.state = state;
        incomingEdges = new LinkedList<ReachabilityGraphEdge>();
        outgoingEdges = new LinkedList<ReachabilityGraphEdge>();
    }

    public void addIncomingEdge(ReachabilityGraphEdge edge) {
        assert edge.getTarget().equals(this);
        if (!incomingEdges.contains(edge)) {
            incomingEdges.add(edge);
        }
    }

    public ReachabilityGraphEdge addEdgeFrom(ReachabilityGraphNode source,
            NodeAdaptor viaNode) {
        for (ReachabilityGraphEdge edge : incomingEdges) {
            if (edge.getSource().equals(source) &&
                    edge.getExecutedNode().equals(viaNode)) {
                return edge;
            }
        }
        ReachabilityGraphEdge addedEdge =
                new ReachabilityGraphEdge(source, this, viaNode);
        incomingEdges.add(addedEdge);
        return addedEdge;
    }

    public void addOutgoingEdge(ReachabilityGraphEdge edge) {
        assert edge.getSource().equals(this);
        if (!outgoingEdges.contains(edge)) {
            outgoingEdges.add(edge);
        }
    }

    public ReachabilityGraphEdge addEdgeTo(ReachabilityGraphNode target,
            NodeAdaptor viaNode) {
        for (ReachabilityGraphEdge edge : outgoingEdges) {
            if (edge.getTarget().equals(target) &&
                    edge.getExecutedNode().equals(viaNode)) {
                return edge;
            }
        }
        ReachabilityGraphEdge addedEdge =
                new ReachabilityGraphEdge(this, target, viaNode);
        incomingEdges.add(addedEdge);
        return addedEdge;
    }

    public ProcessState getProcessState() {
        return state;
    }

    public List<ReachabilityGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }

    public List<ReachabilityGraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ReachabilityGraphNode)) {
            return false;
        }
        ReachabilityGraphNode otherNode = (ReachabilityGraphNode) other;
        return state != null ?
            state.equals(otherNode.getProcessState()) :
            otherNode.getProcessState() == null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.state != null ? this.state.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<node hash=\"");
        builder.append(hashCode());
        builder.append("\">\n");
        builder.append(state.toString());
        builder.append("<outgoingEdges>\n");
        for (ReachabilityGraphEdge edge : outgoingEdges) {
            builder.append(edge.toString());
            builder.append('\n');
        }
        builder.append("</outgoingEdges>\n");
        builder.append("</node>\n");
        return builder.toString();
    }
}
