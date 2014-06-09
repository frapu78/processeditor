/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.server.merger.VersionTreeViewer.VersionNode;
import javax.swing.JTextField;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class VersionTreeListener implements ProcessEditorListener {


    private VersionTreeManager manager;

    public VersionTreeListener() {
    }
    
    

    public VersionTreeListener(VersionTreeManager manager) {
        this.manager = manager;
    }





    @Override
    public void processObjectClicked(ProcessObject o) {
        if (o instanceof VersionNode) {
            VersionNode v = (VersionNode) o;
            manager.changeDisplayedVersion(v.getVersionDescription());
            manager.checkConflictsSolved();
        }
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
      
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
       
    }

    @Override
    public void modelChanged(ProcessModel m) {
   
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
       
    }

}
