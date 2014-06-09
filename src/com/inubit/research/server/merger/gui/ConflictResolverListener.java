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
import javax.swing.JTextField;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author uha
 */
public class ConflictResolverListener implements ProcessEditorListener {

    private VersionTreeManager manager;

    public ConflictResolverListener(VersionTreeManager manager) {
        this.manager = manager;
    }



    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
        //
    }

    public void processNodeEditingFinished(ProcessNode o) {
        manager.checkConflictsSolved();
    }

    public void processObjectClicked(ProcessObject o) {
        if (o==null) return;
        manager.getChangeLog().clearSelection();
        try {
            ProcessObjectMerger m = manager.getEditor().getMerger().getObjectMerger(o);
            manager.getChangeLog().setSelectedValue(m, true);
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Clicked Object not contained");
        }
        manager.checkConflictsSolved();
    }

    public void processObjectDoubleClicked(ProcessObject o) {
        //
    }

    public void modelChanged(ProcessModel m) {
        manager.checkConflictsSolved();
    }

    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        //o.setPos(new Point(oldX, oldY));
    }


}
