/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.testUtils;

import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;
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
public class ModelGeneratorTest {

    public ModelGeneratorTest() {
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
    public void testBase64Strings() {

    }

    /**
     * Test of generate method, of class ModelGenerator.
     */
    @Test
    public void testGenerate() {
        System.out.println("generate");
        Class ModelType = BPMNModel.class;
        int avgNodeNumber = 40;
        int avgEdgeNumber = 40;
        ModelGenerator instance = new ModelGenerator();
        Seed seed = new Seed();
        ProcessModel expResult = instance.generate(seed,ModelType, avgNodeNumber, avgEdgeNumber);
        ProcessModel result = instance.generate(seed, ModelType, avgNodeNumber, avgEdgeNumber);
        assertEquals(expResult.getNodes().size(), result.getNodes().size());
        // TODO review the generated test code and remove the default call to fail.
        
    }

}