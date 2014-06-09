/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.UserCredentials;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import com.inubit.research.gui.plugins.serverLoadTests.TestSeries;
import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author uha
 */
public class PublishModelTestSeries extends TestSeries {

    public int avgNumberOfNodesPerModel = 50;
    public int avgNumberOfEdgesPerModel = 50;

    @Override
    public void prepareTests() {
        Seed seed = new Seed();
        Seed lastSeed;
        ModelGenerator gen = new ModelGenerator();
        ProcessModel model;
        UserCredentials credentials = null;
        credentials = LoadTestConfiguration.credentials;
        for (int i = 1; i <= getNumberOfTests(); i++) {
            lastSeed = new Seed(seed);
            seed = new Seed();
            model = gen.generate(seed, BPMNModel.class, avgNumberOfNodesPerModel, avgNumberOfEdgesPerModel);
            model.setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, credentials);
            PublishModelTest test = new PublishModelTest(model);
            addTest(test);
        }
    }
}
