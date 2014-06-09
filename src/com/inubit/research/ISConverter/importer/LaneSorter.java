/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.util.Comparator;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Lane;

/**
 * @author ff
 *
 */
public class LaneSorter implements Comparator<Lane> {
	
	/**
	 * compares processnodes based on their y Position
	 */
	@Override
	public int compare(Lane o1, Lane o2) {
		if(o1.isVertical()) {
			int x1 = Integer.parseInt(o1.getProperty(ProcessNode.PROP_XPOS));
			int x2 = Integer.parseInt(o2.getProperty(ProcessNode.PROP_XPOS));
			return x1-x2;			
		}else {
			int y1 = Integer.parseInt(o1.getProperty(ProcessNode.PROP_YPOS));
			int y2 = Integer.parseInt(o2.getProperty(ProcessNode.PROP_YPOS));
			return y1-y2;
		}
	}

}
