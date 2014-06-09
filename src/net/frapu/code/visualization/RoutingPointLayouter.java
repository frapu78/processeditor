/**
 *
 * Process Editor - Core Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization;

/**
 *
 * Provides a generic interface for automatically layouting routing points
 *
 * @author fpu
 */
public interface RoutingPointLayouter extends ProcessModelListener {

    /**
     * Optimizes the routing points on the given edge.
     * @param edge The edge to be modified
     * @param updatedNode The node that has changed its position
     */
    public void optimizeRoutingPoints(ProcessEdge edge, ProcessNode updatedNode);

    /**
     * Checks whether an edge is layouted correctly accoring to the criteria of this layouter
     * @param edge The edge to check
     * @return True if the edge has a valid layout
     */
    public boolean isCorrectlyLayouted(ProcessEdge edge);

    /*
     * Layouts all edges connected to a ProcessNode
     */
    public void optimizeAllEdges(ProcessNode updatedNode);


}
