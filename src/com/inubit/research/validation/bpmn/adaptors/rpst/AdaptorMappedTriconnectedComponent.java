/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors.rpst;

import com.inubit.research.rpst.graph.Edge;
import com.inubit.research.rpst.tree.ComponentType;
import com.inubit.research.rpst.tree.TriconnectedComponent;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import java.util.ArrayList;
import java.util.List;

/**
 * mostly copied from com.inubit.research.rpst.mapping.MappedTriconnectedComponent
 * @author tmi
 */
public class AdaptorMappedTriconnectedComponent {

    private TriconnectedComponent original;
    private List<EdgeAdaptor> plainEdges = new ArrayList<EdgeAdaptor>();
    private List<AdaptorMappedTriconnectedComponent> children =
            new ArrayList<AdaptorMappedTriconnectedComponent>();

    AdaptorMappedTriconnectedComponent(TriconnectedComponent tc, Mapping map) {
        this.original = tc;

        this.collectEdges(map);
        this.mapChildren(map);
    }

    public List<EdgeAdaptor> getEdges() {
        return plainEdges;
    }

    public ComponentType getType() {
        return original.getType();
    }

    public List<AdaptorMappedTriconnectedComponent> getChildren() {
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
        final AdaptorMappedTriconnectedComponent other =
                (AdaptorMappedTriconnectedComponent) obj;
        if (this.original != other.original
                && (this.original == null || !this.original.equals(other.original))) {
            return false;
        }
        if (this.plainEdges != other.plainEdges &&
                (this.plainEdges == null || !this.plainEdges.equals(other.plainEdges))) {
            return false;
        }
        if (this.children != other.children &&
                (this.children == null || !this.children.equals(other.children))) {
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

    private void collectEdges(Mapping map) {
        for (Edge edge : this.original.getEdges()) {
            this.plainEdges.add(map.getMappedEdge(edge));
        }
    }

    private void mapChildren(Mapping map) {
        for (TriconnectedComponent tc : this.original.getChildren()) {
            this.children.add(new AdaptorMappedTriconnectedComponent(tc, map));
        }

    }
}
