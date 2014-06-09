/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;

import com.inubit.research.layouter.gridLayouter.FlowObjectWrapper;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * provides some usefull helper methods
 * which are used in several layouters
 * 
 * @author ff
 *
 */
public class LayoutHelper {
	
	/**
	 * Configuration property keys
	 */
	//Sugiyama Layouter
	public static final String CONF_SCATTER_EDGES = "LayouterScatterEdges";
	public static final String CONF_SHORTEN_EDGES = "LayouterShortenEdges";
    public static final String CONF_CENTER_NODES = "LayouterCenterNodes";
    public static final String CONF_X_DISTANCE_SUGI = "LayouterXDistanceSugi";
    public static final String CONF_Y_DISTANCE_SUGI = "LayouterYDistanceSugi";
    public static final String CONF_SET_CONNECTION_POINTS = "SugiyamaSetConnectionPoints";
    //GridLayouter
    public static final String CONF_X_DISTANCE_GRID = "LayouterXDistanceGrid";
    public static final String CONF_Y_DISTANCE_GRID = "LayouterYDistanceGrid";
    public static final String CONF_MAX_TWF_WIDTH = "LayouterMaxTWFWidth";
    public static final String CONF_RECTIFY = "RectifyEdges";
	public static final String CONF_SYNC_POOLS = "SynchronizePools";
	//RadialLayouter
	public static final String CONF_RADIAL_LAYER_DISTANCE = "RadialLayerDistance";
	//Org Chart Layouter
	public static final String CONF_X_DISTANCE_ORG_COMPOUND = "LayouterXDistanceOrgChart";
	public static final String CONF_Y_DISTANCE_ORG_COMPOUND = "LayouterYDistanceOrgChart";
	public static final String CONF_RATIO_ORG_COMPOUND = "LayouterOrgChartCompoundRatio";
    public static String CONF_ROUTE_MESSAGEFLOW = "LayouterBPMNRouteMessageFlow";
	
	 /**
	 * @param _rps
	 */
	public static void removeUnneccesaryPoints(List<Point> pts) {
		for(int i=0;i<pts.size()-2;i++) {
			Point p1 = pts.get(i);
			Point p2 = pts.get(i+1);
			Point p3 = pts.get(i+2);
			if(isStraight(p1, p2, p3)) {
				pts.remove(i+1);
				i--;
			}
		}		
	}
	
	public static void straightenEdge(EdgeInterface edge) {
		List<Point> _rps = edge.getRoutingPoints();
		int _i1 = _rps.size();
		removeUnneccesaryPoints(_rps);
		if(_i1 != _rps.size()) { //only do it if anything changed
			edge.clearRoutingPoints();
			_rps.remove(0);
			_rps.remove(_rps.size()-1);
			edge.setRoutingPoints(_rps);
		}
	}
	
