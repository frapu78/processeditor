/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;


/**
 *
 * @author uha
 */
public class ServerLoadTestPlugin extends WorkbenchPlugin {

    Workbench workbench;

    public ServerLoadTestPlugin(Workbench workbench) {
        super(workbench);
        this.workbench = workbench;
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem menuItem = new JMenuItem("Server Load Tests");

        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ServerLoadTestDialog win = new ServerLoadTestDialog(workbench, true);
                win.setVisible(true);
            }
        });
        return menuItem;
    }
}
