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
import java.awt.Point;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessNode;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class ProcessNodeAdapter implements NodeInterface{

	private ProcessNode f_node;
	
	public ProcessNodeAdapter(ProcessNode n){
		if(n == null) {
			System.out.println("null node added!");
			//	throw new Exception("You tried to add a Null node");
		}
		f_node = n;
	}
	
	@Override
	public boolean equals(Object obj) {
                if (f_node==null) return false;
		if(obj instanceof ProcessNodeAdapter) {
			return f_node.equals(((ProcessNodeAdapter)obj).getNode());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return f_node.hashCode();
	}
	
	@Override
	public Point getPos() {
		return f_node.getPos();
	}

	@Override
	public Dimension getSize() {
		return f_node.getBoundingBox().getSize();
	}

	@Override
	public String getText() {
		return f_node.getText();
	}

	@Override
	public void setPos(int x, int y) {
		f_node.setProperty(ProcessNode.PROP_XPOS, ""+x);
		f_node.setProperty(ProcessNode.PROP_YPOS, ""+y);
	}
	
	public ProcessNode getNode() {
		return f_node;
	}
	
	@Override
	public String toString() {
		return f_node.toString();
	}

	@Override
	public EdgeInterface getDockedTo() {
		if(getNode() instanceof EdgeDocker) {
			return new ProcessEdgeAdapter(((EdgeDocker)getNode()).getDockedEdge());
		}
		return null;
	}

	@Override
	public boolean isVirtualNode() {
		return getNode() instanceof EdgeDocker;
	}

	@Override
	public int getPaddingX() {
		return 0;
	}

	@Override
	public int getPaddingY() {
		return 0;
	}

}
