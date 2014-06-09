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
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.XMLHttpRequestException;
import com.inubit.research.gui.plugins.serverLoadTests.LoadTest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author uha
 */
public class RequestPreviewTest extends LoadTest {

    private ModelVersionDescription modelVersionDescription;

    public RequestPreviewTest(ModelVersionDescription modelVersionDescription) {
        this.modelVersionDescription = modelVersionDescription;
    }



    @Override
    public void runTest() {
        try {
            modelVersionDescription.getPreview();
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLHttpRequestException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidUserCredentialsException ex) {
            Logger.getLogger(RequestPreviewTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
