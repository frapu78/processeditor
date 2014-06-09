/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class BPMNDataObjectStackProcessor{
	
	private ArrayList<DOTree> f_data = new ArrayList<DOTree>();
	


	public void extractStackedDataObjects(AbstractModelAdapter model) {
		//finding all data-object-trees (data objects that are connected to other data objects)
		for(NodeInterface n : model.getNodes()) {
			BPMNNodeInterface _n = (BPMNNodeInterface) n;
			if(_n.isDataObject()) {
				if(isPartOfTree(_n,model)) {
					f_data.add(new DOTree(_n,model));
				}
			}
		}
	}

	/**
	 * checks whether any predecessors or successors are also dataobjects of dat
	 * @param dat
	 * @param model
	 * @return
	 */
	private boolean isPartOfTree(BPMNNodeInterface dat, AbstractModelAdapter model) {
		for(EdgeInterface e:model.getEdges()) {
			if(e.getTarget().equals(dat) && ((BPMNNodeInterface)e.getSource()).isDataObject()) {
				return true;
			}
			if(e.getSource().equals(dat) && ((BPMNNodeInterface)e.getTarget()).isDataObject()) {
				return true;
			}
		}
		return false;
	}


	
	public void LayoutStacks(AbstractModelAdapter model,Hypergrid grid,GridLayouter layouter) {
		for(DOTree d:f_data) {
			if(d.handle()) {
				d.addToModel(model);
				BPMNNodeInterface _start = d.getStartNode();
				FlowObjectWrapper _fow = FlowObjectWrapper.getFlowObjectWrapper(_start, model);
				layouter.layoutDataObject(_fow);
				_fow.setMoveMode(0);
				layoutStack(model,grid,layouter,_fow,0);		
			}
		}
	}
	
	/**
	 * @param model
	 * @param grid
	 * @param layouter
	 * @param _fow
	 */
	private void layoutStack(AbstractModelAdapter model, Hypergrid grid,GridLayouter layouter, FlowObjectWrapper _fow,int xOff) {
		int _dir = ((BPMNNodeInterface)_fow.getWrappedObject()).placeDataObjectUpwards() ? -1 : +1;
		Point _p = _fow.getPosition();
		_p.x += xOff;
		List<FlowObjectWrapper> _list = _fow.getSuccessors();
		if(((BPMNNodeInterface)_fow.getWrappedObject()).placeDataObjectUpwards()) {
			Collections.reverse(_list);
		}
		for(FlowObjectWrapper w:_list) {
			_p.y += _dir;
			Grid _myGrid = grid.getGrid(_fow.getGrid());
			if(_p.y<0 || _p.y >= _myGrid.getRowCount() || _myGrid.getObject(_p.y, _p.x) != null) {
				if(_p.y<0)_p.y=0;
				_myGrid.addRow(_p.y);
				_myGrid.addCol(_p.x);
			}
			w.setMoveMode(xOff==0?+1:0);
			_myGrid.setObject(_p.y, _p.x, w);
			layoutStack(model, grid, layouter, w, xOff+1);
		}
	}

	private class DOTree{
		
		private boolean f_handle = false;
		private HashSet<BPMNNodeInterface> f_nodes = new HashSet<BPMNNodeInterface>();
		private HashSet<EdgeInterface> f_edges = new HashSet<EdgeInterface>();
		
		public DOTree(BPMNNodeInterface startNode,AbstractModelAdapter model) {
			f_nodes.add(startNode);
			int _num = 0;
			while(_num != f_nodes.size()) {
				_num = f_nodes.size();
				for(EdgeInterface e:model.getEdges()) {
					if(f_nodes.contains(e.getSource()) || f_nodes.contains(e.getTarget())) {
						if(((BPMNNodeInterface) e.getSource()).isDataObject()) {
							f_nodes.add((BPMNNodeInterface) e.getSource());
						}
						if(((BPMNNodeInterface) e.getTarget()).isDataObject()) {
							f_nodes.add((BPMNNodeInterface) e.getTarget());
						}
						f_edges.add((BPMNEdgeInterface) e);
						//checking if the tree is connected to at least one other node
						//if not, it will not be handled and processed regularily
						if(!((BPMNNodeInterface)e.getSource()).isDataObject() || 
						   !((BPMNNodeInterface)e.getTarget()).isDataObject()) {
							f_handle = true;
						}
					}
				}
			}
			if(f_handle) {
				removeFromModel(model);
			}
		}

		/**
		 * @return
		 */
		public boolean handle() {
			return f_handle;
		}

		/**
		 * @param model
		 */
		public void addToModel(AbstractModelAdapter model) {
			for(BPMNNodeInterface n:f_nodes) {
				model.addNode(n);
			}
			for(EdgeInterface n:f_edges) {
				model.addEdge(n);
			}
		}

		/**
		 * @return
		 */
		public BPMNNodeInterface getStartNode() {
			for(EdgeInterface e:f_edges) {
				//due to preprocessing a dataObject is never a source when connected to anything else!
				if(!((BPMNNodeInterface)e.getSource()).isDataObject()) {
					return (BPMNNodeInterface) e.getTarget();
				}
			}
			return null;
		}

		/**
		 * @param model
		 */
		private void removeFromModel(AbstractModelAdapter model) {
			for(EdgeInterface n:f_edges) {
				model.removeEdge(n);
			}
			for(BPMNNodeInterface n:f_nodes) {
				model.removeNode(n);
			}
		}		
	}
}
