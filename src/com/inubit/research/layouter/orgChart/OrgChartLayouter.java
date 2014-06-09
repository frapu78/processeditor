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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.OrgChartNodeInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;
import com.inubit.research.layouter.sugiyama.LayerStructure;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import com.inubit.research.layouter.sugiyama.TopologicalSorter;

/**
 * @author ff
 *
 */
public class OrgChartLayouter  extends SugiyamaLayoutAlgorithm  {

	
	private HashMap<OrgChartNodeInterface,List<NodeInterface>> f_personMap = new HashMap<OrgChartNodeInterface,List<NodeInterface>>();
	private HashMap<OrgChartNodeInterface,List<EdgeInterface>> f_personEdgeMap = new HashMap<OrgChartNodeInterface,List<EdgeInterface>>();
	private HashMap<OrgChartNodeInterface,Dimension> f_originalSize = new HashMap<OrgChartNodeInterface, Dimension>();

	private HashMap<CompoundLeafNode, List<EdgeInterface>> f_compoundLeaves = new HashMap<CompoundLeafNode, List<EdgeInterface>>();
	private ArrayList<DummyEdge> f_dummyEdges = new ArrayList<DummyEdge>();
	
	
	/**
	 * @param topToBttom
	 * @param distanceX
	 * @param distanceY
	 * @param centerNodes
	 * @param shortenEdges
	 * @param scatterEdges
	 * @param ratioAim - has to be greater 0.0f!
	 */
	public OrgChartLayouter(boolean topToBttom, int distanceX, int distanceY,
			boolean centerNodes, boolean shortenEdges, boolean scatterEdges, int compoundSpacingX, int compoundSpacingY,float ratioAim) {
		super(topToBttom, distanceX, distanceY, centerNodes, shortenEdges, scatterEdges);
		CompoundLeafNode.setSpacingX(compoundSpacingX);
		CompoundLeafNode.setSpacingY(compoundSpacingY);
		CompoundLeafNode.setCompoundRatio(ratioAim);
	}
	
	public OrgChartLayouter(boolean topToBottom,Properties properties) {
		super(topToBottom, properties);
		readProperties(properties);
	}
	
	public OrgChartLayouter(Properties properties) {
		super(properties);
		readProperties(properties);
	}
	
	/**
	 * @param properties
	 */
	private void readProperties(Properties properties) {
		CompoundLeafNode.setSpacingX(LayoutHelper.toInt(properties.getProperty(LayoutHelper.CONF_X_DISTANCE_ORG_COMPOUND,"26"),26));
		CompoundLeafNode.setSpacingY(LayoutHelper.toInt(properties.getProperty(LayoutHelper.CONF_Y_DISTANCE_ORG_COMPOUND,"5"),5));
		CompoundLeafNode.setCompoundRatio(LayoutHelper.toFloat(properties.getProperty(LayoutHelper.CONF_RATIO_ORG_COMPOUND,"1.0"),1.0f));
		
	}

	@Override
	protected LayerStructure buildLayers(AbstractModelAdapter model,TopologicalSorter _sorter) {
		LayerStructure _layers = super.buildLayers(model, _sorter);
		//minor modification which makes org charts look better!
		_layers.setInitialSortDirection(false);
		return _layers;
	}
	
	@Override
	public String getDisplayName() {
		return "Organizational Chart Hierarchical Layout";
	}
	
	@Override
	protected void applyMainAlgorithm(AbstractModelAdapter model, int offSet,int offset2) {
		f_personMap.clear();
		f_personEdgeMap.clear();
		f_originalSize.clear();
		
		f_compoundLeaves.clear();
		f_dummyEdges.clear();
		processChildNodes(model);
		super.applyMainAlgorithm(model, offSet, offset2);
		unprocessChildNodes(model);
	}
	
