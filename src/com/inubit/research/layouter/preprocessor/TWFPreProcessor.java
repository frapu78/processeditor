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
import java.util.HashMap;
import java.util.List;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFEdgeInterface;
import com.inubit.research.layouter.interfaces.TWFModelInterface;
import com.inubit.research.layouter.interfaces.TWFNodeInterface;

/**
 * @author ff
 *
 */
public class TWFPreProcessor implements IPreProcessor {
	
	private HashMap<EdgeInterface, NodeInterface> f_orignalTargets = new HashMap<EdgeInterface, NodeInterface>();
	private HashMap<EdgeInterface, NodeInterface> f_orignalSources = new HashMap<EdgeInterface, NodeInterface>();

	private HashMap<DummyEdge, EdgeInterface> f_replacedEdges = new HashMap<DummyEdge, EdgeInterface>();
	
	private List<EdgeInterface> f_removedEdges = new ArrayList<EdgeInterface>(); //used to store (removed) edges
	private List<NodeInterface> f_removedNodes = new ArrayList<NodeInterface>(); //used to store (removed) TooldDockers etc.
	
	private AbstractModelAdapter f_model;
	
	@Override
	public void process(AbstractModelAdapter model) {
		//clearing lists
		f_orignalSources.clear();
		f_orignalTargets.clear();
		f_replacedEdges.clear();
		f_removedEdges.clear();
		f_removedNodes.clear();		
		//starting processing
		f_model = (AbstractModelAdapter) model;
		checkErrorConnections();
		toolHandling();
		checkInClusterConnections();				
	}

	private void toolHandling() {
		//checking if flows cross cluster boundaries
		for(NodeInterface n:new ArrayList<NodeInterface>(f_model.getNodes())) {
			TWFNodeInterface _node = (TWFNodeInterface) n;
			if(_node.isToolDocker()) {
				//special handling to make it layoutable
				List<EdgeInterface> _edges = f_model.getEdges();
				for(int i=_edges.size()-1;i>=0;i--){
					EdgeInterface e = (EdgeInterface) _edges.get(i); //could be a dummyedge too
					if(e.getTarget().equals(_node)){
						if(_node.getParent().getContainedNodes().contains(e.getSource())) {
							//inside connection
							removeEdge(e);
						}else {
							//changing target of edge from tool docker to the actual tool!
							f_orignalTargets.put(e, (NodeInterface)e.getTarget());
							e.setTarget(_node.getParent());	
						}
					}else if(e.getSource().equals(_node)) {
						if(_node.getParent().getContainedNodes().contains(e.getTarget())) {
							//inside connection
							removeEdge(e);
						}else {
							//changing source of edge from tool docker to the actual tool!
							f_orignalSources.put(e, (NodeInterface)e.getSource());
							e.setSource(_node.getParent());	
						}
					}
				}
				removeNode(_node);
			}
			else if(_node.isToolErrorConnector()) {
				//special handling to make it layoutable
				List<EdgeInterface> _edges = f_model.getEdges();
				for(int i=_edges.size()-1;i>=0;i--){
					EdgeInterface e = (EdgeInterface) _edges.get(i);
					if(e.getTarget().equals(_node)){
						//should not happen, but lets just be careful
						f_orignalTargets.put(e, (NodeInterface)e.getTarget());
						e.setTarget(_node.getParent());								
					}else if(e.getSource().equals(_node)) {
						//this is the usual case
						f_orignalSources.put(e, (NodeInterface)e.getSource());
						//directly connected to the tool
						e.setSource(_node.getParent());		
					}
				}
				removeNode(_node);
			}
		}
	}

	/**
	 * @param node
	 */
	private void removeNode(TWFNodeInterface node) {
		f_removedNodes.add(node);
		f_model.removeNode(node);
	}

	private void removeEdge(EdgeInterface e) {
		//just ignore them for layouting
		f_removedEdges.add(e);
		f_model.removeEdge(e);
	}
	
	private void checkInClusterConnections() {
		//checking if flows cross cluster boundaries
		for(NodeInterface _node:f_model.getNodes()) {
			TWFNodeInterface _cluster = (TWFNodeInterface) _node;
			if(_cluster.isSubProcess()) {
				List<EdgeInterface> _edges = f_model.getEdges();
				List<NodeInterface> _nodes = _cluster.getContainedNodes();
				for(int i=_edges.size()-1;i>=0;i--){
					EdgeInterface e = (EdgeInterface) _edges.get(i);
					if(_nodes.contains(e.getSource())^_nodes.contains(e.getTarget())){//XOR				
						//this edge crosses the boundaries of the process
						if(_nodes.contains(e.getSource())) {
							//change from actual source/target to the cluster and save changes
							//so they can be reverted later on
							f_orignalSources.put(e, (NodeInterface) e.getSource());
							e.setSource(_cluster);
						}else if(_nodes.contains(e.getTarget())){
							f_orignalTargets.put(e, (NodeInterface) e.getTarget());
							e.setTarget(_cluster);
						}											
					}
				}
			}
		}
	}
	
	/**
	 * replaces TWF error connections with dummy edges so the same processing
	 * as for attached intermediate events is applied.
	 */
	private void checkErrorConnections() {
		for(int i=f_model.getEdges().size()-1;i>=0;i--) {
			TWFEdgeInterface _edge = (TWFEdgeInterface) f_model.getEdges().get(i);
			if(_edge.isErrorConnection() || _edge.getSource().isToolErrorConnector() || _edge.getTarget().isToolErrorConnector()) {
				//replace with dummy edge to give it a different layout
				DummyEdge _de = new DummyEdge(_edge.getSource(),_edge.getTarget());
				f_replacedEdges.put(_de, _edge);
				f_model.removeEdge(_edge);
				f_model.addDummyEdge(_de);				
			}
		}		
	}

	@Override
	public boolean supports(AbstractModelAdapter model) {
		return model instanceof TWFModelInterface;
	}

	@Override
	public void unprocess(AbstractModelAdapter model) {
		revertErrorConnections();
		revertSourcesAndTargets();
		revertEdges();
		revertNodes();
	}

	/**
	 * 
	 */
	private void revertNodes() {
		for(NodeInterface e:f_removedNodes) {
			f_model.addNode(e);
		}
	}

	/**
	 * 
	 */
	private void revertEdges() {
		for(EdgeInterface e:f_removedEdges) {
			f_model.addEdge(e);			
		}
	}

	/**
	 * resets the connection to the appropriate nodes
	 */
	private void revertSourcesAndTargets() {
		for(EdgeInterface e: f_orignalSources.keySet()) {
			e.setSource(f_orignalSources.get(e));
		}
		for(EdgeInterface e: f_orignalTargets.keySet()) {
			e.setTarget(f_orignalTargets.get(e));
		}
	}
	
	/**
	 * replaces the created dummy edges with ErrorConnections again
	 */
	private void revertErrorConnections() {
		for(int i=f_model.getEdges().size()-1;i>=0;i--) {
			EdgeInterface _edge = f_model.getEdges().get(i);
			if(_edge instanceof DummyEdge) {
				f_model.removeDummyEdge(_edge);
				TWFEdgeInterface _realEdge = (TWFEdgeInterface) f_replacedEdges.get(_edge);
				f_model.addEdge(_realEdge);					
				i++;
			}
		}		
	}

}
