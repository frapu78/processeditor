/**
 *
 * Process Editor - Lifecycle Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.lifecycle;

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
 * @author fpu
 */
public class LifecycleUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return new Transition(source, target);
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

        if (node instanceof DataObject) {
            result.add(DataObject.class);
        }

        return result;
    }


}
