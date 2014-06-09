/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.gui.plugins.serverLoadTests.TestSeries;
import com.inubit.research.testUtils.Seed;

/**
 *
 * @author uha
 */
public class TestCompany extends TestSeries{

    private int defaultDelay = 100;
    private int actionsPerDay = 10000;




    public int getActionsPerDay() {
        return actionsPerDay;
    }

    public void setActionsPerDay(int actionsPerDay) {
        this.actionsPerDay = actionsPerDay;
    }

    public int getDefaultDelay() {
        return defaultDelay;
    }

    public void setDefaultDelay(int defaultDelay) {
        this.defaultDelay = defaultDelay;
    }




    @Override
    public void prepareTests() {
        TestUser.averageNumberOfOperations = actionsPerDay;
        TestUser.millisecondsBetweenOperations = defaultDelay;
        for (int i = 1; i <= getNumberOfTests(); i++) {
            Seed seed = new Seed();
            TestUser test = new TestUser(seed);
            addTest(test);
        }
    }

}
