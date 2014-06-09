/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

/**
 * Class for representing simple edges.
 * 
 * @author fel
 */
public class Edge {
    private int id;

    private Node source;
    private Node target;

    private boolean isVirtual = false;
    private boolean isTemporary = false;
    private boolean isDirected = true;

    Edge ( int id , Node source , Node target ) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    Edge ( int id , Node source , Node target, boolean isDirected ) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.isDirected = isDirected;
    }

    public int getId() {
        return this.id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public Node getSource() {
        return this.source;
    }

    public Node getTarget() {
        return this.target;
    }

    public boolean isVirtual() {
        return this.isVirtual;
    }

    public void setVirtual( boolean v ) {
        this.isVirtual = v;
    }

    public boolean isTemporary() {
        return this.isTemporary;
    }

    public void setTemporary( boolean t ) {
        this.isTemporary = t;
    }

    public boolean isDirected() {
        return this.isDirected;
    }

    public void setDirected( boolean dir ) {
        this.isDirected = dir;
    }

    public Node getOpposite( Node ref ) {
        if ( ref == this.source )
            return this.target;
        else if ( ref == this.target )
            return this.source;
        else
            return null;
    }

    void setSource( Node n ) {
        this.source = n;
    }

    void setTarget( Node n ) {
        this.target = n;
    }

    public void switchVertices() {
        Node tmp = this.source;
        this.source = this.target;
        this.target = tmp;
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("(ID = " + this.id + ") ");

        if (this.isVirtual)
            b.append("^^");
        b.append(this.source + " --> " + this.target);

        if (this.isVirtual)
            b.append("^^");

        return b.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if ( !(o instanceof Edge) )
            return false;

        Edge e = (Edge) o;

        boolean value = e.source == this.source && e.target == this.target && e.isVirtual == this.isVirtual;

        if ( this.isVirtual && e.isVirtual )
            value = value && this == e;
        return value;
    }

//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 89 * hash + (this.source != null ? this.source.hashCode() : 0);
//        hash = 89 * hash + (this.target != null ? this.target.hashCode() : 0);
//        hash = 89 * hash + (this.isVirtual ? 1 : 0);
//        return hash;
//    }
}
