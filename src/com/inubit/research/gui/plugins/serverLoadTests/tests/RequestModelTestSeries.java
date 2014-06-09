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

/**
 *
 * @author uha
 */
public class RequestModelTestSeries extends RequestModelVersionDescriptionTestSeries {

    @Override
    void makeTest(ModelVersionDescription modelVersionDesc) {
        addTest(new RequestModelTest(modelVersionDesc));
    }

}
