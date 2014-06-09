/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

/**
 *
 * @author uha
 */
public abstract class LoadTest implements Runnable {

    protected int testID = 0;

    public int getTestID() {
        return testID;
    }

    public void setTestID(int testID) {
        this.testID = testID;
    }



    @Override
    public final void run() {
        System.out.println(this.getClass().getSimpleName() + " number: " + testID + " requests started");
        runTest();
        System.out.println(this.getClass().getSimpleName() + " number: " + testID + " requests finished");
    }

    public abstract void runTest();

}
