/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inubit.research.ISConverter.importer.LaneSorter;
import java.util.HashSet;
import java.util.Set;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 * @author ff
 *
 */
public abstract class LaneableCluster extends Cluster {

    protected static final int POOL_LABEL_WIDTH = 18;
    public List<Lane> f_lanes = new ArrayList<Lane>();
    public static final String PROP_VERTICAL = "vertical_Pool";

    
    protected void init() {
    	setProperty(PROP_VERTICAL, FALSE);
    	setPropertyEditor(PROP_VERTICAL, new BooleanPropertyEditor());
    }
    
    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    public void addLane(Lane lane) {
        if (!f_lanes.contains(lane)) {
            f_lanes.add(lane);
            addProcessNode(lane);
        }
    }
    
    public boolean isVertical() {
    	return TRUE.equals(getProperty(PROP_VERTICAL));
    }

    public void removeLane(Lane lane) {
        f_lanes.remove(lane);
        removeProcessNode(lane);
    }

    public List<Lane> getLanes() {
        return f_lanes;
    }

    public Set<Lane> getLanesRecursively() {
        Set<Lane> lanes = new HashSet<Lane>( this.f_lanes );

        for ( Lane lane : this.f_lanes )
            lanes.addAll( lane.getLanesRecursively() );

        return lanes;
    }

    protected void drawLanes(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setStroke(BPMNUtils.defaultStroke);
        
        int x = getPos().x - (getSize().width/2);
        int y = getPos().y - (getSize().height/2);
        if(isVertical()) {
        	y += POOL_LABEL_WIDTH;
        }else {
        	x += POOL_LABEL_WIDTH;
        }
        int remainingSpace = (int) (isVertical() ? this.getSize().getWidth() : this.getSize().height);

        for (int lane = 0; lane < f_lanes.size(); lane++) {
            Lane l = f_lanes.get(lane);
            if (lane == f_lanes.size() - 1) {
                //last one
                l.drawLast(g2, x, y, remainingSpace);
            } else {
                l.draw(g2, x, y, remainingSpace);
            }
            if(isVertical()) {
            	x += l.getLaneSize();
            }else {
            	y += l.getLaneSize();
            }
            remainingSpace -= l.getLaneSize();
        }
    }

    @Override
    protected void paintInternal(Graphics g) {
        drawLanes((Graphics2D) g);
    }

    /**
     * determines the center position of this lane within this cluster
     * @param lane
     * @return
     */
    public Point getMyPosition(Lane lane) {
        //getting upper center
        Point _result = this.getPos();
        Dimension _d = this.getSize();
        _result.translate(0, -_d.height / 2);
        if(this.isVertical()) {
	        _result.y += POOL_LABEL_WIDTH / 2; //leave this part free
	        //y coords are correct now!
	        for (Lane l : f_lanes) {
	            if (l == lane) {
	                //okay finished
	                _result.x += getMySize(l).width / 2;
	                return _result;
	            }
	            _result.x += l.getLaneSize();
	        }
        }else {
        	_result.x += POOL_LABEL_WIDTH / 2; //leave this part free
	        //x coords are correct now!
	        for (Lane l : f_lanes) {
	            if (l == lane) {
	                //okay finished
	                _result.y += getMySize(l).height / 2;
	                return _result;
	            }
	            _result.y += l.getLaneSize();
	        }
        }
        return _result;
    }
    
    /**
     * @param lane
     * @return
     */
    public Dimension getMySize(Lane lane) {
        Dimension _d = getSize();
        if(this.isVertical()) {
        	_d.height -= LaneableCluster.POOL_LABEL_WIDTH;
 	        _d.width = lane.getLaneSize();
 	        int remainingWidth = this.getSize().width;
 	        for (int i = 0; i < f_lanes.size(); i++) {
 	            Lane l = f_lanes.get(i);
 	            if ((l == lane) && (i == f_lanes.size() - 1)) {
 	                //last one
 	                _d.width = remainingWidth;
 	            }
 	           remainingWidth -= l.getLaneSize();
 	        }
        }else {
	        _d.width -= LaneableCluster.POOL_LABEL_WIDTH;
	        _d.height = lane.getLaneSize();
	        int remainingHeight = this.getSize().height;
	        for (int i = 0; i < f_lanes.size(); i++) {
	            Lane l = f_lanes.get(i);
	            if ((l == lane) && (i == f_lanes.size() - 1)) {
	                //last one
	                _d.height = remainingHeight;
	            }
	            remainingHeight -= l.getLaneSize();
	        }
        }
        return _d;
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        // Check if key=PROP_CONTAINED_NODES
        if (key.equals(PROP_CONTAINED_NODES)) {
            // reestablishing connection with lanes
            ArrayList<ProcessNode> _nodes = new ArrayList<ProcessNode>(getProcessNodes());
            if (_nodes != null) {
                if (f_lanes != null) {
                    f_lanes.clear();
                }
                for (ProcessNode pn : _nodes) {
                    if (pn instanceof Lane) {
                        if (!f_lanes.contains(pn)) {
                            ((Lane) pn).setParent(this);
                            addLane((Lane) pn);
                            Collections.sort(f_lanes, new LaneSorter());
                        }
                    }else if(pn instanceof Pool) {//this should not happen...
                    	this.removeProcessNode(pn);
                    }
                }
            }
        }
    }
}
