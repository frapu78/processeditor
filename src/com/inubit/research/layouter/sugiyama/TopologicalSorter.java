/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.sugiyama;

import java.util.ArrayList;
import java.util.List;

import com.inubit.research.layouter.TopologicalSorterBasis;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * Provides the order in which nodes are layouted.
 * It basically picks all nodes with an inflow or outflow of 0 and repeats.
 * if this is not possible anymore a cycle exists. to break the cycle an edge has to be inverted (switch source and target).
 * The standard implementation is to pick the nodes with the smallest inflow and the biggest outflow
 * thus minimizing impact on the structure
 * @author ff
 *
 */
public class TopologicalSorter extends TopologicalSorterBasis {
	
	private List<NodeInterface> f_sortedNodesRight = new ArrayList<NodeInterface>();
	
	
	/**
	 * 
	 */
	public TopologicalSorter(AbstractModelAdapter model) {
		super(model);
		while(f_allNodes.size() > 0) {
			
			//removing all sources
			selectStartNodes(); //written to f_selectedNodes
			while(f_selectedNodes.size() > 0) {
				//regular processing
				for(NodeInterface n:f_selectedNodes) {
					f_allNodes.remove(n);
					f_sortedNodes.add(n);
					f_removedYourEdge.remove(n);
					removeOutgoingEdges(n);
				}
				f_selectedNodes.clear();
				selectStartNodes();
			}
			
			//removing all sinks
			selectEndNodes();
			while(f_selectedNodes.size() > 0) {
				//regular processing
				int _pos = 0;
				for(NodeInterface n:f_selectedNodes) {
					f_allNodes.remove(n);
					f_removedYourEdge.remove(n);
					f_sortedNodesRight.add(_pos++,n);
					removeIncomingEdges(n);
				}
				f_selectedNodes.clear();
				selectEndNodes();
			}
			
			if(f_allNodes.size() > 0) {
				breakCycle();
			}
		}
		f_sortedNodes.addAll(f_sortedNodesRight);
	}


	protected void breakCycle() {
		//breaking cycles
		//finding node with max(OutFlow - Inflow)
		int _max = Integer.MIN_VALUE;
		NodeInterface _node = null;
		if(f_removedYourEdge.size() == 0) {
			//the model starts with a big cycle set first node that is in the list 
			//so we can continue
			_node = f_allNodes.get(0);
		}
		for(int i=0;i<f_removedYourEdge.size();i++){
			NodeInterface _curr = f_removedYourEdge.get(i);
			int _val = getOutDegree(_curr) - getInDegree(_curr);
			if(_val > _max) {
				_max = _val;
				_node = _curr;
			}
		}
		//reversing all in edges
		for(int i = f_allEdges.size()-1;i>=0;i--) {
			if(f_allEdges.get(i).getTarget().equals(_node)) {
				switchEdge(f_allEdges.get(i));
			}
		}
	}
}
