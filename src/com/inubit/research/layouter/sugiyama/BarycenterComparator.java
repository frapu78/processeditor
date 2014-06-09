/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.sugiyama;

import java.util.Comparator;


/**
 * @author ff
 *
 */
public class BarycenterComparator implements Comparator<NodeWrapper> {

	@Override
	public int compare(NodeWrapper o1, NodeWrapper o2) {
		if((o1 != null) && (o2 != null))
			return Float.compare(o1.getBaryCenter(), o2.getBaryCenter());
		return 0;
	}

}
