/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.radial;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * @author ff
 *
 */
public class PolarCoordinates {
	
	private double f_distance;
	private double f_angle;
	private Point f_center;
	
	private int f_x;
	private int f_y;
	
	/**
	 * 
	 */
	public PolarCoordinates(PolarCoordinates original) {
		f_center = original.f_center;
		f_distance = original.f_distance;
		f_angle = original.f_angle;
		f_x = original.f_x;
		f_y = original.f_y;
	}
	
	/**
	 * 
	 */
	public PolarCoordinates(int x, int y,Point center) {
		f_center = center;
		f_x = x;
		f_y = y;
		calcCoords();
	}
	
	/**
	 * 
	 */
	public PolarCoordinates(double distance, double angle,Point center) {
		f_center = center;
		f_distance = distance;
		f_angle = angle;
		f_x = getX();
		f_y = getY();
	}
	
	/**
	 * 
	 */
	private void calcCoords() {
		f_distance = Point2D.distance(f_center.x, f_center.y, 
				f_x, f_y);
		f_angle = Math.atan2(f_x-f_center.x, f_y-f_center.y);
	}
	
	public double getDistance() {
		return f_distance;
	}
	
	public double getAngle() {
		return f_angle;
	}
	
	public double getAngleDeg() {
		return Math.toDegrees(f_angle);
	}
	
	public int getX() {
		return (int) (f_distance * Math.sin(f_angle)) + f_center.x;
	}
	
	public int getY() {
		return (int) (f_distance * Math.cos(f_angle)) + f_center.y;
	}

	/**
	 * does not change the x or y coordinates, but recalculates the polar coordinates
	 * relative to the given Center.
	 * @param center
	 */
	public void setCenter(Point center) {
		f_center = center;
		calcCoords();
	}
	
	public void setDistance(double radius) {
		f_distance = radius;
	}
	
	/**
	 * sets the angle in radians
	 * @param angle
	 */
	public void setAngle(double angle) {
		f_angle = angle;
	}
	
	/**
	 * sets teh angle in degrees
	 * @param angle
	 */
	public void setAngleDeg(double angle) {
		setAngle(Math.toRadians(angle));
	}
	
	public void add(PolarCoordinates po2) {
		this.f_angle += po2.f_angle;
		checkAngle();
		this.f_distance = this.f_distance + po2.f_distance;
	}
	
	public void subtract(PolarCoordinates po2) {
		this.f_angle -= po2.f_angle;
		checkAngle();
		this.f_distance = this.f_distance - po2.f_distance;
	}
	
	/**
	 * 
	 */
	private void checkAngle() {
		if(f_angle < -Math.PI) {
			f_angle += 2*Math.PI;
		}else if(f_angle > Math.PI) {
			f_angle -= 2*Math.PI;
		}
	}

	public void mult(double scale) {
		this.f_angle *= scale;
		this.f_distance *= scale; 
	}
	
	@Override
	public String toString() {
		return "PolarCoordinates (r="+f_distance+",a="+getAngleDeg()+")";
	}

	

}
