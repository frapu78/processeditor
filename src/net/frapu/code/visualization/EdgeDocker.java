/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 *
 * @author fpu
 */
public class EdgeDocker extends ProcessNode {

    public final static String PROP_DOCKED_EDGE = "#docked_edge";

    private ProcessEdge dockedEdge;

    public EdgeDocker() {
        initializeProperties();
    }

    public EdgeDocker(ProcessEdge edge) {
        initializeProperties();
        setDockedEdge(edge);
    }

    private void initializeProperties() {
        this.setProperty(PROP_DOCKED_EDGE, "null");
    }

    /**
     * Overwritten to return the default docking position at the edge.
     * @todo: Refactor, so that DockedEdges can be moved.
     * @return
     */
    @Override
    public Point getPos() {
        if (dockedEdge==null) return new Point(0,0);

        List<Point> rp = dockedEdge.getRoutingPoints();
        int x = rp.get(0).x + (rp.get(1).x-rp.get(0).x)/2;
        int y = rp.get(0).y + (rp.get(1).y-rp.get(0).y)/2;

        Point result = new Point(x,y);

        return result;
    }
    
    @Override
    public Set<Point> getDefaultConnectionPoints() {
    	HashSet<Point> _points = new HashSet<Point>();
    	_points.add(new Point());
    	return _points;
    }

    public void setDockedEdge(ProcessEdge edge) {
        dockedEdge = edge;
        if(dockedEdge != null)
        this.setProperty(PROP_DOCKED_EDGE, dockedEdge.getProperty(PROP_ID));
    }

    public ProcessEdge getDockedEdge() {
        return dockedEdge;
    }

    @Override
    protected void paintInternal(Graphics g) {
        // Nothing to do here
    }

    @Override
    protected Shape getOutlineShape() {
        return null;
    }
    
    @Override
    public String toString() {
    	return "EdgeDocker ("+dockedEdge+")";
    }

}
