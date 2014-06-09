/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.UserCredentials;
import java.net.URI;
import java.util.List;

/**
 *
 * @author uha
 */
public class EdgesTestSeries extends ProcessObjectTestSeries {

    @Override
    public List<URI> getURIs(URI uri, UserCredentials credentials) {
        /*
        try {
            return ProcessUtils.getEdgeURIs(uri, credentials);
        } catch (IOException ex) {
            Logger.getLogger(EdgesTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(EdgesTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EdgesTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return null;
    }
}
