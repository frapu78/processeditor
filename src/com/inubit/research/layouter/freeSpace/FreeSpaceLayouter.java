/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.freeSpace;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.FlowObjectWrapper;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFModelInterface;
import com.inubit.research.layouter.interfaces.TWFNodeInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;
import com.inubit.research.layouter.preprocessor.LayoutPreprocessor;

/**
 * @author ff
 *
 */
public class FreeSpaceLayouter extends ProcessLayouter{

	/**
	 * 
	 */
	public static final int PADDING_RIGHT_AFTER_EXTENSION = 10;
	private AbstractModelAdapter f_model;
	private NodeInterface f_SelNode;
	private int f_spaceToFree = 100; //pixels
	private HashMap<NodeInterface,Point> f_orgPos = new HashMap<NodeInterface,Point>();
	
	private ArrayList<FlowObjectWrapper> f_toProcess = new ArrayList<FlowObjectWrapper>();
	private ArrayList<FlowObjectWrapper> f_processed = new ArrayList<FlowObjectWrapper>();
	
	@Override
	public String getDisplayName() {
		return "Free Space";
	}

	@Override
	public void layoutModel(AbstractModelAdapter model, int xstart, int ystart, int direction) throws Exception {
		LayoutPreprocessor.process(model);
		f_orgPos.clear();
		f_toProcess.clear();
		f_processed.clear();
		if(f_SelNode != null) {
			f_model = model;
			//cleaning all lists
			FlowObjectWrapper.clear();
			f_toProcess.clear();
			
			FlowObjectWrapper _fow = FlowObjectWrapper.getFlowObjectWrapper(f_SelNode, model);
			f_processed.add(_fow); //so outgoing edges are considered
			addAll(_fow.getSuccessors(),_fow.getSuccessorEdges());
			//moving all successor nodes
			moveSuccessorNodes();
			
			//expanding pools, so conflicts are treated correctly
			if(model instanceof BPMNModelInterface || model instanceof TWFModelInterface) {
				expandPools();
			}	
			//moving nodes that are in a graphically conflict (intersecting)
			moveGraphicallyConfictedNodes();
			//after conflicts were treated it might be necessary to expand again
			if(model instanceof BPMNModelInterface || model instanceof TWFModelInterface) {
				expandPools();
			}			
		}
		LayoutPreprocessor.unprocess(model);		
	}
	
	/**
	 * 
	 */
	private void moveGraphicallyConfictedNodes() {
		for(NodeInterface n:f_model.getNodes()) {
			if(n.equals(f_SelNode)) {
				continue;
			}	
			if(n instanceof TWFNodeInterface) {
				TWFNodeInterface _twf = (TWFNodeInterface) n;
				if(_twf.isToolDocker() || _twf.isToolErrorConnector()) {
					//exclude those nodes
					continue;
				}
			}
			for(int i=0;i<f_processed.size();i++) {
				FlowObjectWrapper n2 = f_processed.get(i);
				if(contains(n, n2.getWrappedObject()) || contains(n2.getWrappedObject(),n)) {
					continue;
				}
				if(!n2.getWrappedObject().equals(f_SelNode) && !n.equals(n2.getWrappedObject())) {
					Rectangle _r1 = getBoundingBox(n,10);
					Rectangle _r2 = getBoundingBox(n2.getWrappedObject(),0);
					if(_r1.intersects(_r2)) {
						if(getPos(n).x > getPos(n2.getWrappedObject()).x) {
							moveNode(n, Math.max(_r1.intersection(_r2).width,_r2.intersection(_r1).width));
							//adding in front to avoid self check in the end
							f_processed.add(0,FlowObjectWrapper.getFlowObjectWrapper(n,f_model));
							//checking all nodes checked prior to this again, due to new circumstances
							moveGraphicallyConfictedNodes();
							break;
						}//else {
							moveNode(n2.getWrappedObject(), Math.max(_r1.intersection(_r2).width,_r2.intersection(_r1).width));
							//adding in front to avoid self check in the end
							f_processed.add(0,n2);
							moveGraphicallyConfictedNodes();
							break;
						//}
					}
				}
			}
			
		}
	}

