/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

/**
 *
 * @author uha
 */
public class GetTest extends URITest{


    private XmlHttpRequest req;

    @Override
    public void setURI(URI uri, URI second) {
        try {
            req = new XmlHttpRequest(uri);
            req.addCredentials(LoadTestConfiguration.credentials);
        } catch (Exception ex) {
            Logger.getLogger(GetTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }




    @Override
    public void runTest() {
        try {
            Document executeGetRequest = req.executeGetRequest();
        } catch (IOException ex) {
            Logger.getLogger(GetTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(GetTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
