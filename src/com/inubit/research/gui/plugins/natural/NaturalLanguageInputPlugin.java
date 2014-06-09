/**
 *
 * Process Editor - inubit Workbench Natural Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.natural;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.WorkbenchEditorListener;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author fpu
 */
public class NaturalLanguageInputPlugin extends WorkbenchPlugin implements WorkbenchEditorListener {

    private Workbench workbench;
    private NaturalLanguageInputModel nlModel;
    private NaturalLanguageInputDialog dialog;

    public NaturalLanguageInputPlugin(Workbench workbench) {
        super(workbench);
        this.workbench = workbench;
        workbench.addWorkbenchEditorListener(this);
        dialog = new NaturalLanguageInputDialog(workbench, this);
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("Natural language input...");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //if (dialog==null) dialog = new NaturalLanguageInputDialog(workbench, outer);
                dialog.setVisible(true);
            }
        });

        return menuItem;
    }

    public NaturalLanguageInputModel getInputModel() {
        return nlModel;
    }

    public ProcessEditor getCurrentEditor() {
        return workbench.getSelectedProcessEditor();
    }

    @Override
    public void newEditorCreated(ProcessEditor editor) {
        selectedProcessEditorChanged(editor);
    }

    @Override
    public void selectedProcessEditorChanged(ProcessEditor editor) {
       if (editor.getModel() instanceof BPMNModel) {
        nlModel = new NaturalLanguageInputModel((BPMNModel)editor.getModel());
        dialog.setEnabled(true);
       } else {
           nlModel = null;
           dialog.setEnabled(false);
       }
    }

}
