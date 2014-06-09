/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFModelInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;

/**
 * determines routing points for the edges between the nodes.
 * also contains an A* implementation for the routing of message flows.
 * But due to the penalties and constraints the state space quite easily explodes and layouting 
 * of several message flows then easily takes several seconds. Thus it can be disables in the 
 * properties.
 * @author ff
 *
 */
public class GridLayouterEdgeRouter {
	
	private GridLayouter f_layouter;
	private AbstractModelAdapter f_model;

	/**
	 * needed for A* Message Flow Routing
	 */
	private static final int JUMPDISTANCE = 20; //pixels
	private static final int BOUNDINGBOX_EXTENSION = 14;
	private static final int BEND_PENALTY = 5;
	private static final int NODE_CROSSING_PENALTY = 100;
	private static final int POOL_CROSSING_PENALTY = 10;

	private HashSet<ANode> f_open = new HashSet<ANode>();
	private HashSet<ANode> f_closed = new HashSet<ANode>();
	private NodeInterface f_source;
	private NodeInterface f_target;
	private List<EdgeInterface> f_routedEdges = new ArrayList<EdgeInterface>();

	/**
	 * 
	 */
	public GridLayouterEdgeRouter(GridLayouter layouter,AbstractModelAdapter model) {
		f_layouter = layouter;
		f_model = model;
	}
	
	public void routeEdges() {
		Hypergrid _grids = f_layouter.getGrids();
		for(int g = 0; g < _grids.getNumOfGrids();g++) {
			Grid _grid = _grids.getGrid(g);
			for(int col=0;col<_grid.getColCount();col++) {
				for(int row=0;row<_grid.getRowCount();row++) {
					FlowObjectWrapper _obj = _grid.getObject(row, col);
					if(_obj != null) {
						if(_obj.getWrappedObject() instanceof BPMNNodeInterface) {
							if(isGateway(_obj) || isDataObject(_obj)) {
								route(_grid,row,col,_obj);
							}
						}
					}
				}
			}
		}		
	}

	public void routeMessageFlows() {
		//long start = System.currentTimeMillis();
		for(EdgeInterface e:f_model.getEdges()) {
			if(e instanceof BPMNEdgeInterface) {
				if(((BPMNEdgeInterface)e).isMessageFlow()){
					routeMessageFlow(e);
				}
			}
		}
		//System.out.println("MSF Routing: "+(System.currentTimeMillis()-start)+" ms");
	}
	
	/**
	 * @param obj
	 * @return
	 */
	private boolean isDataObject(FlowObjectWrapper obj) {
		return obj.isDataObject() && 
			(obj.getPredecessorEdges().size()==1);
	}

	/**
	 * @param e
	 */
	private void routeMessageFlow(EdgeInterface e) {
		f_open.clear();
		f_closed.clear();
		f_source = (NodeInterface) e.getSource();
		f_target = (NodeInterface)e.getTarget();
		Point _start = f_source.getPos();
		Point _end = f_target.getPos();
		ANode _node = new ANode(_start,_end,null);
		_node.setG(0);
		f_open.add(_node);
		Rectangle _source = getBounds((NodeInterface) e.getSource(),-20);
		Rectangle _target = getBounds((NodeInterface)e.getTarget(),-20);
		ANode _goal = routeMSF(_source,_target);
		_source = getBounds((NodeInterface) e.getSource(),BOUNDINGBOX_EXTENSION);
		_target = getBounds((NodeInterface)e.getTarget(),BOUNDINGBOX_EXTENSION);
		List<Point> _rps = new ArrayList<Point>();
		while(_goal != null) {
			if(!(_source.contains(_goal.getPoint()) || _target.contains(_goal.getPoint()))) {
				_rps.add(0,_goal.getPoint());
			}
			_goal = _goal.getCameFrom();
		}
		LayoutHelper.removeUnneccesaryPoints(_rps);
		e.clearRoutingPoints();
		e.setRoutingPoints(_rps);
	}
	/**
	 * @param _target 
	 * @param _source 
	 * 
	 */
	private ANode routeMSF(Rectangle _source, Rectangle _target) {
		int max = 0;
		ANode _a = null;
		//some edges are quite long and need lots of steps
		while(f_open.size() > 0 && max < 2000) {
			_a = getNodeWithLowestF();
			f_open.remove(_a);
			f_closed.add(_a);
			for(ANode neighbor:getNeighbors(_a)) {
				if(_target.contains(neighbor.getPoint())) {
					return neighbor;
				}
				if(f_closed.contains(neighbor)) {
					continue;
				}
				int _g = 0;
				if(!_source.contains(neighbor.getPoint())) {
				 _g = _a.getG() + JUMPDISTANCE + getPenalty(neighbor,f_model);
				}
				if(f_open.contains(neighbor)) {
					ANode _other = getClone(f_open,neighbor);
					if(_other.getG() > _g) {
						_other.setG(_g);
						_other.setCameFrom(_a);
					}
				}else {
					neighbor.setG(_g);
					f_open.add(neighbor);
				}				
			}
			max++;
		}
		return getNodeWithLowestH();
	}

