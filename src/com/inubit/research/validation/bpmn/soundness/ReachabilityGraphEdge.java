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

/**
 *
 * @author tmi
 */
public class ReachabilityGraphEdge {

    private ReachabilityGraphNode source, target;
    private NodeAdaptor executedNode;

    public ReachabilityGraphEdge(ReachabilityGraphNode source,
            ReachabilityGraphNode target, NodeAdaptor executedNode) {
        this.source = source;
        this.target = target;
        this.executedNode = executedNode;
    }

    public ReachabilityGraphNode getSource() {
        return source;
    }

    public ReachabilityGraphNode getTarget() {
        return target;
    }

    public NodeAdaptor getExecutedNode() {
        return executedNode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(source.hashCode());
        builder.append("--");
        builder.append(executedNode.getText());
        builder.append("-->");
        builder.append(target.hashCode());
        return builder.toString();
    }
}
