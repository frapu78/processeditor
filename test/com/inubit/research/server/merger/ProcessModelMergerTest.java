/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger;

import com.inubit.research.server.merger.animator.ProcessMergeAnimatorTest;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
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
public class ProcessModelMergerTest {

    private BPMNModel model1;
    private BPMNModel model2;
    private ProcessModelMerger merger;

    public ProcessModelMergerTest() {
    }

    private BPMNModel load(String filename) {
        try {
            ProcessEditorImporter importer = new ProcessEditorImporter();
            File file = new File("models\\Merger-Tests\\" + filename );
            return (BPMNModel) importer.parseSource(file).get(0);
        } catch (Exception ex) {
            Logger.getLogger(ProcessMergeAnimatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        model1 = new BPMNModel();
        model2 = new BPMNModel();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void TestEdgeDocker() {
        model2 = (BPMNModel) load("edgeDocker1.model");
        merger = new ProcessModelMerger(model1, model1, model2);
        ModelComparator comp = new ModelComparator();
        assertTrue(comp.modelEquals(load("edgeDocker1.model"),merger.getUnmarkedMergedModel()));
    }

    @Test
    public void TestObjectsHavePositionOfTarget() {
        Task testTask1 = new Task(100, 100, "Test");
        Task testTask2 = (Task) testTask1.clone();
        testTask2.setPos(200,250);
        model1.addNode(testTask1);
        model2.addNode(testTask2);
        merger = new ProcessModelMerger(model1, model1, model2);
        ProcessObjectMerger m = merger.getObjectMerger(testTask2);
        assertTrue(m.isDestinyEqual());
        //assertFalse(m.getMergedObject().equals(testTask1));
        assertTrue(((ProcessNode)m.getMergedObject()).getPos().x==200);
        assertTrue(((ProcessNode)m.getMergedObject()).getPos().y==250);

    }

    @Test
    public void TestAlphaRemovedObject() {
        Task testTask1 = new Task(100, 100, "Test");
        model1.addNode(testTask1);
        merger = new ProcessModelMerger(model1, model1, model2);
        ProcessObjectMerger m = merger.getObjectMerger(testTask1);
        assertTrue(m.isDestinyRemove());
        assertTrue(((ProcessNode)m.getAnimateTo()).getBackground().equals(ProcessObjectMerger.ColorRemoved));
        assertTrue(m.getAnimateTo().getAlpha()<1.0);
        assertTrue(merger.getMergedModel().getNodes().get(0).getAlpha()<1.0);
    }

    
    public void TestRoutingPoints() {
        model1 = (BPMNModel) load("routing1.model");
        model2 = (BPMNModel) load("routing2.model");
        merger = new ProcessModelMerger(model1, model1, model2);
        for (ProcessObjectMerger o : merger.getMergeRelations()) {
            if(!o.getMergedObject().equals(o.getTargetObject()) && !(o.getMergedObject() instanceof Cluster)) {
                assertTrue(false);
            }
        }
    }

    @Test
    public void TestRemoveTask() {
        Task testTask1 = new Task(100, 200, "Test");
        model1.addNode(testTask1);
        Task testTask2 = new Task(100, 400, "Test");
        model1.addNode(testTask2);
        Task testTask3 = new Task(100, 600, "Test");
        model1.addNode(testTask3);
        SequenceFlow edge1 = new SequenceFlow(testTask1, testTask2);
        model1.addEdge(edge1);
        SequenceFlow edge2 = new SequenceFlow(testTask2, testTask3);
        model1.addEdge(edge2);

        model2 = (BPMNModel) model1.clone();
        BPMNModel model3 = (BPMNModel) model1.clone();

        model3.removeNode(model3.getNodeById(testTask2.getId()));
        merger = new ProcessModelMerger(model1, model2, model3);
        assert(merger.getObjectMerger(testTask2.getId()).isDestinyRemove());
        assert(merger.getMergedModel().getNodeById(testTask2.getId()).getAlpha()<1.0);
        assert(merger.getMergedModel().getNodeById(testTask2.getId()).getBackground().equals(ProcessObjectMerger.ColorRemoved));
    }

    @Test
    public void TestRemoveTaskwithMyVersion() {
        Task testTask1 = new Task(100, 200, "Test");
        model1.addNode(testTask1);
        Task testTask2 = new Task(100, 400, "Test");
        model1.addNode(testTask2);
        Task testTask3 = new Task(100, 600, "Test");
        model1.addNode(testTask3);
        SequenceFlow edge1 = new SequenceFlow(testTask1, testTask2);
        model1.addEdge(edge1);
        SequenceFlow edge2 = new SequenceFlow(testTask2, testTask3);
        model1.addEdge(edge2);

        model2 = (BPMNModel) model1.clone();
        BPMNModel model3 = (BPMNModel) model1.clone();

        model3.removeNode(model3.getNodeById(testTask2.getId()));
        merger = new ProcessModelMerger(model1, model3, model2);
        assertTrue(merger.getMergedModel().getObjectById(testTask1.getId())!=null);
        assertTrue(merger.getMergedModel().getObjectById(testTask2.getId())==null);
        assertTrue(merger.getObjectMerger(testTask2.getId()).isDestinyKeep());
    }

    @Test
    public void TestAttachedEventContainedInCluster() {
        model1 = load("AttachedEventPool.v0.model");
        model2 = load("AttachedEventPool.v1.model");
       ProcessModel model3 = load("AttachedEventPool.v2.model");
       merger = new ProcessModelMerger(model1, model2, model3);
       ProcessNode contNode = merger.getMergedModel().getNodeById("8490467");
       Pool cluster = (Pool) merger.getMergedModel().getNodeById("2183019");
       assertTrue(merger.getMergedModel().getObjectById("32443204")!=null);
       //assertTrue(cluster.getProcessNodes().size()==8);
       //assertTrue(merger.getMergedModel().getClusterForNode(contNode).equals(cluster));

    }


    

}