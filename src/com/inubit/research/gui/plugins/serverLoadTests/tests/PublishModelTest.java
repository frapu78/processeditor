/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.ModelDirectory;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTest;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author uha
 */
public class PublishModelTest extends LoadTest {

    private ProcessModel model;

    public PublishModelTest(ProcessModel model) {
        this.model = model;
    }



    @Override
    public void runTest() {
        try {
            ModelDirectory.publishToServer(model, true, null, LoadTestConfiguration.serverURL.toString(), "comment", LoadTestConfiguration.testFolder, "aTitle", null);
        } catch (Exception ex) {
            Logger.getLogger(PublishModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
