/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization;

import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;
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
public class ProcessModelTest {

    public ProcessModelTest() {
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

    @Test
    public void testSomeMethod() {
    }

    @Test
    public void TestRemovalOfEdgeConnectedToEdgeDocker() {
        BPMNModel m = new BPMNModel();
        Task task1 = new Task(100, 100, "Task1");
        m.addNode(task1);
        Task task2 = new Task(100, 200, "Task2");
        m.addNode(task2);
        DataObject data = new DataObject(300, 300, "DataObject");
        m.addNode(data);
        ProcessEdge edge1 = new SequenceFlow(task1, task2);
        m.addEdge(edge1);
        EdgeDocker edgeDocker = new EdgeDocker(edge1);
        m.addNode(edgeDocker);
        ProcessEdge edge2 = new Association(edgeDocker, data);
        m.addEdge(edge2);
        m.removeObject(edge2);
        m.removeNode(data);
       assertFalse(m.getNodes().contains(edgeDocker));
       assertFalse(m.getEdges().contains(edge2));
       assertFalse(m.getNodes().contains(data));
    }

    @Test
    public void TestRemoveNodeFromCluster() {
        BPMNModel m = new BPMNModel();
        Task task1 = new Task(100, 100, "Task1");
        m.addNode(task1);
        Pool pool = new Pool(50, 50, "Pool");
        m.addNode(pool);
        pool.addProcessNode(task1);
        m.removeNode(task1);
        assertFalse(pool.getProcessNodes().contains(task1));
    }

    @Test
    public void TestgetAttachedNode() {
        BPMNModel m = new BPMNModel();
        Task task1 = new Task(100, 100, "Task1");
        m.addNode(task1);
        MessageIntermediateEvent attached = new MessageIntermediateEvent();
        attached.setParentNode(task1);
        m.addNode(attached);
        Task task2 = new Task(100, 100, "Task1");
        m.addNode(task2);
        assertTrue(m.getAttachedNode(task1)==attached);
        assertTrue(m.getAttachedNode(task2)==null);
    }

}