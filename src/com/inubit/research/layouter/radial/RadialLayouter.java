/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.radial;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.frapu.code.visualization.Configuration;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;


/**
 * @author ff
 *
 */
public class RadialLayouter extends ProcessLayouter implements Comparator<RadialNodeWrapper>{

	private NodeInterface f_centerNode = null;
	
	private AbstractModelAdapter f_model;
	private List<NodeInterface> f_unlayoutedNodes;
	private List<RadialNodeWrapper> f_layoutedNodes;

	private boolean f_isCenter = true;
	private Point f_center;
	private Point f_compCenter;

	private Configuration f_props;
	
	private int f_layerDistance = 130;
	
	/**
	 * @param properties
	 */
	public RadialLayouter(Configuration properties) {
		f_props = properties;
                try {
                    f_layerDistance = Integer.parseInt(f_props.getProperty(LayoutHelper.CONF_RADIAL_LAYER_DISTANCE, "100"));
                } catch (Exception e) {
                    f_layerDistance = 100;
                }
	}

	@Override
	public String getDisplayName() {
		return "Radial Layouter";
	}

	@Override
	public void layoutModel(AbstractModelAdapter model, int xstart, int ystart, int direction) throws Exception {
		f_model = model;
		f_isCenter = true;
		if(f_centerNode == null)
			f_centerNode = model.getNodes().get(0);
		f_unlayoutedNodes = new ArrayList<NodeInterface>(model.getNodes().size());
		f_unlayoutedNodes.addAll(model.getNodes());
		f_layoutedNodes = new ArrayList<RadialNodeWrapper>();
		//start layouting
		f_center = new Point(xstart,ystart);
		RadialNodeWrapper _centerNode = new RadialNodeWrapper(this,f_model,f_centerNode,0);
		//can use the whole circle
		_centerNode.setStart(0);
		_centerNode.setEnd(360);		
		
		f_unlayoutedNodes.remove(f_centerNode);
		layout(_centerNode);
		for(RadialNodeWrapper r:f_layoutedNodes) {
			setPosition(r,f_center);
		}
		for(EdgeInterface e:model.getEdges()) {
			e.clearRoutingPoints();
		}
	}

	/**
	 * @param r 
	 * @param _center
	 */
	private void setPosition(RadialNodeWrapper r, Point _center) {
		PolarCoordinates _p = new PolarCoordinates((double)(r.getLayer()*f_layerDistance),r.getAngle(),_center);
		r.setPos(_p.getX(), _p.getY());
	}

	/**
	 * @param node
	 */
	private void layout(RadialNodeWrapper node) {
		f_layoutedNodes.add(node);
		//finding all neighbors
		f_unlayoutedNodes.removeAll(node.getNeighbors());
		//creating
		ArrayList<RadialNodeWrapper> _list = new ArrayList<RadialNodeWrapper>();
		for(NodeInterface n:node.getNeighbors()) {
			_list.add(new RadialNodeWrapper(this,f_model,n,node.getLayer()+1));
		}
		if(_list.size() > 0) {
			sortNodes(node, _list);
			int _sumWeight = 0;
			for(RadialNodeWrapper rnw:_list) {
				_sumWeight += rnw.getNeighbors().size();
				_sumWeight++; //count the node itself too
			}
			int _breadth = node.getEnd()-node.getStart();
			int _weightFactor = _breadth/_sumWeight;
			int _start = node.getStart();
			for(RadialNodeWrapper rnw:_list) {
				rnw.setStart(_start);
				_start += _weightFactor * (rnw.getNeighbors().size()+1);
				rnw.setEnd(_start);
			}
			for(RadialNodeWrapper rnw:_list) {
				layout(rnw);
			}
		}
	}

	private void sortNodes(RadialNodeWrapper node,ArrayList<RadialNodeWrapper> _list) {
		if(f_isCenter) {
			f_compCenter = node.getNode().getPos();
		}else {
			f_compCenter = f_center;
		}
		Collections.sort(_list,this);
		f_isCenter = false; //just use it the first time
	}

	

	public void setCenterNode(NodeInterface node) {
		f_centerNode = node;
	}

	/**
	 * @param target
	 * @return
	 */
	public boolean isUnlayouted(NodeInterface target) {
		return f_unlayoutedNodes.contains(target);
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
		f_centerNode = selectedNode;
	}

	@Override
	public int compare(RadialNodeWrapper o1, RadialNodeWrapper o2) {
		PolarCoordinates _pc1 = new PolarCoordinates(o1.getNode().getPos().x,o1.getNode().getPos().y, f_compCenter);
		PolarCoordinates _pc2 = new PolarCoordinates(o2.getNode().getPos().x,o2.getNode().getPos().y, f_compCenter);
		double a1 = _pc1.getAngle();
		if(a1 < 0.0) {
			a1 = 2*Math.PI+a1;
		}
		double a2 = _pc2.getAngle();
		if(a2 < 0.0) {
			a2 = 2*Math.PI+a2;
		}
		return (int) (a1*1000 - a2*1000);
	}

	/**
	 * @return
	 */
	public int getLayerDistance() {
		return f_layerDistance;
	}

}
