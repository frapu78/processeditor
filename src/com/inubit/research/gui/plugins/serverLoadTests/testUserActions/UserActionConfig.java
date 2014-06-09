/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.testUserActions;

import com.inubit.research.gui.plugins.serverLoadTests.tests.UserAction;
import java.util.HashMap;

/**
 *
 * @author uha
 */
public class UserActionConfig {

    private static HashMap<Class<? extends UserAction>, Integer> defaultUserActionsWeights = null;

    public static int WEIGHT_ADD_NODE = 1;

    public static HashMap<Class<? extends UserAction>, Integer> getDefaultUserActionsWeights() {
        if (defaultUserActionsWeights==null) {
            defaultUserActionsWeights = createDefaultUserActionsWeights();
        }
        return defaultUserActionsWeights;
    }

    private static HashMap<Class<? extends UserAction>, Integer> createDefaultUserActionsWeights() {
        HashMap<Class<? extends UserAction>, Integer> result = new HashMap<Class<? extends UserAction>, Integer>();
        //add additional UserAction classes here
        
        result.put(AddNode.class, WEIGHT_ADD_NODE);
        result.put(GetObject.class, 2);
        result.put(GetNodeURIs.class, WEIGHT_ADD_NODE);
        result.put(GetEdgeURIs.class, WEIGHT_ADD_NODE);
        result.put(SetDimension.class, WEIGHT_ADD_NODE);
        result.put(SetPos.class, WEIGHT_ADD_NODE);
        result.put(GetImage.class, WEIGHT_ADD_NODE);
        return result;
    }



}
