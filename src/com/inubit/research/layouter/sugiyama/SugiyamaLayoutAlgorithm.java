/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.sugiyama;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.SubModel;
import com.inubit.research.layouter.gridLayouter.XPositionComparator;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.preprocessor.LayoutPreprocessor;

/**
 * @author ff
 *
 */
public class SugiyamaLayoutAlgorithm extends ProcessLayouter {

	private boolean f_topToBottom;
	private LayerStructure f_layers;
	private TopologicalSorter f_sorter;
	// needed to handle edges which have an equal source and target node
	private List<EdgeInterface> f_unprocessedEdges = null;

	private int f_distanceX;
	private int f_distanceY;
	private boolean f_center;
	private boolean f_shortenEdges;
	private boolean f_scatterEdges;
	
	private final boolean f_debug = false;
	private boolean f_manual = false;
	
	private HashSet<NodeInterface> f_processedMap = null;
	private Properties f_props;
	
	/**
	 * Constructor for external calls.
	 * Does not require Properties class as it sets all parameters.
	 */
	public SugiyamaLayoutAlgorithm(boolean topToBttom, int distanceX,int distanceY,boolean centerNodes,boolean shortenEdges, boolean scatterEdges) {
		f_topToBottom = topToBttom;
		f_manual  = true;
		f_distanceX = distanceX;
		f_distanceY = distanceY;
		f_center = centerNodes;
		f_shortenEdges = shortenEdges;
		f_scatterEdges = scatterEdges;
	}   

	/**
	 * standard Inubit Workbench constructor
	 * reads all settings from the Properties 
	 */
	public SugiyamaLayoutAlgorithm(boolean topToBottom,Properties properties) {
		f_topToBottom = topToBottom;
		f_props = properties;
	}
	
	/**
	 * standard Inubit Workbench constructor
	 * reads all settings from the Properties 
	 */
	public SugiyamaLayoutAlgorithm(Properties properties) {
		this(true,properties);
	}
	
	@Override
	public String getDisplayName() {
		return "Sugiyama Hierarchical Layouter" + (f_topToBottom ? "(Top-to-Bottom)":"(Left-to-Right)");
	}

	/**
	 * start and direction are not needed
	 */
	@Override
	public void layoutModel(AbstractModelAdapter model, int xstart, int ystart,int direction) throws Exception {
		clearTemporaryData(model);
		LayoutPreprocessor.process(model);
		if(f_debug)
			System.out.println("Model Nodes: "+model.getNodes().size());
		int _offx = 0;
		for(NodeInterface n:model.getNodes()) {
			if(!f_processedMap.contains(n)) {
				long _start = System.currentTimeMillis();
				SubModel _graph = new SubModel(model); 
				addRecursively(_graph,n,model);
				if(f_debug) {
					System.out.println("Time to build subgraph: "+(System.currentTimeMillis()-_start));
					System.out.println("Found Subgraph. Nodes: "+_graph.getNodes().size());
				}
				applyMainAlgorithm(_graph,_offx,0);
				//clear edge cache again
				f_unprocessedEdges = null;
				_offx += (f_layers.getWidth()-_offx);
			}
		}
		LayoutPreprocessor.unprocess(model);
	}

	/**
	 * should be executed before running the algorithm
	 * @param model
	 */
	protected void clearTemporaryData(AbstractModelAdapter model) {
		if(!f_manual) {
			//the layouter was created using a "manual"-constructor
			f_distanceX = LayoutHelper.toInt(f_props.getProperty(LayoutHelper.CONF_X_DISTANCE_SUGI,"30"),30);
			f_distanceY = LayoutHelper.toInt(f_props.getProperty(LayoutHelper.CONF_Y_DISTANCE_SUGI,"35"),35);
			f_center = "1".equals(f_props.getProperty(LayoutHelper.CONF_CENTER_NODES));
			f_shortenEdges = "1".equals(f_props.getProperty(LayoutHelper.CONF_SHORTEN_EDGES));
			f_scatterEdges = "1".equals(f_props.getProperty(LayoutHelper.CONF_SCATTER_EDGES));
		}else {
			f_props = new Properties();
		}		
		f_processedMap = new HashSet<NodeInterface>(model.getNodes().size());
		f_unprocessedEdges = null;
		NodeWrapper.clear();
	}

