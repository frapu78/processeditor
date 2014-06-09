/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

import com.inubit.research.rpst.tree.ComponentType;
import com.inubit.research.rpst.tree.TriconnectedComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class representing a split component of a graph
 * @author fel
 */
public class SplitComponent {
    private List<Edge> edges;
    private ComponentType type;

    public SplitComponent( List<Edge> edges ) {
        this.edges = edges;
    }

    public SplitComponent () {
        this.edges = new ArrayList<Edge>();
    }

    public void add( Edge edge ) {
        this.edges.add(edge);
    }

    public void union( SplitComponent sc , Edge virtual ) {
        this.edges.remove( virtual );
        sc.edges.remove( virtual );
        this.edges.addAll( sc.edges );

        sc.edges.clear();
    }

    public void finishTriconnectedOrPolygon() {
        if ( this.edges.size() >= 4 )
            this.setType( ComponentType.TRICONNECTED );
        else
            this.setType( ComponentType.POLYGON );
    }

    public ComponentType getType() {
        return type;
    }

    public void setType( ComponentType type ) {
        this.type = type;
    }

    public boolean isEmpty () {
        return this.edges.isEmpty();
    }

    public List<Edge> getVirtualEdges() {
        List<Edge> virtual = new ArrayList<Edge>();

        for ( Edge e : edges )
            if ( e.isVirtual() )
                virtual.add(e);

        return virtual;
    }

    public List<Edge> getPlainEdges() {
        List<Edge> virtual = new ArrayList<Edge>();

        for ( Edge e : edges )
            if ( !e.isVirtual() )
                virtual.add(e);

        return virtual;
    }

    public boolean contains ( Edge e ) {
        return this.edges.contains( e );
    }

    public void remove( Edge e ) {
        this.edges.remove( e );
    }

    /**
     * Reduce this split component such that all temporary edges are removed
     */
    public void reduce() {
        for ( Iterator<Edge> it = this.edges.iterator(); it.hasNext(); ) {
            if ( it.next().isTemporary() )
                it.remove();
        }
    }

    public TriconnectedComponent toTriconntected( Map<Edge, List<SplitComponent>> virtual ) {
        TriconnectedComponent tc = new TriconnectedComponent( this.getType(), this.getPlainEdges() );

        for ( Edge e : this.getVirtualEdges() ) {
            List<SplitComponent> splits = virtual.get(e);
            splits.remove(this);
            if ( splits.size() == 1 )
                tc.addChild(e, splits.get(0).toTriconntected( virtual));
            else
                if ( splits.size() != 0 )
                    System.err.println("ERROR");
        }

        return tc;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(type);
        b.append("{ ");
        for ( Edge e : edges ) {
           b.append(e);
           b.append(" ");
        }

        b.append("}");

        return b.toString();
    }
}
