/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.adapter;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.PetriNetModel;

import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.PetriNetModelInterface;

/**
 * @author ff
 *
 */
public class PetriNetModelAdapter extends ProcessModelAdapter implements PetriNetModelInterface {

	/**
	 * @param p
	 */
	public PetriNetModelAdapter(PetriNetModel p) {
		super(p);
	}
	
	
	@Override
	public List<NodeInterface> getNodes() {
		ArrayList<NodeInterface> _list = new ArrayList<NodeInterface>();
		for(ProcessNode e : getModel().getNodes()) {
			_list.add(new PetriNetNodeAdapter(e));
		}
		return _list;
	}

}
