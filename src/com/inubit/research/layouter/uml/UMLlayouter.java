/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.uml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.UMLModelInterface;
import com.inubit.research.layouter.preprocessor.HierarchyExtractor;
import com.inubit.research.layouter.preprocessor.LonelyNodesRemover;
import com.inubit.research.layouter.sugiyama.LayerStructure;
import com.inubit.research.layouter.sugiyama.NodeWrapper;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import com.inubit.research.layouter.sugiyama.TopologicalSorter;

/**
 * This is a first start of a layouter
 * specialized for UML diagrams, but it is producing poor results so far.
 * It is probably better to use the sugiyama layouter directly
 * @author ff
 *
 */
public class UMLlayouter extends SugiyamaLayoutAlgorithm  {

	
	
	/**
	 * @param properties
	 */
	public UMLlayouter(Properties properties) {
		super(properties);
	}

	private TopologicalSorter f_sorter;
	private LayerStructure f_HierarchyLayers;

	@Override
	public String getDisplayName() {
		return "UML layouter";
	}

	@Override
	public void layoutModel(AbstractModelAdapter model, int xstart, int ystart,int direction) throws Exception {
		if(!(model instanceof UMLModelInterface)) {
			System.out.println("Model type not supported");
		}else {			
			layout( model);
		}
	}

	/**
	 * @param model
	 * @throws Exception 
	 */
	private void layout(AbstractModelAdapter model) throws Exception {
		clearTemporaryData(model);
		HierarchyExtractor _ex = new HierarchyExtractor();
		_ex.process(model);
		if(model.getNodes().size() == 0) {
			_ex.unprocess(model);
			//no hierarchy given - fallback to standard sugiyama layout
			super.layoutModel(model, 0, 0, 0);
		}else {
			ArrayList<NodeWrapper> _layoutedNodes = new ArrayList<NodeWrapper>();
			ArrayList<NodeWrapper> _unLayoutedNodes = new ArrayList<NodeWrapper>();
			for(NodeInterface n:_ex.getHierarchyNodes()) {
				NodeWrapper _n = NodeWrapper.getNodeWrapper(n, model);
				_layoutedNodes.add(_n);
			}
			//sugiyama layout of hierarchy
			f_sorter = topologicalSorting(model);
			f_HierarchyLayers = buildLayers(model, f_sorter);
			f_HierarchyLayers.markAllAsHierarchy();
			//reading nodes
			_ex.unprocess(model);
			//removing lonely nodes
			LonelyNodesRemover _lnr = new LonelyNodesRemover();
			_lnr.process(model);
			//updating structure
			for(NodeInterface n:model.getNodes()) {
				NodeWrapper nw = NodeWrapper.getNodeWrapper(n, model);
				nw.buildLinks(); //rebuilding successors and predecessors
				if(!_layoutedNodes.contains(nw)) {
					_unLayoutedNodes.add(nw);
				}
			}
			//incrementally adding nodes not layouted yet
			incrementallyaddnodes(_layoutedNodes,model,_unLayoutedNodes);
			checkNodeSpanning();
			//writing back coords
			optimizePositioning(model, f_sorter, f_HierarchyLayers,0,0);
			//routeHierarchyEdges(f_sorter,new ArrayList<NodeInterface>(_ex.getHierarchyNodes()), model,true);
			_lnr.unprocess(model);
		}
		
	}

	

	/**
	 * 
	 */
	private void checkNodeSpanning() {
		for(int layer=0;layer<f_HierarchyLayers.getNumberOfLayers();layer++) {
			ArrayList<NodeWrapper> _layer = f_HierarchyLayers.getLayer(layer);
			//checking this layer
			HashSet<NodeWrapper> _candidates = new HashSet<NodeWrapper>();
			for(NodeWrapper w:_layer) {
				if(!w.isHierarchyNode()) {
					checkForHierarchyNodeSpanning(_layer, _candidates, w,w.getSuccessors());
					checkForHierarchyNodeSpanning(_layer, _candidates, w,w.getPredecessors());
				}
			}
			boolean wasadded = false;
			if(_candidates.size() > 0) {
				f_HierarchyLayers.addSubLayer(layer);
				wasadded = true;
				ArrayList<NodeWrapper> _newLayer = f_HierarchyLayers.getLayer(layer+1);
				addToNewLayer(layer, _layer, _candidates, _newLayer);
			}
			_candidates.clear();
			for(NodeWrapper w:_layer) {
				if(!w.isHierarchyNode()) {
					checkForNodeSpanning(_layer, _candidates, w,w.getSuccessors());
					checkForNodeSpanning(_layer, _candidates, w,w.getPredecessors());
				}
			}
			if(_candidates.size() > 0) {
				if(!wasadded)
					f_HierarchyLayers.addSubLayer(layer);
				ArrayList<NodeWrapper> _newLayer = f_HierarchyLayers.getLayer(layer+1);
				addToNewLayer(layer, _layer, _candidates, _newLayer);
			}
		}
	}

