/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author frank
 */
public class NodeViewPlugin extends WorkbenchPlugin implements WorkbenchEditorListener, ProcessModelListener {

    private NodeViewPluginDialog dialog;
    private ProcessEditor currentEditor;
    private Workbench workbench;

    public NodeViewPlugin(Workbench workbench) {
        super(workbench);
        this.workbench=workbench;
    }

    private void init() {
        dialog = new NodeViewPluginDialog(workbench, false);
        workbench.addWorkbenchEditorListener(this);
        selectedProcessEditorChanged(workbench.getSelectedProcessEditor());
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("Node view...");

        menuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show dialog
                if (dialog==null) init();
                dialog.setVisible(true);
            }
        });

        return menuItem;
    }

    @Override
    public void newEditorCreated(ProcessEditor editor) {
        //
    }

    @Override
    public void selectedProcessEditorChanged(ProcessEditor editor) {
        if (editor==null) return;
        if (currentEditor!=null) currentEditor.getModel().removeListener(this);
        editor.getModel().addListener(this);
        dialog.setEditor(editor);
        currentEditor = editor;
    }

    @Override
    public void processNodeAdded(ProcessNode newNode) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        dialog.setEditor(currentEditor);
    }   

}
