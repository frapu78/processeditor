/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;


import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author uha
 */
public class PutTest extends URITest {


    private XmlHttpRequest reqGet;
    private XmlHttpRequest reqPut;
    private Document doc;

    @Override
    public void setURI(URI uri, URI second) {
        try {
            reqGet = new XmlHttpRequest(second);
            reqGet.addCredentials(LoadTestConfiguration.getCredentials());
            reqPut = new XmlHttpRequest(uri);
            reqPut.addCredentials(LoadTestConfiguration.getCredentials());
           
        } catch (Exception ex) {
            Logger.getLogger(PutTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void runTest() {
        /*
        try {
            //doc = PutRequests.getUpdateRequest();
            reqGet.executePutRequest(doc);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PutTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PutTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PutTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(PutTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         *
         */
    }
}
