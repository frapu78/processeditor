/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.testUserActions;

import com.inubit.research.client.TemporaryServerProcessObject;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import com.inubit.research.gui.plugins.serverLoadTests.tests.UserAction;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author uha
 */
public class GetNodeURIs extends UserAction {

    public void run() {
        try {
            List<URI> uris = getExecutingUser().getWorkingModel().getNodeURIs();
            if (uris.size() > 0) {
                int i = getExecutingUser().getSeed().decide(uris.size());
                getExecutingUser().setRandomNode(new TemporaryServerProcessObject(uris.get(i), LoadTestConfiguration.getCredentials()));
            }
        } catch (IOException ex) {
            Logger.getLogger(GetNodeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetNodeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GetNodeURIs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
