/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.tree;

import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.graph.Graph;
import com.inubit.research.rpst.graph.NormalizedGraph;
import com.inubit.research.rpst.graph.PalmTree;
import com.inubit.research.rpst.graph.SplitComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fel
 */
public class RPST {
    protected TriconnectedComponent root;
    protected Graph mg;
    protected NormalizedGraph normalizedGraph;

    //integer values for component numbering
    private int pCount = 0;
    private int bCount = 0;
    private int tCount = 0;

    public RPST( Graph mg ) throws SinkNodeException, SourceNodeException {
        this.mg = mg;
        this.normalizedGraph = new NormalizedGraph(mg);
        this.create();
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    public TriconnectedComponent getRoot() {
        return this.root;
    }

    private void create() {
        //get basic split components
        List<SplitComponent> splitComponents = this.determineSplitComponents( );
        //union split components --> maximize them to triconnected components
        this.maximizeSplitComponents( splitComponents );

        //generate tree structure and save the root node
        this.root = this.generateTreeStructure( splitComponents );

        //project onto original edges and remove redundant fragments
        this.project( this.root );
        
        //remove polygons with only 1 edge and no children
        this.facilitate( this.root );

        this.setIds( this.root );
    }

    private List<SplitComponent> determineSplitComponents( ) {
        List<SplitComponent> splits = null;
        try {
            PalmTree pt = new PalmTree(this.normalizedGraph);
            splits = pt.determineSplitComponents();
        } catch ( SourceNodeException ex ) {
            System.err.println("RPST.determineSplitComponents: " + ex.getMessage());
        }

        return splits;
    }

    private void maximizeSplitComponents( List<SplitComponent> splits ) {
        //precompute map of virtual edges and their referenced components
        Map<Edge, List<SplitComponent>> virtual = new HashMap<Edge, List<SplitComponent>>();

        for ( SplitComponent split : splits ) {
            List<Edge> virtualEdges = split.getVirtualEdges();

            for ( Edge cE : virtualEdges ) {
                if ( !virtual.containsKey(cE) ) {
                    List<SplitComponent> newL = new ArrayList<SplitComponent>();
                    virtual.put( cE, newL );
                }
                virtual.get(cE).add( split );
            }
        }

        //maximize components using the precomputed map
        for ( SplitComponent sc : splits ) {
            if ( !sc.isEmpty() && (sc.getType().equals( ComponentType.POLYGON ) || sc.getType().equals( ComponentType.BOND )) ) {
                for ( Edge vEdge : sc.getVirtualEdges() ) {
                    Iterator<SplitComponent> it = virtual.get(vEdge).iterator();

                    while ( it.hasNext() ) {
                        SplitComponent sc2 = it.next();
                        if ( sc2 != sc && sc2.getType().equals( sc.getType() )) {
                            List<Edge> newVirtuals = sc2.getVirtualEdges();
                            for ( Edge newVEdge : newVirtuals ) {
                                if ( newVEdge != vEdge ) {
                                    virtual.get(newVEdge).remove(sc2);
                                    virtual.get(newVEdge).add(sc);
                                }
                            }
                            sc.union(sc2, vEdge);
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private TriconnectedComponent generateTreeStructure( List<SplitComponent> splits  ) {
        TriconnectedComponent tc = null;

        //normalize to original edges ( those of the directed version )
        Map<Edge, Edge> reverseEdges = this.normalizedGraph.getReverseEdgeMapping();

        //precoompute map of virtual edges and normalize plain edges
        Map<Edge, List<SplitComponent>> virtual = new HashMap<Edge, List<SplitComponent>>();

        for ( SplitComponent split : splits ) {
            List<Edge> virtualEdges = split.getVirtualEdges();

            for ( Edge cE : virtualEdges ) {
                if ( !virtual.containsKey(cE) ) {
                    List<SplitComponent> newL = new ArrayList<SplitComponent>();
                    virtual.put( cE, newL );
                }
                virtual.get(cE).add( split );
            }

            List<Edge> plainEdges = split.getPlainEdges();

            for ( Edge pE : plainEdges ) {
                if ( reverseEdges.containsKey( pE ) ) {
                    split.remove(pE);
                    split.add( reverseEdges.get(pE) );
                }
            }

            //Remove temporary edges
            split.reduce();
        }

        //generate tree starting from split component that contains the return edge
        for ( SplitComponent split : splits ) {
            if ( split.contains( normalizedGraph.getReturnEdge()) ) {
                //remove return edge
                split.remove( normalizedGraph.getReturnEdge() );
                tc = split.toTriconntected(virtual);
                break;
            }
        }

        return tc;
    }

    private Map<Edge, TriconnectedComponent> project( TriconnectedComponent tc ) {
        Map<Edge, TriconnectedComponent> toAdd = new HashMap<Edge, TriconnectedComponent>();
        Iterator<Map.Entry<Edge, TriconnectedComponent>> it = tc.getSubComponents().entrySet().iterator();

        while ( it.hasNext() ) {
            Map.Entry<Edge, TriconnectedComponent> entry = it.next();
            int sizeBefore = toAdd.size();
            toAdd.putAll(this.project(entry.getValue()));

            if ( sizeBefore != toAdd.size() ) {
                it.remove();
            }
        }

        for ( Map.Entry<Edge, TriconnectedComponent> entry : toAdd.entrySet() ) 
            tc.addChild(entry.getKey(), entry.getValue());

        Map<Edge, TriconnectedComponent> returnMap = new HashMap<Edge, TriconnectedComponent>();
        
        if ( tc.getPlainEdges().size() == 0 && tc.getSubComponents().size() < 2 ) {
            Map<Edge, TriconnectedComponent> subCmps = tc.getSubComponents();

            for ( Map.Entry<Edge, TriconnectedComponent> entry : subCmps.entrySet() ) {
                returnMap.put( entry.getKey(), entry.getValue() );
            }
        }

        return returnMap;
    }

    private void facilitate( TriconnectedComponent tc ) {
        Iterator<Map.Entry<Edge, TriconnectedComponent>> it = tc.getSubComponents().entrySet().iterator();

        while ( it.hasNext() ) {
            TriconnectedComponent sub = it.next().getValue();

            if ( sub.getPlainEdges().size() == 1 && sub.getSubComponents().size() == 0 ) {
                tc.addPlainEdge( sub.getPlainEdges().get(0) );
                it.remove();
            } else if (sub.getSubComponents().size() > 0) {
                facilitate(sub);
            }
        }
    }

    private void setIds( TriconnectedComponent tc ) {
        if (tc.getType().equals( ComponentType.POLYGON ))
            tc.setId("POLYGON " + (++pCount));
        else if (tc.getType().equals( ComponentType.BOND ))
            tc.setId("BOND " + (++bCount));
        else
            tc.setId("RIGID " + (++tCount));

        for ( TriconnectedComponent sub : tc.getChildren() )
            setIds(sub);
    }
}
