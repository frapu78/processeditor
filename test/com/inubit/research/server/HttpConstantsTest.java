/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author uha
 */
public class HttpConstantsTest {

    public HttpConstantsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }



    /**
     * Test of getBaseDirectory method, of class HttpConstants.
     */
    @Test
    public void testGetBaseDirectory() {
        System.out.println("getBaseDirectory");
        URI modelURI = null;
        try {
            modelURI = new URI("http://localhost:8080/Processeditor/models/new?type=net.frapu.code.visualization.uml.ClassModel");
        } catch (URISyntaxException ex) {
            Logger.getLogger(HttpConstantsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String expResult = "http://localhost:8080/Processeditor";
        String result = HttpConstants.getBaseDirectory(modelURI);
        assertEquals(expResult, result);
    }

}