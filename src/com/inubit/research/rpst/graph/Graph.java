/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all simple graph structures.
 * @author fel
 */
public class Graph {
    protected Set<Node> nodes;

    /**
     * Set of edges contained in this graph
     */
    protected List<Edge> edges;
    protected Set<Node> sources = new HashSet<Node>();
    protected Set<Node> sinks = new HashSet<Node>();
    protected Map<Node, List<Edge>> adjacency = new HashMap<Node, List<Edge>>();
    protected boolean isDirected = true;

    /**
     * The maximal ID that is currently assigned to a node
     */
    private int maxNodeID = 0;

    /**
     * The maximal ID that is currently assigned to an edge
     */
    private int maxEdgeID = 0;

    /**
     * Create an empty graph
     */
    public Graph() {
        this.nodes = new HashSet<Node>();
        this.edges = new ArrayList<Edge>();
    }

    public Node createNode( String label ) {
        Node n = new Node( this.maxNodeID++, label);
        this.nodes.add( n );
        return n;
    }

    /**
     * Add a new edge to the graph, connecting the given source and target
     * @param source the source
     * @param target the target
     * @return the edge's ID or -1 if no edge was created
     */
    public Edge createEdge( Node source, Node target ) {
        if ( source == null || target == null )
            return null;

        Edge e = new Edge( this.maxEdgeID++ , source , target );
        this.edges.add( e );

        this.addToAdjacency(source, e);
        if ( !e.isDirected() )
            this.addToAdjacency(target, e);

        return e;
    }

    /**
     * Get all nodes of the graph
     * @return the nodes
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    public void addSink( Node n ) {
        this.sinks.add(n);

        if ( !nodes.contains(n) ) {
            nodes.add(n);
        }
    }

    public void addSource( Node n ) {
        this.sources.add(n);

        if ( !nodes.contains(n) ) {
            nodes.add(n);
        }
    }

    /**
     * Get the source nodes of this graph
     * @return the set of designated source
     */
    public Set<Node> getSources( ) {
        return this.sources;
    }

    public Set<Node> getSinks( ) {
        return this.sinks;
    }

    /**
     * Get all nodes that are adjacent to n
     * @param n the node
     * @return the list of adjacent nodes
     */
    public List<Edge> getAdjacency( Node n ) {
        return adjacency.get(n);
    }

    /**
     * @return <ul>
     * <li> true, if it is a directed graph </li>
     * <li> false, if it is an undirected graph </li>
     * </ul>
     */
    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected( boolean isDirected ) {
        if ( isDirected == this.isDirected )
            return;

        this.isDirected = isDirected;

        for ( Edge e : this.edges )
            e.setDirected(isDirected);

        if ( isDirected ) {
            for ( Edge e : this.edges ) {
                this.adjacency.get(e.getTarget()).remove( e );
            }
        } else {
            for ( Edge e : this.edges ) {
                if ( this.adjacency.get( e.getTarget() ) == null )
                    this.adjacency.put( e.getTarget(), new LinkedList<Edge>() );

                if ( this.adjacency.get( e.getTarget() ) != null && ! this.adjacency.get( e.getTarget() ).contains(e) )
                    this.adjacency.get( e.getTarget() ).add( e );
            }
        }
    }

     /**
     * Determine if this graph has only a single source node and a single sink node.
     * @return <ul>
     *  <li> true, if there is exactly one source and exactly one sink </li>
     *  <li> false, otherwise </li>
     * </ul>
     */
    public boolean isTwoTerminalGraph() {
        return this.getSources().size() == 1 && this.getSinks().size() == 1;
    }

    /**
     * Add target to the adjacency list of source
     * @param source the source node
     * @param target the target node
     */
    protected void addToAdjacency( Node source, Edge e ) {
        if ( adjacency.containsKey(source) )
            adjacency.get(source).add(e);
        else {
            List<Edge> list = new LinkedList<Edge>();
            list.add(e);
            adjacency.put(source, list);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(100);

        for ( Map.Entry<Node, List<Edge>> ad : this.adjacency.entrySet() ) {
            b.append( ad.getKey().getLabel() + ":( ");

            for ( Edge e : ad.getValue() )
            	b.append( e.getOpposite(ad.getKey()).getLabel() + " ");

            		b.append(")\n");
        }

        return b.toString();
    }
}
