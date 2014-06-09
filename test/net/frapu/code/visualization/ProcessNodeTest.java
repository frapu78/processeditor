/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization;

import java.awt.Point;
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
public class ProcessNodeTest {

    public ProcessNodeTest() {
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
    public void testBoundContains() {
        Task t1 = new Task(500, 500, "test");
        Point test = t1.getPos();
        test.x = 549;
        System.out.println(t1.getBounds().getMaxX());
        System.out.println(t1.getBounds().getMinX());
        assertTrue(t1.getBounds().contains(test));
        test.x = 549;
        assertTrue(t1.getBounds().contains(test));
        test = t1.getPos();
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMinX()+1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMaxX()-1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.y=(int) t1.getBounds().getMinY()+1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.y=(int) t1.getBounds().getMaxY()-1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMinX()+1;
        test.y=(int) t1.getBounds().getMinY()+1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMaxX()-1;
        test.y=(int) t1.getBounds().getMinY()+1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMaxX()-1;
        test.y=(int) t1.getBounds().getMaxY()-1;
        assertTrue(t1.getBounds().contains(test));
        test.setLocation(t1.getPos());
        test.x=(int) t1.getBounds().getMinX()+1;
        test.y=(int) t1.getBounds().getMaxY()-1;
        assertTrue(t1.getBounds().contains(test));

    }

}