	/**
	 * nodes which can be placed in the same line, i.e. next to each other, are
	 * combined here and split later on.
	 * This only applies to up to 2 person nodes and only if there are other children apart from persons
	 */
	private void processChildNodes(AbstractModelAdapter model) {
		for(NodeInterface _node:new ArrayList<NodeInterface>(model.getNodes())) {
			OrgChartNodeInterface _oNode = (OrgChartNodeInterface) _node;
			List<NodeInterface> _children = LayoutHelper.getSuccessors(model,_node);
			ArrayList<NodeInterface> _noChildren = new ArrayList<NodeInterface>(); //persons
			ArrayList<EdgeInterface> _noChildrenEdges = new ArrayList<EdgeInterface>(); //persons
			int _oCOunt = 0; //others
			for(NodeInterface n:_children) {
				OrgChartNodeInterface _n = (OrgChartNodeInterface) n;
				if(!hasChidlren(model,_n)) {
					_noChildren.add(_n);
					_noChildrenEdges.add(LayoutHelper.getEdge(model, _node, _n));
				}else {
					_oCOunt++;
				}				
			}
			//manager role heuristic applicable if....
			if((_noChildren.size() <= 2) && (_noChildren.size() > 0) && (_oCOunt > 0)) {
				int w = _node.getSize().width;
				int h = _node.getSize().height;
				
				for(NodeInterface _person : _noChildren) {
					w = Math.max(w, _person.getSize().width);
					h = Math.max(h, _person.getSize().height);
					model.removeNode(_person);
				}
				for(EdgeInterface _edge : _noChildrenEdges) {
					model.removeEdge(_edge);
				}
				Dimension _orgSize = new Dimension(_oNode.getSize());
				f_originalSize.put(_oNode, _orgSize);
				int newWidth = 3*w + 2* CompoundLeafNode.XSPACING;
				_oNode.setSize(newWidth, h);
				model.saveEdgeLayoutSize(_oNode,_orgSize);
				f_personMap.put(_oNode, _noChildren);		
				f_personEdgeMap.put(_oNode, _noChildrenEdges);
			}else if((_noChildren.size() >= 3) && (_oCOunt == 0)) {
				//found a big chunk of persons for a leaf role, putting them in a compound to have
				//a more compact layout
				CompoundLeafNode _leaf = new CompoundLeafNode(_noChildren);
				//removing all nodes and the edges (edges are saved through mapping, nodes in the compound)
				for(NodeInterface _person : _noChildren) {
					model.removeNode(_person);
				}
				for(EdgeInterface _edge : _noChildrenEdges) {
					model.removeEdge(_edge);
				}
				f_compoundLeaves.put(_leaf, _noChildrenEdges);
				model.addDummyNode(_leaf);
				DummyEdge _de = new DummyEdge(_oNode,_leaf);
				model.addDummyEdge(_de);
				f_dummyEdges.add(_de);				
			}			
		}		
	}	
	
	/**
	 * @param model
	 * @param _n
	 * @return
	 */
	private boolean hasChidlren(AbstractModelAdapter model,	OrgChartNodeInterface n) {
		for(EdgeInterface e:model.getEdges()) {
			if(e.getSource().equals(n)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param model
	 */
	private void unprocessChildNodes(AbstractModelAdapter model) {
		for(OrgChartNodeInterface _node:f_originalSize.keySet()) {
			Dimension _d = f_originalSize.get(_node);
			_node.setSize(_d.width,_d.height);
		}
		for(OrgChartNodeInterface _node:f_personMap.keySet()) {
			List<NodeInterface> _persons = f_personMap.get(_node);
			OrgChartNodeInterface _p1 = (OrgChartNodeInterface) _persons.get(0);
			model.addNode(_p1);
			
			Point _pos = _node.getPos();
			_pos.x += _node.getSize().width/2;
			_pos.x += _p1.getSize().width/2;
			_pos.x += CompoundLeafNode.XSPACING;
			_p1.setPos(_pos.x, _pos.y);			
			//if a second node is available, the same principle can be applied
			if(_persons.size()>1) {
				OrgChartNodeInterface _p2 = (OrgChartNodeInterface) _persons.get(1);			
				model.addNode(_p2);
				_pos = _node.getPos();
				_pos.x -= _node.getSize().width/2;
				_pos.x -= _p1.getSize().width/2;
				_pos.x -= CompoundLeafNode.XSPACING;
				_p2.setPos(_pos.x, _pos.y);
			}			
		}
		for(OrgChartNodeInterface n:f_personEdgeMap.keySet()) {
			for(EdgeInterface e:f_personEdgeMap.get(n)) {
				model.addEdge(e);
				e.clearRoutingPoints();
			}
		}
		
		for(EdgeInterface e:f_dummyEdges) {
			model.removeDummyEdge(e);
		}
		for(CompoundLeafNode _cl:f_compoundLeaves.keySet()) {
			List<EdgeInterface> _edges = f_compoundLeaves.get(_cl);
			for(EdgeInterface e:_edges) {
				model.addEdge(e);
			}
			model.removeDummyNode(_cl);
			_cl.layout(model,_edges); //
		}
		
		
	}
	

}
