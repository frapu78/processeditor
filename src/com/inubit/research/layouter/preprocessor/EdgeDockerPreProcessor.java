/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.util.ArrayList;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class EdgeDockerPreProcessor implements IPreProcessor {

	private ArrayList<EdgeInterface> f_virtualEdgeDockerReplacements = new ArrayList<EdgeInterface>();
	private ArrayList<NodeInterface> f_removedEdgeDockers = new ArrayList<NodeInterface>();
	private ArrayList<EdgeInterface> f_removedEdgeEdges = new ArrayList<EdgeInterface>();
	
	
	@Override
	public void process(AbstractModelAdapter model) {
		f_virtualEdgeDockerReplacements.clear();
		f_removedEdgeDockers.clear();
		f_removedEdgeEdges.clear();
		
		processEdgeDockers(model);
	}
	
	/**
	 * @param model
	 */
	private void processEdgeDockers(AbstractModelAdapter model) {
		for(EdgeInterface e:new ArrayList<EdgeInterface>(model.getEdges())) {
			NodeInterface _docker;
			NodeInterface _node;
			if(((NodeInterface)e.getSource()).isVirtualNode() ){
				_docker =  (NodeInterface)e.getSource();
				_node =  (NodeInterface)e.getTarget();
			}else if(((NodeInterface)e.getTarget()).isVirtualNode()) {
				_docker =  (NodeInterface)e.getTarget();
				_node = (NodeInterface)e.getSource();
			}else {
				continue;
			}
			EdgeInterface _e = _docker.getDockedTo();
			//adding new dummy edges to keep association
			DummyEdge _d1 = new DummyEdge((NodeInterface) _e.getSource(),_node);
			DummyEdge _d2 = new DummyEdge(_node,(NodeInterface)_e.getTarget());
			f_virtualEdgeDockerReplacements.add(_d1);
			f_virtualEdgeDockerReplacements.add(_d2);
			model.addDummyEdge(_d1);
			model.addDummyEdge(_d2);
			
			//removing edgedocker and its edge, but saving it for later reinsertion
			f_removedEdgeEdges.add(e);
			model.removeEdge(e);
			f_removedEdgeDockers.add(_docker);
			model.removeNode(_docker);
		}
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return true;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		unprocessEdgeDockers(model);
	}
	
	/**
	 * @param model
	 */
	private void unprocessEdgeDockers(AbstractModelAdapter model) {
		for(NodeInterface n:f_removedEdgeDockers) {
			model.addNode(n);
		}
		for(EdgeInterface e:f_removedEdgeEdges) {
			model.addEdge(e);
		}
		for(EdgeInterface e:f_virtualEdgeDockerReplacements) {
			model.removeDummyEdge(e);
		}
	}

}