	private void addToNewLayer(int layer, ArrayList<NodeWrapper> _layer,
			HashSet<NodeWrapper> _candidates, ArrayList<NodeWrapper> _newLayer) {
		for(NodeWrapper c:_candidates) {
			int idx = _layer.indexOf(c);
			while(_newLayer.size()<=idx) {
				_newLayer.add(new NodeWrapper(layer+1));//dummy Node
			}
			_newLayer.set(idx,c);
			_layer.set(idx, new NodeWrapper(layer));
			c.setMinLayer(layer+1);
		}
	}

	private void checkForHierarchyNodeSpanning(ArrayList<NodeWrapper> _layer,HashSet<NodeWrapper> _candidates, NodeWrapper w,List<NodeWrapper> checkThis) {
		for(NodeWrapper suc:checkThis) {
			if(suc.getLayer() == w.getLayer()) {
				//same layer!
				int _idx1 = _layer.indexOf(w);
				int _idx2 = _layer.indexOf(suc);
				if(Math.abs(_idx1-_idx2)>1) {
					for(int i=1+Math.min(_idx1, _idx2);i<Math.max(_idx1, _idx2);i++) {
						if(_layer.get(i).isHierarchyNode()) {
							//spans a hierarchy node!!!
							_candidates.add(w);
						}
					}
				}
			}
			
		}
	}
	
	private void checkForNodeSpanning(ArrayList<NodeWrapper> _layer,HashSet<NodeWrapper> _candidates, NodeWrapper w,List<NodeWrapper> checkThis) {
		for(NodeWrapper suc:checkThis) {
			
				if(suc.getLayer() == w.getLayer()) {
					//same layer!
					int _idx1 = _layer.indexOf(w);
					int _idx2 = _layer.indexOf(suc);
					if(Math.abs(_idx1-_idx2)>1) {
						for(int i=1+Math.min(_idx1, _idx2);i<Math.max(_idx1, _idx2);i++) {
							if(!_layer.get(i).isDummyNode()) {
								//spans another node!!!
								_candidates.add(w);
							}
						}
					}
				}
			
		}
	}

	/**
	 * @param nodes
	 * @param model
	 * @param unLayoutedNodes 
	 */
	private void incrementallyaddnodes(ArrayList<NodeWrapper> nodes,AbstractModelAdapter model, ArrayList<NodeWrapper> unLayoutedNodes) {
		if(unLayoutedNodes.size()== 0) {
			return;
		}
		HashMap<NodeWrapper,HashSet<NodeWrapper>> _LayoutedLists = new HashMap<NodeWrapper, HashSet<NodeWrapper>>();
		//determining the connections between the layouted and unlayouted graph
		HashMap<NodeWrapper,HashSet<NodeWrapper>> _unlayoutedConnectionLists = new HashMap<NodeWrapper, HashSet<NodeWrapper>>();
		boolean foundConnections = false;
		for(NodeWrapper w:unLayoutedNodes) {
			HashSet<NodeWrapper> _connected = new HashSet<NodeWrapper>();
			for(NodeWrapper wrap:w.getPredecessors()) {
				if(nodes.contains(wrap)) {
					_connected.add(wrap);
				}
			}
			for(NodeWrapper wrap:w.getSuccessors()) {
				if(nodes.contains(wrap)) {
					_connected.add(wrap);
				}
			}
			_unlayoutedConnectionLists.put(w, _connected);
			//directly handling nodes which only have a single connection to the layouted tree
			if(_connected.size() == 1) {
				put(_LayoutedLists,w,_connected.iterator().next());
			}
			
			if(_connected.size() > 0) {
				foundConnections = true;
			}
		}
		if(!foundConnections) {
			//finished layouting the main tree, the rest 
			return;
		}
		//assigning nodes
		assignNodesWithMoreThanOneConnection(_LayoutedLists,_unlayoutedConnectionLists);
		//all nodes assigned, add new nodes to layer structure
		addNodesToLayer(_LayoutedLists);
		//updating lists
		for(NodeWrapper n:_unlayoutedConnectionLists.keySet()) {
			if(_unlayoutedConnectionLists.get(n).size() > 0) {
				//is connected, was handeed
				nodes.add(n);
				unLayoutedNodes.remove(n);
			}
		}
		
		incrementallyaddnodes(nodes, model, unLayoutedNodes);
		
	}

