/**
 *
 * Process Editor - Gantt Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.gantt;

import com.inubit.research.layouter.ProcessLayouter;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class GanttUtils extends ProcessUtils {

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return null;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        return null;
    }

}
