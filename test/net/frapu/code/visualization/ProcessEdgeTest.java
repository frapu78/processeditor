/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.frapu.code.visualization;

import net.frapu.code.visualization.bpmn.Task;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNRoutingPointLayouter;
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
public class ProcessEdgeTest {

    public ProcessEdgeTest() {
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
    public void testCorrectedDockPointOffsetSimple() {
        System.out.println("correctedDockPointOffset");
        Task t1 = new Task();
        Task t2 = new Task();
        t1.setPos(500, 500);
        t2.setPos(200, 200);
        ProcessEdge instance = new Association(t1, t2);
        BPMNRoutingPointLayouter l = new BPMNRoutingPointLayouter();
        l.optimizeRoutingPoints(instance, null);
        assertTrue(l.isCorrectlyLayouted(instance));
        Point result;
        int x = -300;
        int y = -300;
        result = instance.correctedDockPointOffset(new Point(x, y), t1);
        result.translate(t2.getPos().x, t2.getPos().y);
        assertTrue(l.isStraight(instance, 0, 0));
        assertTrue(l.endDirectionsCorrect(instance, true));
        assertTrue(new Point(x, y) + "---" + result, t2.contains(result));
        assertFalse(new Point(x, y) + "---" + result, t2.containsDeepInside(result));
    }

    /**
     * Test of correctedDockPointOffset method, of class ProcessEdge.
     */
    @Test
    public void testCorrectedDockPointOffset() {
        System.out.println("correctedDockPointOffset");
        Task t1 = new Task();
        Task t2 = new Task();
        t1.setPos(500, 500);
        t2.setPos(200, 200);
        ProcessEdge instance = new Association(t1, t2);
        BPMNRoutingPointLayouter l = new BPMNRoutingPointLayouter();
        l.optimizeRoutingPoints(instance, null);
        assertTrue(l.isCorrectlyLayouted(instance));

        Point result;
        for (int x = -150; x < 150; x++) {
            for (int y = -150; y < 150; y++) {
                result = instance.correctedDockPointOffset(new Point(x, y), t2);
                result.translate(t2.getPos().x, t2.getPos().y);
                assertTrue(l.isStraight(instance, 0, 0));
                assertTrue(l.endDirectionsCorrect(instance, true));
                assertTrue(new Point(x, y) + "---" + result, t2.contains(result));
                assertFalse(new Point(x, y) + "---" + result, t2.containsDeepInside(result));
            }
        }

    }

    public class ProcessEdgeImpl extends ProcessEdge {

        public Shape getSourceShape() {
            return null;
        }

        public Shape getTargetShape() {
            return null;
        }

        public Stroke getLineStroke() {
            return null;
        }
    }
}
