/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.tracking;

import java.awt.Point;
import net.frapu.code.visualization.*;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextField;

/**
 *
 * This class tracks the actions of the ProcessModel and the ProcessNodes
 * contained in a ProcessEditor.
 *
 * @todo: Add support for resizement of nodes!
 * @todo: Add support for multiple selections!
 *
 * @author frank
 */
public class ProcessEditorActionTracker implements
        ProcessModelListener, ProcessObjectListener, ProcessEditorListener {
    
    private ProcessModel currentModel = null;

    private List<ProcessEditorActionRecord> actionTrack = new LinkedList<ProcessEditorActionRecord>();

    private int actionPointer = -1;

    private boolean tracking = true;

    public ProcessEditorActionTracker(ProcessEditor editor) {
        editor.addListener(this);
        modelChanged(editor.getModel());
        log("New ProcessEditorActionTracker created.");
    }
    
    /**
	 * @param text  
	 */
    protected void log(String text) {
        //System.out.println(this.hashCode()+": "+text);
    }

    public boolean undoLastAction() {
        ProcessEditorActionRecord lastRecord = popAction();

        log("UNDO: "+lastRecord);

        if (lastRecord!=null) {

            if (lastRecord instanceof ProcessEditorDragableMovedAction) {
                ProcessEditorDragableMovedAction action =
                        (ProcessEditorDragableMovedAction)lastRecord;
                // Return Dragable to last position
                Dragable o = action.getDragable();
                setTracking(false);
                o.setPos(new Point(action.getOldX(), action.getOldY()));
                setTracking(true);
                return true;
            }

            if (lastRecord instanceof ProcessEditorPropertyChangedAction) {
                ProcessEditorPropertyChangedAction action =
                        (ProcessEditorPropertyChangedAction)lastRecord;
                // Undo property change
                ProcessObject o = action.getProcessObject();
                setTracking(false);
                o.setProperty(action.getKey(), action.getOldValue());
                // Check if Cluster
//                if (true) {
//                    // Walk back until no more of the same action is to be found
//                    boolean check = true;
//                    while (check) {
//                        ProcessEditorActionRecord prevRecord = getAction();
//                        if (prevRecord instanceof ProcessEditorPropertyChangedAction) {
//                            ProcessEditorPropertyChangedAction prevAction =
//                                    (ProcessEditorPropertyChangedAction) prevRecord;
//                            // Still the same object
//                            if (prevAction.getProcessObject() == action.getProcessObject()) {
//                                if (prevAction.getKey().equals(action.getKey())) {
//                                    // Indeed the same
//                                    prevAction.getProcessObject().setProperty(prevAction.getKey(), prevAction.getOldValue());
//                                    System.out.println("XY");
//                                    popAction();
//                                }
//                            }
//                        } else {
//                            check = false;
//                        }
//                    }
//                }

                setTracking(true);

                return true;
            }

            if (lastRecord instanceof ProcessEditorObjectCreatedAction) {
                ProcessEditorObjectCreatedAction action =
                        (ProcessEditorObjectCreatedAction)lastRecord;
                // Delete object
                setTracking(false);
                // Check nodes
                if (action.getProcessObject() instanceof ProcessNode)
                    currentModel.removeNode((ProcessNode)action.getProcessObject());
                // Check edges
                if (action.getProcessObject() instanceof ProcessEdge)
                    currentModel.removeEdge((ProcessEdge)action.getProcessObject());
                setTracking(true);

                return true;
            }

            if (lastRecord instanceof ProcessEditorObjectDeletedAction) {
                ProcessEditorObjectDeletedAction action =
                        (ProcessEditorObjectDeletedAction)lastRecord;
                // Create object
                setTracking(false);
                if (action.getProcessObject() instanceof ProcessNode)
                    currentModel.addNode((ProcessNode)action.getProcessObject());
                if (action.getProcessObject() instanceof ProcessEdge)
                    currentModel.addEdge((ProcessEdge)action.getProcessObject());
                setTracking(true);
                return true;
            }

        }
        return false;
    }


    private ProcessEditorActionRecord popAction() {
        if (actionPointer<0) return null;
        actionPointer--;
        return actionTrack.get(actionPointer+1);
    }

    @SuppressWarnings("unused")
	private ProcessEditorActionRecord getAction() {
        if (actionPointer<0) return null;
        return actionTrack.get(actionPointer+1);
    }

    private void pushAction(ProcessEditorActionRecord record) {
        if (!isTracking()) return;
        // Check if pointer != size
        if (actionPointer!=actionTrack.size()-1) {
            // Remove everything after the pointer
            for (int i=actionPointer+1; i<actionTrack.size(); i++) {
                actionTrack.remove(i);
            }
        }

        // Add action to record
        actionTrack.add(record);
        // Log
        log("RECORD: "+record);
        // Set actionPointer
        actionPointer = actionTrack.size()-1;
    }

    protected void setTracking(boolean b) {
        tracking = b;
    }

    public boolean isTracking() {
        return tracking;
    }


    //
    //
    // ProcessEditor Listener
    //
    //

    @Override
    public void processObjectClicked(ProcessObject o) {
        // Ignore
    }
    
    @Override
	public void processObjectDoubleClicked(ProcessObject o) {
    	// Ignore
    }

    @Override
    public void modelChanged(ProcessModel m) {

        if (currentModel!=null) {
            currentModel.removeListener(this);
            // Remove all ProcessObjectListeners from the old Nodes
            for (ProcessNode n: currentModel.getNodes()) {
                n.removeListener(this);
            }
            for (ProcessEdge e: currentModel.getEdges()) {
                e.removeListener(this);
            }
        }

        // Attach ProcessObject listeners to all nodes/edge of the new model
        for (ProcessNode n: m.getNodes()) {
            n.addListener(this);
        }
        for (ProcessEdge e: m.getEdges()) {
            e.addListener(this);
        }

        m.addListener(this);
        currentModel = m;
        // Clear recorded tracks
        actionTrack.clear();
     }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        ProcessEditorDragableMovedAction record =
                new ProcessEditorDragableMovedAction(o, oldX, oldY, o.getPos().x, o.getPos().y);
        // Add action to record
        pushAction(record);
    }

    //
    // ProcessModel Listener
    //
    @Override
    public void processNodeAdded(ProcessNode newNode) {
        if (newNode==null) return;
        newNode.addListener(this);
        ProcessEditorObjectCreatedAction record =
                new ProcessEditorObjectCreatedAction(newNode);
        pushAction(record);
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
        if (remNode==null) return;
        remNode.removeListener(this);
        ProcessEditorObjectDeletedAction record =
                new ProcessEditorObjectDeletedAction(remNode);
        pushAction(record);

    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
        if (edge==null) return;
        edge.addListener(this);
        ProcessEditorObjectCreatedAction record =
                new ProcessEditorObjectCreatedAction(edge);
        pushAction(record);
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
        if (edge==null) return;
        edge.removeListener(this);
        ProcessEditorObjectDeletedAction record =
                new ProcessEditorObjectDeletedAction(edge);
        pushAction(record);
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        // ignore
    }   

    //
    // ProcessObjectListener
    //
    @Override
    public void propertyChanged(ProcessObject o, String key, String oldValue, String newValue) {
        // Ignore x, y, width, height (tracked via ProcessEditorListener)
        if (key.equals(ProcessNode.PROP_XPOS) |
                key.equals(ProcessNode.PROP_YPOS) |
                key.equals(ProcessNode.PROP_WIDTH) |
                key.equals(ProcessNode.PROP_HEIGHT)) return;

        // Track property changes here!!!
        ProcessEditorPropertyChangedAction record =
                new ProcessEditorPropertyChangedAction(o, key, oldValue, newValue);        
        // Add action to record
        pushAction(record);
    }

	@Override
	public void processNodeEditingFinished(ProcessNode o) {
	}

	@Override
	public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
	}	

}
