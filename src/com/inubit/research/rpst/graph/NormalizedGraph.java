/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.tree.ComponentType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Normalized version of a directed multigraph. This graph will be input to the
 * computation of the RPST.
 * @author fel
 */
public class NormalizedGraph extends Graph {

    /**
     * Map of bonds that are created while preprocessing the graph
     */
    protected Map<Edge, SplitComponent> multipleEdges = new HashMap<Edge, SplitComponent>();

    /**
     * map: reversedEdge --> origEdge
     */
    protected Map<Edge, Edge> reversedEdges = new HashMap<Edge, Edge>();

    protected Edge returnEdge = null;

    public NormalizedGraph( Graph mg ) throws SinkNodeException, SourceNodeException {
        this.adjacency = new HashMap<Node, List<Edge>>(mg.adjacency);
        this.nodes = new HashSet<Node>(mg.nodes);
        this.sinks = new HashSet<Node>(mg.sinks);
        this.sources = new HashSet<Node>(mg.sources);
        this.edges = new ArrayList<Edge>(mg.edges);

        if ( !mg.isTwoTerminalGraph() )
            this.transformIntoTwoTerminalGraph();

        this.normalize();
        this.splitOffMultipleEdges();
        this.addReturnEdge();

        this.setDirected( false );
    }

    protected NormalizedGraph() { }

    @Override
    public boolean isDirected() {
        return false;
    }

    public Collection<SplitComponent> getMultipleEdgeBonds() {
        return this.multipleEdges.values();
    }

    public Edge getReturnEdge() {
        return this.returnEdge;
    }

    public Map<Edge, Edge> getReverseEdgeMapping() {
        return this.reversedEdges;
    }

    /**
     * Transform this graph into a two terminal graph, so that it has a single source node and a
     * single sink node. This is done by adding one (two) node(s) and respective edges to (from) the original
     * multiple sources (sinks).
     */
    private void transformIntoTwoTerminalGraph() throws SinkNodeException, SourceNodeException {
        if ( sources.size()  > 1 ) {
            Node tmpNode = this.createNode( "tmpSource" );

            for ( Node n : sources )
                this.addNewTemporaryEdge(tmpNode, n);

            this.sources.clear();
            this.sources.add(tmpNode);
        } else if ( sources.size() < 1 ) {
            throw new SourceNodeException("zero");
        }

        if ( sinks.size() > 1 ) {
            Node tmpNode = this.createNode("tmpSink");

            for ( Node n : sinks )
                this.addNewTemporaryEdge(n, tmpNode);

            this.sinks.clear();
            this.sinks.add(tmpNode);
        } else if ( sinks.size() < 1) {
            throw new SinkNodeException(("zero"));
        }
    }

    /**
     * Add a new temporary egde connecting source and target
     * @param source the source
     * @param target the target
     * @return the edge's ID or -1 if no edge was created
     */
    private Edge addNewTemporaryEdge( Node source , Node target ) {
        Edge e =  this.createEdge( source , target );
        e.setTemporary(true);

        return e;
    }

    /**
     * Normalize nodes with multiple incoming AND multiple outgoing edges
     * by introducing new temporary nodes as suggested by Polyvyanyy et al. in
     * "Simplified Computation of the RPST"
     */
    private void normalize() {
        Map<Node, List<Edge>> incoming = new HashMap<Node, List<Edge>>();

        for ( Node n : this.nodes ) {
            incoming.put(n, new ArrayList<Edge>());
        }

        for ( Edge e : this.edges ) {
            incoming.get(e.getTarget()).add(e);
        }

        for ( Map.Entry<Node, List<Edge>> entry : incoming.entrySet() ) {
            List<Edge> adj = adjacency.get(entry.getKey());
            if ( entry.getValue().size() > 1 && adj != null && adj.size() > 1 ) {
                Node tmp = this.createNode("tmp");

                if ( entry.getKey().getLabel() != null ) 
                    tmp.setLabel( entry.getKey().getLabel() + "'");

                for ( Edge e : adj ) {
                    e.setSource(tmp);
                    this.addToAdjacency(tmp, e);
                }

                this.adjacency.put(entry.getKey(), new LinkedList<Edge>());

                this.addNewTemporaryEdge( entry.getKey(), tmp );
            }
        }
    }

    private Edge addReturnEdge() {
        if ( sources.size() == 1 && sinks.size() == 1 ) {
            Node start = sources.iterator().next();
            Node end = sinks.iterator().next();

            returnEdge = this.createEdge( end, start );
        }

        return returnEdge;
    }

    private void splitOffMultipleEdges() {
        for ( Map.Entry<Node, List<Edge>> adj : this.adjacency.entrySet() ) {
            Map<Node, List<Edge>> targets = new HashMap<Node, List<Edge>>();

            for ( Edge e : adj.getValue() ) {
                if (!targets.containsKey(e.getTarget())) {
                    List<Edge> newList = new LinkedList<Edge>();
                    targets.put(e.getTarget(), newList);
                }
                targets.get(e.getTarget()).add(e);
            }

            for ( Map.Entry<Node, List<Edge>> entry : targets.entrySet() ) {
                if (entry.getValue().size() > 1) {
                    Edge ce = this.splitOffMultipleEdge(adj.getKey(), entry.getValue());
                    adj.getValue().removeAll( entry.getValue() );
                    adj.getValue().add(ce);
                }
            }
        }
    }

    private Edge splitOffMultipleEdge( Node source, List<Edge> adj ) {
        Node target = adj.get(0).getTarget();

        SplitComponent cmp = new SplitComponent();
        for ( Edge e : adj ) {
            cmp.add( e );
            this.edges.remove( e );
        }
        Edge virt = new Edge(0, source, target);
        virt.setVirtual(true);

        cmp.add(virt);
        cmp.setType(ComponentType.BOND);

        this.multipleEdges.put(virt , cmp );
        this.edges.add(virt);

        return virt;
    }

}
