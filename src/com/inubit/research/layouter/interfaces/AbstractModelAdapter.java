/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.interfaces;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Defines a generic superclass for ProcessModel Layout.
 * More specialized classes extend this class.
 * it provides the nodes and edgeinterfaces needed for the layout.
 * @author ff
 *
 */
public abstract class AbstractModelAdapter{

	private ArrayList<EdgeInterface> f_edgeCache = null; //cache edges so they are not re-read on every call
	
	private ArrayList<EdgeInterface> f_dummyEdges = new ArrayList<EdgeInterface>();
	private ArrayList<NodeInterface> f_dummyNodes = new ArrayList<NodeInterface>();

	private ArrayList<EdgeInterface> f_removedEdges = new ArrayList<EdgeInterface>();
	private ArrayList<NodeInterface> f_removedNodes = new ArrayList<NodeInterface>();
	protected HashMap<NodeInterface, Dimension> f_edgeLayoutSizeStore = new HashMap<NodeInterface, Dimension>();
	
	
	protected ArrayList<NodeInterface> getRemovedNodes(){
		return f_removedNodes;
	}
	
	protected ArrayList<EdgeInterface> getRemovedEdges(){
		return f_removedEdges;
	}

	public void addEdge(EdgeInterface e) {
		f_removedEdges.remove(e);
	}
	
	public void addNode(NodeInterface n) {
		f_removedNodes.remove(n);
	}
	
	
	/**
	 * returns the interfaces to all edges within the model
	 * @return
	 */
	public List<EdgeInterface> getEdges(){
		List<EdgeInterface> _list = getEdgeList();
		_list.removeAll(f_removedEdges);
		_list.addAll(f_dummyEdges);
		return _list;
	}

	/**
	 * adds a cache in between for "real" models
	 * @return
	 */
	protected List<EdgeInterface> getEdgeList() {
		if(f_edgeCache == null) {
			f_edgeCache = new ArrayList<EdgeInterface>(getEdgesInternal());
		}
		return new ArrayList<EdgeInterface>(f_edgeCache);
	}

	/**
	 * returns the interfaces to all nodes within the model
	 * @return
	 */
	public List<NodeInterface> getNodes(){
		List<NodeInterface> _list = getNodesInternal();
		_list.removeAll(f_removedNodes);
		_list.addAll(f_dummyNodes);
		return _list;
	}	
	
	public abstract List<EdgeInterface> getEdgesInternal();
	public abstract List<NodeInterface> getNodesInternal();
	
	public void removeEdge(EdgeInterface e) {
		f_removedEdges.add(e);
	}
	
	public void removeNode(NodeInterface n) {
		f_removedNodes.add(n);		
	}
	
	
	/**
	 * dummy edges are needed for preprocessing steps.
	 * They add a temporary edge to the model.
	 * As This Edge is just an interface implementation of the EdgeInterface
	 * it cannot be added like regular edges.
	 * Nevertheless a call to getEdges() includes the Dummy Edges!
	 * @param edge
	 */
	public void addDummyEdge(EdgeInterface edge) {
		f_dummyEdges.add(edge);
	}

	/**
	 * All dummy edges will be removed after the layouting using this method.
	 * @param edge
	 */
	
	public void removeDummyEdge(EdgeInterface edge) {
		f_dummyEdges.remove(edge);
	}
	
	
	public void addDummyNode(NodeInterface node) {
		f_dummyNodes.add(node);
	}
	
	public void removeDummyNode(NodeInterface node) {
		f_dummyNodes.remove(node);
	}
	
	public List<EdgeInterface> getDummyEdges(){
		return f_dummyEdges;
	}
	
	public List<NodeInterface> getDummyNodes(){
		return f_dummyNodes;
	}

	/**
	 * this method is needed to store the changed edgelayout size
	 * a call to getNodes does not guarantee that the same object is returned
	 * and thus this information could get lost if a new adapter
	 * is created
	 * @param node
	 * @param size
	 */
	public void saveEdgeLayoutSize(NodeInterface node, Dimension size) {
		f_edgeLayoutSizeStore.put(node,size);
	}
	
	/**
	 * @return the f_edgeLayoutSizeStore
	 */
	public HashMap<NodeInterface, Dimension> getEdgeLayoutSizeStore() {
		return f_edgeLayoutSizeStore;
	}

	/**
	 * @param layoutSizeStore the f_edgeLayoutSizeStore to set
	 */
	public void setEdgeLayoutSizeStore(
			HashMap<NodeInterface, Dimension> layoutSizeStore) {
		f_edgeLayoutSizeStore = layoutSizeStore;
	}
	
	/**
	 * used to modify the handling of edge layouts.
	 * set a new size here if you do not want the algortihm
	 * to be able to use the full width/height for the layout of edges
	 * but only parts of it (e.g. for orgchart using sugiyama)
	 * method is implemented in AbstractNodeInterface - usage of it is encouraged
	 * @return
	 */
	public Dimension getEdgeLayoutSize(NodeInterface node) {
		if(f_edgeLayoutSizeStore.containsKey(node)) {
			return f_edgeLayoutSizeStore.get(node);
		}
		return node.getSize();
	}
	
	/**
	 * @see getEdgeLayoutSize
	 * @param value
	 */
	public void setEdgeLayoutSize(NodeInterface node,Dimension value) {
		f_edgeLayoutSizeStore.put(node, value);
	}

}
