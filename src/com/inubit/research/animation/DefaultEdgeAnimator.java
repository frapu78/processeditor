/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;

/**
 * @author ff
 *
 */
public class DefaultEdgeAnimator extends DefaultAlphaAnimator{

	private List<Point> f_starts;
	private List<Point> f_targets;
	private List<Point> f_realTargets;
	private List<Point> f_distances;
	
	private EdgeConnectionPointHandler f_sourceHandler;
	private EdgeConnectionPointHandler f_targetHandler;
		
	private ProcessEdge f_newEdge;


	//for color
	private Color f_color;
	private Color f_newColor;

	//color differences, cannot be saved to a color object as negative values are needed
	private int f_diffRed = 0;
	private int f_diffGreen = 0;
	private int f_diffBlue = 0;
	
	/**
	 * 
	 */
	public DefaultEdgeAnimator(ProcessEdge edge,Animator animator) {
		super(edge,animator);
		
		
	}

        public void setNewColor(Color c) {
		f_newColor = c;
	}
	
	public ProcessEdge getEdge() {
		return (ProcessEdge) getProcessObject();
	}
	
	public void transformTo(ProcessEdge newEdge) {
		resetCurrentTick();
		f_newEdge = newEdge;		
	}
	
	/**
	 * 
	 */
	private void determineDistances() {
		f_distances = new ArrayList<Point>(f_starts.size());
		for(int i=0;i<f_starts.size();i++) {
			f_distances.add(getDistance(f_starts.get(i),f_targets.get(i)));
		}
	}

	/**
	 * @param point
	 * @param point2
	 * @return
	 */
	private Point getDistance(Point p, Point p2) {
		return new Point(p2.x-p.x,p2.y-p.y);
	}

	/**
	 * @param f_starts2
	 * @param f_targets2
	 * @param b
	 */
	private void buildMoreRoutingPoints(ProcessEdge addTo,List<Point> starts,List<Point> targets) {
		int idx = Math.round(starts.size()/2);
		Point _dupMe = starts.get(idx);
		for(int i=starts.size();i<targets.size();i++) {
			addTo.addRoutingPoint(idx, _dupMe);
			starts.add(idx,_dupMe);
		}
	}

	/**
	 * @return
	 */
	private List<Point> getNewRoutingPoints() {
		double _perc = getHyperbolicProgress();
		ArrayList<Point> _result = new ArrayList<Point>();
		Point s,diff;
		for(int i=0;i<f_starts.size();i++) {
			s = f_starts.get(i);
			diff = f_distances.get(i);
			_result.add(new Point((int)(s.x+(diff.x * _perc)),(int)(s.y+(diff.y*_perc))));
		}
		return _result;
	}

	@Override
	protected void setNewValues() {
                super.setNewValues();
                double _percentage = getHyperbolicProgress();
		if(getCurrentTick() < getSteps()) {
			List<Point> _newRoutingPoints = getNewRoutingPoints();
			getEdge().clearRoutingPoints();
			f_sourceHandler.setValue(getHyperbolicProgress());
			f_targetHandler.setValue(getHyperbolicProgress());
			for(int i = 0;i<_newRoutingPoints.size();i++) {
				getEdge().addRoutingPoint(i,_newRoutingPoints.get(i));
			}

                        if(f_newColor != null) {
                        int _r = (int) (f_color.getRed() + f_diffRed*_percentage);
                        int _g = (int) (f_color.getGreen() + f_diffGreen*_percentage);
                        int _b = (int) (f_color.getBlue() + f_diffBlue*_percentage);
                        getEdge().setColor(new Color(_r,_g,_b));

			}
		}else {
			getEdge().clearRoutingPoints();
			f_sourceHandler.setLastValue();
			f_targetHandler.setLastValue();
			for(int i = 0;i<f_realTargets.size();i++) {
				getEdge().addRoutingPoint(i,f_targets.get(i));
			}
                        if(f_newColor != null) getEdge().setColor(f_newColor);
		}
	}

	@Override
	protected void firstStep() {
                super.firstStep();
		f_starts = getEdge().getRoutingPoints();
		f_starts.remove(f_starts.size()-1);
		f_starts.remove(0);
		
		f_targets = f_newEdge.getRoutingPoints();
		f_targets.remove(f_targets.size()-1);
		f_targets.remove(0);
		
		f_realTargets = new ArrayList<Point>(f_targets);
		
		f_sourceHandler = new EdgeConnectionPointHandler(getEdge().getSourceDockPointOffset(),
				f_newEdge.getSourceDockPointOffset(),getEdge(),f_newEdge,true);
		f_targetHandler = new EdgeConnectionPointHandler(getEdge().getTargetDockPointOffset(),
				f_newEdge.getTargetDockPointOffset(),getEdge(),f_newEdge,false);
		
                if(f_newColor != null) {
                    f_color = getEdge().getColor();
                    if (f_newColor==null) f_newColor = new Color(f_color.getRGB());
                    f_diffRed = f_newColor.getRed() - f_color.getRed();
                    f_diffGreen = f_newColor.getGreen() - f_color.getGreen();
                    f_diffBlue = f_newColor.getBlue() - f_color.getBlue();
		}

		if(f_starts.size() != f_targets.size()) {
			if(f_starts.size() < f_targets.size()) {
				//adding a first point in the center of the node
				if(f_starts.size() == 0) {
					Point _p1 = getEdge().getSource().getPos();
					Point _p2 = getEdge().getTarget().getPos();
					_p1.x += (_p2.x-_p1.x)/2;
					_p1.y += (_p2.y-_p1.y)/2;
					f_starts.add(_p1);
				}
				buildMoreRoutingPoints(getEdge(),f_starts,f_targets);
			}else {
				//adding a first point in the center of the node
				if(f_targets.size() == 0) {
					Point _p1 = f_newEdge.getSource().getPos();
					Point _p2 = f_newEdge.getTarget().getPos();
					
					int _offset = f_newEdge.getTarget().getSize().width/2;
					_p2.x += _p2.x < _p1.x ? _offset : -_offset;
					
					f_targets.add(_p2);
				}
				buildMoreRoutingPoints(f_newEdge,f_targets,f_starts);
			}
		}
		determineDistances();
	}

}
