/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

/**
 *
 * @author uha
 */
public abstract class UserAction implements Runnable {

    public static int probability;

    protected TestUser executingUser;

    public UserAction() {
    }



    public UserAction(TestUser executingUser) {
        this.executingUser = executingUser;
    }


    public TestUser getExecutingUser() {
        return executingUser;
    }

    public void setExecutingUser(TestUser executingUser) {
        this.executingUser = executingUser;
    }








}
