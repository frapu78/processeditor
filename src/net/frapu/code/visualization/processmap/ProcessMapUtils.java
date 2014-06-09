/**
 *
 * Process Editor - Process Map Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.processmap;

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
public class ProcessMapUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        // Process Owners are attached via Associations
        if (source instanceof ProcessOwner | target instanceof ProcessOwner)
            return new Association(source, target);

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

        if (node instanceof ProcessGroup) {
            result.add(Process.class);
            result.add(ProcessGroup.class);
            result.add(ProcessOwner.class);
        }

        if (node instanceof Process) {
            result.add(Process.class);
            result.add(ProcessOwner.class);
        }

        return result;
    }
}
