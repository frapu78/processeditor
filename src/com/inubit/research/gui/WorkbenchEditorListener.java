/**
 *
 * Process Editor - inubit Workbench Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 * 
 */
package com.inubit.research.gui;

import net.frapu.code.visualization.ProcessEditor;

/**
 * @author ff
 *
 */
public interface WorkbenchEditorListener {
	
	public void newEditorCreated(ProcessEditor editor);

        public void selectedProcessEditorChanged(ProcessEditor editor);

}
