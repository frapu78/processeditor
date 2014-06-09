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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class provides an internal clipboard for the ProcessEditor.
 *
 * @author frank
 */
public class ProcessEditorClipboard {

    private static ProcessEditorClipboard CLIPBOARD_INSTANCE = new ProcessEditorClipboard();

    private ProcessModel clipboard;

    /**
     * Returns the singleton instance of the ProcessEditorClipboard.
     * @return
     */
    public static ProcessEditorClipboard getInstance() {
        return CLIPBOARD_INSTANCE;
    }

    /**
     * Returns the current content of the clipboard.
     * @return
     */
    public ProcessModel getContent() {
        return clipboard;
    }

    /**
     * Pastes the clipboard content to the current ProcessModel (if the type
     * matches)
     * @param model
     * @param sel
     */
    public void paste(ProcessEditor editor, SelectionHandler sel) {

        if (clipboard == null) return;

        // Check if type matches
        if (editor.getModel().getClass() != clipboard.getClass()) {
            System.err.println("Incompatible types for clipboard action!");
            return;
        }

        try {
             // Stores the relations between (org_id, new_id)
            Map<String, String> localIdMap = new HashMap<String, String>();
            // Stores the realtions between (new_id, ProcessNode)
            Map<String, ProcessNode> nodeMap = new HashMap<String, ProcessNode>();

            // Clear current selection
            sel.clearSelection();

            // Create a copy of all known ProcessNodes in the current selection
            for (ProcessNode node : clipboard.getNodes()) {
                ProcessNode newNode = node.copy();
                editor.getAnimator().addProcessObject(newNode,ProcessEditor.NEW_FADE_TIME);
                localIdMap.put(node.getId(), newNode.getId());
                nodeMap.put(newNode.getId(), newNode);
                // Add node to selection
                sel.addSelectedObject(newNode);
            }
            // Check if node is Cluster, if so copy containments
            for (ProcessObject node: clipboard.getNodes()) {
                if (node instanceof Cluster) {
                    // Copy containments
                    Cluster c = (Cluster)node;
                    Cluster newC = (Cluster)nodeMap.get(localIdMap.get(c.getId()));
                    for (ProcessNode subNode: c.getProcessNodes()) {
                        newC.addProcessNode(nodeMap.get(localIdMap.get(subNode.getId())));
                    }
                }
            }
            // Copy all edges
            for (ProcessEdge edge: clipboard.getEdges()) {
                ProcessEdge newEdge = edge.getClass().newInstance();
                // Get new source id
                String newSourceId = localIdMap.get(edge.getSource().getId());
                String newTargetId = localIdMap.get(edge.getTarget().getId());
                newEdge.setSource(nodeMap.get(newSourceId));
                newEdge.setTarget(nodeMap.get(newTargetId));
                editor.getAnimator().addProcessObject(newEdge, ProcessEditor.NEW_FADE_TIME);
                // Add edge to selection
                sel.addSelectedObject(newEdge);
            }
            // Move 10 points in x,y
            sel.moveSelection(10, 10);

        } catch (Exception ex) {
        }

    }

    /**
     * Copies the current selection of the ProcessModel to the clipboard.
     * @param model
     * @param sel
     */
    public void copy(ProcessEditor editor, SelectionHandler sel) {
        try {
            // Stores the relations between (org_id, new_id)
            Map<String, String> localIdMap = new HashMap<String, String>();
            // Stores the realtions between (new_id, ProcessNode)
            Map<String, ProcessNode> nodeMap = new HashMap<String, ProcessNode>();

            // Create corresponding model type...
            clipboard = editor.getModel().getClass().newInstance();
            // Create a copy of all known ProcessNodes in the current selection
            for (ProcessObject o: sel.getSelection()) {
                if (o instanceof ProcessNode) {
                    ProcessNode node = (ProcessNode) o;
                    ProcessNode newNode = node.clone();
                    clipboard.addNode(newNode);
                    localIdMap.put(node.getId(), newNode.getId());
                    nodeMap.put(newNode.getId(), newNode);
                }
            }
            // Check if node is Cluster, if so copy containments
            for (ProcessObject node: sel.getSelection()) {
                if (node instanceof Cluster) {
                    // Copy containments
                    Cluster c = (Cluster)node;
                    String newId = localIdMap.get(c.getId());
                    Cluster newC = (Cluster)nodeMap.get(newId);
                    for (ProcessNode subNode: c.getProcessNodes()) {
                        newC.addProcessNode(nodeMap.get(localIdMap.get(subNode.getId())));
                    }
                }
            }
            // Copy all edges
            for (ProcessObject o: sel.getSelection()) {
                if (o instanceof ProcessEdge) {
                    ProcessEdge edge = (ProcessEdge) o;
                    ProcessEdge newEdge = edge.getClass().newInstance();
                    // Get new source id
                    String newSourceId = localIdMap.get(edge.getSource().getId());
                    String newTargetId = localIdMap.get(edge.getTarget().getId());
                    newEdge.setSource(nodeMap.get(newSourceId));
                    newEdge.setTarget(nodeMap.get(newTargetId));
                    clipboard.addEdge(newEdge);
                }
            }
        } catch (Exception ex) {            
        }
    }
    
    /**
     * Deletes the selection from the ProcessModel. (For completeness, does
     * not influence the clipboard).
     * @param model
     * @param sel
     */
    public void delete(ProcessEditor editor, SelectionHandler sel) {
      for (ProcessObject o: sel.getSelection()) {
                if (o instanceof ProcessNode) {
                    ProcessNode node = (ProcessNode) o;
                    editor.getAnimator().removeProcessObject(node, ProcessEditor.DELETE_FADE_TIME);
                }
                if (o instanceof ProcessEdge) {
                    ProcessEdge edge = (ProcessEdge) o;
                    editor.getAnimator().removeProcessObject(edge, ProcessEditor.DELETE_FADE_TIME);
                }
            }   
      sel.clearSelection();
    }

    /**
     * Cuts the current selection.
     * @param model
     * @param sel
     */
    public void cut(ProcessEditor editor, SelectionHandler sel) {
        copy(editor, sel);
        delete(editor,sel);
    }


}
