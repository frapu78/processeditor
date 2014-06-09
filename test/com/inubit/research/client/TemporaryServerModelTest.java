/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.client;

import com.inubit.research.server.ProcessEditorServer;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessModel;
import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.frapu.code.visualization.bpmn.BPMNModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author uha
 */
public class TemporaryServerModelTest {

    public TemporaryServerModelTest() {
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

    public static TemporaryServerModel getTmpModel() throws IOException, Exception {
        Seed seed = new Seed("srhsrhsrzdfhfgdhngcghncvhdfhrtjejftrjdmdfjhdfjhdfjtjukhg");
        ModelGenerator gen = new ModelGenerator();
        ProcessModel m = gen.generate(seed, BPMNModel.class, 50, 50);
        ModelServer server = new ModelServer();
        m.setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, ModelServer.getDefaultCredentials());
        ModelDirectory.publishToServer(m, true, null, ModelServer.getDefaultLocalURI().toString(), "comment", "testFolder", "titleTest", null);
        TemporaryServerModel tmpModel = new TemporaryServerModel(new URI(m.getProcessModelURI()), ModelServer.getDefaultCredentials());
        return tmpModel;
    }

    @Test
    public void testConstructor() throws URISyntaxException, Exception {

        getTmpModel();

    }

    @Test
    public void testAddTestToModel() throws URISyntaxException, Exception {
        TemporaryServerModel tmpModel = getTmpModel();
        URI uri = tmpModel.addNodeToModel(Task.class);
        boolean found = false;
        ServerProcessObject so = new ServerProcessObject(uri, ModelServer.getDefaultCredentials());
        assert so.getObject() instanceof Task;
    }

    /**
     * Test of getTemporaryModelURI method, of class TemporaryServerModel.
     */
    @Test
    public void testGetTemporaryModelURI() throws Exception {
        System.out.println("getTemporaryModelURI");

    }

    /**
     * Test of setUri method, of class TemporaryServerModel.
     */
    @Test
    public void testSetUri() {
        System.out.println("setUri");
    }


}
