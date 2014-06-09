/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.repository.storage;

import com.inubit.research.server.domainStorage.data.Storage;
import com.inubit.research.server.domainStorage.data.Domain;
import com.inubit.research.server.domainStorage.data.InterpretedInstance;
import com.inubit.research.test.helper.factories.DomainFactory;
import java.util.Iterator;
import java.util.UUID;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import java.util.Collection;
import net.frapu.code.visualization.domainModel.DomainModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Uwe
 */
@RunWith(Parameterized.class)
public class DomainTest {
    
    Storage store;
    Domain domain;

    @Parameters
    public static Collection<Object[]> parameters() {
        return StorageTest.parameters();
    }

    public DomainTest(Storage currentStore) {
        store = currentStore;
        domain = DomainFactory.build(store);
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
        domain.clear();
    }


    /**
     * Test of getClassInstance method, of class Domain.
     */
    @Test
    public void testGetClassInstance() {
        System.out.println("getClassInstance");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        InterpretedInstance expResult = instance.getClassData(classID).createNewInstance();
        UUID InstanceID = domain.getClassData(classID).getInstanceUUIDs().iterator().next();
        InterpretedInstance result = domain.getClassData(classID).getInstanceByIndex(classID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getClassInstances method, of class Domain.
     */
    @Test
    public void testGetClassInstances() {
        System.out.println("getClassInstance");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        Integer classID2 = Integer.valueOf(domain.getModel().getNodes().get(1).getId());
        assertEquals(0, domain.getClassData(classID).getNumberOfInstances());         
        InterpretedInstance expResult1 = instance.getClassData(classID).createNewInstance();
        assertEquals(1, domain.getClassData(classID).getNumberOfInstances());  
        InterpretedInstance NotExpResult = instance.getClassData(classID2).createNewInstance();
        assertEquals(1, domain.getClassData(classID).getNumberOfInstances());  
        InterpretedInstance expResult2 = instance.getClassData(classID).createNewInstance();
        assertEquals(2, domain.getClassData(classID).getNumberOfInstances());  
        Collection<InterpretedInstance> result = domain.getClassData(classID).getInstances();
        assertTrue(result.contains(expResult1));
        assertTrue(result.contains(expResult2));
        assertFalse(result.contains(NotExpResult));     
    }

    /**
     * Test of getInstanceIDs method, of class Domain.
     */
    @Test
    public void testGetInstanceIDs() {
        System.out.println("getInstanceIDs");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        Integer classID2 = Integer.valueOf(domain.getModel().getNodes().get(1).getId());
        assertEquals(0, domain.getClassData(classID).getNumberOfInstances());           
        InterpretedInstance expResult1 = instance.getClassData(classID).createNewInstance();
        assertEquals(1, domain.getClassData(classID).getNumberOfInstances());   
        InterpretedInstance NotExpResult = instance.getClassData(classID2).createNewInstance();
        assertEquals(1, domain.getClassData(classID).getNumberOfInstances());   
        InterpretedInstance expResult2 = instance.getClassData(classID).createNewInstance();
        assertEquals(2, domain.getClassData(classID).getNumberOfInstances());  
        Iterator<Integer> it = domain.getClassData(classID).getInstanceIndexes().iterator();
        Integer InstanceID1 = it.next();
        Integer InstanceID2 = it.next();
        assertFalse(InstanceID1.equals(InstanceID2));

    }

    /**
     * Test of getModel method, of class Domain.
     */
    @Ignore
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        Domain instance = domain;
        DomainModel expResult = null;
        DomainModel result = instance.getModel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class Domain.
     */
    @Ignore
    @Test
    public void testGetName() {
        System.out.println("getName");
        Domain instance = domain;
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createClassInstance method, of class Domain.
     */
    @Test
    public void testCreateClassInstance_Integer() {
        System.out.println("createClassInstance");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        int sizeBefore = instance.getClassData(classID).getNumberOfInstances();
        InterpretedInstance result = instance.getClassData(classID).createNewInstance();
        assertEquals(instance.getClassData(classID).getNumberOfInstances(),sizeBefore+1);
    }

    /**
     * Test of createClassInstance method, of class Domain.
     */
    @Test
    public void testCreateClassInstance_String() {
        System.out.println("createClassInstance");
        Domain instance = domain;
        String classID = domain.getModel().getNodes().get(0).getId();
        int sizeBefore = instance.getClassData(classID).getNumberOfInstances();
        InterpretedInstance result = instance.getClassData(classID).createNewInstance();
        assertEquals(instance.getClassData(classID).getNumberOfInstances(),sizeBefore+1);
    }

    /**
     * Test of getUsedClassIDs method, of class Domain.
     */
    @Test
    public void testGetUsedClassIDs() {
        System.out.println("getUsedClassIDs");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());
        Integer classID2 = Integer.valueOf(domain.getModel().getNodes().get(1).getId());
        assertEquals(0, domain.getUsedClassIDs().size());         
        instance.getClassData(classID).createNewInstance();
        assertEquals(1, domain.getUsedClassIDs().size());  
        assertTrue(domain.getUsedClassIDs().contains(classID));
        assertFalse(domain.getUsedClassIDs().contains(classID2));
        instance.getClassData(classID).createNewInstance();
        assertEquals(1, domain.getUsedClassIDs().size());  
        assertTrue(domain.getUsedClassIDs().contains(classID));
        assertFalse(domain.getUsedClassIDs().contains(classID2));
        instance.getClassData(classID2).createNewInstance();
        assertEquals(2, domain.getUsedClassIDs().size());  
        assertTrue(domain.getUsedClassIDs().contains(classID));
        assertTrue(domain.getUsedClassIDs().contains(classID2));
    }

    /**
     * Test of setModel method, of class Domain.
     */
    @Ignore
    @Test
    public void testSetModel() {
        System.out.println("setModel");
        DomainModel model = null;
        Domain instance = domain;
        Domain expResult = null;
        Domain result = instance.setModel(model);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class Domain.
     */
    @Test
    (expected=IllegalArgumentException.class)
    public void testClear() {
        System.out.println("clear");
        Domain instance = domain;
        Integer classID = Integer.valueOf(domain.getModel().getNodes().get(0).getId());        
        instance.getClassData(classID).createNewInstance();
        Integer instanceID = domain.getClassData(classID).getInstanceIndexes().iterator().next();
        assertEquals(1, instance.getUsedClassIDs().size());
        instance.clear();
        assertTrue(instance.getUsedClassIDs().isEmpty());
        assertTrue(instance.getClassData(classID).getInstances().isEmpty());
        instance.getClassData(classID).getInstanceByIndex(instanceID);
    }

    


}