/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.inubit.research.gui.Workbench;

/**
 * @author ff
 *
 */
public class OntologyGeneratorPlugin extends WorkbenchPlugin implements ActionListener {

	private JMenuItem f_menuEntry = new JMenuItem("Ontology Generator...");
	
	/**
	 * 
	 */
	public OntologyGeneratorPlugin(Workbench workbench) {
		super(workbench);
		f_menuEntry.addActionListener(this);
	}
	
	@Override
	public Component getMenuEntry() {
		return f_menuEntry;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	}

}
