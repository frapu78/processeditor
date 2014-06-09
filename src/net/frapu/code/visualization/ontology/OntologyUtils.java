/**
 *
 * Process Editor - Ontology Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.ontology;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.radial.RadialLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

/**
 *
 * @author frank
 */
public class OntologyUtils extends ProcessUtils {

	private ArrayList<ProcessLayouter> layouters = null;

	@Override
	public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
		return new ContainmentEdge(source,target);
	}

	@Override
	public List<ProcessLayouter> getLayouters() {
		if(layouters == null) {
			layouters = new ArrayList<ProcessLayouter>();
                        layouters.add(new RadialLayouter(Configuration.getInstance()));
			layouters.add(new SugiyamaLayoutAlgorithm(Configuration.getProperties()));
			
		}
		return layouters;
	}

}
