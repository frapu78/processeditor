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
 * Class for representing nodes in a multi graph. 
 * 
 * @author fel
 */
public class Node {
    private int id;
    private String label;

    Node(int id) {
        this.id = id;
    }

    Node(int id, String label ) {
        this(id);
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public void setLabel( String label ) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return String.valueOf(id) + "(" + label + ")";
    }
}
