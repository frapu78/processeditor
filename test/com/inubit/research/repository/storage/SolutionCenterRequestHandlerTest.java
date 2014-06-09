/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inubit.research.repository.storage;

import com.inubit.research.server.ProcessEditorServer;
import com.inubit.research.server.domainStorage.rest.DomainStorageRequestHandler;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.ProcessEditorServerHelper;
import java.io.InputStream;
import net.frapu.code.converter.ProcessEditorExporter;
import java.io.File;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.domainStorage.data.InterpretedInstance;
import com.inubit.research.server.domainStorage.data.Storage;
import com.inubit.research.server.HttpConstants;
import net.frapu.code.visualization.domainModel.DomainClass;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.domainModel.DomainModel;
import org.json.JSONArray;
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
public class SolutionCenterRequestHandlerTest {

    DomainModel domainModel;
    DomainClass domainClass;

    public SolutionCenterRequestHandlerTest() throws Exception {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ProcessEditorServer.startForWorkbench();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        domainModel = new DomainModel("AppStore");
        domainClass = new DomainClass("App");
        domainClass.addAttribute("Price");
        domainClass.addAttribute("Name");
        domainModel.addObject(domainClass);
        ProcessEditorExporter exporter = new ProcessEditorExporter();
        File f = new File(ProcessEditorServerHelper.TMP_DIR + "/" + domainModel.getId() + ".model");
        exporter.serialize(f, domainModel);
        String key = ModelManager.getInstance().addPersistentModel(f, "nothing", "/", new SingleUser("name", "password"));
        f.delete();
        assert ModelManager.getInstance().getPersistentModel(domainModel.getId(), -1)!= null;
    }

    @After
    public void tearDown() {
        Storage.getDefault().clear();
    }

    /**
     * Test of handleGetRequest method, of class SolutionCenterRequestHandler.
     */
    @Ignore
    @Test
    public void testHandleGetRequest() throws Exception {
        System.out.println("handleGetRequest");
        RequestFacade req = null;
        ResponseFacade resp = null;
        DomainStorageRequestHandler instance = new DomainStorageRequestHandler();
        instance.handleGetRequest(req, resp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handlePostRequest method, of class SolutionCenterRequestHandler.
     */
    @Ignore
    @Test
    public void testHandlePostRequest() throws Exception {
        System.out.println("handlePostRequest");
        RequestFacade req = null;
        ResponseFacade resp = null;
        DomainStorageRequestHandler instance = new DomainStorageRequestHandler();
        instance.handlePostRequest(req, resp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handlePutRequest method, of class SolutionCenterRequestHandler.
     */
    @Ignore
    @Test
    public void testHandlePutRequest() throws Exception {
        System.out.println("handlePutRequest");
        RequestFacade req = null;
        ResponseFacade resp = null;
        DomainStorageRequestHandler instance = new DomainStorageRequestHandler();
        instance.handlePutRequest(req, resp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleDeleteRequest method, of class SolutionCenterRequestHandler.
     */
    @Ignore
    @Test
    public void testHandleDeleteRequest() throws Exception {
        System.out.println("handleDeleteRequest");
        RequestFacade req = null;
        ResponseFacade resp = null;
        DomainStorageRequestHandler instance = new DomainStorageRequestHandler();
        instance.handleDeleteRequest(req, resp);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testAddAndGetObject() throws Exception {
        System.out.println("AddAndGetAttribute");
        Storage.getDefault().clear();
        InterpretedInstance ins = Storage.getDefault().createDomain("AppleAppStore", domainModel).getClassData(Integer.valueOf(domainClass.getId())).createNewInstance();
        assertEquals(1,Storage.getDefault().getDomain("AppleAppStore").getClassData(Integer.valueOf(domainClass.getId())).getNumberOfInstances());
        ins.setAttribute("Price", 24);
        ins.setAttribute("Name", "MyCrazyApp");
        String uriPath = Configuration.getInstance().getProperty("server_uri") + "/data/" + domainModel.getId() + "/AppleAppStore/" + domainClass.getId();
        URI uri = new URI(uriPath);
        //XmlHttpRequest req = new XmlHttpRequest(uri);        
        //Document response = req.executePostRequest(null);
        //req = new XmlHttpRequest(uri);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(HttpConstants.REQUEST_GET);
        InputStream responseGet = conn.getInputStream();
        Reader reader = new BufferedReader(new InputStreamReader(responseGet));
        Writer writer = new StringWriter();
        int n;
        while ((n = reader.read())!=-1) {
            writer.write(n);
        }
        String s = writer.toString();
        JSONArray arr = new JSONArray(s);
        assertEquals(1,arr.length());
    }

}