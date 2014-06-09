/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */

package com.inubit.research.rpst.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fel
 */
public class GraphCopy extends NormalizedGraph {

    public GraphCopy( NormalizedGraph ng ) {
        this.nodes = new HashSet<Node>( ng.getNodes() );
        this.edges = new LinkedList<Edge>( ng.edges );
        this.adjacency = new HashMap<Node, List<Edge>>(ng.adjacency);

    }

    public void reverseEdge( Edge edge ) {
        this.adjacency.get(edge.getSource()).remove(edge);
        this.adjacency.get(edge.getTarget()).add( edge );

        Node source = edge.getSource();
        edge.setSource( edge.getTarget() );
        edge.setTarget(source);
    }

    public void addEdge( Edge e ) {
        this.edges.add(e);
    }
}
