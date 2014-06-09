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
import java.util.List;

import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * provides a dummy edge docker which will be added to the model and treated like a normal 
 * edgeDocker. This is needed for models like the IS' one where the target of an edge can be an 
 * edge.
 * @author ff
 *
 */
public class DummyEdgeDocker implements BPMNNodeInterface{

	private EdgeInterface f_docked;

	public DummyEdgeDocker(EdgeInterface dockedTo) {	
		f_docked = dockedTo;
	}
	
	@Override
	public List<NodeInterface> getContainedNodes() {
		return null;
	}

	@Override
	public EdgeInterface getDockedTo() {
		return f_docked;
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
		return false;
	}

	@Override
	public boolean isVirtualNode() {
		return true;
	}

	@Override
	public boolean placeDataObjectUpwards() {
		return false;
	}


	@Override
	public void setSize(int i, int j) {
	}

	@Override
	public Point getPos() {
		return new Point();
	}

	@Override
	public Dimension getSize() {
		return new Dimension();
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public void setPos(int x, int y) {
	}

	@Override
	public void setAttatchedTo(AbstractModelAdapter model, NodeInterface node) {
	}

	@Override
	public int getPaddingX() {
		return 0;
	}

	@Override
	public int getPaddingY() {
		return 0;
	}

	@Override
	public boolean isVertical() {
		return false;
	}
}
