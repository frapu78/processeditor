/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;


/**
 * @author ff
 *
 */
public class StoryboardUtils extends ProcessUtils {

    public final static Font defaultFont = new Font("Comic Sans MS",Font.BOLD, 14);

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        if ((source instanceof Action | source instanceof Scene | source instanceof Rule | source instanceof Service) &&
                (target instanceof Action | target instanceof Scene | target instanceof Rule | target instanceof Service)) {
            return new Sequence(source, target);
        }
        if ((source instanceof Action | source instanceof Scene) && target instanceof Stop) {
            return new Sequence(source, target);
        }

        return new Association(source,target);
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        layouters = new ArrayList<ProcessLayouter>();
        layouters.add(new GridLayouter(Configuration.getProperties()));
        return layouters;
    }

       @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        if (node instanceof Action) {
            result.add(Action.class);
            result.add(Rule.class);
            result.add(Stop.class);
            result.add(Scene.class);
            result.add(Service.class);
        }
        if (node instanceof Scene ) {
            result.add(Scene.class);
            result.add(Service.class);
            result.add(Actor.class);
            result.add(BusinessObject.class);
        }
        if (node instanceof Rule) {
            result.add(Action.class);
            result.add(Stop.class);
            result.add(Scene.class);
            result.add(Service.class);
        }
        if (node instanceof Service) {
            result.add(Action.class);
            result.add(Rule.class);
            result.add(Stop.class);
            result.add(Scene.class);
        }

        return result;
    }
}