	/**
	 * @param layoutedLists
	 * @param connectionLists
	 */
	private void addNodesToLayer(HashMap<NodeWrapper, HashSet<NodeWrapper>> layoutedLists) {
		for(NodeWrapper current:layoutedLists.keySet()) {
			HashSet<NodeWrapper> _assignedNodes = layoutedLists.get(current);
			int _layer = current.getLayer();
			int _idx = f_HierarchyLayers.getLayer(_layer).indexOf(current);
			assert _idx >= 0;
			if(_assignedNodes.size() > 2) {
				//get the layer the nodes will be added to
				ArrayList<NodeWrapper> _layerToInsert;
				if(!f_HierarchyLayers.isSubLayer(_layer+1)) {
					f_HierarchyLayers.addSubLayer(_layer);
				}
				_layerToInsert = f_HierarchyLayers.getLayer(_layer+1);
				//fill the layer if necessary
				int _addIndex = _idx;
				while(_layerToInsert.size()<_addIndex) {
					_layerToInsert.add(new NodeWrapper(_layer+1));
				}
				for(int i=_addIndex;i<_layerToInsert.size();i++) {
					_addIndex=i;
					if(_layerToInsert.get(i).isDummyNode()) {
						//all clear
						break;
					}
				}
				
				for(NodeWrapper n :_assignedNodes) {
					_layerToInsert.add(_addIndex,n);
					n.setMinLayer(_layer+1); //setting layer for newly added node
					_addIndex++;
				}
			}else {
				//adding to the right and possibly left
				Iterator<NodeWrapper> _itr = _assignedNodes.iterator();
				NodeWrapper node = _itr.next();
				f_HierarchyLayers.getLayer(_layer).add(_idx+1, node);
				node.setMinLayer(_layer);
				if(_itr.hasNext()) {
					node = _itr.next();
					f_HierarchyLayers.getLayer(_layer).add(_idx, node);
					node.setMinLayer(_layer);
				}
			}
		}
	}

	private void assignNodesWithMoreThanOneConnection(
			HashMap<NodeWrapper, HashSet<NodeWrapper>> _LayoutedLists,
			HashMap<NodeWrapper, HashSet<NodeWrapper>> _unlayoutedConnectionLists) {
		for(NodeWrapper w:_unlayoutedConnectionLists.keySet()) {
			HashSet<NodeWrapper> _set = _unlayoutedConnectionLists.get(w);
			if(_set.size() > 1) {
				HashSet<NodeWrapper> _addTo = null;
				for(NodeWrapper lNode : _set) {
					HashSet<NodeWrapper> _assigned = _LayoutedLists.get(lNode);
					if(_addTo == null) {
						_addTo = _assigned;
						if(_assigned == null) {
							_assigned = new HashSet<NodeWrapper>();
							_addTo = _assigned;
							_LayoutedLists.put(lNode, _addTo);
							break;
						}
					}else {
						if(_assigned == null) {
							_assigned = new HashSet<NodeWrapper>();
							_addTo = _assigned;
							_LayoutedLists.put(lNode, _addTo);
							break;
						}else if(_addTo.size()>_assigned.size()) {
							_addTo = _assigned;
						}
					}
				}
				_addTo.add(w);
			}
		}
	}

	/**
	 * @param layoutedLists
	 * @param w
	 * @param next
	 */
	private void put(HashMap<NodeWrapper, HashSet<NodeWrapper>> layoutedLists,NodeWrapper unlayoutedNode, NodeWrapper layoutedNode){
		if(layoutedLists.containsKey(layoutedNode)) {
			layoutedLists.get(layoutedNode).add(unlayoutedNode);
		}else {
			HashSet<NodeWrapper> _set = new HashSet<NodeWrapper>();
			_set.add(unlayoutedNode);
			layoutedLists.put(layoutedNode, _set);
		}
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
		//not needed
	}

}
