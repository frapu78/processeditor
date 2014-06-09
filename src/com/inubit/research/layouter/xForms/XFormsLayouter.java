/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.xForms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.inubit.research.layouter.adapter.BPMNEdgeAdapter;
import com.inubit.research.layouter.adapter.XFormsNodeAdapter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.gridLayouter.XPositionComparator;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * specialized for ProcessEditor xForms.
 * Will not work in IS, yet as the adapter is directly accessed!
 * A simple layouter for xForms
 * @author ff
 *
 */
public class XFormsLayouter extends GridLayouter{


	/**
	 * @param properties
	 */
	public XFormsLayouter(Properties properties) {
		super(properties);
	}

	@Override
	public String getDisplayName() {
		return "Simple X-Forms Layouter";
	}

	@Override
	public void layoutModel(AbstractModelAdapter model, int xstart, int ystart,int direction) throws Exception {
		for(NodeInterface node:model.getNodes()) {
			XFormsNodeAdapter _xNode = (XFormsNodeAdapter)node;
			if(_xNode.isPanel()) {
				List<NodeInterface> _triggers = new ArrayList<NodeInterface>();
				for(NodeInterface child:_xNode.getContainedNodes()) {
					if(((XFormsNodeAdapter)child).isTrigger()) {						
						_triggers.add(child);
					}
				}
				Collections.sort(_triggers, new XPositionComparator());
				for(int i=1;i<_triggers.size();i++) {
					XFormsNodeAdapter _start = (XFormsNodeAdapter) _triggers.get(i-1);
					XFormsNodeAdapter _end = (XFormsNodeAdapter) _triggers.get(i);
					model.addEdge(new BPMNEdgeAdapter(new VirtualXFormsEdge(_start.getNode(),_end.getNode())));
				}
			}
		}
		
		super.layoutModel(model, xstart, ystart, direction);
		
		for(EdgeInterface e:new ArrayList<EdgeInterface>(model.getEdges())) {
			BPMNEdgeAdapter _bpmnedge = (BPMNEdgeAdapter) e;
			if(_bpmnedge.getEdge() instanceof VirtualXFormsEdge) {
				model.removeEdge(e);
			}
		}
		
		//adding appropriate Offsets to all inputs so they align to each other
		addOffsets(model.getNodes());
		for(NodeInterface n:model.getNodes()) {
			XFormsNodeAdapter _n = (XFormsNodeAdapter) n;
			if(_n.isPanel()) {
				addOffsets(_n.getContainedNodes());
			}			
		}		
	}

	private void addOffsets(List<NodeInterface> nodeList) {
		int _maxLabelWidth = 0;
		int _maxInputSize = 0;
		//determining label offset
		for(NodeInterface n2 :nodeList) {
			XFormsNodeAdapter _n2 = (XFormsNodeAdapter) n2;
			if((!_n2.isPanel()) && (!_n2.isTrigger())){ //isInput		
				_maxLabelWidth = Math.max(_maxLabelWidth, _n2.getSize().width - _n2.getNodeSize().width);
				_maxInputSize = Math.max(_maxInputSize, _n2.getNodeSize().width);
			}
		}
		ArrayList<NodeInterface> _skip = new ArrayList<NodeInterface>();
		//finding nodes to skip (will be handled by their own panel
		for(NodeInterface n2 :nodeList) {
			XFormsNodeAdapter _n2 = (XFormsNodeAdapter) n2;
			if(_n2.isPanel()) {
				_skip.addAll(_n2.getContainedNodes());
			}					
		}
		//adding label offset to all nodes
		for(NodeInterface n2 :nodeList) {
			XFormsNodeAdapter _n2 = (XFormsNodeAdapter) n2;
			if((!_skip.contains(_n2)) && (!_n2.isPanel()) && (!_n2.isTrigger())){ //isInput						
				Point _pos = _n2.getPos();
				_pos.x += _maxLabelWidth/2;
				_pos.x -= (_maxInputSize - _n2.getNodeSize().width)/2;
				_n2.setPos(_pos.x, _pos.y);
			}
		}
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
		//we do not care
	}

}
