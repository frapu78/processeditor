/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public abstract class TopologicalSorterBasis {
	
	protected AbstractModelAdapter f_model;
	
	protected List<EdgeInterface> f_allEdges = new ArrayList<EdgeInterface>();
	protected List<NodeInterface> f_allNodes = new ArrayList<NodeInterface>();
	protected List<NodeInterface> f_selectedNodes = new ArrayList<NodeInterface>();
	protected List<NodeInterface> f_sortedNodes = new ArrayList<NodeInterface>();
	protected List<EdgeInterface> f_backwardsEdges = new ArrayList<EdgeInterface>();
	
	protected List<NodeInterface> f_removedYourEdge = new ArrayList<NodeInterface>();
	
	
	/**
	 * 
	 */
	public TopologicalSorterBasis(AbstractModelAdapter copy) {
		initLists(copy);
	}
	
	/**
	 * @param curr
	 * @return
	 */
	protected int getOutDegree(NodeInterface curr) {
		int _result = 0;
		for(EdgeInterface n:f_allEdges) {
			if(n.getSource().equals(curr)) {
				_result++;
			}
		}
		return _result;
	}
	
	/**
	 * @param curr
	 * @return
	 */
	protected int getInDegree(NodeInterface curr) {
		int _result = 0;
		for(EdgeInterface n:f_allEdges) {
			if(n.getTarget().equals(curr)) {
				_result++;
			}
		}
		return _result;
	}


	/**
	 * a list with all nodes in the topologically sorted order
	 * @return
	 */
	public List<NodeInterface> getSortedNodes(){
		return f_sortedNodes;
	}
	
	/**
	 * the given processModel, with reversed edges.
	 * @return
	 */
	public AbstractModelAdapter getModel() {
		return f_model;
	}
	
	/**
	 * @param n
	 * @return
	 */
	protected boolean isCyclic(NodeInterface n) {
		LinkedList<NodeInterface> _visited = new LinkedList<NodeInterface>();
		return isCyclicR(n, _visited);
	}
	
	protected boolean isCyclicR(NodeInterface current,LinkedList<NodeInterface> visited) {
		if(visited.contains(current)) {
			if(visited.indexOf(current) == 0)
				return true;
			return false;
		}
		boolean result = false;
		for(EdgeInterface e: f_allEdges) {
			if(e.getSource().equals(current)) {
				NodeInterface _tgt = (NodeInterface)e.getTarget();
				visited.add(current);
				result = result || isCyclicR(_tgt, visited);
			}
		}
		return result;
	}

	/**
	 * reverses the edge to break cycles.
	 * the reversed edge is stored and f_backwardsEdges so no information is lost
	 * @param processEdge
	 * @return
	 */
	protected NodeInterface switchEdge(EdgeInterface processEdge) {
		NodeInterface _src = LayoutHelper.switchEdge(processEdge);
		if(f_backwardsEdges.contains(processEdge)) {
			f_backwardsEdges.remove(processEdge); // if it is switched twice, it was not switched at all
		}else {
			f_backwardsEdges.add(processEdge);
		}
		return _src;
	}


	/**
	 * initialises starting lists
	 * @param model
	 */
	protected void initLists(AbstractModelAdapter model) {
		f_model = model;
		f_allNodes.addAll(f_model.getNodes());
		f_allEdges.addAll(f_model.getEdges());
		/* This piece of code removes self references.
		 * This is necessary as a self reference would lead to an infinite loop otherwise
		 * Usually the LoopPreProcessor removes all self-references, but we leave
		 * the code here as a double check!
		 * */
		for(int i=f_allEdges.size()-1;i>=0;i--) {
			EdgeInterface _e = f_allEdges.get(i);
			if(_e.getSource().equals(_e.getTarget())) {
				f_allEdges.remove(i);
			}
		}
	}


	/**
	 * puts all nodes into the f_SelectedNodes Nodelist, which
	 * have an InFlow = 0
	 */
	protected void selectStartNodes() {
		f_selectedNodes.addAll(f_allNodes);
		for(EdgeInterface e:f_allEdges) {
			f_selectedNodes.remove(e.getTarget());
		}
	}
	
	/**
	 * puts all nodes into the f_SelectedNodes Nodelist, which
	 * have an OutFlow = 0
	 */
	protected void selectEndNodes() {
		f_selectedNodes.addAll(f_allNodes);
		for(EdgeInterface e:f_allEdges) {
			f_selectedNodes.remove(e.getSource());
		}
	}


	/**
	 * removes all outgoing edges of Node n from the list
	 * @param n
	 */
	protected void removeOutgoingEdges(NodeInterface n) {
		for(int i=f_allEdges.size()-1;i>=0;i--) {
			if(f_allEdges.get(i).getSource().equals(n)) {
				//could be needed next iteration to break cycles
				if(!f_removedYourEdge.contains(f_allEdges.get(i).getTarget()))
					f_removedYourEdge.add((NodeInterface)f_allEdges.get(i).getTarget());
				f_allEdges.remove(i);
			}
		}
	}
	
	/**
	 * removes all outgoing edges of Node n from the list
	 * @param n
	 */
	protected void removeIncomingEdges(NodeInterface n) {
		for(int i=f_allEdges.size()-1;i>=0;i--) {
			if(f_allEdges.get(i).getTarget().equals(n)) {
				f_allEdges.remove(i);
			}
		}
	}
	
	/**
	 * restores the original direction of all edges
	 */
	public void restoreEdges() {
		for(EdgeInterface e:f_backwardsEdges) {
			LayoutHelper.switchEdge(e);
			LayoutHelper.invertRoutingPoints(e);
		}
	}

}
