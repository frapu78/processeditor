/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.client.InvalidUserCredentialsException;
import com.inubit.research.client.ModelDescription;
import com.inubit.research.client.ModelDirectory;
import com.inubit.research.client.ModelDirectoryEntry;
import com.inubit.research.client.ModelServer;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import com.inubit.research.gui.plugins.serverLoadTests.TestSeries;
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
public abstract class RequestModelVersionDescriptionTestSeries extends TestSeries {

    protected int createdTests = 0;

    abstract void makeTest(ModelVersionDescription modelVersionDesc);

    private void createTestsForModelsInDirectory(ModelDirectory dir) {
        for (ModelDirectoryEntry entry : dir.getEntries()) {
            if (entry instanceof ModelDescription) {
                try {
                    ModelDescription md = (ModelDescription) entry;
                    for (ModelVersionDescription modelVersionDesc : md.getModelVersionDescriptions()) {
                        if (createdTests < getNumberOfTests()) {
                            makeTest(modelVersionDesc);
                            createdTests++;
                        } else {
                            return;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XMLHttpRequestException ex) {
                    Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidUserCredentialsException ex) {
                    Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (entry instanceof ModelDirectory) {
                createTestsForModelsInDirectory((ModelDirectory) entry);
            }
        }

    }

    @Override
    public void prepareTests() {
        try {            
            ModelServer server = new ModelServer(LoadTestConfiguration.serverURL, "", LoadTestConfiguration.credentials);
            createTestsForModelsInDirectory(server.getDirectory());
        } catch (IOException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidUserCredentialsException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(RequestModelVersionDescriptionTestSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
