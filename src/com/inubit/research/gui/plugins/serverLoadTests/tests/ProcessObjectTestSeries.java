/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.UserCredentials;
import java.net.URI;
import java.util.List;

/**
 *
 * @author uha
 */
public abstract class ProcessObjectTestSeries extends RequestModelVersionDescriptionTestSeries {

    private Class testClass = null;

    public abstract List<URI> getURIs(URI uri, UserCredentials credentials);

    public void setTest(Class<? extends URITest> testClass) {
        this.testClass = testClass;
    }

    @Override
    void makeTest(ModelVersionDescription modelVersionDesc) {
        /*
        try {
            URI uri = modelVersionDesc.getParentModelDescription().getUri();
            URI tmpModelURI = TemporaryServerModel.getTemporaryModelURI(uri);
            List<URI> nodeURIs = getURIs(tmpModelURI, LoadTestConfiguration.getCredentials());
            URI last = null;
            for (URI nodeUri : nodeURIs) {
                try {
                    if (createdTests >= numberOfTests) {
                        return;
                    }
                    URITest test = (URITest) testClass.newInstance();
                    test.setURI(nodeUri, last != null ? last : nodeUri);
                    addTest(test);
                    last = nodeUri;
                    createdTests++;
                } catch (InstantiationException ex) {
                    Logger.getLogger(ProcessObjectTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ProcessObjectTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessObjectTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
         * 
         */
    }
}
