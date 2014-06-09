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
import com.inubit.research.client.TemporaryServerModel;
import com.inubit.research.client.TemporaryServerProcessObject;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTest;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import com.inubit.research.gui.plugins.serverLoadTests.testUserActions.UserActionConfig;
import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * @author uha
 */
public class TestUser extends LoadTest {

    public static int averageNumberOfOperations = 1000;
    public static int millisecondsBetweenOperations = 100;
    private static int possibleActions = 1;
    private Seed seed;
    private ProcessModel model;
    private TemporaryServerProcessObject randomNode;
    private TemporaryServerModel workingModel;    
    private HashMap<Integer, UserAction> userActionsWeights = new HashMap<Integer, UserAction>();




    public TestUser(Seed seed) {
        this.seed = seed;
        ModelGenerator gen = new ModelGenerator();
        model = gen.generate(seed, BPMNModel.class, LoadTestConfiguration.nodesPerModel, LoadTestConfiguration.edgesPerModel);
        //ModelServer server = new ModelServer();
        model.setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, LoadTestConfiguration.getCredentials());
        initUserActions();
    }

    private void initUserActions() {
        int i = 0;
        HashMap<Class<? extends UserAction>, Integer> actions = UserActionConfig.getDefaultUserActionsWeights();

        for (Class<? extends UserAction> key : actions.keySet()) {
            int w = actions.get(key);
            i+= w;
            addUserAction(key, i);
        }
        possibleActions = i;
    }

    private UserAction getAction() {
        int dice = seed.decide(possibleActions);
        int lastkey = 0;
        for (Integer key : userActionsWeights.keySet()) {
            if (dice<key && lastkey <= dice) {
                return userActionsWeights.get(key);
            }
        }
        return null;
    }

    public void addUserAction(Class<? extends UserAction> action, int weight) {
        try {
            UserAction instance = action.newInstance();
            instance.setExecutingUser(this);
            userActionsWeights.put(weight, instance);
        } catch (InstantiationException ex) {
            Logger.getLogger(TestUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(TestUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getAverageNumberOfOperations() {
        return averageNumberOfOperations;
    }

    public void setAverageNumberOfOperations(int averageNumberOfOperations) {
        TestUser.averageNumberOfOperations = averageNumberOfOperations;
    }

    public int getMillisecondsBetweenOperations() {
        return millisecondsBetweenOperations;
    }

    public void setMillisecondsBetweenOperations(int millisecondsBetweenOperations) {
        TestUser.millisecondsBetweenOperations = millisecondsBetweenOperations;
    }

    public static int getPossibleActions() {
        return possibleActions;
    }

    public static void setPossibleActions(int possibleActions) {
        TestUser.possibleActions = possibleActions;
    }

    public TemporaryServerProcessObject getRandomNode() {
        return randomNode;
    }

    public void setRandomNode(TemporaryServerProcessObject randomNode) {
        this.randomNode = randomNode;
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public ProcessModel getModel() {
        return model;
    }

    public void setModel(ProcessModel model) {
        this.model = model;
    }

    public TemporaryServerModel getWorkingModel() {
        return workingModel;
    }

    public void setWorkingModel(TemporaryServerModel workingModel) {
        this.workingModel = workingModel;
    }







    @Override
    public void runTest() {
        try {
            boolean leaveWork = false;
            ModelDirectory.publishToServer(model, true, null, LoadTestConfiguration.serverURL.toString(), "comment", "testFolder", "titleTest", null);
            workingModel = new TemporaryServerModel(new URI(model.getProcessModelURI()), LoadTestConfiguration.getCredentials());
            if (workingModel.getNodeURIs().size() < 1) {
                workingModel.addNodeToModel(Task.class);
            }
            randomNode = new TemporaryServerProcessObject(workingModel.getNodeURIs().get(0), LoadTestConfiguration.getCredentials());
            do {
                    getAction().run();                   
                    getCoffee();
                    leaveWork = seed.decide(getAverageNumberOfOperations()) == 0;               
            } while (!leaveWork);
        } catch (Exception ex) {
            Logger.getLogger(TestUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getCoffee() {
        try {
            Thread.sleep(getMillisecondsBetweenOperations());
        } catch (InterruptedException ex) {
            Logger.getLogger(TestUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
