/**
 *
 * Process Editor - XForms Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.xforms;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.xForms.XFormsLayouter;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;


/**
 * @author ff
 *
 */
public class XFormsUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return null;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        layouters = new ArrayList<ProcessLayouter>();
        layouters.add(new XFormsLayouter(Configuration.getProperties()));
        layouters.add(new GridLayouter(Configuration.getProperties()));
        return layouters;
    }
}
