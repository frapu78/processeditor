/**
 *
 * Process Editor - EPK Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.epk;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class EPKUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return new Connection(source, target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new SugiyamaLayoutAlgorithm(true,Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
        }
        return layouters;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();

        if (node instanceof Event) {
            result.add(Function.class);
            result.add(ANDConnector.class);
        }
        if (node instanceof Function) {
            result.add(Event.class);
            result.add(XORConnector.class);
            result.add(ORConnector.class);
        }
        if (node instanceof XORConnector ||
                node instanceof ORConnector) {
            result.add(Event.class);
        }
        if (node instanceof ANDConnector) {
            result.add(Function.class);
        }

        return result;
    }
}
