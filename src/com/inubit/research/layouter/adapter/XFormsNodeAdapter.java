/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.inubit.research.layouter.interfaces.NodeInterface;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.xforms.Panel;
import net.frapu.code.visualization.xforms.Trigger;

/**
 * @author ff
 *
 */
public class XFormsNodeAdapter extends BPMNNodeAdapter {

	/**
	 * @param n
	 */
	public XFormsNodeAdapter(ProcessNode n) {
		super(n);
	}
	
	@Override
	public boolean isSubProcess() {
		return getNode() instanceof Panel;
	}
	
	public boolean isPanel() {
		return isSubProcess();
	}
	
	@Override
	public void setPos(int x, int y) {
		//adding bounding box offset
		//(bounding box is bigger due to label
		//super.setPos(x+(getNode().getBoundingBox().width - getNode().getSize().width)/2, y);           
		super.setPos(x, y);
	}
	
	@Override
	public Dimension getSize() {
		Rectangle _bounds = getNode().getBoundingBox();
		return _bounds.getSize();
	}

	/**
	 * returns only the size of the noe (without the label)
	 * @return
	 */
	public Dimension getNodeSize() {
		return getNode().getSize();
	}

	
	@Override
	public List<NodeInterface> getContainedNodes() {
		List<ProcessNode> nodes = ((Cluster)getNode()).getProcessNodes();
		ArrayList<NodeInterface> _result = new ArrayList<NodeInterface>();
		for(ProcessNode n : nodes) {
			_result.add(new XFormsNodeAdapter(n));
		}
		return _result;
	}
	
	public boolean isTrigger() {
		return getNode() instanceof Trigger;
	}
	
	
}
