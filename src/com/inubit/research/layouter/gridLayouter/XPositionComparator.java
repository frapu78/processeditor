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
import java.util.Comparator;

import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.sugiyama.NodeWrapper;

/**
 * @author ff
 *
 */
public class XPositionComparator implements Comparator<Object> {

	private boolean f_asc;

	/**
	 * 
	 */
	public XPositionComparator() {
		this(true);
	}
	
	
	/**
	 * @param b
	 */
	public XPositionComparator(boolean b) {
		f_asc = b;
	}


	@Override
	public int compare(Object o1, Object o2) {
		if((o1 == null) || (o2 == null)) {
			return 0;
		}
		if(!f_asc) {
			//sorting in descending order by switching the nodes!
			Object _temp = o1;
			o1 = o2;
			o2 = _temp;
		}
		if(o1 instanceof FlowObjectWrapper) {
			Point p1 = ((FlowObjectWrapper)o1).getWrappedObject().getPos();
			Point p2 = ((FlowObjectWrapper)o2).getWrappedObject().getPos();
			if(p1 == null || p2 == null){
				return 0;
			}			
			return comparePoints(p1,p2);
		}else if(o1 instanceof NodeInterface) {
			Point p1 = ((NodeInterface)o1).getPos();
			Point p2 = ((NodeInterface)o2).getPos();
			if(p1 == null || p2 == null){
				return 0;
			}
			
			return comparePoints(p1,p2);				
		}else if(o1 instanceof NodeWrapper) {
			Point p1 = ((NodeWrapper)o1).getPos();
			Point p2 = ((NodeWrapper)o2).getPos();
			if(p1 == null || p2 == null){
				return 0;
			}			
			return comparePoints(p1,p2);				
		}
		return 0;
	}


	/**
	 * @param p1
	 * @param p2
	 * @return
	 */
	private int comparePoints(Point p1, Point p2) {
		int val = p1.x - p2.x;
		if(val == 0) {
			return p1.y-p2.y;//second ordering criterion
		}
		return val;
	}
	
	
}
