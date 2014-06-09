/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.server.merger.ProcessObjectMerger;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class ChangeLogSelectionListener implements ListSelectionListener {

    ProcessEditor editor;
    JList changeLog;

    public ChangeLogSelectionListener(ProcessEditor editor, JList changeLog) {
        this.editor = editor;
        this.changeLog = changeLog;
    }

    public void valueChanged(ListSelectionEvent e) {
        for (ProcessObject o : editor.getModel().getObjects()) {
            o.setSelected(false);
        }
        if (changeLog.getSelectedValue() instanceof ProcessObjectMerger) {
            ProcessObjectMerger selected = (ProcessObjectMerger) changeLog.getSelectedValue();
            if (selected != null) {
                if (selected.getMergedObject()==null) return;
                ProcessObject objectInModel = editor.getModel().getObjectById(selected.getMergedObject().getId());
                if (objectInModel == null) {
                    System.err.println("Clicked Object not in Model");
                } else {
                    objectInModel.setSelected(true);
                }

            }
            editor.repaint();
        }
    }
}
