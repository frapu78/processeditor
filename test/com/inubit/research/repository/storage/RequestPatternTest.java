/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.repository.storage;

import com.inubit.research.server.domainStorage.rest.RequestPattern;
import java.util.LinkedList;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
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
public class RequestPatternTest {

    String url;
    String modelID;
    int version;
    String dbName;
    Integer classID;
    int instanceID;

    @Parameters
    public static Collection<Object[]> getExampleURIs() {
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        Object[] parameters;
        parameters = new Object[]{"/data/modelID/dbName/12345/1234", "modelID", -1, "dbName", 12345, 1234};
        result.add(parameters);
        parameters = new Object[]{"/data/modelID/versions/1/dbName/12345/1234", "modelID", 1, "dbName", 12345, 1234};
        result.add(parameters);
        return result;
    }

    public RequestPatternTest(String url, String modelID, int version, String dbName, Integer classID, int instanceID) {
        this.url = url;
        this.modelID = modelID;
        this.version = version;
        this.dbName = dbName;
        this.classID = classID;
        this.instanceID = instanceID;
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
    public void testCreation() {
        RequestPattern instance = new RequestPattern(url);
        assertEquals(modelID, instance.modelID);
        assertEquals(classID, instance.classID);
        assertEquals(dbName, instance.domainName);
        assertEquals(version, instance.version);
        assertEquals(instanceID, (int)instance.instanceID);
    }

    /**
     * Test of isAddInstance method, of class RequestPattern.
     */
    @Test
    public void testIsAddInstance() {
        System.out.println("isAddInstance");
        RequestPattern instance = new RequestPattern("/data/modelID/versions/1/dbName/1234");
        boolean expResult = true;
        boolean result = instance.isAddInstance();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsAddInstance2() {
        System.out.println("isAddInstance2");
        RequestPattern instance = new RequestPattern("/data/modelID/dbName/12345/");
        boolean expResult = true;
        boolean result = instance.isAddInstance();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsAddInstance3() {
        System.out.println("isAddInstance2");
        RequestPattern instance = new RequestPattern(url);
        boolean expResult = false;
        boolean result = instance.isAddInstance();
        assertEquals(expResult, result);
    }
}