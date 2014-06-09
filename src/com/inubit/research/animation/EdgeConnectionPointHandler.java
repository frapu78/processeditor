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
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;

/**
 * @author ff
 *
 */
public class EdgeConnectionPointHandler {
	
	private boolean f_workToDo = true;
	private boolean f_source; //working on source or target point
	private boolean f_deleteAtEnd = false;
	
	private Point f_start;
	private Point f_end;
	private Point f_diff;
	
	private ProcessEdge f_edge;

	/**
	 * @param sourceDockPointOffset
	 * @param sourceDockPointOffset2
	 */
	public EdgeConnectionPointHandler(Point start,	Point end, ProcessEdge original, ProcessEdge newEdge, boolean source) {
		f_edge = original;
		f_source = source;
		if(start == null && end == null) {
			f_workToDo = false;
		}else {
			List<Point> _rps = f_edge.getRoutingPoints();
			if(start == null) {			
				if(f_source) {
					f_start = _rps.get(0);
					f_start.x -= f_edge.getSource().getPos().x;
					f_start.y -= f_edge.getSource().getPos().y;
				}
				else {
					f_start = _rps.get(_rps.size()-1);	
					f_start.x -= f_edge.getTarget().getPos().x;
					f_start.y -= f_edge.getTarget().getPos().y;
				}
			}else {
				f_start = start;
			}
			_rps = newEdge.getRoutingPoints();
			if(end == null) {	
				f_deleteAtEnd = true;
				if(f_source) {
					f_end = _rps.get(0);
					f_end.x -= newEdge.getSource().getPos().x;
					f_end.y -= newEdge.getSource().getPos().y;
				}
				else {
					f_end = _rps.get(_rps.size()-1);		
					f_end.x -= newEdge.getTarget().getPos().x;
					f_end.y -= newEdge.getTarget().getPos().y;
				}	
			}else {
				f_end = end;
			}
			
			f_diff = new Point(f_end);
			f_diff.translate(-f_start.x, -f_start.y);
		}
	}

	/**
	 * @param hyperbolicProgress
	 */
	public void setValue(double progress) {
		if(f_workToDo) {
			Point offset = new Point(f_start);
			offset.x += progress * f_diff.x;
			offset.y += progress * f_diff.y;
			if(f_source)
				f_edge.setSourceDockPointOffset(offset);
			else
				f_edge.setTargetDockPointOffset(offset);
		}
	}

	/**
	 * 
	 */
	public void setLastValue() {
		if(f_workToDo) {
			if(f_source)
				if(f_deleteAtEnd)
					f_edge.clearSourceDockPointOffset();
				else
					f_edge.setSourceDockPointOffset(f_end);
			else
				if(f_deleteAtEnd)
					f_edge.clearTargetDockPointOffset();
				else
					f_edge.setTargetDockPointOffset(f_end);
		}
	}

}
