/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.tree;

import com.inubit.research.rpst.graph.Edge;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fel
 */
public class TriconnectedComponent {
    private String id;
    private ComponentType type;
    private List<Edge> plainEdges;
    private TriconnectedComponent parent;
    private Map<Edge, TriconnectedComponent> virtualEdges = new HashMap<Edge, TriconnectedComponent>();

    public TriconnectedComponent ( ComponentType type , List<Edge> edges ) {
        this.type = type;
        this.plainEdges = edges;
    }

    public ComponentType getType() {
        return this.type;
    }

    public TriconnectedComponent getParent() {
        return this.parent;
    }

    public void addChild( Edge virtual , TriconnectedComponent cmp ) {
        if (!virtual.isVirtual())
            return;

        this.virtualEdges.put(virtual, cmp);
        cmp.setParent(this);
    }

    public Collection<TriconnectedComponent> getChildren() {
        return this.virtualEdges.values();
    }

    public String getId() {
        return this.id;
    }

    public List<Edge> getEdges() {
        return this.getPlainEdges();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append( this.type );
        b.append("(" + id + ")");
        b.append(": [");
        b.append( plainEdges );
        b.append("\n{");
        for ( TriconnectedComponent tc : virtualEdges.values() ) {
            b.append("\n\t");
            b.append(tc);
            b.append("");
        }
        b.append("}");
        b.append("]");

        return b.toString();
    }

    void addPlainEdge( Edge e ) {
        this.plainEdges.add(e);
    }

    List<Edge> getPlainEdges() {
        return this.plainEdges;
    }

    void removeChild( TriconnectedComponent cmp ) {
        Iterator<Map.Entry<Edge, TriconnectedComponent>> it = virtualEdges.entrySet().iterator();

        while ( it.hasNext() ) {
            Map.Entry<Edge, TriconnectedComponent> entry = it.next();

            if ( entry.getValue().equals( cmp )) {
                it.remove();
                break;
            }
        }
    }

    Map<Edge, TriconnectedComponent> getSubComponents() {
        return this.virtualEdges;
    }

    void setId( String id ) {
        this.id = id;
    }

    private void setParent( TriconnectedComponent parent ) {
        this.parent = parent;
    }
}