/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Point;

/**
 * Used for A* algorithm based message flow routing
 * @author ff
 *
 */
public class ANode {

	private Point f_point;
	private int f_g;
	private int f_h;
	private ANode f_cameFrom;
	private Point f_target;
	
	/**
	 * @param _start
	 * @param i
	 * @param _end
	 */
	public ANode(Point p, Point target,ANode cameFrom) {
		f_point = p;
		this.f_cameFrom = cameFrom;
		f_target = target;
		int _dx = p.x - target.x;
		int _dy = p.y - target.y;
		f_h = (int) Math.sqrt(_dx*_dx+_dy*_dy); //our heuristic
	}

	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ANode) {
			ANode a = (ANode) obj;
			return a.getPoint().x == this.getPoint().x && a.getPoint().y == this.getPoint().y;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return f_point.x * 1000000 + f_point.y;
	}
	/**
	 * @return the point
	 */
	public Point getPoint() {
		return f_point;
	}
	
	public int getF() {
		return getG() + getH();
	}

	/**
	 * @return the g
	 */
	public int getG() {
		return f_g;
	}

	/**
	 * @return the h
	 */
	public int getH() {
		return f_h;
	}
	
	public Point getTarget() {
		return f_target;
	}

	public ANode getCameFrom() {
		return f_cameFrom;
	}
	
	/**
	 * @param point the point to set
	 */
	public void setPoint(Point point) {
		this.f_point = point;
	}

	/**
	 * @param g the g to set
	 */
	public void setG(int g) {
		this.f_g = g;
	}

	/**
	 * @param h the h to set
	 */
	public void setH(int h) {
		this.f_h = h;
	}

	public void setCameFrom(ANode cameFrom) {
		this.f_cameFrom = cameFrom;
	}

	@Override
	public String toString() {
		return "ANode ("+f_point.x+";"+f_point.y+")-"+getG();
	}
	

}
