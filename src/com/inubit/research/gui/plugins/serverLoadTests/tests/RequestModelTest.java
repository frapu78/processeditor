/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uha
 */
public class RequestModelTest extends LoadTest {

    private ModelVersionDescription modelVersionDescription;

    public RequestModelTest(ModelVersionDescription modelVersionDescription) {
        this.modelVersionDescription = modelVersionDescription;
    }




    @Override
    public void runTest() {
        try {
            modelVersionDescription.getProcessModel();
        } catch (IOException ex) {
            Logger.getLogger(RequestModelTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(RequestModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
