/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author tmi
 */
abstract class AbstractAdaptor {

    protected List<EdgeAdaptor> adaptEdgeList(
            List<? extends ProcessEdge> edges, ModelAdaptor context) {
        List<EdgeAdaptor> result = new LinkedList<EdgeAdaptor>();
        for (ProcessEdge edge : edges) {
            result.add(new EdgeAdaptor(edge, context));
        }
        return result;
    }

    protected<NodeType extends NodeAdaptor> List<NodeType> adaptNodeList(
            List<? extends ProcessNode> nodes, ModelAdaptor context) {
        List<NodeType> result = new LinkedList<NodeType>();
        for (ProcessNode node : nodes) {
            result.add((NodeType)NodeAdaptor.adapt(node, context));
        }
        return result;
    }

}
