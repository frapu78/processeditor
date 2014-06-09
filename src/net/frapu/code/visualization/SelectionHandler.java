/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * This class implements an Handler for multiple selections in the
 * ProcessEditor. Nodes can be added, removed, or the list can be cleared.
 * The update of the selection state of the objects is handled by this
 * class.
 *
 * @author frank
 */
public class SelectionHandler {

    private ProcessModel model;
    private ProcessEditor editor;
    private volatile List<ProcessObject> selection = new ArrayList<ProcessObject>();

    /** 
     * Creates a new selection handler for a certain ProcessModel.
     * @param m
     */
    public SelectionHandler(ProcessEditor editor) {
        this.editor = editor;
        this.model = editor.getModel();
    }

    /**
     * Adds a node to the selection.
     * @param n The ProcessObject n is only added if it is not already contained.
     */
    public void addSelectedObject(ProcessObject n) {
        if (!selection.contains(n)) {
            selection.add(n);
//            if (n instanceof ProcessEdge) {
//                ProcessEdge e = (ProcessEdge)n;
//                 // Add all contained routing points
//                for (int i1 = 1; i1 < e.getRoutingPointShapes().size() - 1; i1++) {
//                    Shape s = e.getRoutingPointShapes().get(i1);
//                    RoutingPointDragable rpd = new RoutingPointDragable(e, i1);
//                    this.addSelectedObject(rpd);
//                }
//            }
            n.setSelected(true);
        }
    }

    /**
     * Adds the ProcessObject if it is currently not contained in the
     * selection and removes it otherwise.
     * @param n
     */
    public synchronized void toggleSelectedObject(ProcessObject n) {
        if (!selection.contains(n)) {
            selection.add(n);
            n.setSelected(true);
        } else {
            selection.remove(n);
            n.setSelected(false);
        }
    }

    /**
     * Removes a node from the selection.
     * @param n
     */
    public boolean removeSelectedObject(ProcessObject n) {
        if (selection.contains(n)) {
            n.setSelected(false);
        }
        return selection.remove(n);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (selection == null) {
            return;

        }
        for (ProcessObject n : selection) {
            n.setSelected(false);
        }
        selection.clear();
    }

    /**
     * Returns the last selected node (the last one that has been added to
     * the selection).
     * @return
     */
    public ProcessObject getLastSelection() {
        if (selection.size() > 0) {
            return selection.get(selection.size() - 1);
        }
        return null;
    }

    /**
     * convenience method that returns the lastSelection
     * as a ProcessNode (if it is one) or null otherwise
     * @return
     */
    public ProcessNode getLastSelectedNode() {
        ProcessObject obj = getLastSelection();
        if (obj instanceof ProcessNode) {
            return (ProcessNode) obj;
        }
        return null;
    }

    /**
     * Returns the current selection.
     * @return
     */
    public List<ProcessObject> getSelection() {
        return selection;
    }

    /**
     * Returns if a certain ProcessObject is contained.
     * @param o
     * @return
     */
    public boolean contains(ProcessObject o) {
        return selection.contains(o);
    }

    /**
     * Checks whether the selection is empty.
     * @return
     */
    public boolean isEmpty() {
        return selection.size() == 0;
    }

    /**
     * Returns the size of the selection.
     * @return
     */
    public int getSelectionSize() {
        return selection.size();
    }

    /**
     * Moves the ProcessNodes of the selection according to the given offsets.
     * Takes care of Cluster containments.
     */
    public void moveSelection(int offsetX, int offsetY) {
        if (getSelectionSize() > 1) {
            editor.pauseLayoutEdges();
            
        }
        List<Dragable> dragables = new LinkedList<Dragable>();
        // Collect all Dragables of the selection to move
        for (ProcessObject d : selection) {
            if (d instanceof Dragable) {
                dragables.add((Dragable) d);

                
            }
        }
        // Remove all that are contained in Clusters
        for (ProcessObject o : selection) {
            if (o instanceof Cluster) {
                Cluster c = (Cluster) o;
                for (ProcessNode n : c.getProcessNodes()) {
                    dragables.remove((Dragable) n);
                }
            }
        }

        // Move selection
        for (Dragable d : dragables) {
            d.setPos(new Point(d.getPos().x + offsetX, d.getPos().y + offsetY));
        }

        // Also move routing points on all contained edges
        for (ProcessObject d : model.getEdges()) {
            if (d instanceof ProcessEdge) {
                ProcessEdge e = (ProcessEdge) d;
                if (dragables.contains(e.getSource()) && dragables.contains(e.getTarget())) {
                    for (int i = 1; i < e.getRoutingPoints().size() - 1; i++) {
                        Point p = e.getRoutingPoints().get(i);
                        e.moveRoutingPoint(i, new Point(p.x + offsetX, p.y + offsetY));
                    }
                }
            }
        }
        if (getSelectionSize() > 1) {
            editor.continueLayoutEdges();
            if (this.editor.getModel().getUtils() != null) {
                if (this.editor.getModel().getUtils().getRoutingPointLayouter() != null) {
                    RoutingPointLayouter l = this.editor.getModel().getUtils().getRoutingPointLayouter();
                    for (ProcessObject o : this.selection) {
                        if (o instanceof ProcessNode) {
                            l.optimizeAllEdges((ProcessNode) o);
                        }
                    }
                }
            }

        }
    }
}