	/**
	 * @return
	 */
	private ANode getNodeWithLowestH() {
		ANode _result = null;
		for(ANode a:f_open) {
			if(_result == null) {
				_result=a;
			}
			if(_result.getH() > a.getH()) {
				_result = a;
			}
		}
		return _result;
	}

	/**
	 * @param f_open2
	 * @param neighbor
	 * @return
	 */
	private ANode getClone(HashSet<ANode> set, ANode neighbor) {
		for(ANode n:set) {
			if(n.hashCode() == neighbor.hashCode()) {
				return n;
			}
		}
		return null;
	}

	/**
	 * @param point
	 * @param f_model2
	 * @return
	 */
	private int getPenalty(ANode node, AbstractModelAdapter model) {
		int _result = 0;
		for(NodeInterface n : model.getNodes()) {
			if((n.equals(f_source)) || (n.equals(f_target))) continue; //those are okay!
			Rectangle bounds = getBounds(n,BOUNDINGBOX_EXTENSION);
			if(bounds.contains(node.getPoint())) {
				BPMNNodeInterface _n = (BPMNNodeInterface) n;
				if(_n.isPool() || _n.isSubProcess()) {
					_result += POOL_CROSSING_PENALTY;
				}else
				if(_n.isLane()) {
					//nothing already handled by pool
				}else {
					_result += NODE_CROSSING_PENALTY; //we do not want to cross other nodes!
				}
			}
		}
		if(node.getCameFrom() != null && node.getCameFrom().getCameFrom() != null) {
			Point _p1 = node.getCameFrom().getPoint();
			Point _p2 = node.getCameFrom().getCameFrom().getPoint();
			if(!LayoutHelper.isStraight(node.getPoint(),_p1,_p2)) {
				_result += BEND_PENALTY;
			}
			
		}
		return _result;
	}
	
	/**
	 * @param n
	 * @return
	 */
	private Rectangle getBounds(NodeInterface n,int offset) {
		Point _p = n.getPos();
		Dimension _d = n.getSize();
		_p.translate(-_d.width/2-offset/2, -_d.height/2-offset/2);
		_d.width += offset;
		_d.height += offset;
		return new Rectangle(_p,_d);
	}

	/**
	 * @param _a
	 * @return
	 */
	private List<ANode> getNeighbors(ANode _a) {
		LinkedList<ANode> _result = new LinkedList<ANode>();
		
		Point p = new Point(_a.getPoint());
		p.translate(JUMPDISTANCE, 0);
		_result.add(new ANode(p,_a.getTarget(),_a));
		
		p = new Point(_a.getPoint());
		p.translate(-JUMPDISTANCE, 0);
		_result.add(new ANode(p,_a.getTarget(),_a));
		
		p = new Point(_a.getPoint());
		p.translate(0, JUMPDISTANCE);
		_result.add(new ANode(p,_a.getTarget(),_a));
		
		p = new Point(_a.getPoint());
		p.translate(0, -JUMPDISTANCE);
		_result.add(new ANode(p,_a.getTarget(),_a));
		
		return _result;
	}

