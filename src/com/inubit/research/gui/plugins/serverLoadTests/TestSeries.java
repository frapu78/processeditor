/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uha
 */
public abstract class TestSeries {

    protected int numberOfTests = 10;
    public boolean runParallel = true;
    public Date timerStart = new Date();
    private ArrayList<Thread> requestThreads = new ArrayList<Thread>(numberOfTests);
    protected ArrayList<LoadTest> tests = new ArrayList<LoadTest>(numberOfTests);

    public int getNumberOfTests() {
        return numberOfTests;
    }

    public void setNumberOfTests(int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public void prepareParallelExecution() {
        System.out.println("Starting threads for parallel execution");
        for (LoadTest test : this.tests) {
            Thread t = new Thread(test);
            requestThreads.add(t);
        }
        System.out.println("Finished starting threads");
    }

    public void runTestsParallel() {
        int i = 0;
        startTimer();
        for (Thread t : requestThreads) {
            t.start();
            i++;
            System.out.println("Started test thread : " + i);

        }

    }

    public void setTestIDs() {
        int i = 1;
        for (LoadTest t : tests) {
            t.setTestID(i);
            i++;
        }

    }

    public void runTestsSequential() {
        int i = 0;
        startTimer();
        for (LoadTest t : tests) {
            t.run();
            i++;
            System.out.println("Started test : " + i);
        }
    }

    public void addTest(LoadTest test) {
        tests.add(test);
    }

    public abstract void prepareTests();

    private void joinThreads() {
         for (Thread t : requestThreads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(TestSeries.class.getName()).log(Level.SEVERE, null, ex);
            }
         }    
    }

    private void startTimer() {
        System.out.println("Start sending requests");
        timerStart = new Date();
    }

    private void stopTimer() {
        Date now = new Date();
        long seconds = (now.getTime()-timerStart.getTime()) /1000;
        System.out.println("Requests took: " + seconds + " seconds" );
    }

    public void test() {
        System.out.println("Prepare tests");
        prepareTests();
        setTestIDs();
        System.out.println("Test preparation completed");

        if (runParallel) {
            prepareParallelExecution();
            runTestsParallel();
            //joinThreads();
        } else {
            runTestsSequential();
        }
        System.out.println("ALL REQUESTS COMPLETED");
        stopTimer();
    }
}
