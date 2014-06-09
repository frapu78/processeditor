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
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.twf.Tool;
import net.frapu.code.visualization.twf.ToolDocker;
import net.frapu.code.visualization.twf.ToolErrorConnector;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFNodeInterface;

/**
 * @author ff
 *
 */
public class TWFNodeAdapter extends BPMNNodeAdapter implements TWFNodeInterface{

	/**
	 * @param n
	 */
	public TWFNodeAdapter(ProcessNode n) {
		super(n);		
	}

	@Override
	public List<NodeInterface> getContainedNodes() {
		if(getNode() instanceof Cluster) {			
			List<ProcessNode> nodes = ((Cluster)getNode()).getProcessNodes();
			ArrayList<NodeInterface> _result = new ArrayList<NodeInterface>();
			for(ProcessNode n : nodes) {
				_result.add(new BPMNNodeAdapter(n));
			}
			return _result;
		}
		return null;
	}

	@Override
	public boolean isAnnotation() {
		return false;
	}

	@Override
	public NodeInterface isAttatchedTo(AbstractModelAdapter model) {
		return null;
	}

	@Override
	public boolean isDataObject() {
		return false;
	}

	@Override
	public boolean isGateway() {
		return false;
	}

	@Override
	public boolean isLane() {
		return false;
	}

	@Override
	public boolean isPool() {
		return false;
	}
	@Override
	public boolean isSubProcess() {
		return getNode() instanceof Cluster;
	}

	@Override
	public boolean placeDataObjectUpwards() {
		return false;
	}

	@Override
	public void setAttatchedTo(AbstractModelAdapter model, NodeInterface node) {
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
	public void setSize(int w, int h) {
		getNode().setProperty(ProcessNode.PROP_WIDTH, ""+w);
		getNode().setProperty(ProcessNode.PROP_HEIGHT, ""+h);
	}
	
	@Override
	public int getPaddingY() {
		if(getNode() instanceof Tool) {
			return 30;
		}
		return 0;
	}

	@Override
	public boolean isToolErrorConnector() {
		return getNode() instanceof ToolErrorConnector;
	}

	@Override
	public TWFNodeInterface getParent() {
		if(getNode() instanceof ToolDocker)
			return new TWFNodeAdapter(((ToolDocker)getNode()).getParent());
		else if(getNode() instanceof ToolErrorConnector)
			return new TWFNodeAdapter(((ToolErrorConnector)getNode()).getParent());
		else
			return null;
	}

	@Override
	public boolean isToolDocker() {
		return getNode() instanceof ToolDocker;
	}
}