	/**
	 * @param point
	 * @param _p1
	 * @param _p2
	 * @return
	 */
	public static boolean isStraight(Point p1, Point p2, Point p3) {
		if(allEquals(p1.y,p2.y,p3.y) || allEquals(p1.x,p2.x,p3.x)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param y
	 * @param y2
	 * @param y3
	 * @return
	 */
	private static boolean allEquals(int y, int y2, int y3) {
		if((y == y2) && (y2 == y3)){
				return true;
		}
		return false;
	}
    
    /**
     * Reverses an edge, which can be useful in preprocessing
     * or to remove cycles.
     * The Source of the given ProcessEdge will be returned
     * @param processEdge
     * @return
     */
    public static NodeInterface switchEdge(EdgeInterface processEdge) {
        NodeInterface _src = (NodeInterface) processEdge.getSource();
        processEdge.toString();
        boolean value = processEdge.isSwitched();
        processEdge.setSwitched(!value);
        return _src;
    }
    
    /**
     * tries to parse the given String into an integer
     * if this fails the default value is returned
     * @param value
     * @param defaultValue
     * @return
     */
    public static int toInt(String value,int defaultValue){
        try{
            return  Integer.parseInt(value);
        }catch(NumberFormatException ex){
            return defaultValue;
        }
    }
    
    /**
     * tries to parse the given String into a float value
     * if this fails the default value is returned
     * @param property
     * @param f
     * @return
     */
    public static float toFloat(String property, float f) {
        try {
            return Float.parseFloat(property);
        } catch (NumberFormatException ex) {
            return f;
        }
    }
	
	/**
	 * @param f
	 * @return
	 */
	public static boolean isDataObject(FlowObjectWrapper f) {
		return isDataObject(f.getWrappedObject());
	}
	
	/**
	 * @param f
	 * @return
	 */
	public static  boolean isDataObject(NodeInterface f) {
		if(f instanceof BPMNNodeInterface) {
			if(((BPMNNodeInterface)f).isDataObject()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param e
	 */
	public static void invertRoutingPoints(EdgeInterface e) {
		List<Point> _pts = e.getRoutingPoints();
		if(_pts.size() > 0) {
			_pts.remove(0);
			_pts.remove(_pts.size()-1);
			Collections.reverse(_pts);
			e.setRoutingPoints(_pts);
		}
	}
	
	/**
	 * returns a List with all successors of the given node
	 * @param model
	 * @param _node
	 * @return
	 */
	public static List<NodeInterface> getSuccessors(AbstractModelAdapter model,NodeInterface node) {
		ArrayList<NodeInterface> _result = new ArrayList<NodeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e.getSource().equals(node)) {
				_result.add((NodeInterface) e.getTarget());
			}
		}
		return _result;
	}
	
	/**
	 * returns a List with all successors of the given node
	 * @param model
	 * @param _node
	 * @return
	 */
	public static List<NodeInterface> getPredecessors(AbstractModelAdapter model,NodeInterface node) {
		ArrayList<NodeInterface> _result = new ArrayList<NodeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e.getTarget().equals(node)) {
				_result.add((NodeInterface) e.getSource());
			}
		}
		return _result;
	}

	
	
	/**
	 * @param model
	 * @param _node
	 * @param _p1
	 * @return 
	 */
	public static EdgeInterface getEdge(AbstractModelAdapter model,NodeInterface start, NodeInterface end) {
		for(EdgeInterface _e : model.getEdges()) {
			if(_e.getSource().equals(start) && _e.getTarget().equals(end)) {
				//found the edge
				return _e;
			}
		}
		return null;
	}
	
	
	public static void routeSelfEdge(EdgeInterface e) {
		e.clearRoutingPoints();
		ArrayList<Point> _rps = new ArrayList<Point>();
		//routing the edge
		Point _pos = ((NodeInterface)e.getSource()).getPos();
		Dimension _d = ((NodeInterface)e.getSource()).getSize();
		
		Point _start = new Point(_pos);
		_start.x += _d.width/2;
		_start.y -= _d.height/4;
		_rps.add(_start);
		
		Point _p = new Point(_start);
		_p.x += _d.width/4;
		_rps.add(_p);
		
		_p = new Point(_p);
		_p.y -= _d.height/2;
		_rps.add(_p);
		
		_p = new Point(_p);
		_p.x -= _d.width/2;
		_rps.add(_p);
		
		Point _end = new Point(_p);
		_end.y += _d.height/4;
		_rps.add(_end);	
		
		e.setRoutingPoints(_rps);
		
		//circular routing
		/*Point _p1 = ((NodeInterface) e.getSource()).getPos();
		Dimension d = ((NodeInterface) e.getSource()).getSize();
		int w = d.width/2;
		int h = d.height/2;
		_p1.x += w;
		_p1.y -= h;
		double radius = w;
		PolarCoordinates _start = new PolarCoordinates(radius,0.0,_p1);
		addRoutingPoints(e,_start,270.0,5.0);*/
	}

	/* used for circular routing, not needed at the moment! 
	 * /**
	 *
	 * @param _start
	 * @param d
	 * @param e
	 *
	private void addRoutingPoints(EdgeInterface edge,PolarCoordinates _start, double degrees, double increment) {
		double _startAngle = _start.getAngleDeg();
		ArrayList<Point> _rps = new ArrayList<Point>();
		for(double d = 0.0;d < degrees; d+= increment) {
			_start.setAngleDeg(_startAngle+d);
			_rps.add(new Point(_start.getX(),_start.getY()));
		}
		edge.setRoutingPoints(_rps);
	}
	*/
	

	public static void setDockingPointOffset(ProcessEdge edge) {
		setDockingPointOffset(edge, true);
	}
	
	public static void setDockingPointOffset(ProcessEdge edge, boolean topToBottom) {
		if(edge.getRoutingPoints().size() > 2) {
			List<Point> _rps = edge.getRoutingPoints();
			_rps.remove(0);
			_rps.remove(_rps.size()-1);
			
			Point _lastrp = new Point(_rps.get(_rps.size()-1));
			calcDockingOffset(edge.getTarget(), _lastrp,topToBottom);
			edge.setTargetDockPointOffset(_lastrp);
			_rps.remove(_rps.size()-1);
			
			_lastrp = new Point(_rps.get(0));
			calcDockingOffset(edge.getSource(), _lastrp,topToBottom);
			edge.setSourceDockPointOffset(_lastrp);
			_rps.remove(0);
			        			
			edge.setRoutingPoints(_rps);
		}
	}

	private static void calcDockingOffset(ProcessNode node, Point rp, boolean topToBottom) {
		Point _pos = node.getPos();
		Dimension _size = node.getSize();
		if(topToBottom && ( (rp.y < _pos.y - _size.height/2 + 1) || (rp.y > _pos.y + _size.height/2 -1)  )) { //special check if left or right is needed
			if(_pos.y < rp.y) {
				//below
				rp.y = _pos.y + _size.height/2;
			}else {
				//above
				rp.y = _pos.y - _size.height/2;
			}
		}else {
			if(_pos.x < rp.x) {
				//left of it
				rp.x = _pos.x + _size.width/2;
			}else {
				//right
				rp.x = _pos.x - _size.width/2;
			}
		}
		//transformation to relative coordinates
		rp.x -= _pos.x;
		rp.y -= _pos.y;
	}
}
