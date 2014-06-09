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
public class MetaTestSeries extends ProcessObjectTestSeries {

    @Override
    public List<URI> getURIs(URI uri, UserCredentials credentials) {
        /*
        try {
           List<URI> uris = ProcessUtils.getEdgeURIs(uri, credentials);
           uris.addAll(ProcessUtils.getNodeURIs(uri, credentials));
           List<URI> result = new ArrayList<URI>(uris.size());
           String path;
           for (URI objectUri : uris) {
               path = objectUri.toString();
               path += "/meta";
               result.add(new URI(path));
           }
           return result;
        } catch (IOException ex) {
            Logger.getLogger(MetaTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MetaTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MetaTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return null;
    }
}
