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
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.SwingUtils;

/**
 *
 * @author fpu
 */
public class PropertiesPlugin extends WorkbenchPlugin implements
        ProcessEditorListener, WorkbenchEditorListener {

    private PropertiesPluginDialog dialog;
    private ProcessEditor currentEditor;

    public PropertiesPlugin(Workbench workbench) {
        super(workbench);
        this.workbench = workbench;
    }

    private void initProcessEditor() {
        if (currentEditor == null) {
            currentEditor = workbench.getSelectedProcessEditor();
            currentEditor.addListener(this);
            return;
        }
        if (workbench.getSelectedProcessEditor() != currentEditor) {
            currentEditor.removeListener(this);
            currentEditor = workbench.getSelectedProcessEditor();
            currentEditor.addListener(this);
        }
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("Properties...");

        final PropertiesPlugin outer = this;

        menuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show dialog
                workbench.addWorkbenchEditorListener(outer);
                if (dialog == null) {
                    dialog = new PropertiesPluginDialog(workbench, false);
                    SwingUtils.center(dialog);
                    initProcessEditor();
                }
                dialog.setVisible(true);
            }
        });

        return menuItem;
    }

    @Override
    public void processObjectClicked(ProcessObject o) {
        dialog.setObject(workbench.getSelectedProcessEditor().getSelectedModel(),o);
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
    }

    @Override
    public void modelChanged(ProcessModel m) {
        dialog.setObject(m, workbench.getSelectedProcessEditor().getLastSelectedNode());
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        //
    }

    @Override
    public void newEditorCreated(ProcessEditor editor) {
        initProcessEditor();
        dialog.setObject(workbench.getSelectedProcessEditor().getSelectedModel(),
                workbench.getSelectedProcessEditor().getLastSelectedNode());
    }

    @Override
    public void selectedProcessEditorChanged(ProcessEditor editor) {
        initProcessEditor();
        dialog.setObject(workbench.getSelectedProcessEditor().getSelectedModel(),
                workbench.getSelectedProcessEditor().getLastSelectedNode());
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    }
}