	private boolean contains(NodeInterface n,NodeInterface n2) {
		if(n instanceof BPMNNodeInterface) {
			BPMNNodeInterface _bpmn = (BPMNNodeInterface) n;
			if(_bpmn.isPool() || _bpmn.isSubProcess() || _bpmn.isLane()) {
				//check containment
				return _bpmn.getContainedNodes().contains(n2);
			}
		}
		return false;
	}

	/**
	 * @param wrappedObject
	 * @return
	 */
	private Point getPos(NodeInterface node) {
		if(f_orgPos.containsKey(node)) {
			return f_orgPos.get(node);
		}
		return node.getPos();
	}

	/**
	 * @param n
	 * @return
	 */
	private Rectangle getBoundingBox(NodeInterface n,int extension) {
		
		Point p = n.getPos();
		Dimension d = n.getSize();
		d.height += extension*2;
		d.width += extension*2;
		p.x -= d.width/2;
		p.y -= d.height/2;
		return new Rectangle(p,d);
	}

	/**
	 * 
	 */
	private void expandPools() {
		for(NodeInterface n:f_model.getNodes()) {
			BPMNNodeInterface _n = (BPMNNodeInterface) n;
			if(_n.isLane() || _n.isPool() || _n.isSubProcess()) {
				List<NodeInterface> _containedNodes = _n.getContainedNodes();
				if(containsAny(_containedNodes,f_processed)) {
					//possible candidate
					int maxX = getMaxX(_containedNodes)+PADDING_RIGHT_AFTER_EXTENSION;
					int poolX = (_n.getPos().x +_n.getSize().width/2);
					if(maxX > poolX) {
						expandPoolTo(_n,maxX);						
					}
				}
			}
		}
	}

	/**
	 * @param pool 
	 * @param maxX
	 */
	private void expandPoolTo(BPMNNodeInterface pool, int maxX) {
		if(pool.isLane()) {
			pool = getContainingPool(pool);
		}
		Point _p = pool.getPos();
		Dimension _d = pool.getSize();
		int _diff = (maxX- (_p.x + _d.width/2));
		if(_diff > 0) {
			_p.x += _diff/2;
			_d.width += _diff;
			pool.setSize(_d.width, _d.height);
			f_orgPos.put(pool,pool.getPos());
			pool.setPos(_p.x, _p.y);
		}
	}

	/**
	 * @param node
	 * @return
	 */
	private BPMNNodeInterface getContainingPool(BPMNNodeInterface node) {
		for(NodeInterface n:f_model.getNodes()) {
			BPMNNodeInterface _n = (BPMNNodeInterface) n;
			if(_n.isLane() && _n.getContainedNodes().contains(node)) {
				return getContainingPool(_n);
			}else if(_n.isPool() && _n.getContainedNodes().contains(node)) {
				return _n;
			}
		}
		return null;
	}

	/**
	 * @param nodes
	 * @return
	 */
	private int getMaxX(List<NodeInterface> nodes) {
		int result = 0;
		for(NodeInterface n:nodes) {
			int xRight = n.getPos().x + n.getSize().width/2;
			if(xRight > result) {
				result = xRight;
			}
		}
		return result;
	}

