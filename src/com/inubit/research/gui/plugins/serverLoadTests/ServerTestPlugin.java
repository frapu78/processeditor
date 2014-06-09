/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author jos
 */
public class ServerTestPlugin extends WorkbenchPlugin implements WorkbenchEditorListener {

    private ServerTestDialog dialog;

    public ServerTestPlugin(Workbench workbench) {
        super(workbench);
        this.workbench = workbench;
    }

    private void init() {
        ProcessEditor editor = workbench.getSelectedProcessEditor();
        workbench.addWorkbenchEditorListener(this);
        dialog = new ServerTestDialog(workbench, false);
        dialog.setCurrentModel(editor.getModel());
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("ProcessEditor Server Test");

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

    public void newEditorCreated(ProcessEditor editor) {
        //nix
    }

    public void selectedProcessEditorChanged(ProcessEditor editor) {
        if (editor == null) {
            return;
        }
        dialog.setCurrentModel(editor.getModel());
    }
}
