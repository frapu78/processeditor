/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import com.inubit.research.client.ModelServer;
import com.inubit.research.client.UserCredentials;
import java.net.URI;

/**
 *
 * @author uha
 */
public class LoadTestConfiguration {

    public static  URI serverURL = ModelServer.getDefaultLocalURI();
    public static  String testFolder = "TestFolder";
    public static  UserCredentials credentials = ModelServer.getDefaultCredentials();
    public static int nodesPerModel = 50;
    public static int edgesPerModel = 50;

    public static UserCredentials getCredentials() {
        return credentials;
    }

}
