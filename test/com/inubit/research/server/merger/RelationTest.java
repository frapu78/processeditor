/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.merger;

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
public class RelationTest {

    public RelationTest() {
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
     * Test of containsKeys method, of class Relation.
     */
    @Test
    public void testContainsKeys() {
        System.out.println("containsKeys");
        String key1 = null;
        String key2 = null;
        Relation instance = new Relation();
        boolean expResult = false;
        boolean result = instance.containsKeys(key1, key2);
        assertEquals(expResult, result);
        key1 = "Key1";
        key2 = "Key2";
        String[] res = {key1,key2};
        instance.put(res, "Value");
        assertTrue(instance.containsKeys(key1, key2));

    }

    /**
     * Test of containsKey1 method, of class Relation.
     */
    @Test
    public void testContainsKey1() {
        System.out.println("containsKey1");
        Object key1 = null;
        Relation instance = new Relation();
        boolean expResult = false;
        boolean result = instance.containsKey1(key1);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

    /**
     * Test of containsKey2 method, of class Relation.
     */
    @Test
    public void testContainsKey2() {
        System.out.println("containsKey2");
        Object key2 = null;
        Relation instance = new Relation();
        boolean expResult = false;
        boolean result = instance.containsKey2(key2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }





    /**
     * Test of remove method, of class Relation.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Object key = null;
        Relation instance = new Relation();
        Object expResult = null;
        Object result = instance.remove(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }



    /**
     * Test of get method, of class Relation.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        Object key1 = null;
        Object key2 = null;
        Relation instance = new Relation();
        Object expResult = null;
        Object result = instance.get(key1, key2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

}