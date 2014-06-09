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

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.OrgChartNodeInterface;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.general.ColoredFrame;
import net.frapu.code.visualization.orgChart.Person;
import net.frapu.code.visualization.orgChart.Substitute;

/**
 * @author ff
 *
 */
public class OrgChartNodeAdapter extends ProcessNodeAdapter implements OrgChartNodeInterface{

	/**
	 * @param n
	 */
	public OrgChartNodeAdapter(ProcessNode n) {
		super(n);
	}
	
	@Override
	public EdgeInterface getDockedTo() {
		if(getNode() instanceof EdgeDocker) {
			return new OrgChartEdgeAdapter(((EdgeDocker)getNode()).getDockedEdge());
		}
		return null;
	}

	@Override
	public List<NodeInterface> getContainedNodes() {
		if(getNode() instanceof ColoredFrame) {
			List<ProcessNode> _list = ((ColoredFrame)getNode()).getProcessNodes();
			List<NodeInterface> _result = new ArrayList<NodeInterface>();
			for(ProcessNode n:_list) {
				_result.add(new OrgChartNodeAdapter(n));
			}
			return _result;
		}
		return null;
	}

	@Override
	public boolean isCluster() {
		return getNode() instanceof ColoredFrame;
	}

	@Override
	public boolean isPerson() {
		return getNode() instanceof Person || getNode() instanceof Substitute;
	}

	@Override
	public void setSize(int w, int h) {
		getNode().setSize(w, h);
	}

}
