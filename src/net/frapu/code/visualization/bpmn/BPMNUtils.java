/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.RoutingPointLayouter;
import net.frapu.code.visualization.layouter.SimpleBPDLayouter;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.freeSpace.FreeSpaceLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

/**
 * 
 * This class provides methods to support BPMN 2.0 compliant elements.
 *
 * @author frank
 */
public class BPMNUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
      
    	// @todo: Check more exceptional cases
    	if(source == null || target == null) {
    		return null;
    	}
        // Check if the two nodes are already connecte
        for (ProcessModel m1: source.getContexts()) {
                // Find the same context
                if (target.getContexts().contains(m1)) {
                    for (ProcessEdge e: m1.getEdges()) {
                        if (e.getSource()==source && e.getTarget()==target) return null;
                    }
                }
        }

        // StickyNote <--> Somewhere
        if (source instanceof StickyNote | target instanceof StickyNote) {
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // UserArtifact <--> Somewhere
        if (source instanceof UserArtifact | target instanceof UserArtifact) {
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // DataObject|Message --> EdgeDocker
        if ((source instanceof DataObject | source instanceof Message) && target instanceof EdgeDocker) {
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // Compensation Activity
        if (source instanceof CompensationIntermediateEvent && (target instanceof Task | target instanceof SubProcess) ) {
            // Check if catching
            if (source.getProperty(CompensationIntermediateEvent.PROP_EVENT_SUBTYPE).toLowerCase().equals(CompensationIntermediateEvent.EVENT_SUBTYPE_CATCHING.toLowerCase())) {
                Association a = new Association(source, target);
                target.setProperty(Task.PROP_COMPENSATION, Activity.TRUE);
                return a;
            }
        }

        // TextAnnotation
        if (source instanceof TextAnnotation | target instanceof TextAnnotation) {
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // Message
        if (source instanceof Message | target instanceof Message) {
            Association a = new Association(source, target);
            a.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
            return a;
        }

        // Data Object
        if (source instanceof DataObject | target instanceof DataObject) {
            return new Association(source, target);
        }

        // Data Store
        if (source instanceof DataStore | target instanceof DataStore) {
            return new Association(source, target);
        }

        // Conversation
        if (source instanceof Conversation | target instanceof Conversation) {
            return new ConversationLink(source, target);
        }

         // ChoreographySubProcesses -> FlowObject
        if (source instanceof ChoreographySubProcess && target instanceof FlowObject) {
            return new SequenceFlow(source, target);
        }

        // FlowObject --> ChoreographySubProcesses
        if (source instanceof FlowObject && target instanceof ChoreographySubProcess) {
            return new SequenceFlow(source, target);
        }

        // Pool -> ChoreographySubProcess
        if (source instanceof Pool && target instanceof ChoreographySubProcess) {
            return new MessageFlow(source, target);
        }

        // ChoreographySubProcess -> Pool
        if (source instanceof ChoreographySubProcess && target instanceof Pool) {
            return new MessageFlow(source, target);
        }

        //ChoreographyActivity / ChoreographySubProcess --> EdgeDocker at MessageFlow
        if ((source instanceof ChoreographyActivity ||
                source instanceof ChoreographySubProcess) &&
                (target instanceof EdgeDocker &&
                (((EdgeDocker)target).getDockedEdge()) instanceof MessageFlow)) {
            Association defaultEdge = new Association(source, target);
            defaultEdge.setProperty(Association.PROP_DIRECTION,
                    Association.DIRECTION_NONE);
            return defaultEdge;
        }

        // Task/SubProcess/MessageIntermediateEvent/MessageEndEvent -> Pool
        if ((source instanceof Task |
             source instanceof SubProcess |
             source instanceof MessageIntermediateEvent |
             source instanceof MessageEndEvent)
                && target instanceof Pool) {
            // Check if source has a different Pool than Pool
            for (ProcessModel m1: source.getContexts()) {
                if (((BPMNModel)m1).getPoolForNode(source) != target)
                    return new MessageFlow(source, target);
            }
        }

        // Pool -> Task/SubProcess/MessageIntermediateEvent/MessageEndEvent
        if (source instanceof Pool && 
            (target instanceof Task |
             target instanceof SubProcess |
             target instanceof MessageIntermediateEvent |
             target instanceof MessageStartEvent)) {
            // Check if target has a different Pool than Pool
            for (ProcessModel m1: target.getContexts()) {
                if (((BPMNModel)m1).getPoolForNode(target) != source)
                    return new MessageFlow(source, target);
            }
        }

        // Pool -> Pool
        if (source instanceof Pool && target instanceof Pool) {
            return new MessageFlow(source, target);
        }

        // FlowObject/SubProcess -> FlowObject/SubProcess (inside same Cluster)
        if ((source instanceof FlowObject |
                source instanceof SubProcess) &&
                (target instanceof FlowObject |
                target instanceof SubProcess)) {
            // Check if context is the same
            for (ProcessModel m1: source.getContexts()) {
                // Find the same context
                if (target.getContexts().contains(m1)) {
                    // Find cluster of source/target
                    Cluster c1 = m1.getClusterForNode(source);
                    while(c1 instanceof Lane) { // going up in the hierarchy until we find the Pool
                    	c1 = m1.getClusterForNode(c1);
                    }
                    Cluster c2 = m1.getClusterForNode(target);
                    while(c2 instanceof Lane) { // going up in the hierarchy until we find the Pool
                    	c2 = m1.getClusterForNode(c2);
                    }
                    if (c1==c2) return new SequenceFlow(source, target);
                }
            }           
        }

        // Task/SubProcess/MessageIntermediateEvent/MessageEndEvent ->
        // Task/SubProcess/MessageStartEvent/MessageIntermediateEvent (inside different Pools)
        if ((source instanceof Task |
                source instanceof SubProcess |
                source instanceof MessageIntermediateEvent |
                source instanceof MessageEndEvent)
                && ( target instanceof Task |
                target instanceof SubProcess |
                target instanceof MessageStartEvent |
                target instanceof MessageIntermediateEvent)) {
            // Check if context is the same
            for (ProcessModel m1: source.getContexts()) {
                // Find the same context
                if (target.getContexts().contains(m1)) {
                    // Find Pool of source/target
                    Pool c1 = ((BPMNModel)m1).getPoolForNode(source);
                    Pool c2 = ((BPMNModel)m1).getPoolForNode(target);
                    if (c1!=c2) return new MessageFlow(source, target);
                }
            }
        }

        return null;
    }

    @Override
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        if (node instanceof Task |
            node instanceof SubProcess |
            node instanceof CallActivity) {
            result.add(Task.class);
            result.add(IntermediateEvent.class);
            result.add(EndEvent.class);
            result.add(Gateway.class);
            result.add(DataObject.class);
            result.add(TextAnnotation.class);
        }
        if (node instanceof StartEvent) {
            result.add(Task.class);            
            result.add(Gateway.class);
            result.add(ChoreographyTask.class);
            result.add(TextAnnotation.class);
        }
        if (node instanceof IntermediateEvent) {
            result.add(Task.class);
            result.add(EndEvent.class);
            result.add(Gateway.class);
            result.add(TextAnnotation.class);
        }
        if (node instanceof EndEvent) {
            result.add(TextAnnotation.class);
        }
        if (node instanceof Gateway) {
            // Look up predecessors (if Choreography)
            boolean isChoreography = false;
            for (ProcessNode cn: model.getPredecessors(node)) {
                // Check if any direct Predecessor is a Choreography Activity
                if (cn instanceof ChoreographyTask | cn instanceof ChoreographySubProcess) {
                    isChoreography = true;
                }
            }
            if (isChoreography) {
                result.add(ChoreographyTask.class);
            } else {
                result.add(Task.class);
                }
            result.add(IntermediateEvent.class);
            result.add(EndEvent.class);
            result.add(TextAnnotation.class);
        }
        if (node instanceof MessageEndEvent) {
            result.add(Message.class);
        }
        if (node instanceof ChoreographyTask) {
            result.add(EndEvent.class);
            result.add(Gateway.class);
            result.add(ChoreographyTask.class);
            result.add(Message.class);
        }
        if (node instanceof Pool) {
            result.add(Conversation.class);
        }
        if (node instanceof Conversation) {
            result.add(Pool.class);
        }

        return result;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new GridLayouter(Configuration.getProperties()));
            layouters.add(new SimpleBPDLayouter());          
            layouters.add(new SugiyamaLayoutAlgorithm(Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
            layouters.add(new FreeSpaceLayouter());
        }
        return layouters;
    }

    @Override
    public RoutingPointLayouter getRoutingPointLayouter() {
        if (rpLayouter==null) rpLayouter = new BPMNRoutingPointLayouter();
        return rpLayouter;
    }


}
