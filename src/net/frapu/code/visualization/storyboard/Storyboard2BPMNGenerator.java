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

import com.inubit.research.layouter.gridLayouter.GridLayouter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * This class generates a BPMNModel from a StoryboardModel.
 *
 * @author fpu
 */
public class Storyboard2BPMNGenerator {

    public final static BPMNModel convertToBPMN(StoryboardModel story) {
        // Clone Story
        StoryboardModel myStory = (StoryboardModel) story.clone();
        reduceStory(myStory);

        BPMNModel bpmn = new BPMNModel();
        bpmn.setProcessName("BPD for "+story.getProcessName());

        Pool pool = new Pool();
        bpmn.addNode(pool);

        Map<ProcessNode,Task> map = new HashMap<ProcessNode,Task>();

        // Create Lanes for all Actors
        for (ProcessNode node : myStory.getNodesByClass(Actor.class)) {
            Actor actor = (Actor)node;
            Lane lane = new Lane();
            lane.setText(actor.getText());
            pool.addProcessNode(lane);
            bpmn.addNode(lane);

            // Add scene
            for (ProcessNode n : myStory.getSucceedingNodes(Association.class, actor)) {
                if (n instanceof Scene) {
                    // Create Task
                    Task task = new Task();
                    task.setText(n.getText());
                    task.setStereotype(Task.TYPE_USER);
                    lane.addProcessNode(task);
                    bpmn.addNode(task);
                    // Put into map
                    map.put((Scene)n, task);
                }
            }
        }
        
        // Map Stop to EndEvent

        // Map Sequences from Scene to Scene
        for (ProcessEdge e: myStory.getEdges()) {
            if (e instanceof Sequence) {
                if (e.getSource() instanceof Scene && e.getTarget() instanceof Scene) {
                    SequenceFlow sf = new SequenceFlow();
                    sf.setSource(map.get(e.getSource()));
                    sf.setTarget(map.get(e.getTarget()));
                    bpmn.addEdge(sf);
                }
            }
        }

        GridLayouter layouter = new GridLayouter(Configuration.getProperties());
        try {
            layouter.layoutModel(ProcessUtils.getAdapter(bpmn));
            // ...
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        // ...
        //somehow the layout is not correctly transfered.
        //but this output shows that all coordinates are set correctly
        for(ProcessNode n:bpmn.getNodes()) {
        	System.out.println(n.getName() +" "+n.getPos());
        }
        return bpmn;
    }

    /**
     * Removes all actions from a story and re-connects the edges.
     *
     * @param story
     */
    public static void reduceStory(StoryboardModel story) {
        // Re-link edge from Actions
        for (ProcessNode node: story.getNodesByClass(Scene.class)) {
            Scene scene = (Scene)node;
            // Iterate over contained Actions
            for (ProcessNode n: scene.getProcessNodes()) {
                if (n instanceof Action) {
                    // Found contained Action
                    Action action = (Action)n;
                    // Check if Sequence from Action leaves Scene
                    for (ProcessEdge e: story.getOutgoingEdges(Sequence.class, action)) {
                        if (getParentCluster(story, e.getTarget())!=scene) {
                            // Going to different Scene, move Edge
                            e.setSource(scene);
                        }
                        // Check if target is Stop
                        if (e.getTarget() instanceof Stop) {
                            e.setSource(scene);
                        }
                    }
                    // Check if Sequence to Action leaves Scene
                    for (ProcessEdge e: story.getIncomingEdges(Sequence.class, action)) {
                        if (getParentCluster(story, e.getSource())!=scene) {
                            // Going to different Scene, move Edge
                            e.setTarget(scene);
                        }
                    }
                }
            }
        }
        // Remove duplicate edges
        Set<ProcessEdge> remList = new HashSet<ProcessEdge>();

        for (ProcessEdge e1: story.getEdges()) {
            for (ProcessEdge e2: story.getEdges()) {
                if (e1==e2) continue;
                if (remList.contains(e2)) continue;
                if (e1.getTarget()==e2.getTarget() && e1.getSource()==e2.getSource())
                    remList.add(e2);
            }
        }
        for (ProcessEdge e: remList) {
           story.removeEdge(e);
        }

    }

    private static Cluster getParentCluster(ProcessModel model, ProcessNode node) {
        // Iterate over all Cluster
        for (Cluster c: model.getClusters()) {
            for (ProcessNode n: c.getProcessNodes()) {
                if (n==node) return c;
            }
        }
        return null;
    }

}