	/**
	 * @param _graph
	 * @param _w
	 */
	private void addRecursively(SubModel _graph, NodeInterface n, AbstractModelAdapter model) {
		_graph.addNode(n);
		f_processedMap.add(n);
		
		boolean _added = true;
		while(_added == true) {
			_added = false;
			List<NodeInterface> _nodes = _graph.getNodes();
			for(EdgeInterface e: model.getEdges()) {
				if((e.getSource() != null) && (e.getTarget() != null) 
						&& (_nodes.contains(e.getSource()) || _nodes.contains(e.getTarget()))) {
					//okay belongs to this graph
					if(!_nodes.contains(e.getSource())) {						
						f_processedMap.add((NodeInterface) e.getSource());
						_graph.addNode((NodeInterface) e.getSource());
						_added = true;
					}else if(!_nodes.contains(e.getTarget())) {
						f_processedMap.add((NodeInterface)e.getTarget());
						_graph.addNode((NodeInterface)e.getTarget());
						_added = true;
					}
					if(!_graph.getEdges().contains(e)) {
						_graph.addEdge(e);
						_added = true;	
					}
				}
			}
		}
	}

	protected void applyMainAlgorithm(AbstractModelAdapter model,int xOffSet,int yOffset) {
		f_sorter = topologicalSorting(model);
		f_layers = buildLayers(model, f_sorter);
		//shortening edges
		if(getShortenEdges())
			f_layers.shortenEdges();
		//crossing reduction
		f_layers.reduceCrossings(this);
		optimizePositioning(model, f_sorter, f_layers,xOffSet,yOffset);		
	} 
	
	protected LayerStructure buildLayers(AbstractModelAdapter model,TopologicalSorter _sorter) {
		int _maxLayer = 0; 
		//saving the maximum layer (number of different layers)
		for(NodeInterface node :_sorter.getSortedNodes() ) {
			NodeWrapper _node = NodeWrapper.getNodeWrapper(node, model);
			assignLayer(_node);
			if(_maxLayer < _node.getLayer()) {
				_maxLayer = _node.getLayer();
			}
		}
		//inserting dummy nodes for long spanning edges
		for(NodeInterface node :_sorter.getSortedNodes() ) {
			NodeWrapper _node = NodeWrapper.getNodeWrapper(node, model);
			processLongEdge(_node);
		}
		//positioning in layer
		LayerStructure _layers = new LayerStructure(_maxLayer+1);
		for(NodeInterface node :_sorter.getSortedNodes() ) {
			NodeWrapper _node = NodeWrapper.getNodeWrapper(node, model);
			_layers.addAll(_node);
		}		
		return _layers;
	}
	
	protected TopologicalSorter topologicalSorting(AbstractModelAdapter model) {
		TopologicalSorter _sorter = new TopologicalSorter(model);
		return _sorter;
	}

	protected void optimizePositioning(AbstractModelAdapter model,
			TopologicalSorter _sorter, LayerStructure _layers,int xOffset,int yOffset) {
		
		_layers.position();
		_layers.calculatePosition(f_distanceX,f_distanceY,f_topToBottom,xOffset,yOffset,f_center);
		
		/*
		 * remove all routing points,  as they will be set by writeCoords
		 */
		for(EdgeInterface _edge:model.getEdges()) {
			_edge.clearRoutingPoints();
		}
		
		for(NodeInterface node :_sorter.getSortedNodes() ) {
			if(model.getNodes().contains(node)) {//if not, this node belongs to another subgraph and will be processed later
				NodeWrapper _node = NodeWrapper.getNodeWrapper(node, model);
				writeCoords(_node,model);
			}
		}
		//reinverting inverted edges !
		_sorter.restoreEdges();		
	}

	/**
	 * @param _start
	 */
	private void processLongEdge(NodeWrapper node) {

		if(!node.isLongEdgeProcessed()) {
			node.setLongEdgeProcessed(true);
			int _maxLayer = node.getLayer()+1;
			//going though backwards so removing nodes does not influence the loop 
			for(int i=node.getSuccessors().size()-1;i>=0;i--) {
				NodeWrapper w = node.getSuccessors().get(i);
				if(w.getLayer() > _maxLayer) {
					//dummy node needed
					NodeWrapper _dn = new NodeWrapper(_maxLayer,node,w);
					node.getSuccessors().remove(i);
					node.getSuccessors().add(_dn);
					w.getPredecessors().remove(node);
					w.getPredecessors().add(_dn);
					processLongEdge(_dn);
				}
				else {
					processLongEdge(w);
				}
			}
		}
	}

	/**
	 * @param _node
	 */
	private void assignLayer(NodeWrapper _node) {
		int _newLayer = _node.getLayer()+1;
		for(NodeWrapper _w :_node.getSuccessors()) {
			_w.setMinLayer(_newLayer);
		}
	}
	
