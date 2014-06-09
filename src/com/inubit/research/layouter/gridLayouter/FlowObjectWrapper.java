/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;


/**
 * Wraps a BPMN Flow Object and provides further information, which are used
 * in the "Grid Layout" algorithm
 * @author ff
 *
 */
public class FlowObjectWrapper{
	
	// Map which holds all singeltons
	private static HashMap<NodeInterface, FlowObjectWrapper> f_wrappedObjects = new HashMap<NodeInterface, FlowObjectWrapper>();
	
	private ArrayList<FlowObjectWrapper> f_predecessors = new ArrayList<FlowObjectWrapper>();
	private ArrayList<FlowObjectWrapper> f_successors = new ArrayList<FlowObjectWrapper>();
	private ArrayList<EdgeInterface> f_predecessorsEdges = new ArrayList<EdgeInterface>();
	private ArrayList<EdgeInterface> f_successorsEdges = new ArrayList<EdgeInterface>();
	private NodeInterface f_node;
	private AbstractModelAdapter f_model;
	
	private int moveMode = 0; //0 = do not move; +1 = move right; -1 = move left;
	
	private Point f_Pos;
	// needed if a split has connections with 2 join, so 
	//only one of them (the first) gets placed into the same row
	private boolean f_joinAlreadyPlaced = false; 
	
	private Point f_recommendedPosDelta = null;
	private FlowObjectWrapper f_recommendedPositionParent = null;
	private boolean f_needToAddRow = true;
	
	private static int spacingX = 20;
	private static int spacingY = 10;
	
	private int f_gridNumber = 0;
	
	private Comparator<Object> f_successorSorter = new YPositionComparator();
	
	/**
	 * constructor cannot be used directly as this would lead to infinite recursion
	 * a singleton methods getFlowObjectWrapper is provided instead
	 */
	private FlowObjectWrapper(NodeInterface node, AbstractModelAdapter model) {
		f_node = node;
		f_model = model;
	}
	
	/**
	 * fills predecessor and successor lists
	 */
	private void buildLinks() {
		//building predecessor and successor lists
		for(EdgeInterface edge : f_model.getEdges()) {
			if(edge.getSource().equals(f_node)) {
				FlowObjectWrapper _w = getFlowObjectWrapper((NodeInterface)edge.getTarget(),f_model);
				int _idx = getIndex(f_successors,_w);
				f_successors.add(_idx,_w);
				f_successorsEdges.add(_idx,edge);
			}else if(edge.getTarget().equals(f_node)) {
				f_predecessors.add(getFlowObjectWrapper((NodeInterface) edge.getSource(),f_model));
				f_predecessorsEdges.add(edge);
			}
		}
		//moving nodes that are connected through dummy edges to the back
		for(int i = f_successorsEdges.size()-1;i>=0;i--) {
			if(f_successorsEdges.get(i) instanceof DummyEdge) {
				EdgeInterface e = f_successorsEdges.get(i);
				FlowObjectWrapper n = f_successors.get(i);
				
				f_successorsEdges.remove(i);
				f_successors.remove(i);
				
				f_successorsEdges.add(e);
				f_successors.add(n);
			}
		}
	}
	
	/**
	 * @param f_successors2
	 * @param _w
	 * @return
	 */
	private int getIndex(ArrayList<FlowObjectWrapper> list,FlowObjectWrapper w) {
		int _result = 0;
		for(FlowObjectWrapper l:list) {
			if(f_successorSorter.compare(w, l) > 0) {
				_result++;
			}else {
				break;
			}
		}
		return _result;
	}

	public boolean getNeedToAddRow() {
		return f_needToAddRow;
	}
	
	public ArrayList<FlowObjectWrapper> getPredecessors() {
		return f_predecessors;
	}
	
	public ArrayList<EdgeInterface> getPredecessorEdges() {
		return f_predecessorsEdges;
	}
	
	public int getPredecessorsSizeInGrid() {
		return f_predecessors.size()-countDataObjects(f_predecessors)-countObjectsInOtherPools(f_predecessors);
	}

	public ArrayList<FlowObjectWrapper> getSuccessors() {
		return f_successors;
	}
	
	public ArrayList<EdgeInterface> getSuccessorEdges() {
		return f_successorsEdges;
	}
	
	public NodeInterface getWrappedObject() {
		return f_node;
	}

	public boolean hasRecommendedPosition() {
		return f_recommendedPosDelta != null;
	}
	
	public void recommendPosition(int x, int y,FlowObjectWrapper parent) {
		recommendPosition(x, y, true,parent);
	}
	/**
	 * @param _x
	 * @param i
	 * @param b
	 */
	public void recommendPosition(int dx, int dy, boolean createRow,FlowObjectWrapper parent) {
		f_recommendedPosDelta = new Point(dx,dy);
		f_recommendedPositionParent = parent;
		f_needToAddRow = createRow;
		
	}
	
