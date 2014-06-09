/**
 *
 * Process Editor - inubit Workbench Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 * 
 */
package com.inubit.research.gui;

import java.awt.event.ActionEvent;
import net.frapu.code.visualization.ProcessEditorInterface;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author fpu
 */
public class WorkbenchModelFactory implements java.awt.event.ActionListener {

    private ProcessEditorInterface pei;
    private Class<? extends ProcessModel> modelType;

    /**
     * Implements an ActionListener for creating Workbench models.
     * @param frame
     * @param modelType
     */
    public WorkbenchModelFactory(ProcessEditorInterface frame, Class<? extends ProcessModel> modelType) {
        this.pei=frame;
        this.modelType=modelType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // Try to instantiate class
            Object o = modelType.newInstance();
            if (o instanceof ProcessModel) {
                // Cast to ProcessModel
                ProcessModel m = (ProcessModel)o;
                // Add model to frame
                pei.processModelOpened(m);
            }

        } catch (Exception ex) {}
    }

}
