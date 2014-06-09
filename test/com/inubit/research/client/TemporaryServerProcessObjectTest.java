/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.client;

import com.inubit.research.server.ProcessEditorServer;
import java.awt.Point;
import java.net.URI;
import net.frapu.code.visualization.ProcessNode;
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
public class TemporaryServerProcessObjectTest {

    public TemporaryServerProcessObjectTest() {
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

    public static TemporaryServerProcessObject getTemporaryServerProcessObject() throws Exception {
        TemporaryServerModel model = TemporaryServerModelTest.getTmpModel();
        return new TemporaryServerProcessObject(model.getNodeURIs().get(0), ModelServer.getDefaultCredentials());
    }





    /**
     * Test of setPos method, of class TemporaryServerProcessObject.
     */
    @Test
    public void testSetPos() throws Exception {
        TemporaryServerModel tmpModel = TemporaryServerModelTest.getTmpModel();
        Point p = new Point(50,50);
        for (URI uri : tmpModel.getNodeURIs()) {
            TemporaryServerProcessObject to = new TemporaryServerProcessObject(uri, ModelServer.getDefaultCredentials());
            to.setPos(p);
            ProcessNode node = (ProcessNode) to.getObject();
            assertTrue(node.getPos().equals(p));
        }
    }

    /**
     * Test of setDimension method, of class TemporaryServerProcessObject.
     */
    @Test
    public void testSetDimension_Dimension_Point() throws Exception {
//        TemporaryServerModel tmpModel = TemporaryServerModelTest.getTmpModel();
//        Point p = new Point(50,50);
//        Dimension d = new Dimension(40, 40);
//        for (URI uri : tmpModel.getNodeURIs()) {
//            TemporaryServerProcessObject to = new TemporaryServerProcessObject(uri, ModelServer.getDefaultCredentials());
//            to.setDimension(d,p);
//            ProcessNode node = (ProcessNode) to.getObject();
//            assertTrue(node.getPos().equals(p));
//            //assertTrue(node.getSize().equals(d));
//        }
    }





}
