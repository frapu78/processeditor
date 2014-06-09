/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.mapping;

import java.util.LinkedList;
import java.util.List;

import com.inubit.research.rpst.exceptions.SinkNodeException;
import com.inubit.research.rpst.exceptions.SourceNodeException;
import com.inubit.research.rpst.tree.ComponentType;
import com.inubit.research.rpst.tree.RPST;
import com.inubit.research.rpst.tree.TriconnectedComponent;

/**
 *
 * @author fel
 */
public class MappedRPST {
    private Mapping mapping;
    private RPST rpst;

    public MappedRPST( Mapping map ) throws SinkNodeException, SourceNodeException {
        this.rpst = new RPST( map.getGraph() );

        this.mapping = map;
    }

    public MappedTriconnectedComponent getRoot() {
        return new MappedTriconnectedComponent( rpst.getRoot(), mapping );
    }

    public TriconnectedComponent getRawRoot() {
        return rpst.getRoot();
    }
    
    public List<MappedTriconnectedComponent> getComponentsByType( ComponentType type ) {
    	List<MappedTriconnectedComponent> c = new LinkedList<MappedTriconnectedComponent>();
    	MappedTriconnectedComponent root = this.getRoot();
    	if ( root.getType().equals(type) )
    		c.add(root);
    		
    	c.addAll( getComponentsByType(type, root) );
    	return c;
    }
    
    private List<MappedTriconnectedComponent> getComponentsByType( ComponentType type, MappedTriconnectedComponent mtc ) {
    	List<MappedTriconnectedComponent> c = new LinkedList<MappedTriconnectedComponent>(); 
    	//breadth first search, to encounter the "parent-components" first, when iterating over the returned list
    	for ( MappedTriconnectedComponent child : mtc.getChildren() )
    		if ( child.getType().equals( type ) )
    			c.add( child );
    	for ( MappedTriconnectedComponent child : mtc.getChildren() )
    		c.addAll( getComponentsByType(type, child) );
    	
    	return c;
    }
}
