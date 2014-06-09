/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.sugiyama.TopologicalSorter;

/**
 * 
 * Provied a topological sorting of all node of the given ProcessModel.
 * It works as described in the paper "A Simple Algorithm for Automatic Layout of BPMN Processes" by
 * Ingo Kitzmann, Christopher Koenig, Danile Luebke and Leif Singer of the Leibniz University Hannover
 * 
 * @author ff
 *
 */
public class ModifiedTopologicalSorter extends TopologicalSorter{
	
	private boolean f_cycle = false;
	private boolean f_cycle2 = false;
	private int f_cycleNodes = 0;
	private List<EdgeInterface> f_reversedEdges = new ArrayList<EdgeInterface>();
	
	/**
	 * 
	 */
	public ModifiedTopologicalSorter(AbstractModelAdapter model) {
		//behavior fully inherited
		super(model);
	}
	
	@Override
	protected void selectStartNodes() {
		super.selectStartNodes();
		Collections.sort(f_selectedNodes,new YPositionComparator(false)); //so nodes do not change 
																		//when layouting twice
	}
	
	@Override
	protected void selectEndNodes() {
		super.selectEndNodes();
		Collections.sort(f_selectedNodes,new YPositionComparator(false)); //so nodes do not change 
		//when layouting twice
	}
	
	@Override
	protected void breakCycle() {
		//cycle detected
		if(f_allNodes.size() != f_cycleNodes) {
			f_cycle= false;
			f_cycle2 = false;
		}
		if(f_removedYourEdge.size() == 0) {
			super.breakCycle();
			return;
		}
		if(f_cycle == false) {
			//standard case
			reverseEdges(true);
		}else {
			//already tried, special handling necessary
			reverseEdges(true); //do again with trace, so original situation is restored
			reverseEdges(false); // only revert the first edge of the cycle
			if(f_cycle2) {
				//already tried this too -> problem with the model (e.g. edge from a null node
				super.breakCycle();
				return;				
			}
			f_cycle2 = true;
		}		
		f_cycle = true;
		f_cycleNodes = f_allNodes.size();
	}

	private void reverseEdges(boolean trace) {
		if(f_reversedEdges == null) {
			f_reversedEdges = new ArrayList<EdgeInterface>();
		}
		f_reversedEdges.clear(); //needed to avoid double reversing in the same cycle!
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		_list.addAll(f_removedYourEdge);
		for(NodeInterface n : _list) {
			if(isCyclic(n)) {
				for(int i = f_allEdges.size()-1;i>=0;i--) {
					if(f_allEdges.get(i).getTarget().equals(n) && !f_reversedEdges.contains(f_allEdges.get(i))) {
						reverseEdge(f_allEdges.get(i),trace);
					}
				}
				break; //only break one cycle at a time
			}
			//else, this is not a cycle -> ignore it
			f_removedYourEdge.remove(n);
		}
	}
	
	/**
	 * reverses the given process edge, and all following edges
	 * up to a gateway. (That is the modification inherent in the class name)
	 * @param processEdge
	 */
	protected void reverseEdge(EdgeInterface processEdge,boolean trace) {
		NodeInterface _target = (NodeInterface)processEdge.getTarget();
		NodeInterface _src = switchEdge(processEdge);
		EdgeInterface _lastSwitchedEdge = processEdge;
		if(trace && !isPool(_target))
			while(_src instanceof BPMNNodeInterface && 
					!(((BPMNNodeInterface)_src).isGateway()
							||((BPMNNodeInterface)_src).isPool()
							||((BPMNNodeInterface)_target).isPool()) && 
					(!_target.equals(_src))
					&& !((_lastSwitchedEdge instanceof BPMNEdgeInterface) 
							&& ((BPMNEdgeInterface)_lastSwitchedEdge).isMessageFlow())){
				for(EdgeInterface e:f_allEdges) {
					if(!e.equals(_lastSwitchedEdge) && e.getTarget().equals(_src)) {
						_src = switchEdge(e);
						_lastSwitchedEdge = e;
						f_reversedEdges.add(e);
						break;
					}
				}
			}
	}
	
	/**
	 * @param _target
	 * @return
	 */
	private boolean isPool(NodeInterface _target) {
		return (_target instanceof BPMNModelInterface) && ((BPMNNodeInterface)_target).isPool();
	}

	@Override
	protected void removeOutgoingEdges(NodeInterface n) {
		if(getModel() instanceof BPMNModelInterface){
			for(int i=f_allEdges.size()-1;i>=0;i--) {
				if(f_allEdges.get(i).getSource().equals(n)) {
					//could be needed next iteration to break cycles
					
						if(!((BPMNNodeInterface)f_allEdges.get(i).getSource()).isDataObject()) {
							if(!f_removedYourEdge.contains(f_allEdges.get(i).getTarget()))
								f_removedYourEdge.add((NodeInterface)f_allEdges.get(i).getTarget());
						}
					
					f_allEdges.remove(i);
				}
			}
		}else {
			super.removeOutgoingEdges(n);
		}
	}

}
