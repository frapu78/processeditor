/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.testUtils;

import com.inubit.research.testUtils.Seed;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SeedTest {

    public SeedTest() {
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
     * Test of getSeed method, of class Seed.
     */
    @Test
    public void testGetSeed() {
        System.out.println("getSeed");
        Seed instance = new Seed();
        Seed backup = new Seed(instance);
        assertArrayEquals(instance.getSeed(), backup.getSeed());
    }



    /**
     * Test of decide method, of class Seed.
     */
    @Test
    public void testDecide() {
        System.out.println("decide");
        int numberDecissions = 0;
        Seed instance = new Seed();
        Seed backup = new Seed(instance);
        assertArrayEquals(instance.getSeed(), backup.getSeed());
        int d1 = instance.decide(30);
        int d2 = backup.decide(30);
        assertEquals(d1, d2);
    }

    @Test
    public void testHash() {
        try {
            Seed instance = new Seed();
            Seed second = new Seed(instance);
            byte[] h1 = instance.hash(second.getSeed());
            byte[] h2 = second.hash(instance.getSeed());
            assertArrayEquals(h1, h2);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SeedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testBase64Strings() {
        try {
            Seed instance = new Seed();
            String s = instance.toString();
            Seed second = new Seed(s);
            int d1 = instance.decide(2);
            int d2 = second.decide(2);
            assertEquals(d1, d2);
        } catch (Exception ex) {
            Logger.getLogger(SeedTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


}