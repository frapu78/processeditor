/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.LaneableCluster;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.TextAnnotation;
import net.frapu.code.visualization.bpmn.UserArtifact;

import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.Message;

/**
 * @author ff
 *
 */
public class BPMNNodeAdapter extends ProcessNodeAdapter implements BPMNNodeInterface{

	/**
	 * @param n
	 */
	public BPMNNodeAdapter(ProcessNode n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isDataObject() {
		return ((getNode() instanceof DataObject) ||
                        (getNode() instanceof Message) ||
                        (getNode() instanceof DataStore) ||
                        (getNode() instanceof UserArtifact));
	}
	
	@Override
	public boolean placeDataObjectUpwards() {
		return ((getNode() instanceof DataObject) | (getNode() instanceof Message));
	}

	@Override
	public NodeInterface isAttatchedTo(AbstractModelAdapter model) {
		if(getNode() instanceof IntermediateEvent) {
			ProcessModelAdapter _adap = (ProcessModelAdapter) model;
			ProcessNode _parent = ((IntermediateEvent)getNode()).getParentNode(_adap.getModel());
			if(_parent != null) {
				return new BPMNNodeAdapter(_parent);
			}
		}
		return null;
	}
	
	@Override
	public void setAttatchedTo(AbstractModelAdapter model, NodeInterface node) {
		((IntermediateEvent)getNode()).setParentNode(((BPMNNodeAdapter)node).getNode());
	}

	@Override
	public List<NodeInterface> getContainedNodes() {
		List<ProcessNode> nodes = ((Cluster)getNode()).getProcessNodes();
		ArrayList<NodeInterface> _result = new ArrayList<NodeInterface>();
		for(ProcessNode n : nodes) {
			_result.add(new BPMNNodeAdapter(n));
		}
		return _result;
	}
	
//	@Override
//	public void setContainedNodes(List<NodeInterface> nodes) {
//		if(getNode() instanceof Cluster) {
//			Cluster c = (Cluster) getNode();
//			c.clearContainment();
//			for(NodeInterface n : nodes) {
//				ProcessNode _n = ((ProcessNodeAdapter)n).getNode();
//				c.addProcessNode(_n);
//			}
//		}
//	}

	@Override
	public boolean isPool() {
		return getNode() instanceof Pool;
	}
	
	@Override
	public boolean isLane() {
		return getNode() instanceof Lane;
	}

	@Override
	public void setSize(int w, int h) {
		getNode().setProperty(ProcessNode.PROP_WIDTH, ""+w);
		getNode().setProperty(ProcessNode.PROP_HEIGHT, ""+h);
	}
	
	@Override
	public void setPos(int x, int y) {
		getNode().setProperty(ProcessNode.PROP_XPOS, ""+x);
		getNode().setProperty(ProcessNode.PROP_YPOS, ""+y);
	}

	@Override
	public boolean isSubProcess() {
		return getNode() instanceof SubProcess || getNode() instanceof ChoreographySubProcess;
	}

	@Override
	public boolean isGateway() {
		return getNode() instanceof Gateway;
	}

	@Override
	public boolean isAnnotation() {
		return getNode() instanceof TextAnnotation;
	}

	@Override
	public EdgeInterface getDockedTo() {
		if(getNode() instanceof EdgeDocker) {
			return new BPMNEdgeAdapter(((EdgeDocker)getNode()).getDockedEdge());
		}
		return null;
	}
	
	@Override
	public int getPaddingY() {
		if(getNode() instanceof ChoreographySubProcess) {
			ChoreographySubProcess _csp = (ChoreographySubProcess) getNode();
			return Math.max(_csp.getUpperParticipants().size(), _csp.getLowerParticipants().size())*20;			
		}
		return 0;
	}

	@Override
	public boolean isVertical() {
		if(getNode() instanceof LaneableCluster) {
			return ((LaneableCluster)getNode()).isVertical();
		}
		return false;
	}

}
