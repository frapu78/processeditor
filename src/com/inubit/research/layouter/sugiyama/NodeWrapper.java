/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.sugiyama;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class NodeWrapper{
	
	// Map which holds all singeltons
	private static HashMap<NodeInterface, NodeWrapper> f_wrappedObjects = new HashMap<NodeInterface, NodeWrapper>();
	
	
	private ArrayList<NodeWrapper> f_predecessors = new ArrayList<NodeWrapper>();
	private ArrayList<NodeWrapper> f_successors = new ArrayList<NodeWrapper>();
	private NodeInterface f_node;
	private AbstractModelAdapter f_model;
	
	private int f_layer = 0;
	private float f_baryCenter = 0.0f;


	private int f_cellHeight = 0; //special value for dummy nodes so edge routing can be improved
	
	private boolean f_dummy = false;
	private boolean f_fixed = false;
	private boolean f_longEdgeProcessed = false;
	private boolean f_addedToLayerStructure = false;
	private boolean f_coordsWrittenBack = false;


	private float f_prio;


	private Point f_pos;


	private boolean f_hierarchyNode = false;


	private int f_moved;
	
	/**
	 * 
	 */
	public NodeWrapper(NodeInterface node,AbstractModelAdapter model) {
		f_node = node;
		f_model = model;
	}
	
	/**
	 * a dummy NodeWrapper 
	 */
	public NodeWrapper(int layer,NodeWrapper predecessor,NodeWrapper successor) {
		f_dummy  = true;
		f_layer = layer;
		f_predecessors.add(predecessor);
		f_successors.add(successor);
	}
	
	/**
	 * a dummy NodeWrapper 
	 */
	public NodeWrapper(int layer) {
		f_dummy  = true;
		f_layer = layer;
	}
	
	/**
	 * fills predecessor and successor lists
	 */
	public void buildLinks() {
		f_successors.clear();
		f_predecessors.clear();
		//building predecessor and successor lists
		for(EdgeInterface edge : new ArrayList<EdgeInterface>(f_model.getEdges())) {
			if(edge.getSource().equals(f_node)) {
				f_successors.add(getNodeWrapper((NodeInterface)edge.getTarget(),f_model));
			}else if(edge.getTarget().equals(f_node)) {
				f_predecessors.add(getNodeWrapper( (NodeInterface) edge.getSource(),f_model));
			}			
		}
	}
	
	/**
	 * @return the baryCenter
	 */
	public float getBaryCenter() {
		return f_baryCenter;
	}
	
	public int getLayer() {
		return f_layer;
	}
	
	public NodeInterface getNode() {
		return f_node;
	}
	
	/**
	 * factory method for the retrieval of correctly wrapped objects
	 * @param obj
	 * @param model
	 * @return
	 */
	public static NodeWrapper getNodeWrapper(NodeInterface obj,AbstractModelAdapter model) {
		NodeWrapper _result = f_wrappedObjects.get(obj);
		if(_result == null) {
			_result = new NodeWrapper(obj,model);
			f_wrappedObjects.put(obj, _result);
			_result.buildLinks();
		}
		return _result;
	}
	
	public static void clear() {
		f_wrappedObjects.clear();
	}
	
	public List<NodeWrapper> getPredecessors(){
		return f_predecessors;
	}
	
	public List<NodeWrapper> getSuccessors(){
		return f_successors;
	}
	
	public void setMinLayer(int value) {
		if(f_layer < value) {
			f_layer = value;
		}
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NodeWrapper) {
			NodeWrapper _o = (NodeWrapper)obj;
			if(f_node != null) {
				if(_o.getNode() != null) {
					return f_node.equals(_o.getNode());
				}
			}
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if(f_node != null) {
			return f_node.hashCode();
		}
		return super.hashCode();
	}
	
	/**
	 * @param baryCenter the baryCenter to set
	 */
	public void setBaryCenter(float baryCenter) {
		this.f_baryCenter = baryCenter;
	}
	
	
	public void addToBaryCenter(float value) {
		this.f_baryCenter += value;
	}

	@Override
	public String toString() {
		if(f_node != null) {
			return "NodeWrapper ("+f_node.toString()+")";
		}
		return "DummyNode";
	}

	/**
	 * @return
	 */
	public boolean isDummyNode() {
		return f_dummy;
	}

	/**
	 * @param f
	 */
	public void setPriority(float f) {
		f_prio = f;
	}

	/**
	 * @param fixed the fixed to set
	 */
	public void setFixed(boolean fixed) {
		this.f_fixed = fixed;
	}

	/**
	 * @return the fixed
	 */
	public boolean isFixed() {
		return f_fixed;
	}

	
	/**
	 * @return the f_prio
	 */
	public float getPriority() {
		return f_prio;
	}

	/**
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y) {
		if(f_node != null) {
			f_node.setPos(x, y);
		}else {
			//dummy
			f_pos = new Point(x,y);
		}
	}
	
	public Point getPos() {
		if(f_node != null) {
			return f_node.getPos();
		}
		return f_pos;
	}

	/**
	 * @param longEdgeProcessed the longEdgeProcessed to set
	 */
	public void setLongEdgeProcessed(boolean longEdgeProcessed) {
		this.f_longEdgeProcessed = longEdgeProcessed;
	}

	/**
	 * @return the longEdgeProcessed
	 */
	public boolean isLongEdgeProcessed() {
		return f_longEdgeProcessed;
	}

	/**
	 * @param addedToLayerStructure the addedToLayerStructure to set
	 */
	public void setAddedToLayerStructure(boolean addedToLayerStructure) {
		this.f_addedToLayerStructure = addedToLayerStructure;
	}

	/**
	 * @return the addedToLayerStructure
	 */
	public boolean isAddedToLayerStructure() {
		return f_addedToLayerStructure;
	}

	/**
	 * @param coordsWrittenBack the coordsWrittenBack to set
	 */
	public void setCoordsWrittenBack(boolean coordsWrittenBack) {
		this.f_coordsWrittenBack = coordsWrittenBack;
	}

	/**
	 * @return the coordsWrittenBack
	 */
	public boolean isCoordsWrittenBack() {
		return f_coordsWrittenBack;
	}

	/**
	 * 
	 */
	public void markAsHierarchyNode() {
		f_hierarchyNode = true;
	}
	
	public boolean isHierarchyNode() {
		return f_hierarchyNode;
		
	}

	/**
	 * @param i
	 */
	public void setCellHeight(int value) {
		f_cellHeight = value;
	}
	
	public int getCellHeight() {
		return f_cellHeight;
	}

	/**
	 * @param offset
	 */
	public void setMoved(int offset) {
		f_moved = offset;
	}
	
	public int getMoved() {
		return f_moved;
	}
	
	
}
