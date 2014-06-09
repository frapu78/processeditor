/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.client;

import com.inubit.research.server.ProcessEditorServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import net.frapu.code.visualization.ProcessObject;
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
public class ServerProcessObjectTest {

    ServerProcessObject serverProcessObject;

    public ServerProcessObjectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProcessEditorServer.startForWorkbench();
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

    public static ServerProcessObject getServerProcessObject() throws Exception {
        ServerModel model = ServerModelTest.getModel();
        return new ServerProcessObject(model.getNodeURIs().get(0), ModelServer.getDefaultCredentials());
    }

    /**
     * Test of getObject method, of class ServerProcessObject.
     */
    @Test
    public void testGetObject() throws Exception {
        System.out.println("getObject");
        serverProcessObject = getServerProcessObject();
        serverProcessObject.getObject();
    }

    @Test
    public void testGetImage() throws IOException, ParserConfigurationException, XMLHttpRequestException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        getServerProcessObject().getImage();
    }

}