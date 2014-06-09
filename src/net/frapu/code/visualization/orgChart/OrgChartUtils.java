/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.orgChart;

import java.util.ArrayList;
import java.util.List;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.orgChart.OrgChartLayouter;
import java.util.LinkedList;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class OrgChartUtils extends ProcessUtils {

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        return new Connection(source, target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        ArrayList<ProcessLayouter> _list = new ArrayList<ProcessLayouter>();
        _list.add(new OrgChartLayouter(Configuration.getProperties()));
        return _list;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(
            ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        if (node instanceof OrgUnit) {
            result.add(OrgUnit.class);
            result.add(ManagerialRole.class);
            result.add(Role.class);
        }
        if (node instanceof ManagerialRole | node instanceof Role) {
            result.add(Person.class);
            result.add(Substitute.class);
        }

        return result;
    }
}