	public void setPosition(Point pos) {
		f_Pos = (Point) pos.clone();
	}
	
	/**
	 * sets the amount of pixels which are left free
	 * on the sides of a node. (This value gets added to their width while layouting)
	 * @param value
	 */
	public static void setSpacingX(int value) {
		spacingX = value;
	}
	/**
	 * sets the amount of pixels which are left free
	 * on the sides of a node. (This value gets added to their height while layouting)
	 * @param value
	 */
	public static void setSpacingY(int value) {
		spacingY = value;
	}
	
	public boolean wraps(NodeInterface f) {
		if(f_node.equals(f)) {
			return true;
		}
		return false;
	}
	
	/**
	 * factory method for the retrieval of correctly wrapped objects
	 * @param obj
	 * @param model
	 * @return
	 */
	public static FlowObjectWrapper getFlowObjectWrapper(NodeInterface obj,AbstractModelAdapter model) {
		FlowObjectWrapper _result = f_wrappedObjects.get(obj);
		if(_result == null) {
			_result = new FlowObjectWrapper(obj,model);
			f_wrappedObjects.put(obj, _result);
			_result.buildLinks();
		}
		return _result;
	}
	
	public static void clear() {
		f_wrappedObjects.clear();
	}
	
	public boolean isSplit() {
		if(this.isDataObject()) {
		//	return f_successors.size()-countObjectsInOtherPools(f_successors) > 1;
		}
		return f_successors.size()-countDataObjects(f_successors)
			-countObjectsInOtherPools(f_successors)-countDummyEdges(f_successorsEdges) > 1;
	}
	
	/**
	 * @param edges
	 * @return
	 */
	private int countDummyEdges(ArrayList<EdgeInterface> edges) {
		int result = 0;
		for(EdgeInterface e:edges) {
			if(e instanceof DummyEdge) {
				result++;
			}
		}
		return result;
	}

	public boolean isJoin() {
		return getPredecessorsSizeInGrid() > 1;
	}
	
	
	
	/**
	 * @param f_predecessors2
	 * @return
	 */
	private int countObjectsInOtherPools(ArrayList<FlowObjectWrapper> predecessors) {
		int i=0;
		for(FlowObjectWrapper f:predecessors) {
			if(!f.isDataObject())
				if(f.getGrid() != this.getGrid()) {
					i++;
				}
		}
		return i;
	}

	/**
	 * @param f_predecessors2 
	 * @return
	 */
	private int countDataObjects(ArrayList<FlowObjectWrapper> list) {
		int _result = 0;
		for (FlowObjectWrapper f : list) {
			if(LayoutHelper.isDataObject(f)) {
				_result++;
			}
			
		}
		return _result;
	}
	
	

	public Point getPosition() {
		if(f_Pos == null) {
			return new Point();
		}
		return (Point) f_Pos.clone();
	}
	
	public Point getRecommendedPosition() {
		Point _result =  f_recommendedPositionParent.getPosition();
		_result.translate(f_recommendedPosDelta.x, f_recommendedPosDelta.y);
		return _result;
	}
	
	
	
	public Dimension getSize() {
		return new Dimension(
				(int)f_node.getSize().getWidth()+spacingX+f_node.getPaddingX(),
				(int)f_node.getSize().getHeight()+spacingY+f_node.getPaddingY());
	}

	/**
	 * @param joinAlreadyPlaced the joinAlreadyPlaced to set
	 */
	public void setJoinAlreadyPlaced(boolean joinAlreadyPlaced) {
		this.f_joinAlreadyPlaced = joinAlreadyPlaced;
	}

	/**
	 * @return the joinAlreadyPlaced
	 */
	public boolean isJoinAlreadyPlaced() {
		return f_joinAlreadyPlaced;
	}
	
	@Override
	public String toString() {
		return "FOW ("+f_node.toString()+")";
	}

	/**
	 * @param i
	 */
	public void setGrid(int i) {
		f_gridNumber = i;
	}
	
	public int getGrid() {
		return f_gridNumber;
	}

	public void setMoveMode(int moveMode) {
		this.moveMode = moveMode;
	}

	public int getMoveMode() {
		return moveMode;
	}

	/**
	 * @return
	 */
	public boolean isDataObject() {
		if(getWrappedObject() instanceof BPMNNodeInterface) {
			BPMNNodeInterface _n = (BPMNNodeInterface) getWrappedObject();
			return _n.isDataObject();
		}
		return false;
	}

	/**
	 * returns the width offset which gets added to nodes
	 * @return
	 */
	public static int getSpacingX() {
		return spacingX;
	}
	/**
	 * returns the height offset which gets added to nodes
	 * @return
	 */
	public static int getSpacingY() {
		return spacingY;
	}
	
}
