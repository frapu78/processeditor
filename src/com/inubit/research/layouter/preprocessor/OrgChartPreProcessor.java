/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.OrgChartModelInterface;
import com.inubit.research.layouter.interfaces.OrgChartNodeInterface;

/**
 * @author ff
 *
 */
public class OrgChartPreProcessor implements IPreProcessor {
	
	/**
	 * 
	 */
	public static final int DISTANCE_TO_FRAME = 50;
	private List<OrgChartNodeInterface> f_clusters = new ArrayList<OrgChartNodeInterface>();
	private List<DummyEdge> f_dummyEdges = new ArrayList<DummyEdge>();
	
	@Override
	public void process(AbstractModelAdapter model) {
		f_clusters.clear();				
		processCluster(model);
	}	

	/**
	 * @param model
	 */
	private void processCluster(AbstractModelAdapter model) {
		for(NodeInterface n :model.getNodes()) {
			OrgChartNodeInterface _node = (OrgChartNodeInterface) n;
			if(_node.isCluster() ) { 
				if(_node.getContainedNodes().size() > 0) {
					f_clusters.add(_node);
					model.removeNode(_node);
				}else {
					//how to layout? get closest (real-) node and add a dummy edge
					//find closest node
					NodeInterface _closest = null;
					double _dist = Double.MAX_VALUE;
					for(NodeInterface close :model.getNodes()) {
						OrgChartNodeInterface _close = (OrgChartNodeInterface) close;
						if(!_close.isCluster()) {
							double _d = _close.getPos().distance(_node.getPos());
							if(_d < _dist) {
								_closest = _close;
								_dist = _d;
							}
						}
					}
					if(_closest != null) {
						DummyEdge _dummy = new DummyEdge(_closest,_node);
						model.addDummyEdge(_dummy);
						f_dummyEdges.add(_dummy);
					}
				}
			}
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof OrgChartModelInterface;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		unprocessClusters(model);
	}

	/**
	 * @param model 
	 * 
	 */
	private void unprocessClusters(AbstractModelAdapter model) {
		for(OrgChartNodeInterface _cluster :f_clusters) {
			model.addNode(_cluster);
			Point _min = new Point();
			Point _max = new Point();
			_min.x = Integer.MAX_VALUE;
			_min.y = Integer.MAX_VALUE;
			for(NodeInterface cont:_cluster.getContainedNodes()) {
				Point _pos_min = cont.getPos();
				_pos_min.y -= cont.getSize().height/2;
				_pos_min.x -= cont.getSize().width/2;
				Point _pos_max = cont.getPos();
				_pos_max.x += cont.getSize().width/2;
				_pos_max.y += cont.getSize().height/2;
				
				_min.x = Math.min(_min.x, _pos_min.x);
				_min.y = Math.min(_min.y, _pos_min.y);
				_max.x = Math.max(_max.x, _pos_max.x);
				_max.y = Math.max(_max.y, _pos_max.y);
			}
			Dimension _size = new Dimension(_max.x - _min.x + OrgChartPreProcessor.DISTANCE_TO_FRAME ,_max.y-_min.y + OrgChartPreProcessor.DISTANCE_TO_FRAME);
			Point _pos = new Point((_max.x+_min.x)/2,(_max.y+_min.y)/2);
			if(_size.height > 0 && _size.width > 0) {
				_cluster.setSize(_size.width, _size.height);
				_cluster.setPos(_pos.x, _pos.y);
			}
		}
		for(DummyEdge d:f_dummyEdges) {
			model.removeDummyEdge(d);
		}
	}
	
	

	

}
