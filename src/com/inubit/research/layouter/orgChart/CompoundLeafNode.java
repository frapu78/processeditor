/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.orgChart;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class CompoundLeafNode implements NodeInterface {
	
	public static float RATIO_AIM = 2.0f; //space ratio (width/height)
	public static int XSPACING = 26; //pixels left between the nodes
	public static int YSPACING = 5;
	private List<NodeInterface> f_nodes;
	
	private int f_rows;
	private int f_cols;
	private int f_width;
	private int f_height;
	private Point f_pos;
	private int f_wMax = 0; //width of the largest node
	private int f_hMax = 0;// height of the largest contained node
	
	/**
	 * 
	 */
	public CompoundLeafNode(List<NodeInterface> nodes) {
		f_nodes = nodes;
		
		//determining largest node
		for(NodeInterface n:f_nodes) {
			Dimension _d = n.getSize();
			if(f_wMax < _d.width) {
				f_wMax = _d.width;
			}
			if(f_hMax < _d.height) {
				f_hMax = _d.height;
			}
		}
		/*
		 * calculation of needed rows and cols.
		 * It is based on 2 simple formulas:
		 * 
		 * f_rows * f_cols >= f_nodes.size()
		 * _wMax * cols (the width) / _hMax * rows (the height) =(nearly) RATIO_AIM 
		 * 
		 * a RATIO_aim of e.g. 1.0 will make it look like a square
		 */
		f_cols = (int)Math.ceil(Math.sqrt((RATIO_AIM*f_nodes.size()*30)/(130)));
		f_rows = (int)Math.ceil(((float)f_nodes.size())/f_cols);	
		f_width = f_cols * (f_wMax + XSPACING) - XSPACING; // subtracting one of them
		f_height = f_rows * (f_hMax + YSPACING) - YSPACING;
	}

	@Override
	public EdgeInterface getDockedTo() {
		return null;
	}

	@Override
	public int getPaddingX() {
		return 0;
	}

	@Override
	public int getPaddingY() {
		return 0;
	}

	@Override
	public Point getPos() {
		return f_pos;
	}

	@Override
	public Dimension getSize() {
		return new Dimension(f_width,f_height);
	}

	@Override
	public String getText() {
		return "Compact Leaf Node (contains "+f_nodes.size()+" nodes)";
	}

	@Override
	public boolean isVirtualNode() {
		return false;
	}

	@Override
	public void setPos(int x, int y) {
		f_pos = new Point(x,y);
	}

	/**
	 * @param model
	 * @param _edges
	 */
	public void layout(AbstractModelAdapter model, List<EdgeInterface> edges) {
		Point _start = new Point(this.getPos());
		_start.x -= f_width/2;
		_start.y -= f_height/2;
		_start.x += f_wMax/2;
		_start.y += f_hMax/2;
		Collections.sort(f_nodes, new NodeByNameSorter());		
		Collections.sort(edges, new EdgeByTargetNameSorter());
		
		outer: for(int i = 0;i<f_rows;i++) {
			for(int j=0;j<f_cols;j++) {
				int _idx = i*f_cols+j;
				if(_idx >= f_nodes.size()) {
					break outer;
				}
				NodeInterface _node = f_nodes.get(_idx);
				Point _pos = new Point(_start);
				_pos.x += (j*(f_wMax+CompoundLeafNode.XSPACING));
				_pos.y += (i*(f_hMax+CompoundLeafNode.YSPACING));
				_node.setPos(_pos.x, _pos.y);					
				model.addNode(_node);
			}
		}
		//------------ EDGE ROUTING - parts taken from SugiyamaLayoutAlgorithm.writeCoords()----------
		for(EdgeInterface edge:edges) {
			edge.clearRoutingPoints();
		}
		
		NodeInterface _source = (NodeInterface) edges.get(0).getSource(); 
		Point _rp = new Point(_source.getPos());
		_rp.y += _source.getSize().height/2;
		int _useableDistance = (this.getPos().y - f_height/2) - (_rp.y);
		for(int i = 0;i<f_cols;i++) {
			List<Point> _rps = new ArrayList<Point>();
			Point _p = new Point(_rp);
			double startIndex = i - ((f_cols-1)/2.0);					
			//first point, directly at the border of the source node
			int width = model.getEdgeLayoutSize(_source).width;
			_p.x +=  startIndex * (width/(double)f_cols);		
			_rps.add(_p);
			//second point a bit lower with (with scatter edges)
			_p = new Point(_p);
			int _yOffset = (int) ((1-((Math.abs(startIndex)/(f_cols+0.001/2)))) * (_useableDistance*0.7));			
			_p.y += _yOffset;
			_rps.add(_p);
			
			int _dir = f_cols%2==0 ? i%2== 0 ? 1: -1: -1; //direction - to the left or right of nodes
			Point _topOfColumn = new Point(_start);
			_topOfColumn.y = _p.y; //making the line straight
			_topOfColumn.x += i * (f_wMax+XSPACING);
			_topOfColumn.x += _dir * ((f_wMax+XSPACING)/2);
			_rps.add(_topOfColumn);
			Point _atNode1 = new Point(_topOfColumn);
			_atNode1.y = _start.y; //getting it down on node level;
			_rps.add(_atNode1);
			//Point _atNode2 = new Point(_atNode1);
			//_atNode2.x -= _dir * (XSPACING/2);
			//_rps.add(_atNode2);
			
			for(int r=i;r<edges.size();r+=f_cols) {
				edges.get(r).setRoutingPoints(_rps);
				//now we have to move the last 2 points downwards
				_rps = new ArrayList<Point>(_rps);
				_atNode1 = _rps.get(_rps.size()-1);
				//_atNode2 = _rps.get(_rps.size()-2);
				_atNode1.y += f_hMax+YSPACING;
				//_atNode2.y += f_hMax+YSPACING;
			}
		}		
	}
	
	@Override
	public String toString() {
		return getText();
	}
	
	public static void setSpacingX(int value) {
		XSPACING = value;
	}
	
	public static void setSpacingY(int value) {
		YSPACING = value;
	}
	
	public static void setCompoundRatio(float value) {
		RATIO_AIM = value;
	}

	
}
