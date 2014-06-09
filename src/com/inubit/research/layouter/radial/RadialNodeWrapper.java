/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.radial;

import java.util.HashSet;
import java.util.Set;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;


/**
 * @author ff
 *
 */
public class RadialNodeWrapper {
	
	private NodeInterface f_node;
	private AbstractModelAdapter f_model;
	private RadialLayouter f_parent;
	private Set<NodeInterface> f_neighbors;
	private int f_start;
	private int f_end;
	private int f_layer;

	/**
	 * 
	 */
	public RadialNodeWrapper(RadialLayouter layouter,AbstractModelAdapter model,NodeInterface node,int layer) {
		f_parent = layouter;
		f_node = node;
		f_model = model;
		f_neighbors = calcNeighbors(node);
		setLayer(layer);
	}

	public NodeInterface getNode() {
		return f_node;
	}
	
	
	/**
	 * @param node
	 * @return
	 */
	private Set<NodeInterface> calcNeighbors(NodeInterface node) {
		Set<NodeInterface> _result = new HashSet<NodeInterface>();
		for(EdgeInterface e:f_model.getEdges()) {
			if(e.getSource().equals(node)) {
				if(f_parent.isUnlayouted((NodeInterface)e.getTarget()))
					_result.add((NodeInterface)e.getTarget());
			}
			if(e.getTarget().equals(node)) {
				if(f_parent.isUnlayouted((NodeInterface) e.getSource()))
					_result.add((NodeInterface) e.getSource());
			}
		}
		_result.remove(this.getNode());
		return _result;
	}
	
	public Set<NodeInterface> getNeighbors(){
		return f_neighbors;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.f_start = start;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return f_start;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.f_end = end;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return f_end;
	}

	/**
	 * @param f_layer the f_layer to set
	 */
	public void setLayer(int layer) {
		this.f_layer = layer;
	}

	/**
	 * @return the f_layer
	 */
	public int getLayer() {
		return f_layer;
	}

	/**
	 * returns teh angle in radians
	 * @return
	 */
	public double getAngle() {
		return  Math.toRadians(getAngleDEG());
	}
	
	/**
	 * returns the angle in degrees
	 * @return
	 */
	public double getAngleDEG() {
		return  (f_start+f_end)/2;
	}
	
	@Override
	public String toString() {
		return "RadialNodeWrapper ("+f_node.toString()+")";
	}

	/**
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y) {
		f_node.setPos(x, y);
	}
	
	
	
}