	/**
	 * @param containedNodes
	 * @param f_processed2
	 * @return
	 */
	private boolean containsAny(List<NodeInterface> containedNodes,	ArrayList<FlowObjectWrapper> processedNodes) {
		for(FlowObjectWrapper f:processedNodes) {
			if(containedNodes.contains(f.getWrappedObject())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	private void moveSuccessorNodes() {
		while(f_toProcess.size() > 0) {
			FlowObjectWrapper fow = f_toProcess.get(0);
			f_toProcess.remove(0);
			
			//special handling for negative shifts
			if(!(f_spaceToFree < 0 && fow.getPredecessorEdges().size() > 1 )) {
				NodeInterface _node = fow.getWrappedObject();
				moveNode(_node,f_spaceToFree);
				if(_node instanceof TWFNodeInterface) {
					for(FlowObjectWrapper succ:fow.getSuccessors()) {
						if(getPos(succ.getWrappedObject()).x > getPos(_node).x) {
							add(succ);
						}
					}					
				}else {
					addAll(fow.getSuccessors(),fow.getSuccessorEdges());
				}
				if(_node instanceof BPMNNodeInterface) {
					BPMNNodeInterface _bnode = (BPMNNodeInterface) _node;
					if(_bnode.isSubProcess()) {
						List<NodeInterface> _cnodes = _bnode.getContainedNodes();
						for(NodeInterface n:_cnodes) {
							add(FlowObjectWrapper.getFlowObjectWrapper(n, f_model));
						}
					}
				}
			}
			f_processed.add(fow);
		}
		//allNodes moved - now also move the routing points
		for(EdgeInterface e:f_model.getEdges()) {
			if(f_processed.contains(FlowObjectWrapper.getFlowObjectWrapper((NodeInterface)e.getSource(),f_model))) {
				//found a candidate
				if(!(e instanceof DummyEdge)) {
					List<Point> _pts = e.getRoutingPoints();
					_pts.remove(0);
					_pts.remove(_pts.size()-1);
					for(Point p:_pts) {
						if(!f_processed.contains(FlowObjectWrapper.getFlowObjectWrapper((NodeInterface)e.getTarget(),f_model))) {
							//only moved nodes which are right of the node
							if(getPos((NodeInterface)e.getSource()).x < p.x) {
								p.x += f_spaceToFree;
							}
						}else {
							if(e.getSource().equals(f_SelNode)) {
								//defines a range (width of the node + height of the node) in the diagram
								//where points are not moved, as selNode was not moved as well!!!
								if(!((p.x > (f_SelNode.getPos().x-f_SelNode.getSize().width/2))&&
									(p.x < (f_SelNode.getPos().x+f_SelNode.getSize().width/2)) &&
									(Math.abs(p.y-f_SelNode.getPos().y) < f_SelNode.getSize().height*1.5))) {
									p.x += f_spaceToFree;
								}
							}else {
								p.x += f_spaceToFree;
							}
						}
					}
					e.setRoutingPoints(_pts);
				}
			}
			
		}
	}

	/**
	 * @param successors
	 * @param edges 
	 */
	private void addAll(ArrayList<FlowObjectWrapper> successors, ArrayList<EdgeInterface> edges) {
		for(int i=0;i<edges.size();i++) {
			if(edges.get(i) instanceof BPMNEdgeInterface) {
				if(!((BPMNEdgeInterface)edges.get(i)).isMessageFlow()) {
					add(successors.get(i));
				}
			}else {
				add(successors.get(i));
			}
		}
	}

	/**
	 * @param process
	 * @param flowObjectWrapper
	 */
	private void add(FlowObjectWrapper flowObjectWrapper) {
		//avoid double processing!
		if(!f_toProcess.contains(flowObjectWrapper) && !f_processed.contains(flowObjectWrapper)) {
			f_toProcess.add(flowObjectWrapper);
		}
	}

	/**
	 * @param _node
	 * @param toFree
	 */
	private void moveNode(NodeInterface node, int toFree) {
		Point _p = node.getPos();
		f_orgPos.put(node,new Point(_p));
		_p.x += toFree;
		node.setPos(_p.x, _p.y);
	}

	/**
	 * return a set containing all nodes which were
	 * moved or resized (in case of pools) during the layout process
	 * @return
	 */
	public Set<NodeInterface> getChangedNodes(){
		return f_orgPos.keySet();
	}
	
	/**
	 * returns a map which contains the position the changed nodes
	 * had before the layouting process
	 * @return
	 */
	public HashMap<NodeInterface, Point> getOriginalPositions(){
		return f_orgPos;
	}
	
	public void setSpaceToFree(int space) {
		f_spaceToFree = space;
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
		f_SelNode = selectedNode;
	}

}
