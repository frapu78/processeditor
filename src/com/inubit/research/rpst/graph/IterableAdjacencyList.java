/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

import java.util.List;

/**
 *
 * @author fel
 */
public class IterableAdjacencyList {

    private List<Edge> adj;
    private int currentIndex = -1;

    IterableAdjacencyList( List<Edge> adj ) {
        this.adj = adj;
    }

    boolean hasNext() {
        return this.currentIndex < this.adj.size() - 1;
    }

    Edge next() {
        this.currentIndex++;
        return this.adj.get(currentIndex);
    }

    void remove( Edge e ) {
        int delIndex = this.adj.indexOf(e);
        //update index
        if ( this.adj.remove(e) && this.currentIndex > -1 && delIndex <= this.currentIndex )
            this.currentIndex--;
    }

    void insert( Edge e ) {
        int insInd = this.currentIndex + 1;
        this.adj.add( insInd, e );
        this.currentIndex = insInd;
    }

    void replace( Edge e1, Edge e2 ) {
        int index = this.adj.indexOf(e1);

        this.adj.add(index, e2);
        this.adj.remove(index + 1);
    }

    void reset( ) {
        this.currentIndex = -1;
    }

    int size() {
        return this.adj.size();
    }

    Edge getFirst() {
        return  this.adj.get(0);
    }

    void add( Edge e ) {
        this.adj.add(e);
    }

    @Override
    public String toString() {
        return this.adj.toString();
    }
}
