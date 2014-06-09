/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.testUtils;

import com.inubit.research.testUtils.TestUtils;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
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
public class TestUtilsTest {

    public TestUtilsTest() {
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
     * Test of getModelCode method, of class TestUtils.
     */
    @Test
    public void testGetModelCode() throws Exception {
        System.out.println("getModelCode");
        ProcessModel model = TestUtils.loadTestModel("Backery.model");
        TestUtils.getModelCode(model);
        // TODO review the generated test code and remove the default call to fail.

    }

}