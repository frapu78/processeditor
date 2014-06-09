/**
 *
 * Process Editor - Archimate Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.archimate;

import com.inubit.research.layouter.ProcessLayouter;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * Implements a Utils class for the Archimate specification.
 *
 * @see http://www.opengroup.org/archimate
 * @author fpu
 */
public class ArchimateUtils extends ProcessUtils {

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return new Association(source, target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        return null;
    }

}
