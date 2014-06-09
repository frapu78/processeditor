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

import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class NodeByNameSorter implements Comparator<NodeInterface> {

	@Override
	public int compare(NodeInterface a, NodeInterface b) {
		return a.getText().compareToIgnoreCase(b.getText());
	}

}
