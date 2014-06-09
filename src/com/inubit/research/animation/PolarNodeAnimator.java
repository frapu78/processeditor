/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.awt.Point;

import com.inubit.research.layouter.radial.PolarCoordinates;

import net.frapu.code.visualization.ProcessNode;

/**
 * Wraps a ProcessNode so it can be animated using polar coordinates
 * 
 * @author ff
 *
 */
public class PolarNodeAnimator extends DefaultNodeAnimator{
	
	private ProcessNode f_node;
	
	private PolarCoordinates f_pCoords;
	private PolarCoordinates f_newCoords;
	private PolarCoordinates f_difference;
	
	private Point f_center = new Point(0,0);
	
	/**
	 * 
	 */
	public PolarNodeAnimator(ProcessNode node,Animator animator) {
		super(node,animator);
		f_node = node;
		f_pCoords = new PolarCoordinates(node.getPos().x,node.getPos().y,f_center);
		f_newCoords = new PolarCoordinates(f_pCoords);
		f_difference = new PolarCoordinates(0, 0, f_center);
	}
	
	
	public void setCenter(Point center) {
		f_center = center;
		f_pCoords.setCenter(f_center);
		f_newCoords.setCenter(f_center);
	}
	
	@Override
	public void setNewCoords(Point p) {
		f_pCoords = f_newCoords;
		f_newCoords = new PolarCoordinates(p.x,p.y,f_center);
		f_difference = new PolarCoordinates(f_newCoords);
		f_difference.subtract(f_pCoords);
		resetCurrentTick();
	}
	
	/**
	 * @param value
	 */
	@Override
	protected void setNewValues() {
		PolarCoordinates _pos = new PolarCoordinates(f_pCoords);
		PolarCoordinates _inc = new PolarCoordinates(f_difference);
		_inc.mult(getHyperbolicProgress());
		_pos.add(_inc);
		setNodePosition(_pos);
	}

	/**
	 * @param pos
	 */
	private void setNodePosition(PolarCoordinates pos) {
		f_node.setPos(pos.getX(), pos.getY());
	}
	
}
