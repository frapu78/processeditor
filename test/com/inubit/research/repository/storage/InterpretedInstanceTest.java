/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.repository.storage;

import com.inubit.research.server.domainStorage.data.Storage;
import com.inubit.research.server.domainStorage.data.InterpretedInstance;
import org.junit.Ignore;
import com.inubit.research.test.helper.factories.InterpretedInstanceFactory;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import java.util.HashMap;
import net.frapu.code.visualization.domainModel.DomainClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Uwe
 */
@RunWith(Parameterized.class)
public class InterpretedInstanceTest {
    
    
    InterpretedInstance instance;
    Storage store;
    
    @Parameters
    public static Collection<Object[]> parameters() {
        return StorageTest.parameters();
    }

    public InterpretedInstanceTest(Storage currentStore) {
        store = currentStore;
        instance = InterpretedInstanceFactory.build(currentStore);
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
        store.clear();
    }


    /**
     * Test of setAttribute method, of class InterpretedInstance.
     */
    @Test
    @Ignore
    public void testSetAttribute() {
        System.out.println("setAttribute");

        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        //InterpretedInstance result = instance.setAttribute(attribute, value);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAttribute method, of class InterpretedInstance.
     */
    @Test
    public void testGetAttribute() {
        System.out.println("getAttribute");
        String attribute = "Price";
        instance.getInterpreter().addAttribute("Price");
        String expResult = "29.30";
        instance.setAttribute(attribute, expResult);        
        Object result = instance.getAttribute(attribute);
        assertEquals(expResult, result);
    }

    /**
     * Test of setAssociation method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testSetAssociation() {
        System.out.println("setAssociation");
        String role = "";
        InterpretedInstance value = null;
        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        InterpretedInstance result = instance.setAssociation(role, value);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAssociation method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testGetAssociation() {
        System.out.println("getAssociation");
        String role = "";
        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        InterpretedInstance result = instance.getAssociation(role);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAggregation method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testSetAggregation() {
        System.out.println("setAggregation");
        String role = "";
        InterpretedInstance value = null;
        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        InterpretedInstance result = instance.setAggregation(role, value);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAggregation method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testGetAggregation() {
        System.out.println("getAggregation");
        String role = "";
        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        InterpretedInstance result = instance.getAggregation(role);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAttributes method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testGetAttributes() {
        System.out.println("getAttributes");
        InterpretedInstance instance = null;
        HashMap expResult = null;
        HashMap result = instance.getAttributes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clean method, of class InterpretedInstance.
     */
    @Ignore
    @Test
    public void testClean() {
        System.out.println("clean");
        InterpretedInstance instance = null;
        InterpretedInstance expResult = null;
        InterpretedInstance result = instance.clean();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}