/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger;

import net.frapu.code.visualization.ProcessObject;
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
public class ProcessObjectComparatorTest {

    public ProcessObjectComparatorTest() {
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
     * Test of equals method, of class ProcessObjectComparator.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        ProcessObject processObject1 = new Task();
        ProcessObject processObject2 = (ProcessObject) processObject1.clone();
        ProcessObjectComparator instance = new ProcessObjectComparator();
        boolean expResult = true;
        boolean result = instance.equals(processObject1, processObject2);
        assertEquals(expResult, result);
        processObject2.setProperty(Task.PROP_TEXT, "Changed");
        result = instance.equals(processObject1, processObject2);
        assertFalse(result);
    }

    /**
     * Test of exclude method, of class ProcessObjectComparator.
     */
    @Test
    public void testExclude() {
        System.out.println("exclude");
        String property = Task.PROP_TEXT;
        ProcessObjectComparator instance = new ProcessObjectComparator();
        ProcessObject processObject1 = new Task();
        ProcessObject processObject2 = (ProcessObject) processObject1.clone();
        assertTrue(instance.equals(processObject1, processObject2));
        processObject2.setProperty(property, "Changed");
        instance.exclude(property);
        boolean expResult = true;
        boolean result = instance.equals(processObject1, processObject2);
        assertEquals(expResult, result);
        instance.include(property);
        assertFalse(instance.equals(processObject1, processObject2));
    }



}