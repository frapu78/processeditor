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

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * This class provides a model for a XForms 1.1 compliant form.
 *
 * @author fpu
 */
public class StoryboardModel extends ProcessModel {

    public StoryboardModel() {
        this(null);
    }

    public StoryboardModel(String name) {
        super(name);
        processUtils = new StoryboardUtils();
    }

    @Override
    public String getDescription() {
        return "Storyboard (UI)";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Actor.class);
        result.add(Scene.class);
        result.add(Action.class);
        result.add(Rule.class);
        result.add(Service.class);
        //result.add(Timer.class);
        result.add(Stop.class);
        result.add(BusinessObject.class);
        //result.add(Image.class);
        //result.add(Comment.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Sequence.class);
        result.add(Association.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (Storyboard (UI) Diagram)";
    }

}
