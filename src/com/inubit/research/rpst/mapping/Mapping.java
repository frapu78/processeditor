/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.graph.Graph;

/**
 * Abstract superclass for mapping process models to directed multigraphs.
 * Subclass this class to implement a mapping between a specific modeling notation
 * and directed multigraphs for computing the RPST.
 * 
 * @author fel
 */
public abstract class Mapping {
    protected List<ProcessNode> nodes;
    protected List<ProcessEdge> edges;
    protected ProcessModel model;
    protected Graph graph = new Graph() ;
    protected Map<Edge, ProcessEdge> edgeMap = new HashMap<Edge, ProcessEdge>();

    protected Mapping ( ProcessModel model ) {
        this.nodes = model.getNodes();
        this.edges = model.getEdges();
        this.model = model;
        this.createMapping();
    }

    public Mapping( List<ProcessNode> nodes, List<ProcessEdge> edges, ProcessModel model ) {
        this.nodes = nodes;
        this.edges = edges;
        this.model = model;
        this.createMapping();
    }

    public ProcessEdge getMappedEdge( Edge e ) {
        return this.edgeMap.get(e);
    }

    public Graph getGraph() {
        return this.graph;
    }

    protected abstract void createMapping();

    public ProcessNode getSourceNode( MappedTriconnectedComponent mtc ) {
    	List<ProcessEdge> regionEdges = mtc.getEdgesRecursively(); 
    	Set<ProcessNode> inducedNodes = new HashSet<ProcessNode>();
    	
    	for ( ProcessEdge e : regionEdges ) { 
    		inducedNodes.add( e.getSource() );
    		inducedNodes.add( e.getTarget() );
    	}
    	
    	for ( ProcessNode pn : inducedNodes ) {
    		List<ProcessEdge> in = model.getIncomingEdges(ProcessEdge.class, pn);
    		if ( in.isEmpty() )
    			return pn;
    		
    		if ( in.retainAll( regionEdges ) ) {
    			List<ProcessEdge> compare = model.getIncomingEdges(ProcessEdge.class, pn);
    			compare.removeAll( regionEdges );
    			boolean b = true;
    			for ( ProcessEdge e : compare ) 
    				b = b && e.getTarget().equals(pn);
    					
    			if ( b ) return pn;
    		}
    	}
    			
    	return null;
    }
    
    public ProcessNode getSinkNode( MappedTriconnectedComponent mtc ) {
    	List<ProcessEdge> regionEdges = mtc.getEdgesRecursively(); 
    	Set<ProcessNode> inducedNodes = new HashSet<ProcessNode>();
    	
    	for ( ProcessEdge e : regionEdges ) { 
    		inducedNodes.add( e.getSource() );
    		inducedNodes.add( e.getTarget() );
    	}
    	
    	for ( ProcessNode pn : inducedNodes ) {
    		List<ProcessEdge> out = model.getOutgoingEdges(ProcessEdge.class, pn);
    		if ( out.isEmpty() )
    			return pn;
    		
    		if ( out.retainAll( regionEdges ) ) {
    			List<ProcessEdge> compare = model.getOutgoingEdges(ProcessEdge.class, pn);
    			compare.removeAll( regionEdges );
    			boolean b = true;
    			for ( ProcessEdge e : compare ) 
    				b = b && e.getSource().equals(pn);
    					
    			if ( b ) return pn;
    		}
    	}
    			
    	return null;
    }
}