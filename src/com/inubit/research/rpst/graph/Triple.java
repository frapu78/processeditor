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
 * Helper object for graph decomposition. The variable names are taken from the paper of
 * Gutwenger and Mutzel "A  Linear Time Computation of SPQR-trees" ( see TSTACK )
 * @author fel
 */
public class Triple {
    Integer h;
    Integer a;
    Integer b;

    /**
     * Create a new node triple
     * @param h
     * @param a
     * @param b
     */
    public Triple ( int h, int a, int b ) {
        this.h = h;
        this.a = a;
        this.b = b;
    }

    public String toString()  {
        return "(h:"+h+ ",a:" + a + ",b:" + b +")";
    }
}
