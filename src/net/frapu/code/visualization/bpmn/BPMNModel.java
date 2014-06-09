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

import net.frapu.code.visualization.*;

import java.util.*;

/**
 * This class provides a data model for a BPMN process used within ProcessEditor.
 * 
 * @author fpu
 */
public class BPMNModel extends ProcessModel {

    private AttachedNodeHandler attachedNodeHandler = new AttachedIntermediateEventHandler();

    public BPMNModel() {
        super();
        processUtils = new BPMNUtils();
    }

    /** 
     * Creates a new BPMN ProcessEditorModel with a name.
     * @param name
     */
    public BPMNModel(String name) {
        super(name);
        processUtils = new BPMNUtils();
    }

    public String getDescription() {
        return "BPMN 2.0";
    }

    @Override
    public AttachedNodeHandler getAttachedNodeHandler() {
        return attachedNodeHandler;
    }

    public void addFlowObject(FlowObject o) {
        super.addNode(o);
    }

    public void addFlow(ProcessEdge e) {
        addEdge(e);
    }

    public List<ProcessEdge> getFlows() {
        return getEdges();
    }

    public List<FlowObject> getFlowObjects() {
        // Figure out all flow objects
        List<FlowObject> result = new LinkedList<FlowObject>();
        for (ProcessNode n : super.getNodes()) {
            if (n instanceof FlowObject) {
                result.add((FlowObject) n);
            }
        }

        return result;
    }

    public List<SequenceFlow> getSequenceFlows() {
        List<SequenceFlow> result = new LinkedList<SequenceFlow>();
        for (ProcessEdge f : super.getEdges()) {
            if (f instanceof SequenceFlow) {
                result.add((SequenceFlow) f);
            }
        }
        return result;
    }

    public LinkedList<Association> getAssociations() {
        LinkedList<Association> result = new LinkedList<Association>();
        for (ProcessEdge f : super.getEdges()) {
            if (f instanceof Association) {
                result.add((Association) f);
            }
        }
        return result;
    }

    /**
     * Detects the Pool where the ProcessNode is contained inside. Returns
     * <b>null</b> if not in any Pool.
     * @param node
     * @return
     */
    public Pool getPoolForNode(ProcessNode node) {
        // Get Cluster
        Cluster c = getClusterForNode(node);
        if (c==null) return null; // Not in any Cluster
        while (c!=null) {
            if (c instanceof Pool) return (Pool)c;
            c = getClusterForNode(c);
        }
        return null;
    }

    @Override
    public String toString() {
        if (getProcessName() == null) {
            return super.toString();
        }
        return getProcessName() + " (BPMN)";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Activity.class);
        result.add(StartEvent.class);
        result.add(IntermediateEvent.class);
        result.add(EndEvent.class);
        result.add(Gateway.class);
        result.add(Artifact.class);
        result.add(Pool.class);
        result.add(ChoreographyActivity.class);
        result.add(Conversation.class);
        result.add(Message.class);        
        //result.add(StickyNote.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(SequenceFlow.class);
        result.add(MessageFlow.class);
        result.add(Association.class);
        result.add(ConversationLink.class);
        return result;
    }
    
    @Override
    public void removeNode(ProcessNode node) {
    	super.removeNode(node);
    	if(node instanceof LaneableCluster) {
    		LaneableCluster _lc = (LaneableCluster) node;
        	for(Lane l:_lc.getLanes()) {
        		this.removeNode(l);
        	}
        }
    }
}
