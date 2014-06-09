/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.graph;

import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.tree.ComponentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Class for representing palm trees as introduced by Hopcroft and Tarjan in
 * "Dividing a graph into triconnected components".
 *
 * A palm tree consists of two disjoint sets of edges called tree arcs (-->) and fronds (^-->).
 * The set of tree arcs form a spanning tree of the graph. If B ^--> A, then A -->* B.
 *
 * @author fel
 */
public class PalmTree extends Graph {
    enum ArcType {
        TREE,
        FROND,
        REMOVED,
        UNSEEN
    }

    private NormalizedGraph graph;
    private GraphCopy graphCopy;

    private Map<Node, Integer>   NUMBER; // (first) dfs-number of v
    private Map<Node, Integer>   LOWPT1;
    private Map<Node, Integer>   LOWPT2;
    private Map<Node, Integer>   ND;     // number of descendants in palm tree
    private Map<Node, Integer>   DEGREE; // degree of v
    private Node[] NODEAT; // node with number i
    private Map<Node, Node> FATHER;  // father of v in palm tree
    private Map<Edge, ArcType> TYPE; // type of edge e
    private Map<Node, IterableAdjacencyList > ADJ; // adjacency list of v
    private Map<Node, Integer>  NEWNUM;  // (second) dfs-number of v
    private Map<Edge, Boolean> START;   // edge starts a path
    private Map<Node, Edge> TREE_ARC; // tree arc entering v
    private Map<Node, List<Integer> > HIGHPT; // list of fronds entering v in the order
                                    // they are visited

    private Stack<Edge> ESTACK = new Stack<Edge>(); // stack of currently active edges
    private Stack<Triple> TSTACK = new Stack<Triple>();

    private Node m_start;     // start node of dfs traversal
    private int  m_numCount;  // counter for dfs-traversal
    private boolean m_newPath;

    //list of split components
    List<SplitComponent> splitComponents = new ArrayList<SplitComponent>();


    /**
     * Construct a new palm tree from the given graph. For the sake of simplicity we only consider
     * graphs with one source. If the given graph has multiple sources an exception is thrown.
     *
     * @param graph the graph
     * @throws SourceNodeException This exception is thrown if the graph has multiple source nodes.
     */
    public PalmTree( NormalizedGraph graph ) throws SourceNodeException {
        super();

        Node source = this.getSingleSource( graph );
        
        this.graph = graph;
        this.graphCopy = new GraphCopy(graph);
        
        this.initializeDFSStructures();

        this.depthFirstSearch( source, null );

        for ( Edge e : graphCopy.edges ) {
            boolean up = (NUMBER.get(e.getTarget()) - NUMBER.get(e.getSource()) > 0);
		if ((up && TYPE.get(e).equals(ArcType.FROND)) || (!up && TYPE.get(e).equals(ArcType.TREE))) {
			graphCopy.reverseEdge(e);
                }
        }

        this.ADJ = new HashMap<Node, IterableAdjacencyList>();

        this.buildAcceptableAdjStructure();
    }

