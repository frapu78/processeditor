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
import com.inubit.research.server.merger.IDbasedModelDiff;
import com.inubit.research.server.merger.ProcessModelDiff;
import com.inubit.research.server.merger.ProcessModelMerger;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class ProcessModelMergerPlugin extends WorkbenchPlugin {

    public ProcessModelMergerPlugin(Workbench workbench) {
        super(workbench);
    }

    @Override
    public Component getMenuEntry() {
        JMenu menu = new JMenu("Versioning");

        JMenuItem item1 = new JMenuItem("Merge my changes with head model version");
        item1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ProcessModel wc = workbench.getSelectedModel();
                    // Fetch original version
                    ProcessModel orgModel = ProcessUtils.parseProcessModelSerialization(URI.create(wc.getProcessModelURI()));
                    // Create diff between working copy and original model
                    ProcessModelDiff diff = new IDbasedModelDiff(orgModel, wc);
                    diff.dump(); 
                    // Fetch head model
                    URI headUri = URI.create(wc.getProcessModelURI().substring(0,wc.getProcessModelURI().indexOf("/versions")));
                    ProcessModel headModel = ProcessUtils.parseProcessModelSerialization(headUri);
                    System.out.println(headModel.getProcessModelURI());
                    // Merge diff to headModel
                    ProcessModelMerger merger = new ProcessModelMerger(headModel, diff);
                    ProcessModel resultModel = merger.mergeModels();
                    // Add result
                    workbench.addModel(resultModel.getProcessModelURI(), resultModel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(item1);

        JMenuItem item2 = new JMenuItem("Show diff between current model and head version");
        item2.setEnabled(false);
        menu.add(item2);

        return menu;
    }

}
