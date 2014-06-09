/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.client;

import com.inubit.research.server.ProcessEditorServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.bpmn.BPMNModel;
import java.net.URI;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessModel;
import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import java.io.IOException;
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
public class ServerModelTest {

    public ServerModelTest() {
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

    public static ServerModel getModel() throws IOException, Exception {
        Seed seed = new Seed("srmmhsrhsrdhngcghncvhdfhrtjejftrjdmdfjhdfjhdfjtjukhg");
        ModelGenerator gen = new ModelGenerator();
        ProcessModel m = gen.generate(seed, BPMNModel.class, 50, 50);
        ModelServer server = new ModelServer();
        m.setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, ModelServer.getDefaultCredentials());
        ModelDirectory.publishToServer(m, true, null, ModelServer.getDefaultLocalURI().toString(), "comment", "testFolder", "titleTest", null);
        TemporaryServerModel tmpModel = new TemporaryServerModel(new URI(m.getProcessModelURI()), ModelServer.getDefaultCredentials());
        return tmpModel;
    }

    /**
     * Test of getNodeURIs method, of class ServerModel.
     */
    @Test
    public void testGetNodeURIs() throws Exception {
        Seed seed = new Seed("srmmhsrhsrdhngcghncvhdfhrtjejftrjdmdfjhdfjhdfjtjukhg");
        ModelGenerator gen = new ModelGenerator();
        ProcessModel m = gen.generate(seed, BPMNModel.class, 50, 50);
        assertTrue(getModel().getNodeURIs().size()==m.getNodes().size());
    }

    /**
     * Test of getEdgeURIs method, of class ServerModel.
     */
    @Test
    public void testGetEdgeURIs() throws Exception {
        Seed seed = new Seed("srmmhsrhsrdhngcghncvhdfhrtjejftrjdmdfjhdfjhdfjtjukhg");
        ModelGenerator gen = new ModelGenerator();
        ProcessModel m = gen.generate(seed, BPMNModel.class, 50, 50);
        assertTrue(getModel().getEdgeURIs().size()==m.getEdges().size());
    }

}