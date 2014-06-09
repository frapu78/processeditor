/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.testUserActions;

import com.inubit.research.client.InvalidUserCredentialsException;
import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.gui.plugins.serverLoadTests.tests.TestUser;
import com.inubit.research.gui.plugins.serverLoadTests.tests.UserAction;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author uha
 */
public class GetEdgeURIs extends UserAction {

    public GetEdgeURIs() {
    }

    public GetEdgeURIs(TestUser executingUser) {
        super(executingUser);
    }

    public void run() {
        try {
            this.getExecutingUser().getWorkingModel().getEdgeURIs();
        } catch (IOException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidUserCredentialsException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(GetEdgeURIs.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
