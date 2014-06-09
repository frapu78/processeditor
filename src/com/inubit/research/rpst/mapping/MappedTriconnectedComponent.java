/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.mapping;

import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.tree.ComponentType;
import com.inubit.research.rpst.tree.TriconnectedComponent;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;

/**
 *
 * @author fel
 */
public class MappedTriconnectedComponent {
    private static final int SCALE_ADD = 20;

    private TriconnectedComponent original;
    private List<ProcessEdge> plainEdges = new ArrayList<ProcessEdge>();
    private List<MappedTriconnectedComponent> children = new ArrayList<MappedTriconnectedComponent>();

    MappedTriconnectedComponent( TriconnectedComponent tc , Mapping map ) {
        this.original = tc;

        this.collectEdges( map );
        this.mapChildren( map );
    }

    public List<ProcessEdge> getEdges() {
        return plainEdges;
    }
    
    public List<ProcessEdge> getEdgesRecursively() {
    	List<ProcessEdge> edges = new ArrayList<ProcessEdge>();
    	edges.addAll( this.plainEdges );
    	for ( MappedTriconnectedComponent mtc : this.children )
    		edges.addAll( mtc.getEdgesRecursively() );
    	return edges;
    }
    
    public ComponentType getType() {
        return original.getType();
    }

    public List<MappedTriconnectedComponent> getChildren() {
        return this.children;
    }

    @Override
    public String toString() {
        return this.original.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MappedTriconnectedComponent other = (MappedTriconnectedComponent) obj;
        if (this.original != other.original && (this.original == null || !this.original.equals(other.original))) {
            return false;
        }
        if (this.plainEdges != other.plainEdges && (this.plainEdges == null || !this.plainEdges.equals(other.plainEdges))) {
            return false;
        }
        if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.original != null ? this.original.hashCode() : 0);
        hash = 79 * hash + (this.plainEdges != null ? this.plainEdges.hashCode() : 0);
        hash = 79 * hash + (this.children != null ? this.children.hashCode() : 0);
        return hash;
    }

    

    public Rectangle getOutline() {
        int topX = Integer.MAX_VALUE;
        int topY = Integer.MAX_VALUE;
        int bottomX = Integer.MIN_VALUE;
        int bottomY = Integer.MIN_VALUE;

        for ( ProcessEdge e : this.plainEdges )
            for ( Point p : e.getRoutingPoints() ) {
                if ( p.x - SCALE_ADD < topX )
                    topX = p.x - SCALE_ADD;
                if ( p.y - SCALE_ADD < topY )
                    topY = p.y - SCALE_ADD;

                if ( p.x + SCALE_ADD > bottomX )
                    bottomX = p.x + SCALE_ADD;
                if ( p.y + SCALE_ADD > bottomY )
                    bottomY = p.y + SCALE_ADD;
            }

         for ( MappedTriconnectedComponent child : this.children ) {
             Rectangle childOut = child.getOutline();

             if ( childOut.x - SCALE_ADD < topX )
                 topX = childOut.x - SCALE_ADD;
             if ( childOut.y - SCALE_ADD < topY )
                 topY = childOut.y - SCALE_ADD;

             if ( childOut.x + childOut.width + SCALE_ADD > bottomX )
                 bottomX = childOut.x + childOut.width + SCALE_ADD;
             if ( childOut.y + childOut.height + SCALE_ADD > bottomY )
                 bottomY = childOut.y + childOut.height + SCALE_ADD;
         }

         return new Rectangle(topX, topY, bottomX - topX, bottomY - topY);
    }

    private void collectEdges( Mapping map ) {
        for ( Edge e : this.original.getEdges() )
            this.plainEdges.add( map.getMappedEdge( e ) );
    }

    private void mapChildren( Mapping map ) {
        for ( TriconnectedComponent tc : this.original.getChildren() ) 
            this.children.add( new MappedTriconnectedComponent(tc, map) );
        
    }
}