	/**
	 * writes the coords which were determined by the algorithm back to the nodes
	 * and routes all edges
	 * @param node
	 * @param model
	 */
	public void writeCoords(NodeWrapper node, AbstractModelAdapter model) {
		if(!node.isDummyNode()) {
			if(!node.isCoordsWrittenBack()) {
				node.setCoordsWrittenBack(true);
				NodeInterface _orgStart = node.getNode();
				Point _pos = getCoordinates(node);
				_orgStart.setPos(_pos.x,_pos.y);
				List<NodeWrapper> _succs = node.getSuccessors();
				XPositionComparator _comp = new XPositionComparator();
				Collections.sort(_succs, _comp);
				for(int sIndex = 0;sIndex<_succs.size();sIndex++) {
					NodeWrapper s = _succs.get(sIndex);
					ArrayList<NodeWrapper> _traversed = new ArrayList<NodeWrapper>();
					NodeWrapper _end = getEndNode(s,_traversed);					
					//dummy Edges in between, routing edge along them....
					NodeInterface _orgEnd = _end.getNode();
					
					EdgeInterface _edge = findEdge(_orgStart,_orgEnd,model);
					boolean _inversed = false;
					if(_edge == null) {
						//edge was inversed
						_edge = findEdge(_orgEnd,_orgStart,model);
						_inversed = true;
						
					}
					//the edge was found. now add routing points (2 for each dummy node)
					List<Point> _rps = _edge.getRoutingPoints();
					if(_rps.size()>0) {
						_rps.remove(0);
						_rps.remove(_rps.size()-1);
					}
					int start = _inversed ? _traversed.size()-1: 0;
					int end =_inversed ? -1 : _traversed.size();
					if(_traversed.size() > 0) {						
						int increment = _inversed ? -1 : 1;
						if(_edge == null) {
							System.err.print("Algorithm did not find an edge! Which should be present!");
							return;
						}						
						//handling all dummy nodes in between
						for(int i=start;i!=end;i=i+increment) {
							NodeWrapper _dummyNode = _traversed.get(i);
							Point _p1 = new Point(getCoordinates(_dummyNode));
							if(f_topToBottom)
								_p1.y -= increment * ((_dummyNode.getCellHeight())/2 + f_distanceY/2);
							else
								_p1.x -= increment *  ((_dummyNode.getCellHeight())/2 + f_distanceX/2);
							Point _p2 = new Point(getCoordinates(_dummyNode));
							if(f_topToBottom)
								_p2.y += increment * ((_dummyNode.getCellHeight())/2 + f_distanceY/2);
							else
								_p2.x += increment * ((_dummyNode.getCellHeight())/2 + f_distanceX/2);							
							if(_edge != null){					
								_rps.add(_p1);
								_rps.add(_p2);	
							}							
						}
					}						
					//taking care of the start and end of the edge and its connection to the node
					//start
					Point p = ((NodeInterface) _edge.getSource()).getPos();
					int _dir = ((NodeInterface) _edge.getSource()).getPos().y < ((NodeInterface)_edge.getTarget()).getPos().y ? 1 : -1;
					
					double startIndex = sIndex - ((_succs.size()-1)/2.0);					
					int _yOffset = (int) ((1-((Math.abs(startIndex)/(_succs.size()+0.001/2)))) * (f_distanceY*0.8)/2.0);
					int _addToY = ((node.getCellHeight())/2 + (f_scatterEdges ? _yOffset : f_distanceY/2));
					if(f_topToBottom) {
						//go to the bottom (or top)
						p.y += _dir * _addToY;
						int width = model.getEdgeLayoutSize(node.getNode()).width;
						if(f_scatterEdges)
							p.x +=  startIndex*(width/_succs.size());
					}else {
						_dir = ((NodeInterface) _edge.getSource()).getPos().x < ((NodeInterface)_edge.getTarget()).getPos().x ? 1 : -1;	
						p.x += _dir * _addToY;
						int height = model.getEdgeLayoutSize(node.getNode()).height;
						if(f_scatterEdges)
							p.y += startIndex*(height/_succs.size());
					}		
					
					_rps.add(0, p);					
					
					//end point determination				
					List<NodeWrapper> _preds = _end.getPredecessors();
					Collections.sort(_preds, _comp);
					double endIndex;
					if(_traversed.size() > 0)
						endIndex = _preds.indexOf(_inversed ? _traversed.get(0) :_traversed.get(end-1)) - ((_preds.size()-1)/2.0);
					else
						endIndex = _preds.lastIndexOf(node) - ((_preds.size()-1)/2.0);	
					p = new Point(((NodeInterface)_edge.getTarget()).getPos());
					_dir = -_dir;
					
					_yOffset = (int) ((1-((Math.abs(endIndex)/(_preds.size()+0.001/2)))) * (f_distanceY*0.8)/2.0);
					_addToY = _dir*((_end.getCellHeight())/2 + (f_scatterEdges ? _yOffset : f_distanceY/2));
					if(f_topToBottom) {
						//go to the bottom (or top)
						p.y += _addToY;
						int width = model.getEdgeLayoutSize(_end.getNode()).width;
						if(f_scatterEdges) {				
							p.x +=  endIndex*(width/_preds.size());						
						}
					}else {
						p.x += _addToY;
						int height = model.getEdgeLayoutSize(_end.getNode()).height;
						if(f_scatterEdges)
							p.y += endIndex*(height/_preds.size());
					}					
					_rps.add(_rps.size(), p); // adding it in the end
					
					//setting point following the start point
					Point _next = new Point(_rps.get(1));
					if(f_topToBottom) {
						_next.y = _rps.get(0).y;
					}else {
						_next.x = _rps.get(0).x;
					}
					_rps.add(1,_next);
										
					Point _prev = new Point(_rps.get(_rps.size()-2));
					if(f_topToBottom) {
						_prev.y = p.y;
					}else {
						_prev.x = p.x;
					}
					_rps.add(_rps.size()-1,_prev);	
					//point directly at the border
					
					Point _border = getBorderPoint(node, _rps.get(0));
					_rps.add(0,_border);
					
					_border = getBorderPoint(_end, p);	
					_rps.add(_rps.size(),_border);
					
					LayoutHelper.removeUnneccesaryPoints(_rps);
					if("0".equals(f_props.getProperty(LayoutHelper.CONF_SET_CONNECTION_POINTS, "1"))) {
						if(_rps.size() > 0) { // dummy edges
							_rps.remove(0);
							_rps.remove(_rps.size()-1);
						}
					}
					_edge.clearRoutingPoints();
					_edge.setRoutingPoints(_rps);														
				}
			}
		}
	}