    /**
     * Derive the split components of this palm tree. This method is taken from the paper of
     * Gutwenger and Mutzel "A linear time implementation of SPQR trees".
     * @param v
     */
    public List<SplitComponent> determineSplitComponents( ) {
        try {
            this.preparePathSearch();

            this.tStackPushEos();
//            System.out.println("NEW NUMBERS: " + NEWNUM);
//            System.out.println("ND: " + ND);
//            System.out.println("LOW1:" + LOWPT1);
//            System.out.println("LOW2:" + LOWPT2);
//            System.out.println("ORD ADJ: " + ADJ);
//            System.out.println("--------------------------");

            this.pathSearch( this.m_start );

            SplitComponent newCmp = new SplitComponent();

            if (ESTACK.size() == 1)
                newCmp.setType(ComponentType.TRIVIAL);
            else if (ESTACK.size() >= 4)
                newCmp.setType(ComponentType.TRICONNECTED);
            else
                newCmp.setType(ComponentType.POLYGON);

            while ( !ESTACK.empty() ) {
                newCmp.add( ESTACK.pop() );
            }

//            System.err.println("COMP: " + newCmp);
//
            this.splitComponents.add( newCmp );
            this.splitComponents.addAll( graph.getMultipleEdgeBonds() );

//            System.err.println("\n\nALL COMPONENTS: ");
//            System.err.println(this.splitComponents);
//            System.err.println("\n\n");
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return this.splitComponents;
    }

    /**
     * Do depth first search and build the palm tree recursively. This method is taken from the
     * paper by Hopscroft and Tarjan.
     * @param child the child node
     * @param parent the parent node
     */
    private void depthFirstSearch( Node v, Node u ) {
        this.m_numCount++;
        this.NUMBER.put(v, this.m_numCount);
        this.FATHER.put(v, u);
        this.LOWPT1.put(v, this.NUMBER.get(v));
        this.LOWPT2.put(v, this.NUMBER.get(v));
        this.ND.put(v, 1);
        
        List<Edge> adjacencyList = graphCopy.getAdjacency(v);
//        System.out.println(graphCopy);
        if ( adjacencyList != null )
            for ( Edge e : adjacencyList ) {

                if ( !TYPE.get(e).equals( ArcType.UNSEEN ) ) {
                    continue;
                }
                
                Node w = e.getOpposite(v);
                if ( NUMBER.get(w) == 0 ) {
                    TYPE.put(e, ArcType.TREE);
                    TREE_ARC.put(w, e);

                    depthFirstSearch(w, v);

                    /**
                     * Addition B for lowpt1/2
                     */
                    if ( LOWPT1.get(w) < LOWPT1.get(v) ) {
                        LOWPT2.put(v, Math.min( LOWPT1.get(v) , LOWPT2.get(w) ));
                        LOWPT1.put(v, LOWPT1.get(w));
                    } else if ( LOWPT1.get(w) == LOWPT1.get(v) ) {
                        LOWPT2.put(v, Math.min( LOWPT2.get(v) , LOWPT2.get(w) ));
                    } else {
                        LOWPT2.put(v, Math.min( LOWPT2.get(v) , LOWPT1.get(w) ));
                    }
                    ND.put(v , ND.get(v) + ND.get(w));
                    //END OF ADDITION
                } else {
                    TYPE.put(e, ArcType.FROND);

                    /**
                     * Addition C for lowpt1/2
                     */
                    if ( NUMBER.get(w) < LOWPT1.get(v) ) {
                        LOWPT2.put(v, LOWPT1.get(v) );
                        LOWPT1.put(v, NUMBER.get(w));
                    } else if ( NUMBER.get(w) > LOWPT1.get(v) ) {
                        LOWPT2.put(v, Math.min( LOWPT2.get(v), NUMBER.get(w) ));
                    }
                    //END OF ADDTION
                }
            }
    }

    private void preparePathSearch() throws SourceNodeException {
        NEWNUM = new HashMap<Node, Integer>();
        HIGHPT = new HashMap<Node, List<Integer>>();
        START = new HashMap<Edge, Boolean>();

        m_numCount = this.graphCopy.getNodes().size();
        m_newPath = true;

        for ( Node n : this.graphCopy.nodes ) {
            DEGREE.put(n, 0);
            HIGHPT.put(n, new LinkedList<Integer>());
        }

        for ( Edge e : this.graphCopy.edges ) {
            START.put( e , Boolean.FALSE );
            DEGREE.put( e.getSource(), DEGREE.get( e.getSource() ).intValue() + 1 );
            DEGREE.put( e.getTarget(), DEGREE.get( e.getTarget() ).intValue() + 1 );
        }

        this.pathFinder( this.m_start );

        int[] old2new = new int[ graphCopy.getNodes().size() + 1 ];

        for ( Node n : graphCopy.getNodes() )
            old2new[ NUMBER.get(n) ] = NEWNUM.get(n);

        for ( Node n : graphCopy.getNodes() ) {
            NODEAT[NEWNUM.get(n)] = n ;
            LOWPT1.put( n , old2new[LOWPT1.get(n)] );
            LOWPT2.put( n , old2new[LOWPT2.get(n)] );
        }

//        System.err.println(this);
//        System.err.println("-------------");
    }

    private void pathFinder( Node v ) {
        NEWNUM.put(v, m_numCount - ND.get(v) + 1);
        IterableAdjacencyList adj = ADJ.get(v);
        while ( adj.hasNext() ) {
            Edge e = adj.next();
            Node w = e.getTarget();
            
            if ( m_newPath ) {
                m_newPath = false;
                START.put( e , true );
            }

            if ( TYPE.get(e).equals( ArcType.TREE )) {
                this.pathFinder(w);
                m_numCount--;
            } else {
                HIGHPT.get(w).add( NEWNUM.get(v) );
                int sIndex = HIGHPT.get(w).size();
                m_newPath = true;
            }
        }
    }


    /**
     * Derive the split components of this palm tree. This method is taken from the paper of
     * Gutwenger and Mutzel "A linear time implementation of SPQR trees".
     * UPDATE: This is more taken from the OGDF C++ implementation since the paper hides a lot
     * of necessary information.
     * 
     * @param v the start node
     */
    private void pathSearch ( Node v ) {
//        System.err.println("BEGIN pS(" + v + ");" );
        int vnum = NEWNUM.get(v);
        int wnum;
        int y = 0;
        int a,b;

        IterableAdjacencyList adj = ADJ.get(v);
        adj.reset();
        Node w;
        int outv = adj.size();
        
        while ( adj.hasNext() ) {
            Edge e = adj.next();
//            System.err.println("CURRENT EDGE: " + e);
            w = e.getTarget();
            wnum = NEWNUM.get(w);
            
            if ( TYPE.get(e).equals( ArcType.TREE ) ) {
                if ( START.get(e) ) {
                    y = 0;
                    if ( tStackGetTopA() > LOWPT1.get(w) ) {
                        do {
                            y = Math.max(y,tStackGetTopH());
                            b = tStackGetTopB();
                            TSTACK.pop();
                        } while (tStackGetTopA() > LOWPT1.get(w));
                        Triple t = new Triple( Math.max( y, wnum+ ND.get(w) -1), LOWPT1.get(w),b);
                        TSTACK.push( t );
//                        System.err.println("TSTACK push 1: " + t);
                    } else {
                        Triple t = new Triple(wnum+ ND.get(w) -1, LOWPT1.get(w), vnum);
                        TSTACK.push( t );
//                        System.err.println("TSTACK push 2: " + t);
                    }
                    tStackPushEos();
                }

                pathSearch(w);
                ESTACK.push( TREE_ARC.get(w) );
//                System.err.println("TSTACK: " + TSTACK);
//                System.err.println("CURRENT VNUM: " + vnum);
//                System.err.println("DEG(w): " + DEGREE.get(w));
                //check for type 2 pairs
                while ( vnum != 1 && ( (tStackGetTopA() == vnum ) || ( DEGREE.get(w) == 2  && NEWNUM.get( firstChild(w) ) > wnum)) )  {
                    a = tStackGetTopA();
                    b = tStackGetTopB();

                    Edge eVirt = null;

                    if ( a == vnum && FATHER.get( NODEAT[b]) == NODEAT[a]) {
                        TSTACK.pop();
                    } else {
                        Edge e_ab = null;
                        Node x = null;
//                        System.err.println("W: " + w);
//                        System.err.println("DEG(W): " + DEGREE.get(w) );
                        if ( DEGREE.get(w) == 2 && NEWNUM.get(firstChild(w)) > wnum ) {
//                            System.err.println("FOUND type-2 pair " + v + " , " + firstChild(w));
//                            System.err.println("ESTACK: " + ESTACK);
                            Edge e1 = ESTACK.pop();
                            Edge e2 = ESTACK.pop();

                            ADJ.get( e2.getSource() ).remove( e2 );
                            ADJ.get(e1.getSource() ).remove(e1);
                            x = e2.getTarget();
//                            System.err.println("X:" + x);

                            decrementDegree(x);
                            decrementDegree(v);
                            
                            eVirt = new Edge(0, v, x);
                            eVirt.setVirtual( true );
                            SplitComponent sc = new SplitComponent();
                            sc.add( e1 ); sc.add( e2 ); sc.add(eVirt);
                            sc.setType(ComponentType.POLYGON);
                            splitComponents.add(sc);
//                            System.err.println("COMP1: " + sc);
//                            System.err.println("ESTACK: " + ESTACK);
                            if ( !ESTACK.empty() && ESTACK.peek().getSource() == x &&
                                                ESTACK.peek().getTarget() == v ) {
                                e_ab = ESTACK.pop();
                                ADJ.get(x).remove( e_ab );
                                delHigh(e_ab);
                            }
                        } else {
//                            System.err.println("ESTACK: " + ESTACK);
//                            System.err.println("FOUND type-2 pair " + NODEAT[a] + " , " + NODEAT[b]);
                            int h = TSTACK.peek().h;
                            TSTACK.pop();
                            SplitComponent sc = new SplitComponent();
                            while ( true ) {
                                Edge xy = ESTACK.peek();
                                x = xy.getSource();

                                if (! (a <= NEWNUM.get(x) && NEWNUM.get(x) <= h &&
                                        a <= NEWNUM.get(xy.getTarget()) && NEWNUM.get(xy.getTarget()) <= h)) {
                                    break;
                                }

                                if ((NEWNUM.get(x) == a && NEWNUM.get(xy.getTarget()) == b) ||
                                        ( NEWNUM.get(xy.getTarget()) == a && NEWNUM.get(x) == b) ) {
                                    e_ab = ESTACK.pop();
                                    ADJ.get(e_ab.getSource()).remove( e_ab );
                                    delHigh(e_ab);
                                } else {
                                    Edge eh = ESTACK.pop();
                                    if ( e != eh ) {
                                        ADJ.get(eh.getSource()).remove(eh);
                                        delHigh(eh);
                                    }

                                    sc.add(eh);
                                    decrementDegree(x);
                                    decrementDegree(xy.getTarget());
                                }
                            }

                            eVirt = new Edge( 0, NODEAT[a], NODEAT[b]);
                            eVirt.setVirtual(true);
                            sc.add(eVirt);
                            sc.finishTriconnectedOrPolygon();
                            splitComponents.add(sc);
//                            System.err.println("COMP2: " + sc);
                            x = NODEAT[b];
                            
                        }

                        if ( e_ab != null ) {
                            SplitComponent sc = new SplitComponent();
                            sc.setType(ComponentType.BOND);

                            sc.add( e_ab );
                            sc.add( eVirt );

                            eVirt = new Edge( 0, v, x );
                            eVirt.setVirtual( true );
                            sc.add( eVirt );

                            splitComponents.add(sc);
//                            System.err.println("COMP: " + sc);
                            decrementDegree(x);
                            decrementDegree(v);
                        }

                        ESTACK.push( eVirt );
                        adj.insert(eVirt);

                        START.put( eVirt, START.get(e) );

                        incrementDegree(x);
                        incrementDegree(v);

                        FATHER.put(x, v);
                        TREE_ARC.put(x, eVirt);
                        TYPE.put( eVirt, ArcType.TREE );

                        w = x; wnum = NEWNUM.get(w);
                        
                    }
                }

                //check for type 1 pair
                if ( LOWPT2.get(w) >= vnum && LOWPT1.get(w) < vnum && ( FATHER.get(v) != m_start || outv >= 2 ) ) {
//                    System.err.println( "Found type-1 pair( " +  v + "," + NODEAT[ LOWPT1.get(w) ] + ")");

                    SplitComponent c = new SplitComponent();
                    Edge xy = null;
                    int xnum, ynum;
//                    System.err.println("ESTACK: " + ESTACK);
                    while ( !ESTACK.isEmpty() ) {
                        xy = ESTACK.peek();
                        xnum = NEWNUM.get(xy.getSource());
                        ynum = NEWNUM.get(xy.getTarget());
                        if (!((wnum <= xnum && xnum < wnum+ND.get(w)) || (wnum <= ynum && ynum < wnum+ND.get(w)))) {
                            break;
                        }
                        ESTACK.pop();

                        c.add(xy);
                        delHigh(xy);
                        decrementDegree(xy.getSource());
                        ADJ.get(xy.getSource()).remove(xy);
                        this.graphCopy.edges.remove(xy);
                        decrementDegree(xy.getTarget());
                    }

                    Edge eVirt = new Edge(0, v, NODEAT[ LOWPT1.get(w) ]);
                    eVirt.setVirtual( true );
                    this.graphCopy.addEdge(eVirt);
                    c.add(eVirt);
                    c.finishTriconnectedOrPolygon();
                    splitComponents.add(c);
//                    System.err.println("COMP: " + c);
//                    System.out.println("STACK HERE: " + ESTACK);
                    if ( /*!ESTACK.isEmpty() && */ (
                              xy.getSource() == v && xy.getTarget() == NODEAT[ LOWPT1.get(w) ] ) ||
                            ( xy.getTarget() == v && xy.getSource() == NODEAT[ LOWPT1.get(w) ] ) ) {

                            SplitComponent sc = new SplitComponent();
                            sc.setType(ComponentType.BOND);
                            
                            Edge eh = ESTACK.pop();
                            if ( eh != e ) {
                                ADJ.get(eh.getSource()).remove(eh);
                            }

                            sc.add( eh );
                            ADJ.get(eh.getSource()).remove(eh);
                            this.graphCopy.edges.remove(eh);
                            decrementDegree(eh.getSource());
                            decrementDegree(eh.getTarget());
                            sc.add( eVirt );

                            eVirt = new Edge( 0, v, NODEAT[ LOWPT1.get(w) ]);
                            eVirt.setVirtual( true );

                            //BLOCK IN OGDF left out --> m_IN_HIGH
                            sc.add( eVirt );
                            splitComponents.add(sc);
//                            System.err.println("COMP: " + sc );
                    }

                    if ( ( NODEAT[ LOWPT1.get(w) ] != FATHER.get(v) ) ) {
                        ESTACK.push( eVirt );
                        adj.insert(eVirt);
                        START.put(eVirt, false);

                        TYPE.put( eVirt, ArcType.FROND );
                        if ( high(NODEAT[LOWPT1.get(w)]) < vnum )
                            HIGHPT.get( NODEAT[ LOWPT1.get(w)] ).add( 0, vnum );

                        incrementDegree(v);
                        incrementDegree( NODEAT[LOWPT1.get(w)] );
                    } else {
                        adj.remove(e); //BLOCK from OGDF --> Adj.del(it);

                        SplitComponent sc = new SplitComponent();
                        sc.setType(ComponentType.BOND);
                        sc.add( eVirt );
                        Edge eh = TREE_ARC.get(v);
                        sc.add( eh );

                        splitComponents.add(sc);

                        ADJ.get( eh.getSource() ).remove(eh);

                        eVirt = new Edge( 0, NODEAT[ LOWPT1.get(w) ], v );
                        eVirt.setVirtual( true );

                        sc.add(eVirt);
//                        System.err.println("COMP: " + sc);

                        TYPE.put( eVirt, ArcType.TREE);

//                        System.out.println("INSERT HERE into " + eh.getSource() + "TARGET: " + eh.getTarget());
                        ADJ.get( eh.getSource() ).insert(eVirt);
                        TREE_ARC.put(v, eVirt );

                        START.put( eVirt, START.get(eh) );
                    }
                }

                if (START.get(e)) {
                    while (tStackNotEos()) {
                        TSTACK.pop();
                    }
                    TSTACK.pop();
                }

                while (tStackNotEos() && tStackGetTopA() != vnum && 
                    tStackGetTopB() != vnum && high(v) > tStackGetTopH()) {
                    TSTACK.pop();
                }

                outv--;

            } else { //frond arc
//                System.err.println("TSTACK 2: " + TSTACK);
                if ( START.get(e) ) {
                    y = 0;
                    if ( tStackGetTopA() > LOWPT1.get(w) ) {
                        do {
                            y = Math.max(y,tStackGetTopH());
                            b = tStackGetTopB();
                            TSTACK.pop();
                        } while (tStackGetTopA() > LOWPT1.get(w));
                        Triple t = new Triple( y, wnum, b);
                        TSTACK.push( t );
//                        System.err.println("TSTACK push3: " + t);
                    } else {
                        Triple t = new Triple( vnum, wnum, vnum);
                        TSTACK.push( new Triple( vnum, wnum, vnum) );
//                        System.err.println("TSTACK push4: " + t);
                    }
                    //tStackPushEos();
                }

                //BLOCK FROM PAPER left out --> if ( w = parent(v) )

                ESTACK.push(e);
            }
        }

//        System.err.println("END pS(" + v + ");" );
    }

    private Node firstChild(Node n) {
        return ADJ.get(n).getFirst().getTarget();
    }

    /**
     * Order the adjacency lists according to the generated edge numbering using bucket sort
     */
    private void buildAcceptableAdjStructure() {
        List<Edge>[] buckets = new List[ 3 * graphCopy.getNodes().size() + 3 ];
        for ( int i = 0; i < buckets.length; i++ )
            buckets[i] = new LinkedList<Edge>();

        for ( Edge e : graphCopy.edges ) {
            buckets[this.getEdgeNumber(e)].add(e);
        }

        for ( Node n : graphCopy.getNodes() )
            ADJ.put(n, new IterableAdjacencyList(new LinkedList<Edge>()) );

        for ( List<Edge> bucket : buckets )
            for ( Edge e : bucket ) {
                ADJ.get(e.getSource()).add(e);
                int sIndex = ADJ.get(e.getSource()).size() - 1;
            }

    }

    private int getEdgeNumber( Edge e ) {
        if ( TYPE.get(e).equals( ArcType.TREE ) ) {
            if ( LOWPT2.get(e.getTarget()) < NUMBER.get(e.getSource()) )
                return  3 * LOWPT1.get(e.getTarget());
            else
                return  3 * LOWPT1.get( e.getTarget() ) + 2;
        } else {
            //otherwise it's a frond
            return 3 * NUMBER.get(e.getTarget()) + 1 ;
        }
    }

    private Node getSingleSource( Graph g ) throws SourceNodeException {
        Set<Node> sources = g.getSources();

        if ( sources.size() > 1 )
            throw new SourceNodeException( "multiple" );
        else if ( sources.size() < 1 )
            throw new SourceNodeException( "zero" );
        else
            return sources.iterator().next();
    }

    private void initializeDFSStructures() throws SourceNodeException {
        Set<Node> nodes = graphCopy.getNodes();

        this.NUMBER = new HashMap<Node, Integer>();
        this.LOWPT1 = new HashMap<Node, Integer>();
        this.LOWPT2 = new HashMap<Node, Integer>();
        this.FATHER = new HashMap<Node, Node>();
        this.ND = new HashMap<Node, Integer>();
        this.DEGREE = new HashMap<Node, Integer>();
        this.TREE_ARC = new HashMap<Node, Edge>();
        this.NODEAT = new Node[ nodes.size() + 1];

        this.m_numCount = 0;
        this.m_start = this.getSingleSource( this.graph );
        for ( Node n : nodes ) {
            this.NUMBER.put(n, 0);
            this.LOWPT1.put(n, null);
            this.LOWPT2.put(n, null);
            this.FATHER.put(n, null);
            this.ND.put(n, null);
            this.DEGREE.put(n, null );
        }

        this.TYPE = new HashMap<Edge, ArcType>();
        for ( Edge e : graphCopy.edges )
            this.TYPE.put( e , ArcType.UNSEEN );
    }


    private void tStackPushEos() {
        TSTACK.push( new Triple(-1, -1, -1));
    }

    private boolean tStackNotEos() {
        return TSTACK.peek().a != -1;
    }

    private Integer tStackGetTopA() {
        return TSTACK.peek().a;
    }

    private Integer tStackGetTopB() {
        return TSTACK.peek().b;
    }

    private Integer tStackGetTopH() {
        return TSTACK.peek().h;
    }

    private void delHigh( Edge e ) {
        if ( HIGHPT.get(e.getTarget() ) != null ) {
                Node v = e.getTarget();

                HIGHPT.get(v).remove( NEWNUM.get(e.getSource()) );
        }
    }

    private int high(Node v) {
        return (HIGHPT.get(v).isEmpty()) ? 0 : HIGHPT.get(v).get(0);
    }

    private void decrementDegree( Node n ) {
//        System.err.println("DEC for " + n);
        this.DEGREE.put( n, this.DEGREE.get(n) - 1);
    }
    
    private void incrementDegree( Node n ) {
//        System.err.println("INC for " + n);
        this.DEGREE.put( n, this.DEGREE.get(n) + 1);
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(100);

        b.append("Nodes: ");
        b.append(this.NEWNUM);
        b.append("\nEDGES:\n");

        for ( Edge e : this.graphCopy.edges ) {
            b.append(e.getSource());

            if ( TYPE.get(e).equals( ArcType.TREE ) || TYPE.get(e).equals( ArcType.FROND )) {
                if ( TYPE.get(e).equals(ArcType.FROND) )
                        b.append("^");
                b.append("-->" + e.getTarget() + "\n");
            }
        }
        b.append("START: " + START);

        return b.toString();
    }
}
