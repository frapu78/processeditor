/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization;

import net.frapu.code.visualization.ProcessModel;
import com.inubit.research.gui.WorkbenchHelper;
import java.awt.Point;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.inubit.research.testUtils.TestUtils.*;

/**
 *
 * @author uha
 */
public class DefaultRoutingPointLayouterTest {

    public DefaultRoutingPointLayouterTest() {
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
    public void testDockingPointDirectionTests() {
        Task t = new Task();
        t.getDefaultConnectionPoints();
        int spacing = 30;
        Point p1 = new Point(0 - (t.getSize().width / 2), 0);
        Point p2 = new Point(0, 0 - (t.getSize().height / 2));
        Point p3 = new Point(0 - (t.getSize().width / 2) + spacing, 0 - (t.getSize().height / 2));
        Point p4 = new Point((t.getSize().width / 2) - spacing, 0 - (t.getSize().height / 2));
        Point p5 = new Point(0 - (t.getSize().width / 2) + spacing, (t.getSize().height / 2));
        Point p6 = new Point((t.getSize().width / 2) - spacing, (t.getSize().height / 2));
        Point p7 = new Point((t.getSize().width / 2), 0);
        Point p8 = new Point(0, (t.getSize().height / 2));
        assertTrue(DefaultRoutingPointLayouter.isWestDockingPoint(p1, t));
        assertTrue(DefaultRoutingPointLayouter.isNorthDockingPoint(p2, t));
        assertTrue(DefaultRoutingPointLayouter.isNorthDockingPoint(p3, t));
        assertTrue(DefaultRoutingPointLayouter.isNorthDockingPoint(p4, t));
        assertTrue(DefaultRoutingPointLayouter.isSouthDockingPoint(p5, t));
        assertTrue(DefaultRoutingPointLayouter.isSouthDockingPoint(p6, t));
        assertTrue(DefaultRoutingPointLayouter.isEastDockingPoint(p7, t));
        assertTrue(DefaultRoutingPointLayouter.isSouthDockingPoint(p8, t));
    }

    @Test
    public void testHasRightDirection() {
        Task t1 = new Task();
        t1.setPos(200, 300);
        Task t2 = new Task();
        t2.setPos(221, 261);
        ProcessEdge edge = new MessageFlow(t1, t2);
        edge.addRoutingPoint(0, new Point(281, 261));
        edge.addRoutingPoint(0, new Point(281, 300));
        edge.setSourceDockPointOffset(new Point(50, 0));
        edge.setTargetDockPointOffset(new Point(50, 0));
        assertTrue((new DefaultRoutingPointLayouter()).endDirectionsCorrect(edge, false));
    }

    @Test
    public void testrectify() {
        Task t1 = new Task();
        t1.setPos(350, 250);
        Task t2 = new Task();
        t2.setPos(254, 198);
        ProcessEdge edge = new MessageFlow(t1, t2);
        edge.addRoutingPoint(0, new Point(314, 198));
        edge.addRoutingPoint(0, new Point(290, 250));
        edge.setSourceDockPointOffset(new Point(-50, 0));
        edge.setTargetDockPointOffset(new Point(50, 0));
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        assertTrue(l.endDirectionsCorrect(edge, true));
        l.rectifyEdge(edge);
        assertTrue(l.endDirectionsCorrect(edge, false));
    }

    @Test
    public void testBPMNTaskCase1() {
        BPMNModel model = new BPMNModel();
        ProcessEditor editor = new ProcessEditor(model);
        //we call the layouter manually
        editor.setLayoutEdges(false);
        Task t1 = new Task();
        Task t2 = new Task();
        int testRangeStartX = 100;
        int testRangeStartY = 100;
        int testRangeEndX = (int) (testRangeStartX + 5 * Math.max(t1.getBounds().getWidth(), t2.getBounds().getWidth()));
        int testRangeEndY = (int) (testRangeStartY + 5 * Math.max(t1.getBounds().getHeight(), t2.getBounds().getHeight()));
        t1.setPos(350, 250);
        t2.setPos(254, 198);
        ProcessEdge edge = new MessageFlow(t1, t2);
        edge.addRoutingPoint(0, new Point(410, 231));
        edge.addRoutingPoint(0, new Point(400, 250));
        edge.setSourceDockPointOffset(new Point(50, 0));
        edge.setTargetDockPointOffset(new Point(50, 0));

        RoutingPointLayouter layouter;

        layouter = model.getUtils().getRoutingPointLayouter();

        layouter.optimizeRoutingPoints(edge, t2);
        assertTrue(getLayoutDebugMessage(edge), layouter.isCorrectlyLayouted(edge));

    }

    @Test
    public void testDeltaIntersect() {
        Task t1 = new Task();
        Task t2 = new Task();
        t1.setPos(350, 250);
        t2.setPos(275, 219);
        ProcessEdge edge = new MessageFlow(t1, t2);
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        assertTrue(l.deltaIntersect(edge));
    }

    @Test
    public void testDeltaIntersectSubprocess() {
        Task t1 = new Task();
        SubProcess t2 = new SubProcess();
        t1.setPos(600, 400);
        t2.setPos(435, 515);
        ProcessEdge edge = new MessageFlow(t1, t2);
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        assertFalse(l.deltaIntersect(edge));
    }

    @Test
    public void testGateway() {
        Gateway t1 = new Gateway();
        Task t2 = new Task();
        t1.setPos(75, 198);
        t2.setPos(232, 179);
        ProcessEdge edge = new MessageFlow(t2, t1);
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        l.optimizeRoutingPoints(edge, t1);
        assertTrue(l.isCorrectlyLayouted(edge));
    }

    @Test
    public void testSubProcess() {
        Task t1 = new Task();
        SubProcess t2 = new SubProcess();
        t1.setPos(600, 400);
        t2.setPos(490, 515);
        ProcessEdge edge = new SequenceFlow(t1, t2);
        edge.setSourceDockPointOffset(new Point(-20, 30));
        edge.addRoutingPoint(0, new Point(580, 515));
        edge.addRoutingPoint(1, new Point(601, 515));
        edge.setTargetDockPointOffset(new Point(99, 0));
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        l.optimizeRoutingPoints(edge, t1);
        assertTrue(l.isCorrectlyLayouted(edge));
    }

    @Test
    public void testSubProcess2() {
        Task t1 = new Task();
        SubProcess t2 = new SubProcess();
        t1.setPos(600, 400);
        t2.setPos(465, 515);
        ProcessEdge edge = new SequenceFlow(t1, t2);
        edge.setSourceDockPointOffset(new Point(-20, 29));
        edge.addRoutingPoint(0, new Point(580, 515));
        edge.addRoutingPoint(1, new Point(574, 515));
        edge.setTargetDockPointOffset(new Point(99, 0));
        DefaultRoutingPointLayouter l = new DefaultRoutingPointLayouter();
        l.optimizeRoutingPoints(edge, t1);
        assertTrue(l.isCorrectlyLayouted(edge));
    }

    @Ignore
    @Test
    public void testBPMNTaskLayout() throws InstantiationException, IllegalAccessException {
        int accuracy = 49;

        //System.out.println("Warning! This test may take up to serveral hours.");
        for (Class<? extends ProcessModel> modelClass : WorkbenchHelper.getSupportedProcessModels()) {
            ProcessModel model = modelClass.newInstance();
            ProcessEditor editor = new ProcessEditor(model);
            //we call the layouter manually
            for (Class nodeClass1 : model.getSupportedNodeClasses()) {
                for (Class nodeClass2 : model.getSupportedNodeClasses()) {
                    ProcessNode superNode1 = (ProcessNode) nodeClass1.newInstance();
                    ProcessNode superNode2 = (ProcessNode) nodeClass2.newInstance();
                    for (Class nodeC1 : superNode1.getVariants()) {
                        for (Class nodeC2 : superNode2.getVariants()) {
                            editor.setLayoutEdges(false);
                            ProcessNode t1 = null;
                            ProcessNode t2 = null;
                            t1 = (ProcessNode) nodeC1.newInstance();
                            t2 = (ProcessNode) nodeC2.newInstance();
                            int testRangeStartX = 100;
                            int testRangeStartY = 100;
                            int testRangeEndX = (int) (testRangeStartX + 5 * Math.max(t1.getBounds().getWidth(), t2.getBounds().getWidth()));
                            int testRangeEndY = (int) (testRangeStartY + 5 * Math.max(t1.getBounds().getHeight(), t2.getBounds().getHeight()));
                            MessageFlow edge = new MessageFlow(t1, t2);
                            RoutingPointLayouter layouter;
                            t1.setPos(DefaultRoutingPointLayouter.middle(testRangeStartX, testRangeEndX), DefaultRoutingPointLayouter.middle(testRangeStartY, testRangeEndY));
                            layouter = model.getUtils().getRoutingPointLayouter();
                            for (int x = testRangeStartX; x <= testRangeEndX; x += accuracy) {
                                for (int y = testRangeStartY; y <= testRangeEndY; y += accuracy) {
                                    t2.setPos(x, y);
                                    layouter.optimizeRoutingPoints(edge, t2);
                                    assertTrue(getLayoutDebugMessage(edge), layouter.isCorrectlyLayouted(edge));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
