/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.layouter;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.adapter.ProcessModelAdapter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.FlowObject;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 *
 * @author fpu
 */
public class SimpleBPDLayouter extends ProcessLayouter {

    private int distance = 40;

    public void layoutModel(AbstractModelAdapter model2, int xStart, int yStart, int direction) throws Exception {
    	ProcessModelAdapter adap = (ProcessModelAdapter) model2;
    	ProcessModel model = adap.getModel();
        // 1. Check if ProcessModel type is supported (currently only BPMN)
        if (!(model instanceof BPMNModel)) {
            throw new UnsupportedModelTypeException("Only BPMN models are currently supported!");
        }

        List<FlowObject> fo = ((BPMNModel)model).getFlowObjects();
        List<ProcessEdge> sf = model.getEdges();

        // Find first flow object without incoming sequence flow
        FlowObject currentSource = null;
        int initialNodes = 0;

        for (FlowObject o : fo) {
            // Check if o is not the source of any sequence flow
            boolean hasPredecessors = false;
            for (ProcessEdge f : sf) {
                if (f.getTarget() == o) {
                    hasPredecessors = true;
                }
            }
            if (hasPredecessors == false) {
                // Check if not data object (data objects do not have incoming flows)
                if (o instanceof DataObject) {
                    continue;
                }
                // Otherwise accept
                currentSource = o;
                initialNodes++;
            }
        }

        if (initialNodes == 0) {
            throw new Exception("LAYOUT ERROR: No initial node found!");
        }

        if (initialNodes > 1) {
            throw new Exception("LAYOUT ERROR: More than one initial node found!");
        }

        //
        // Line up all elements of the sequence flow in a horizontal sequence
        //
        int currentX = xStart - (currentSource.getSize().width / 2);
        int currentY = yStart;
        //lineUpSequences(model, currentSource, currentX, currentY);
        while (currentSource != null) {
            // position current source
            currentX += (currentSource.getBounds().width / 2);
            currentSource.setPos(currentX, currentY);
            // increase x
            currentX += ((currentSource.getBounds().width / 2) + distance);
            // get next element
            currentSource = getFirstSuccessor(currentSource, sf);
        }

        //
        // Line up all data objects above their corresponding nodes
        //
        for (FlowObject o: fo) {
            if (o instanceof DataObject) {
                // Get source and target flows
                FlowObject source = getFirstPredecessor(o, sf);
                FlowObject target = getFirstSuccessor(o, sf);
                int newX = (((target.getPos().x - (target.getSize().width / 2)) -
                        (source.getPos().x + (source.getSize().width / 2))) / 2) +
                        source.getPos().x + (source.getSize().width / 2);
                // Detect "highest flow object"
                int newY1 = source.getPos().y - (source.getSize().height / 2) - (o.getSize().height / 2);
                int newY2 = target.getPos().y - (target.getSize().height / 2) - (o.getSize().height / 2);
                int newY = newY1;
                if (newY2 < newY1) {
                    newY = newY2;
                }
                o.setPos(newX, newY - 40);
                // Add routing point to edge
                for (ProcessEdge e: sf) {
                    // Only consider Associations
                    if (e instanceof Association) {
                        // Target Association
                        if (e.getSource()==o && e.getTarget()==target) {
                            e.addRoutingPoint(new Point(target.getConnectionPoint(o.getPos()).x, o.getPos().y));
                        }
                        // Source Association
                        if (e.getSource()==source && e.getTarget()==o) {
                            e.addRoutingPoint(new Point(source.getConnectionPoint(o.getPos()).x, o.getPos().y));
                        }
                    }
                }
            }
        }

    }

    /**
     * @param currentSource
     * @param currentX
     * @param currentY
     * @return The last (unprocessed) flow object or null if done
     */
    @SuppressWarnings("unused")
	private FlowObject lineUpSequences(BPMNModel model, FlowObject currentSource, int currentX, int currentY) {
        LinkedList<FlowObject> preds = getPredecessors(model, currentSource);
        LinkedList<FlowObject> succs = getSuccessors(model, currentSource);

        // If join simply return next element
        if (preds.size() > 1) {
            return currentSource;
        }

        // position current source
        currentX += (currentSource.getBounds().width / 2);
        currentSource.setPos(currentX, currentY);
        // increase x
        currentX += ((currentSource.getBounds().width / 2) + distance);

        // process next element (if existing)
        if (succs.size() == 1) {
            return lineUpSequences(model, succs.getFirst(), currentX, currentY);
        }

        // If multi out, create new lines (simple solution, works not well for nested splits!)
        if (succs.size() > 1) {

            System.out.println("SPLIT");

            // Save old Y
            int oldY = currentY;
            // Calculate new currentY
            currentY -= ((succs.size() * 50) / 2);

            for (FlowObject fo : succs) {
                currentSource = lineUpSequences(model, fo, currentX, currentY);
                // Increase currentY
                currentY += 100;
            }

            // Reset old Y
            currentY = oldY;

            System.out.println("JOIN " + currentSource);

            // Continue line-up
            if (currentSource != null) {
                // Position current element (must be a join)
                currentX += (currentSource.getBounds().width / 2);
                currentSource.setPos(currentX, currentY);
                // increase x
                currentX += ((currentSource.getBounds().width / 2) + distance);

                lineUpSequences(model, succs.getFirst(), currentX, currentY);
            }
        }

        return null;
    }

    /**
     * Returns the first predecessor for a given flow object, or null if none.
     * @param o
     * @param sf
     * @return
     */
    private FlowObject getFirstPredecessor(FlowObject o, List<ProcessEdge> sf) {
        for (ProcessEdge f: sf) {
            // Check if data object -> Only Association
            if ((o instanceof DataObject) && !(f instanceof Association)) continue;
            // Check if other element -> Only Sequence Flow
            if (!(o instanceof DataObject) && !(f instanceof SequenceFlow)) continue;
            // Check if target is found
            if (f.getTarget() == o) return (FlowObject) f.getSource();
            }
        return null;
    }

    private FlowObject getFirstSuccessor(FlowObject o, List<ProcessEdge> sf) {
        for (ProcessEdge f: sf) {
            // Check if data object -> Only Association
            if ((o instanceof DataObject) && !(f instanceof Association)) continue;
            // Check if other element -> Only Sequence Flow
            if (!(o instanceof DataObject) && !(f instanceof SequenceFlow)) continue;
            // Check if target is found
            if (f.getSource() == o) return (FlowObject) f.getTarget();
            }
        return null;
    }

    private LinkedList<FlowObject> getPredecessors(BPMNModel model, FlowObject o) {
        LinkedList<FlowObject> predecessors = new LinkedList<FlowObject>();
        for (ProcessEdge f: model.getSequenceFlows()) {
            // Check if target is found
            if (f.getTarget() == o) predecessors.add((FlowObject) f.getTarget());
            }
        return predecessors;
    }

    private LinkedList<FlowObject> getSuccessors(BPMNModel model, FlowObject o) {
        LinkedList<FlowObject> successors = new LinkedList<FlowObject>();
        for (ProcessEdge f: model.getSequenceFlows()) {
            // Check if target is found
            if (f.getSource() == o) successors.add((FlowObject) f.getTarget());
            }
        return successors;
    }

	@Override
	public String getDisplayName() {
		return "Simple BPD Layouter";
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
	}
}
