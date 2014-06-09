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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class ModelOperationsPlugin extends WorkbenchPlugin {

	private boolean f_shadowValue = false; //meaning all shadows are off
	
    public ModelOperationsPlugin(Workbench workbench) {
        super(workbench);
    }

    @Override
    public Component getMenuEntry() {
        JMenu menu = new JMenu("Model operations");
        JMenuItem item = new JMenuItem("Remove all routing points");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessModel model = workbench.getSelectedModel();
                // Iterate over all ProcessEdges;
                for (ProcessEdge edge : model.getEdges()) {
                    edge.setProperty(ProcessEdge.PROP_POINTS, "");
                }
                // Refresh
                workbench.getSelectedProcessEditor().repaint();
            }
        });
        menu.add(item);

        item = new JMenuItem("Fix containments");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessModel model = workbench.getSelectedModel();
                List<ProcessNode> alreadyContainedSomewhere = new LinkedList<ProcessNode>();
                // Iterate over all Clusters from top to bottom
                for (int pos=model.getNodes().size()-1; pos>=0; pos--) {
                    if (model.getNodes().get(pos) instanceof Cluster) {
                        Cluster c = (Cluster)model.getNodes().get(pos);
                        List<ProcessNode> trashList = new LinkedList<ProcessNode>();
                        for (ProcessNode n: c.getProcessNodes()) {
                            if (!alreadyContainedSomewhere.contains(n)) {
                                alreadyContainedSomewhere.add(n);
                            } else {
                                // Mark for removal
                                trashList.add(n);
                            }
                        }
                        for (ProcessNode n: trashList) c.removeProcessNode(n);
                    }
                }



                // Refresh
                workbench.getSelectedProcessEditor().repaint();
            }
        });
        menu.add(item);
        
        final JMenuItem item2 = new JMenuItem("Turn All Shadows On");
        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	f_shadowValue = !f_shadowValue;
        		item2.setText("Turn Shadows "+(f_shadowValue ? "Off" : "On"));
        		for(ProcessNode node:getWorkbench().getSelectedModel().getNodes()) {
        			node.setShadowEnabled(f_shadowValue);
        		}
        		// Refresh
                workbench.getSelectedProcessEditor().repaint();
            }
        });
        menu.add(item2);

        return menu;
    }
}
