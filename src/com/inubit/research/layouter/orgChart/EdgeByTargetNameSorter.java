/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.orgChart;

import java.util.Comparator;

import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class EdgeByTargetNameSorter implements Comparator<EdgeInterface> {

	@Override
	public int compare(EdgeInterface a, EdgeInterface b) {
		return ((NodeInterface)a.getTarget()).getText().compareToIgnoreCase(((NodeInterface)b.getTarget()).getText());
	}

}
