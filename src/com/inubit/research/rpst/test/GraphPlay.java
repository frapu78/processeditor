/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.test;

import com.inubit.research.rpst.graph.Graph;
import com.inubit.research.rpst.graph.Node;
import com.inubit.research.rpst.tree.RPST;

/**
 * Test class for playing around with the graph implementation.
 * @author fel
 */
public class GraphPlay {
    public static void main( String[] args ) throws Exception {
        Graph mg = new Graph();

        //create the example graph from Polyvyanyy et al. "Simplified Computation
        //and Generalization of the Refinde Process Structure Tree"
//        Node ns = mg.createNode();//1
//        ns.setLabel("s");
//        mg.addSource(ns);
//        Node nu = mg.createNode();//2
//        nu.setLabel("u");
//        Node nv = mg.createNode();//3
//        nv.setLabel("v");
//        Node nw = mg.createNode();//4
//        nw.setLabel("w");
//        Node nx = mg.createNode();//5
//        nx.setLabel("x");
//        Node nt = mg.createNode();//6
//        nt.setLabel("t");
//        mg.addSink(nt);
//
//        mg.createEdge( ns, nu );
//        mg.createEdge( nu, nv );
//        mg.createEdge( nu, nw );
//        mg.createEdge( nv, nw );
//        mg.createEdge( nv, nx );
//        mg.createEdge( nw, nx );
//        mg.createEdge( nx, nt );
////
////
//        mg.addReturnEdge();

//        //create the second example graph from Polyvyanyy et al. "Simplified Computation
//        //and Generalization of the Refinde Process Structure Tree"
//        Node ns = mg.createNode();//1
//        ns.setLabel("s");
//        mg.addSource(ns);
//        Node nu = mg.createNode();//2
//        nu.setLabel("u");
//        Node nv = mg.createNode();//3
//        nv.setLabel("v");
//        Node nw = mg.createNode();//4
//        nw.setLabel("w");
//        Node nx = mg.createNode();//5
//        nx.setLabel("x");
//        Node ny = mg.createNode();//6
//        ny.setLabel("y");
//        Node nz = mg.createNode();//7
//        nz.setLabel("z");
//        Node nt = mg.createNode();//8
//        nt.setLabel("t");
//        mg.addSink(nt);
//
//        mg.createEdge( ns, nu );
//        mg.createEdge( nu, nv );
//        mg.createEdge( nu, nw );
//        mg.createEdge( nv, nw );
//        mg.createEdge( nv, nx );
//        mg.createEdge( nw, nx );
//        mg.createEdge( nx, ny );
//        mg.createEdge( ny, nz );
//        mg.createEdge( ny, nz );
//        mg.createEdge( nz, ny );
//        mg.createEdge( nz, nt );

//
//        mg.addReturnEdge();

//          create the third example graph from Polyvyanyy et al. "Simplified Computation
//        and Generalization of the Refinde Process Structure Tree"
//        DirectedMultiGraph mg = new DirectedMultiGraph(true);

//        Node ns = mg.createNode();//1
//        ns.setLabel("s");
//        mg.addSource(ns);
//        Node ny = mg.createNode();//2
//        ny.setLabel("y");
//        Node nz = mg.createNode();//3
//        nz.setLabel("z");
//        Node nt = mg.createNode();//4
//        nt.setLabel("t");
//        mg.addSink(nt);
//
//        mg.createEdge( ns, ny );
//        mg.createEdge( ny, nz );
//        mg.createEdge( ny, nz );
//        mg.createEdge( nz, ny );
//        mg.createEdge( nz, nt );

//        System.out.println("......................");
//        System.out.println(mg);

//        mg.normalize();
//        mg.toUndirectedGraph();
//        int returnID = mg.addReturnEdge();

//        System.out.println("........................");
//
//
//        System.out.println(mg);

//
        

//        System.out.println(mg);
//        //simple ex
//        int n1 = mg.createNode();
//        int n2 = mg.createNode();
//        int n3 = mg.createNode();
//        int n4 = mg.createNode();
//
//        mg.createEdge( n1, n2);
//        mg.createEdge( n1, n3);
//        mg.createEdge( n2, n4);
//        mg.createEdge( n3, n4);

//        /**
//         * Multi source/sink example
//         */
//        Node nu = mg.createNode("u");
//        mg.addSource(nu);
//        Node nv = mg.createNode("v");
//        mg.addSource(nv);
//        Node nw = mg.createNode("w");
//        Node nx = mg.createNode("x");
//        Node ny = mg.createNode("y");
//        mg.addSink(ny);
//        Node nz = mg.createNode("z");
//        mg.addSink(nz);
//        Node nq = mg.createNode("q");
//        mg.addSink(nq);
//
//        mg.createEdge( nu, nw );
//        mg.createEdge( nv, nw );
//        mg.createEdge( nw, nx );
//        mg.createEdge( nw, nq );
//        mg.createEdge( nx, ny );
//        mg.createEdge( nx, nz );


        /**
         * Hopcroft example
         */
//        Node n1 = mg.createNode();
//        n1.setLabel("1");
//        mg.addSource(n1);
//        Node n2 = mg.createNode();
//        n2.setLabel("2");
//        Node n3 = mg.createNode();
//        n3.setLabel("3");
//        Node n4 = mg.createNode();
//        n4.setLabel("4");
//        Node n5 = mg.createNode();
//        n5.setLabel("5");
//        Node n6 = mg.createNode();
//        n6.setLabel("6");
//        Node n7 = mg.createNode();
//        n7.setLabel("7");
//        Node n8 = mg.createNode();
//        n8.setLabel("8");
//        Node n9 = mg.createNode();
//        n9.setLabel("9");
//        Node n10 = mg.createNode();
//        n10.setLabel("10");
//        Node n11 = mg.createNode();
//        n11.setLabel("11");
//        Node n12 = mg.createNode();
//        n12.setLabel("12");
//        Node n13 = mg.createNode();
//        n13.setLabel("13");
//        mg.addSink(n13);
//
//        mg.createEdge(n1, n2);
//        mg.createEdge(n1, n4);
//        mg.createEdge(n1, n8);
//        mg.createEdge(n1, n12);
//        mg.createEdge(n2, n13);
//        mg.createEdge(n2, n3);
//        mg.createEdge(n3, n4);
//        mg.createEdge(n3, n13);
//        mg.createEdge(n4, n5);
//        mg.createEdge(n4, n6);
//        mg.createEdge(n4, n7);
//        mg.createEdge(n5, n6);
//        mg.createEdge(n5, n7);
//        mg.createEdge(n5, n8);
//        mg.createEdge(n6, n7);
//        mg.createEdge(n8, n9);
//        mg.createEdge(n8, n11);
//        mg.createEdge(n8, n12);
//        mg.createEdge(n9, n10);
//        mg.createEdge(n9, n11);
//        mg.createEdge(n9, n12);
//        mg.createEdge(n10, n11);
//        mg.createEdge(n10, n12);



//
//        System.out.println(mg);
//
//        System.out.println("--------------------");

//        Node ns = mg.createNode();
//        ns.setLabel("s");
//        mg.addSource(ns);
//        Node nv = mg.createNode();
//        nv.setLabel("v");
//        Node nu = mg.createNode();
//        nu.setLabel("u");
//        Node nw = mg.createNode();
//        nw.setLabel("w");
//        mg.addSink(nw);
//        Node nt = mg.createNode();
//        nt.setLabel("t");
//        Node nx = mg.createNode();
//        nx.setLabel("x");
//
//        mg.createEdge(ns, nt);
//        mg.createEdge(nt, nu);
//        mg.createEdge(ns, nx);
//        mg.createEdge(nx, nv);
//        mg.createEdge(nv, nu);
//        mg.createEdge(nu, nw);

        /**
         * Multi source/sink example
         */
        Node n1 = mg.createNode("1");
        mg.addSource(n1);
        Node n2 = mg.createNode("2");
        Node n3 = mg.createNode("3");
        Node n4 = mg.createNode("4");
        mg.addSink(n4);

        mg.createEdge( n1, n2 );
        mg.createEdge( n2, n3 );
        mg.createEdge( n3, n2 );
        mg.createEdge( n2, n4 );
        mg.createEdge( n2, n4 );

//        System.out.println(mg);
//        System.out.println("--------------");
//        NormalizedGraph ng = new NormalizedGraph(mg);
//        System.out.println( ng );
//        System.out.println("---------------");
//        PalmTree pt = new PalmTree(ng);
//        System.out.println( pt );
//        System.out.println("---------------");

//        new RPST(mg);
        System.err.println( new RPST( mg ) );

//        List<Integer> ints = new LinkedList<Integer>();
//        ints.add(1);
//        ints.add(2);
//        ints.add(3);
//
//        int index = ints.indexOf(2);
//
//        ints.add(index, 4);
//        ints.remove(index + 1);
//
//        System.out.println(ints);
//        ints.add(3, 4);
//        System.out.println(ints);


    }
}
