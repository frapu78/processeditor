/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.*;
import net.frapu.code.visualization.layouter.DOTLayouter;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;
import java.util.LinkedList;

/**
 *
 * Provides methods to draw Workflow nets
 * 
 * @author frank
 */
public class PetriNetUtils extends ProcessUtils {

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        // Check if comment
        if ((source instanceof Comment) | (target instanceof Comment)) {
            return new CommentEdge(source, target);
        }
        // Check if lane
        if ((source instanceof Lane) | (target instanceof Lane)) {
            // No connection allowed!
            return null;
        }

        // Check place->transition
        if ((source instanceof Place) && (target instanceof Transition)) {
            return new Edge(source, target);
        }

        // Check transition->place
        if ((source instanceof Transition) && (target instanceof Place)) {
            return new Edge(source, target);
        }

        return null;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        ArrayList<ProcessLayouter> list = new ArrayList<ProcessLayouter>();
        list.add(new GridLayouter(Configuration.getProperties()));
        list.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
        list.add(new SugiyamaLayoutAlgorithm(true,Configuration.getProperties()));        
        list.add(new DOTLayouter());
        return list;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        if (node instanceof Place) {
            result.add(Transition.class);
        }
        if (node instanceof Transition) {
            result.add(Place.class);
        }
        return result;
    }
}
