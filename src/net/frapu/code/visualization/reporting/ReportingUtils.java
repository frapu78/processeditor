/**
 *
 * Process Editor - Reporting Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.reporting;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class ReportingUtils extends ProcessUtils {

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return null;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
    	LinkedList<ProcessLayouter> _layouters = new LinkedList<ProcessLayouter>();
    	_layouters.add(new SugiyamaLayoutAlgorithm(Configuration.getProperties()));
    	return _layouters ;
    }

}
