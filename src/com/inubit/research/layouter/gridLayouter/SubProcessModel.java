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
import java.util.List;

import com.inubit.research.layouter.SubModel;
import com.inubit.research.layouter.gridLayouter.FlowObjectWrapper;
import com.inubit.research.layouter.gridLayouter.Grid;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.gridLayouter.Hypergrid;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;

/**
 * This class handles layouting of BPMN Subprocesses.
 * It removes all nodes that are contained in a subprocess from the model
 * and layouts them independently using the GridLayouter.
 * Afterwards the nodes get reinserted into the model again
 * @author ff
 *
 */
public class SubProcessModel extends SubModel{

	
	private BPMNNodeInterface f_mainNode;
	private ArrayList<EdgeInterface> f_edges = new ArrayList<EdgeInterface>();
	private ArrayList<EdgeInterface> f_msfs = new ArrayList<EdgeInterface>();
	
	private int f_xDist = FlowObjectWrapper.getSpacingX();
	private int f_yDist = FlowObjectWrapper.getSpacingY();
	private List<EdgeInterface> f_unroutedEdges;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public SubProcessModel(BPMNNodeInterface subProcessNode,AbstractModelAdapter originalModel,GridLayouter mainLayouter) throws Exception {
		super(originalModel);
		f_mainNode = subProcessNode;
		addNodes(f_mainNode.getContainedNodes());
		if(getNodesInternal().size() > 0) {
			List<EdgeInterface> _edges = originalModel.getEdges();
			for(int i=_edges.size()-1;i>=0;i--){
				EdgeInterface e = originalModel.getEdges().get(i);
				if(getNodesInternal().contains(e.getSource()) && getNodesInternal().contains(e.getTarget())) {
					this.addEdge(e);
					if(e instanceof DummyEdge) {
						originalModel.removeDummyEdge(e);
					}else {
						originalModel.removeEdge(e);
					}
				}else if(! (e instanceof DummyEdge)) { 
					if(getNodesInternal().contains(e.getSource()) || getNodesInternal().contains(e.getTarget())){
						if(!(((BPMNEdgeInterface)e).isMessageFlow()) && !(e.getSource().equals(f_mainNode) || e.getTarget().equals(f_mainNode))) {
							System.out.println("Error Layouting Subprocess ("+f_mainNode+")!");
							System.out.println("It contains Sequence Flows to the main graph!");
							//correct the error! :-)
						}else {
							f_msfs.add(e);
						}
						originalModel.removeEdge(e);
					}					
				}
			}
			//removing nodes
			for(NodeInterface n : getNodesInternal()) {
				originalModel.removeNode(n);
			}
			//now the SubProcessModel gets layouted
			GridLayouter _layouter = new GridLayouter(mainLayouter.getProperties());
			_layouter.layoutModel(this, 0, 0, 0);
			Grid _g = _layouter.getGrids().getGrid(0);
			if(_g.getGridWidth() > 0) {
				f_mainNode.setSize(_g.getGridWidth()+f_xDist+f_mainNode.getPaddingX(), _g.getGridHeight()+f_yDist+f_mainNode.getPaddingY());
			}
			f_unroutedEdges = _layouter.getUnroutedEdges();
		}
	}
	
	
	/**
	 * Add all nodes present in the list recursively, going
	 * into subprocesses as well
	 * @param containedNodes
	 */
	private void addNodes(List<NodeInterface> containedNodes) {
		for(NodeInterface n:containedNodes) {
			if(!(n.equals(f_mainNode)) && (!getNodesInternal().contains(n))) { //avoid self and double containment
				addNode(n);
				BPMNNodeInterface _bpmnn = (BPMNNodeInterface) n;
				if(_bpmnn.isSubProcess()) {
					addNodes(_bpmnn.getContainedNodes());
				}
			}
		}
	}
	
	public void reInsertSubProcess(AbstractModelAdapter model) {
		int _offSetx = f_mainNode.getPos().x - (f_mainNode.getSize().width/2) - Hypergrid.GRID_DISTANCE_X + f_xDist/2 + f_mainNode.getPaddingX()/2;
		int _offSety = f_mainNode.getPos().y - (f_mainNode.getSize().height/2) - Hypergrid.GRID_DISTANCE_Y + f_yDist/2 + f_mainNode.getPaddingY()/2;
		for(NodeInterface n:getNodesInternal()) {
			Point _p = n.getPos();
			_p.translate(_offSetx, _offSety);
			n.setPos(_p.x, _p.y);
			model.addNode(n);
		}
		for(EdgeInterface e: f_edges) {
			if(e instanceof DummyEdge) {
				model.addDummyEdge(e);
			}else {
				model.addEdge(e);
			}
			List<Point> _rps = e.getRoutingPoints();
			for(Point p:_rps) {
				p.translate(_offSetx, _offSety);
			}
			e.clearRoutingPoints();
			if(_rps.size() > 2) {
				_rps.remove(0);
				_rps.remove(_rps.size()-1);
				e.setRoutingPoints(_rps);
			}
		}
		for(EdgeInterface e: f_msfs) {
			if(e instanceof DummyEdge) {
				model.addDummyEdge(e);
			}else {
				model.addEdge(e);
			}			
		}
		//f_mainNode.setContainedNodes(f_nodes);
	}

	@Override
	public void removeNode(NodeInterface o) {
		 for(NodeInterface n:this.getNodes()) {
			 //also remove node from contained clusters 
        	if(n instanceof BPMNNodeInterface) {
        		BPMNNodeInterface c = (BPMNNodeInterface) n;
        		if(c.isSubProcess()) {
        			List<NodeInterface> _list = c.getContainedNodes();
        			if(_list.contains(o)) {
        				_list.remove(o);
        				//c.setContainedNodes(_list);
        			}
        		}
        	}
        }
		super.removeNode(o);
	}

	public List<EdgeInterface> getUnroutedEdges(){
		return f_unroutedEdges;
	}
}
