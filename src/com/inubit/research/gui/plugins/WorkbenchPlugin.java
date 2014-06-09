/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.gui.SplashScreen;
import com.inubit.research.gui.Workbench;
import java.awt.Component;

/**
 *
 * @author frank
 */
public abstract class WorkbenchPlugin {

    protected Workbench workbench;

    public WorkbenchPlugin(Workbench workbench) {
    	this.workbench = workbench;
    }
    
    /**
	 * used if the workbench is set later
	 * so this plugin can be considered during initialization
	 */
	public WorkbenchPlugin() {
	
	}

    public Workbench getWorkbench() {
    	return workbench;
    }
    
    public void setWorkbench(Workbench wb) {
    	this.workbench = wb;
    }
    
    /**
     * Returns the Menu/MenuItem for this plugin.
     * @return
     */
    public abstract Component getMenuEntry();

	/**
	 * initialization method which is called when a plugin is handed over
	 * to the constructor of a workbench
	 * 
	 * @param splashScreen
	 */
	public void init(SplashScreen splashScreen) {
	}

}
