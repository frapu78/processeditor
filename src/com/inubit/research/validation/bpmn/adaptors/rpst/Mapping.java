/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors.rpst;

import com.inubit.research.rpst.graph.Graph;
import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.graph.Node;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mostly copied from com.inubit.research.rpst.mapping.Mapping and
 * com.inubit.research.rpst.mapping.BPMNMapping
 * @author tmi
 */
public class Mapping {

    protected List<NodeAdaptor> nodes;
    protected List<EdgeAdaptor> edges;
    protected ModelAdaptor model;
    protected Graph graph = new Graph();
    protected Map<Edge, EdgeAdaptor> edgeMap = new HashMap<Edge, EdgeAdaptor>();

    public Mapping(ModelAdaptor model) {
        this.nodes = model.getNodes();
        this.edges = model.getEdges();
        this.model = model;
        this.createMapping();
    }

    /*public Mapping( List<NodeAdaptor> nodes, List<EdgeAdaptor> edges, ModelAdaptor model ) {
    this.nodes = nodes;
    this.edges = edges;
    this.model = model;
    this.createMapping();
    }*/
    public EdgeAdaptor getMappedEdge(Edge graphEdge) {
        return this.edgeMap.get(graphEdge);
    }

    public Graph getGraph() {
        return this.graph;
    }

    protected void createMapping() {
        Map<NodeAdaptor, Node> nodeMap = this.createNodeMapping();

        this.mapEdges(nodeMap);

        System.out.println(edgeMap.keySet());
    }

    private Map<NodeAdaptor, Node> createNodeMapping() {
        Map<NodeAdaptor, Node> nodeMap = new HashMap<NodeAdaptor, Node>();

        for (NodeAdaptor node : this.nodes) {
            Node graphNode = null;

            if (nodeMap.containsKey(node)) {
                continue;
            }

            if (node.isActivity()
                    || node.isStartEvent()
                    || node.isEndEvent()
                    || node.isGateway()
                    || node.isChoreographyActivity()) {

                graphNode = mapProcessNode(node);
            } else if (node.isEvent()) {//IntermediateEvent
                EventAdaptor event = (EventAdaptor)node;
                //map attached event to parent if a parent exists
                if (event.isAttached()) {
                    NodeAdaptor parentNode = event.getParentNode();
//                    System.out.println("found attached");
                    if (!nodeMap.containsKey(parentNode)) {
                        graphNode = this.mapProcessNode(parentNode);
                        if (graphNode != null) {
                            nodeMap.put(parentNode, graphNode);
                        }
                    } else {
                        graphNode = nodeMap.get(parentNode);
                    }
                } else {
                    graphNode = mapProcessNode(node);
                }
            }

            //ignore all other elements

            if (graphNode != null) {
                nodeMap.put(node, graphNode);

                if (node.isStartEvent()) {
                    this.graph.addSource(graphNode);
                }

                if (node.isEndEvent()) {
                    this.graph.addSink(graphNode);
                }
            }
        }

        return nodeMap;
    }

    private Node mapProcessNode(NodeAdaptor node) {
        if (node.getContainingProcess().isSubProcess()) {
            return null;
        } else {
            return this.graph.createNode(node.getText());
        }
    }

    private void mapEdges(Map<NodeAdaptor, Node> nodeMap) {
        for (EdgeAdaptor edge : this.edges) {
            if (edge.isSequenceFlow()) {
                Node source = nodeMap.get(edge.getSource());
                Node target = nodeMap.get(edge.getTarget());

                if (source != null && target != null) {
                    Edge graphEdge = this.graph.createEdge(source, target);
                    this.edgeMap.put(graphEdge, edge);
                }
            }
        }
    }
}
