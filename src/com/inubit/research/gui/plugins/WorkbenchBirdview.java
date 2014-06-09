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
import javax.swing.JTextField;

import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author fpu
 */
public class WorkbenchBirdview extends WorkbenchPlugin implements
        WorkbenchEditorListener, ProcessModelListener, ProcessEditorListener {

    private WorkbenchBirdviewDialog dialog;
    private ProcessEditor currentEditor;
    private Workbench workbench;

    //private ProcessModel model;
    public WorkbenchBirdview(Workbench workbench) {
        super(workbench);
        this.workbench = workbench;
    }

    private void init() {
        dialog = new WorkbenchBirdviewDialog(workbench, false);
        workbench.addWorkbenchEditorListener(this);
        selectedProcessEditorChanged(workbench.getSelectedProcessEditor());
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("Birdview...");

        menuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialog == null) {
                    init();
                }
                // Show dialog
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
        if (editor == null) {
            return;
        }
        if (currentEditor != null) {
            currentEditor.getModel().removeListener(this);
            currentEditor.removeListener(this);
        }
        editor.getModel().addListener(this);
        editor.addListener(this);
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
        // ignore
    }

    @Override
    public void processObjectClicked(ProcessObject o) {
        // ignore
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
        // ignore
    }

    @Override
    public void modelChanged(ProcessModel m) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        dialog.setEditor(currentEditor);
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    }
}