	/**
	 * @return
	 */
	private ANode getNodeWithLowestF() {
		ANode _result = null;
		for(ANode a:f_open) {
			if(_result == null) {
				_result=a;
			}
			if(_result.getF() > a.getF()) {
				_result = a;
			}
		}
		return _result;
	}

	/**
	 * @param _grid
	 * @param row
	 * @param col
	 * @param _o
	 */
	private void route(Grid _grid, int row, int col, FlowObjectWrapper _o) {
		ArrayList<FlowObjectWrapper> f_succ = _o.getSuccessors();
		if(!_o.isDataObject()) {
			for(int s = 0;s<f_succ.size();s++) {
				if(!(_o.getSuccessorEdges().get(s) instanceof DummyEdge)) {
					if(!isGateway(f_succ.get(s)) && 
							(!((BPMNEdgeInterface)_o.getSuccessorEdges().get(s)).isMessageFlow())) {
						if(routeDown(_o, f_succ, s)) {
							//routing downwards
							int _row = row+1;						
							if(_row < _grid.getRowCount()) {
								FlowObjectWrapper _cell = _grid.getObject(_row, col);
								ArrayList<Point> _newrps = new ArrayList<Point>();
								while((_cell == null) && (_row <=  f_succ.get(s).getPosition().y)) {
									//add routing point
									Point p = new Point(_grid.getX(col),_grid.getY(_row));
									if(f_model instanceof TWFModelInterface) {
										p.x += _grid.getColSize(col)/2;
									}
									_newrps.add(p);								
									_row++;
									if(_row < _grid.getRowCount())
										_cell = _grid.getObject(_row, col);
									else
										break;
								}
								LayoutHelper.removeUnneccesaryPoints(_newrps);
								if(_newrps.size()>0) {
									_o.getSuccessorEdges().get(s).setRoutingPoints(_newrps);
									f_routedEdges.add(_o.getSuccessorEdges().get(s));
								}
							}
							
						}else if(routeUp(_o, f_succ, s)) {
							//routing upwards
							int _row = row-1;
							if(_row >= 0) {
								FlowObjectWrapper _cell = _grid.getObject(_row, col);
								ArrayList<Point> _newrps = new ArrayList<Point>();
								while((_cell == null) && (_row >=  f_succ.get(s).getPosition().y)) {
									//add routing point
									Point p = new Point(_grid.getX(col),_grid.getY(_row));
									if(f_model instanceof TWFModelInterface) {
										p.x += _grid.getColSize(col)/2;
									}
									_newrps.add(p);							
									_row--;
									if(_row >= 0)
										_cell = _grid.getObject(_row, col);
									else
										break;
								}
								LayoutHelper.removeUnneccesaryPoints(_newrps);
								if(_newrps.size()>0) {
									_o.getSuccessorEdges().get(s).setRoutingPoints(_newrps);
									f_routedEdges.add(_o.getSuccessorEdges().get(s));
								}
							}
						}
					}
				}
			}
		}
		
		
		
		ArrayList<FlowObjectWrapper> f_pred = _o.getPredecessors();
		if(f_pred.size() == 1) {
			//x direction first
			if(_o.getPosition().y != f_pred.get(0).getPosition().y) {
				int _col = col-1;
				if(_col >= 0) {
					FlowObjectWrapper _cell = _grid.getObject(row, _col);
					ArrayList<Point> _newrps = new ArrayList<Point>();
					while((_cell == null) && (_col >=  f_pred.get(0).getPosition().x)) {
						//add routing point
						Point p = new Point(_grid.getX(_col),_grid.getY(row));
						_newrps.add(0,p);
						_col--;
					}
					LayoutHelper.removeUnneccesaryPoints(_newrps);
					_o.getPredecessorEdges().get(0).setRoutingPoints(_newrps);
					//checking if the way down/up is entirely free
					if(routeDown(_o, f_pred, 0)) {
						//routing downwards
						int _row = row+1;
						if(_row < _grid.getRowCount()) {
							_cell = _grid.getObject(_row, col);
							while(_row <=  f_pred.get(0).getPosition().y) {
								if(_cell == null) {
									_row++;
									continue;
								}
								//not free!
								_o.getPredecessorEdges().get(0).clearRoutingPoints();
								break;
							}
						}
					}else if(routeUp(_o, f_pred, 0)) {
						//routing upwards
						int _row = row-1;
						if(_row >= 0) {
							_cell = _grid.getObject(_row, col);
							while((_row >=  f_pred.get(0).getPosition().y)) {
								if(_cell == null) {
									_row--;
									continue;
								}
								//not free!
								_o.getPredecessorEdges().get(0).clearRoutingPoints();
								break;
							}
						}
					}
				}
			}
		}else {
			for(int pID = 0;pID<f_pred.size();pID++) {
				if(_o.getPredecessorEdges().get(pID) instanceof BPMNEdgeInterface) {
					if(!((BPMNEdgeInterface)_o.getPredecessorEdges().get(pID)).isMessageFlow()) {
						if(routeDown(_o, f_pred, pID)) {
							//routing downwards
							int _row = row+1;
							if(_row < _grid.getRowCount()) {
								FlowObjectWrapper _cell = _grid.getObject(_row, col);
								ArrayList<Point> _newrps = new ArrayList<Point>();
								while((_cell == null) && (_row <=  f_pred.get(pID).getPosition().y)) {
									//add routing point
									Point p = new Point(_grid.getX(col),_grid.getY(_row));
									if(f_model instanceof TWFModelInterface) {
										p.x -= _grid.getColSize(col)/2;
									}
									_newrps.add(0,p);
									_row++;
									if(_row < _grid.getRowCount())
										_cell = _grid.getObject(_row, col);
								}
								LayoutHelper.removeUnneccesaryPoints(_newrps);
								if(_newrps.size()>0) {
									_o.getPredecessorEdges().get(pID).setRoutingPoints(_newrps);
									f_routedEdges.add(_o.getPredecessorEdges().get(pID));
								}
							}
							
						}else if(routeUp(_o, f_pred, pID)) {
							//routing upwards
							int _row = row-1;
							if(_row >= 0) {
								FlowObjectWrapper _cell = _grid.getObject(_row, col);
								ArrayList<Point> _newrps = new ArrayList<Point>();
								while((_cell == null) && (_row >=  f_pred.get(pID).getPosition().y)) {
									//add routing point
									Point p = new Point(_grid.getX(col),_grid.getY(_row));
									if(f_model instanceof TWFModelInterface) {
										p.x -= _grid.getColSize(col)/2;
									}
									_newrps.add(0,p);
									_row--;
									if(_row >= 0)
										_cell = _grid.getObject(_row, col);
								}
								LayoutHelper.removeUnneccesaryPoints(_newrps);
								if(_newrps.size()>0) {
									_o.getPredecessorEdges().get(pID).setRoutingPoints(_newrps);
									f_routedEdges.add(_o.getPredecessorEdges().get(pID));
								}
							}
						}					
					}
			}
			}
		}
	}

	private boolean routeUp(FlowObjectWrapper _o,ArrayList<FlowObjectWrapper> list, int s) {
		FlowObjectWrapper _o2 = list.get(s);
		return _o2.getGrid() < _o.getGrid() || ( _o2.getGrid() == _o.getGrid() && (_o.getPosition().y > _o2.getPosition().y));
	}
	
	private boolean routeDown(FlowObjectWrapper _o,ArrayList<FlowObjectWrapper> list, int s) {
		FlowObjectWrapper _o2 = list.get(s);
		return (_o2.getGrid() > _o.getGrid()) || ( _o2.getGrid() == _o.getGrid() && (_o.getPosition().y < _o2.getPosition().y));
	}

	private boolean isGateway(FlowObjectWrapper fow) {
		//general case
		return (fow.isSplit() || fow.isJoin()) && !fow.isDataObject() ;
		//node dependent case
		//return ((BPMNNodeInterface)fow.getWrappedObject()).isGateway();
	}
	
	protected List<EdgeInterface> getRoutedEdges(){
		return f_routedEdges;
	}


}
