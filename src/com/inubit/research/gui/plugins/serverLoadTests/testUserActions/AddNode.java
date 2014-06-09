/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.testUserActions;

import com.inubit.research.gui.plugins.serverLoadTests.tests.TestUser;
import com.inubit.research.gui.plugins.serverLoadTests.tests.UserAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * @author uha
 */
public class AddNode extends UserAction {

    public AddNode() {
    }





    public AddNode(TestUser executingUser) {
        super(executingUser);
    }



    public void run() {
        try {
            getExecutingUser().getWorkingModel().addNodeToModel(Task.class);
        } catch (Exception ex) {
            Logger.getLogger(AddNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