	private Point getBorderPoint(NodeWrapper node, Point p) {
		Point _border = new Point(p);
		Point _pos = node.getNode().getPos();
		Dimension _size = node.getNode().getSize();
		if(f_topToBottom) {
			if(_pos.y < _border.y) {
				//below
				_border.y = _pos.y + _size.height/2;
			}else {
				//above
				_border.y = _pos.y - _size.height/2;
			}
		}else {
			if(_pos.x < _border.x) {
				//left of it
				_border.x = _pos.x + _size.width/2;
			}else {
				//right
				_border.x = _pos.x - _size.width/2;
			}
		}
		return _border;
	}

	
	private Point getCoordinates(NodeWrapper node) {
		return node.getPos();
	}

	
	
	/**
	 * @param _end
	 * @param end
	 * @param original 
	 * @return
	 */
	private EdgeInterface findEdge(NodeInterface start, NodeInterface end, AbstractModelAdapter model) {
		if(f_unprocessedEdges == null) {
			f_unprocessedEdges = new ArrayList<EdgeInterface>(model.getEdges());
			
		}
		for(EdgeInterface _e : f_unprocessedEdges) {
			if(_e.getSource().equals(start) && _e.getTarget().equals(end)) {
				//found the edge
				f_unprocessedEdges.remove(_e);
				return _e;
			}
		}
		return null;
	}

	/**
	 * returns the end of this path (a node which is not a dummy node) and stores
	 * all traversed dummy nodes in the list, so they can be used for edge routing
	 * @param nw
	 * @param _traversed - the nodes that have been traversed
	 * @return
	 */
	private NodeWrapper getEndNode(NodeWrapper nw, ArrayList<NodeWrapper> _traversed) {
		if(nw.isDummyNode()) {
			_traversed.add(nw);
			return (getEndNode(nw.getSuccessors().get(0), _traversed)); //dummy nodes only have one successor
		}
		return nw;
	}


	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
	}

	public void setShortenEdges(boolean f_shortenEdges) {
		this.f_shortenEdges = f_shortenEdges;
	}

	public boolean getShortenEdges() {
		return f_shortenEdges;
	}

	/**
	 * Tells you if the model was layouted top to bottom (true) or left to right (false)
	 * @return
	 */
	public boolean getTopToBottom() {
		return f_topToBottom;
	}

}
