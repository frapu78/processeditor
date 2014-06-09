/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class BPMNPreProcessor implements IPreProcessor {

	/**
	 * 
	 */
	private static final int ATTACHEDEVENT_DISTANCE = 40;

	/**
	 * for Data Objects
	 */
	private ArrayList<EdgeInterface> f_switchedEdges = new ArrayList<EdgeInterface>();
	/**
	 * for Attachted Intermediate Events
	 */
	private ArrayList<List<EdgeInterface>> f_virtualEdges = new ArrayList<List<EdgeInterface>>();
	private ArrayList<List<EdgeInterface>> f_removedEdges = new ArrayList<List<EdgeInterface>>();
	private ArrayList<NodeInterface> f_removedNodes = new ArrayList<NodeInterface>();
	private ArrayList<NodeInterface> f_removedNodesParent = new ArrayList<NodeInterface>();
	
	/**
	 * For Annotations
	 * 
	 */
	private HashMap<NodeInterface,List<EdgeInterface>> f_AnnEdges = new HashMap<NodeInterface,List<EdgeInterface>>();
	private ArrayList<NodeInterface> f_AnnNodes = new ArrayList<NodeInterface>();
	private ArrayList<Point> f_AnnNodesRelativeCoords = new ArrayList<Point>();
	
	
	private List<EdgeInterface> f_successorEdges = new ArrayList<EdgeInterface>();;
	
	@Override
	public void process(AbstractModelAdapter model) {
		clearLists();
		processDataObjects(model);
		processAttachtedIntermediateEvent(model);
		processAnnotations(model);
	}


	


	/**
	 * @param model
	 */
	private void processAnnotations(AbstractModelAdapter model) {
		for(NodeInterface node: model.getNodes()) {
			BPMNNodeInterface _node = (BPMNNodeInterface) node;
			if(_node.isAnnotation()){
				Point _r = new Point();
				List<EdgeInterface> _edges = getEdges(_node,model);
				if(_edges.size() > 0) {
					EdgeInterface e = _edges.get(0);
					NodeInterface _target = (NodeInterface) (e.getSource().equals(_node) ? (NodeInterface)e.getTarget() : e.getSource());
					_r = _target.getPos();
					Point _myPos = _node.getPos();
					_r.translate(-_myPos.x, -_myPos.y);
				}
				f_AnnNodes.add(_node);
				f_AnnEdges.put(_node,_edges);
				f_AnnNodesRelativeCoords.add(_r);
				
				model.removeNode(_node);
				for(EdgeInterface e:_edges) {
					if(e instanceof DummyEdge) {
						model.removeDummyEdge(e);
					}else {
						model.removeEdge(e);
					}
				}
			}
		}
	}


	/**
	 * @param _node
	 * @param model 
	 * @return
	 */
	private List<EdgeInterface> getEdges(BPMNNodeInterface node, AbstractModelAdapter model) {
		ArrayList<EdgeInterface> _result = new ArrayList<EdgeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e.getSource().equals(node) || e.getTarget().equals(node)) {
				_result.add(e);
			}
		}
		return _result;
		
	}


	private void processAttachtedIntermediateEvent(AbstractModelAdapter model) {
		//attaching a virtual edge for attached intermediate events
		for(NodeInterface n:model.getNodes()) {
			NodeInterface _attachee = ((BPMNNodeInterface)n).isAttatchedTo(model);
			if(_attachee != null) {
				List<NodeInterface> _successors = getSuccessors(n,model); //sets f_successorEdges
				List<EdgeInterface> _edges = new ArrayList<EdgeInterface>();
				ArrayList<EdgeInterface> _virtEdges = new ArrayList<EdgeInterface>();
				if(_successors.size() > 0) {
					for(int i=0;i<_successors.size();i++) {
						EdgeInterface _edge = f_successorEdges.get(i);
						if(!(_edge instanceof BPMNEdgeInterface && ((BPMNEdgeInterface)_edge).isMessageFlow())) {
							NodeInterface _succ = _successors.get(i);						
							DummyEdge _d = new DummyEdge((BPMNNodeInterface)_attachee,(BPMNNodeInterface)_succ);
							_virtEdges.add(_d);
							model.addDummyEdge(_d);							
							_edges.add(f_successorEdges.get(i));
							if(f_successorEdges.get(i) instanceof DummyEdge) {
								model.removeDummyEdge(f_successorEdges.get(i));
							}else {
								model.removeEdge(f_successorEdges.get(i));
							}
						}
							
					}
					f_removedEdges.add(_edges);
					f_virtualEdges.add(_virtEdges);
				}else {
					ArrayList<EdgeInterface> _nulllist =  new ArrayList<EdgeInterface>();
					_nulllist.add(null);
					f_virtualEdges.add(_nulllist);
					f_removedEdges.add(_nulllist);					
				}			
				//also not to lose predecessors (if there are any, e.g. a message flow)
				//we change their target from the AIE to the main node
				for(EdgeInterface e:getPredecessorEdges(n, model)) {
					e.setTarget(_attachee);					
				}
				
				f_removedNodes.add(n);
				f_removedNodesParent.add(_attachee);				
				model.removeNode(n);
			}
		}
	}


	private void processDataObjects(AbstractModelAdapter model) {
		//ensuring data objects are never a source
		for(EdgeInterface e:model.getEdges()) {
			if(e.getSource() instanceof BPMNNodeInterface) {
				if(((BPMNNodeInterface)e.getSource()).isDataObject() && 
						!(((BPMNNodeInterface)e.getTarget()).isDataObject())){
					f_switchedEdges.add(e);
					LayoutHelper.switchEdge(e);
				}
			}
		}
	}
	
	
	/**
	 * @param _att
	 * @param model
	 * @return
	 */
	private List<NodeInterface> getSuccessors(NodeInterface aie, AbstractModelAdapter model) {
		f_successorEdges.clear();
		List<NodeInterface> _result = new ArrayList<NodeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e.getSource().equals(aie)) {
				f_successorEdges.add(e);
				_result.add((NodeInterface) e.getTarget());
			}
		}
		return _result;
	}
	
	/**
	 * @param _att
	 * @param model
	 * @return
	 */
	private List<EdgeInterface> getPredecessorEdges(NodeInterface aie, AbstractModelAdapter model) {
		f_successorEdges.clear();
		List<EdgeInterface> _result = new ArrayList<EdgeInterface>();
		for(EdgeInterface e:model.getEdges()) {
			if(e.getTarget().equals(aie)) {
				_result.add(e);
			}
		}
		return _result;
	}
	
	private HashMap<NodeInterface,Integer> f_useCounts = new HashMap<NodeInterface, Integer>();

	@Override
	public void unprocess(AbstractModelAdapter model) {
		unprocessAnnotations(model);
		unprocessAttachtedIntermediateEvents(model);
		unprocessDataObjects();
		
	}





	private void clearLists() {
		f_switchedEdges.clear();
		f_virtualEdges.clear();
		f_removedEdges.clear();
		f_removedNodes.clear();
		f_removedNodesParent.clear();
		f_useCounts.clear();
		f_successorEdges.clear();
		f_AnnEdges.clear();
		f_AnnNodes.clear();
		f_AnnNodesRelativeCoords.clear();
	}


	


	/**
	 * @param model 
	 * 
	 */
	private void unprocessAnnotations(AbstractModelAdapter model) {
		for(int i=0;i<f_AnnNodes.size();i++) {
			NodeInterface _node = f_AnnNodes.get(i);
			List<EdgeInterface> _edges = f_AnnEdges.get(_node);
			if(_edges.size() > 0) {
				EdgeInterface e = _edges.get(0);
				NodeInterface _target = (NodeInterface) (e.getSource().equals(_node) ? (NodeInterface)e.getTarget() : e.getSource());
				Point _pos = _target.getPos();
				Point _r = f_AnnNodesRelativeCoords.get(i);
				_node.setPos(_pos.x-_r.x, _pos.y-_r.y);
			}
			model.addNode(_node);
			for(EdgeInterface e:_edges) {
				if(e instanceof DummyEdge) {
					model.addDummyEdge(e);
				}else {
					model.addEdge(e);
					e.clearRoutingPoints();
				}
			}			
		}
	}


	private void unprocessAttachtedIntermediateEvents(AbstractModelAdapter model) {
		int _lasti = -1;
		for(int i=0;i<f_removedNodes.size();i++) {
			BPMNNodeInterface _aie = (BPMNNodeInterface) f_removedNodes.get(i);
			List<EdgeInterface> _virtEdges = f_virtualEdges.get(i);
			for(EdgeInterface e:_virtEdges) {
				if(e != null) {
					model.removeDummyEdge(e);
				}
			}
			
			model.addNode(_aie);
			
			for(EdgeInterface edge : f_removedEdges.get(i)) {
				if(edge != null) {
					if(edge instanceof DummyEdge) {
						model.addDummyEdge(edge);
					}else {
						model.addEdge(edge);
					}
				}
			}
			
			//"merging" positions
			NodeInterface _src = null;
			NodeInterface _tgt = null;			
			for(int ec=0;ec<_virtEdges.size();ec++) {
				EdgeInterface e = _virtEdges.get(ec);
				EdgeInterface realEdge = f_removedEdges.get(i).get(ec);
				if(e != null) {
					_src = (NodeInterface) e.getSource();
					_tgt = (NodeInterface)e.getTarget();
				}else {
					_src = f_removedNodesParent.get(i);
				}
				Point _pos = _src.getPos();
				boolean top = _tgt != null && (_src.getPos().y < _tgt.getPos().y);
				if(top) {
					//layout on top	
					_pos.y += _src.getSize().height/2;
				}else {
					//layout below
					_pos.y -= _src.getSize().height/2;
				}
				//correcting x Position if necessary
				Integer _uses = f_useCounts.get(_src);
				if(_uses == null) {
					_uses = 0;
				}
				if(_lasti == i) {
					_uses--;
				}
				_pos.x += _uses++ * -ATTACHEDEVENT_DISTANCE;				
				f_useCounts.put(_src, _uses);
				_aie.setPos(_pos.x, _pos.y);
				_aie.setAttatchedTo(model, _src);
				if(realEdge != null) {					
					realEdge.clearRoutingPoints();
					//adding a routing point to edge
					Point p = new Point(_pos.x,_tgt.getPos().y);
					ArrayList<Point> _rps = new ArrayList<Point>();
					_rps.add(p);
					realEdge.setRoutingPoints(_rps);					
				}	
				_lasti = i;
			}			
		}		
	}


	private void unprocessDataObjects() {
		for(EdgeInterface e: f_switchedEdges) {
			LayoutHelper.switchEdge(e);
			LayoutHelper.invertRoutingPoints(e);
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof BPMNModelInterface;
	}
}
