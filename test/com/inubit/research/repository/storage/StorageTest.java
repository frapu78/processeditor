/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.repository.storage;

import com.inubit.research.server.domainStorage.data.Storage;
import com.inubit.research.server.domainStorage.data.Domain;
import com.inubit.research.server.domainStorage.utils.ExampleStorageFixture;
import com.inubit.research.test.helper.factories.DomainModelFactory;
import java.util.Collection;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import com.inubit.research.server.domainStorage.db4o.DB4oStorage;
import java.util.LinkedList;
import net.frapu.code.visualization.domainModel.DomainModel;
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
public class StorageTest {
    
    Storage store;
    DomainModel domainModel;
    Domain domain;
    String domainName;
    
    @Parameters
    public static Collection<Object[]> parameters() {
        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (Class<? extends Storage> clazz : Storage.getStorageClasses()) {
            Object[] parameters = new Object[] {Storage.get(clazz)};
            result.add(parameters);
        }
        return result;
    }

    public StorageTest(Storage currentStore) {
        store = currentStore;          
        domainModel = DomainModelFactory.build();            
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
     * Test of getDefault method, of class Storage.
     */
    @Test
    public void testGetDefault() {
        Storage result = Storage.getDefault();
        assertTrue(result instanceof Storage);
    }

    /**
     * Test of getCurrent method, of class Storage.
     */
    @Test
    public void testGetCurrent() {        
        Storage result = Storage.getCurrent();
        assertTrue(result instanceof Storage);
    }

    /**
     * Test of getCurrent method, of class Storage.
     */
    @Test
    public void testGetSetCurrent() {
        Class<? extends Storage> currentStorageClass = DB4oStorage.class;
        Storage.setCurrentClass(currentStorageClass);
        Storage result = Storage.getCurrent();
        assertTrue(result instanceof DB4oStorage);
    }

    /**
     * Test of getDefault method, of class Storage.
     */
    @Test
    public void testSetGetDefault() {
        Class<? extends Storage> defaultStorageClass = DB4oStorage.class;
        Storage.setDefaultClass(defaultStorageClass);
        Storage result = Storage.getDefault();
        assertTrue(result instanceof DB4oStorage);
    }

    /**
     * Test of get method, of class Storage.
     */
    @Test
    public void testGet() {
        Class<? extends Storage> storageClass = DB4oStorage.class;
        Storage result = Storage.get(storageClass);
        assertTrue(result instanceof DB4oStorage);
        Storage result2 = Storage.get(storageClass);
        assertSame(result2, result);
    }

    /**
     * Test of createDomain method, of class Storage.
     */
    @Test
    public void testCreateDomain() {
        System.out.println("createDomain");
        String name = "testDomain";
        Storage instance = store;
        Domain result = instance.createDomain(name, domainModel);
        assertEquals(result.getModel(), domainModel);
        assertEquals(result.getName(), name);       
    }

    /**
     * Test of getDomain method, of class Storage.
     */
    @Test
    public void testGetDomain() {
        System.out.println("getDomain");
        String name = "testDomain";
        Storage instance = store;
        Domain createResult = instance.createDomain(name, domainModel);
        Domain result = instance.getDomain(name);
        assertEquals(result.getModel(), domainModel);
        assertEquals(result.getName(), name); 
        assertSame(createResult, result);
    }
    
    
    @Test
    public void testDiskPersistence() throws Exception {
        System.out.println("testPersistence");
        String name = "testDomain";
        Storage instance = ExampleStorageFixture.build(store.getClass());
        Storage.setCurrentClass(store.getClass());
        Collection<Integer> before = Storage.getCurrent().getDomain("AppleAppStore").getUsedClassIDs();
        Storage storageBefore = Storage.getCurrent();
        assertFalse(before.isEmpty());
        Storage.unloadAll();        
        assertEquals(before, Storage.getCurrent().getDomain("AppleAppStore").getUsedClassIDs());
        assertEquals(storageBefore, Storage.getCurrent());
        Storage.getCurrent().clear();
    }

}