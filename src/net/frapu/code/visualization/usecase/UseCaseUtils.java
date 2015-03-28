/**
 *
 * Process Editor - Use Case Package
 *
 * (C) 2015 the authors
 *
 * http://frapu.de
 *
 */
package net.frapu.code.visualization.usecase;

import com.inubit.research.layouter.ProcessLayouter;
import net.frapu.code.visualization.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Utils for UML Use Cases.
 *
 * @author fpu
 */
public class UseCaseUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        if ((source instanceof Actor) && (target instanceof UseCase)) {
            return new Association(source, target);
        }

        if ((source instanceof UseCase) && (target instanceof UseCase)) {
            return new Dependency(source, target);
        }

        return new Association(source, target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        return layouters;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();

        if (node instanceof Actor) {
            result.add(UseCase.class);
        }
        if (node instanceof UseCase) {
            result.add(UseCase.class);
        }

        return result;
    }

    @Override
    public boolean isPreferLayoutEdges() {
        return false;
    }
}
