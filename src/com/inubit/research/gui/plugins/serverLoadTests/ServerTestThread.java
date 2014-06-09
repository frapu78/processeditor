/**
 *
 * Process Editor - inubit Workbench Server Load Test Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests;

import com.inubit.research.client.ModelDirectory;
import com.inubit.research.client.ModelServer;
import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.client.TemporaryServerModel;
import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.testUtils.ModelGenerator;
import com.inubit.research.testUtils.Seed;
import java.awt.Dimension;
import java.awt.Point;
import java.net.URI;
import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jos
 */
public class ServerTestThread extends Observable implements Runnable {

    private String tempModelUrl = "#server#/models/tmp"; //"http://novplex:1205/models/tmp";
    private String addNodeUrl = "#server#/models/tmp/#fullId#/nodes"; //"http://novplex:1205/models/tmp/#fullId#/nodes";
    private String addEdgeUrl = "#server#/models/tmp/#fullId#/edges"; //"http://novplex:1205/models/tmp/#fullId#/edges";
    private String getImageUrl = "#server#/models/tmp/#fullId#/nodes/#nodeId#/png"; //"http://novplex:1205/models/tmp/#fullId#/nodes/#nodeId#/png";
    private String updateNodeUrl = "#server#/models/tmp/#fullId#/nodes/#nodeId#";
    private String saveModelUrl = "#server#/models"; //"http://novplex:1205/models";
    private String requestHeader_Commit = "Commit-SourceRef";
    private String requestHeader_Comment = "Comment";
    private String requestHeader_Name = "Commit-Name";
    private DocumentBuilder builder;
    private String name;
    private int sleepTime;
    private ProcessModel testModel;
    // Id in Model, Id on Server
    private HashMap<String, String> nodemap;
    // Number of Retries, Request
    private HashMap<String, Integer> retryMap;
    //Request Number , execution Time
    private HashMap<String, Integer> executionMap;
    private int request;
    private static Logger jlog = Logger.getAnonymousLogger();

    public ServerTestThread(String name, int sleepTime, int taskNummer) {
        this.name = name;
        this.sleepTime = sleepTime;
    }

    public ServerTestThread(String name, int sleepTime, String serverURL) {
        this.name = name;
        this.sleepTime = sleepTime;
        addNodeUrl = addNodeUrl.replaceAll("#server#", serverURL);
        tempModelUrl = tempModelUrl.replaceAll("#server#", serverURL);
        addEdgeUrl = addEdgeUrl.replaceAll("#server#", serverURL);
        getImageUrl = getImageUrl.replaceAll("#server#", serverURL);
        saveModelUrl = saveModelUrl.replaceAll("#server#", serverURL);
        updateNodeUrl = updateNodeUrl.replaceAll("#server#", serverURL);
        nodemap = new HashMap<String, String>();
        retryMap = new HashMap<String, Integer>();
        executionMap = new HashMap<String, Integer>();
        request = 0;
    }

    public void setTestModel(ProcessModel testModel) {
        this.testModel = testModel;
    }

    public HashMap<String, Integer> getRetryMap() {
        return retryMap;
    }

    public HashMap<String, Integer> getExecutionMap() {
        return executionMap;
    }

    public void run() {
        String fullId;
        try {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            builder = xmlFactory.newDocumentBuilder();
            fullId = getTempModelUri(null);
            if (testModel == null) {
                return;
            } else {
                for (Cluster c : testModel.getClusters()) {
                    String node_serverId = addNodeToModel(c.getClass().getCanonicalName(), fullId, c.getPos(), c.getSize(), c.getName());
                    nodemap.put(c.getId(), node_serverId);
                    addClusterNodes(c, fullId);
                }
                // add Nodes which are in no Cluster
                for (ProcessNode n : testModel.getNodes()) {
                    if (!(n instanceof Cluster) && (nodemap.get(n.getId()) == null)) {
                        if (!(n instanceof EdgeDocker)) {
                            String node_serverId = addNodeToModel(n.getClass().getCanonicalName(), fullId, n.getPos(), n.getSize(), n.getName());
                            getImage(fullId, node_serverId);
                            nodemap.put(n.getId(), node_serverId);
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
                for (ProcessEdge e : testModel.getEdges()) {
                    String sourceId = nodemap.get(e.getSource().getId());
                    String targetId = nodemap.get(e.getTarget().getId());
                    addEdgeToModel(fullId, sourceId, targetId);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ex) {
                    }
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
            saveModel(fullId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        jlog.log(Level.OFF, "Finished Test");
        this.setChanged();
        this.notifyObservers();
    }

    private void addClusterNodes(Cluster c, String fullId) throws Exception {
        c.getProcessNodes();
        String id = c.getId();
        String parentId = nodemap.get(id);
        for (ProcessNode n : c.getProcessNodes()) {
            if (!(n instanceof Cluster)) {
                String node_serverId = addNodeToModel(n.getClass().getCanonicalName(), fullId, n.getPos(), n.getSize(), n.getName());
                getImage(fullId, node_serverId);
                nodemap.put(n.getId(), node_serverId);
                // Logger.getLogger(ServerTestThread.class.getName()).log(Level.OFF, "Node "+ n.getText() +" added to Model");
                //String fullModelId, String parentNodeId, String nodeId
                setParentCluster(fullId, parentId, node_serverId);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * 
     * @param fullId
     * @throws Exception
     */
    private void saveModel(String fullId) throws Exception {
        URI uri = new URI(saveModelUrl);
        XmlHttpRequest newReq = new XmlHttpRequest(uri);
        newReq.addCredentials(LoadTestConfiguration.getCredentials());
        newReq.setRequestProperty(requestHeader_Commit, fullId);
        newReq.setRequestProperty(requestHeader_Comment, "test ");
        newReq.setRequestProperty(requestHeader_Name, name);
        newReq.executePostRequest(null);
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    private String getTempModelUri(ModelVersionDescription des) throws Exception {
        Seed seed = new Seed();
        ModelGenerator gen = new ModelGenerator();
        ProcessModel m = gen.generate(seed, BPMNModel.class, 50, 50);
        ModelServer server = new ModelServer();
        m.setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, ModelServer.getDefaultCredentials());
        ModelDirectory.publishToServer(m, true, null, ModelServer.getDefaultLocalURI().toString(), "comment", "testFolder", "titleTest", null);
        TemporaryServerModel tmpModel = new TemporaryServerModel(new URI(m.getProcessModelURI()), ModelServer.getDefaultCredentials());
        return tmpModel.getUri().toString();
        /*URI newURI = new URI(tempModelUrl);
        XmlHttpRequest newRequest = new XmlHttpRequest(newURI);
        newRequest.addCredentials(LoadTestConfiguration.getCredentials());
        newRequest.setRequestProperty("Model-Type", testModel.getClass().getCanonicalName());
        Document responseXML = newRequest.executePutRequest(null);//builder.parse(stream);
        Element uriElement = (Element) responseXML.getElementsByTagName("uri").item(0);
        String fullId = uriElement.getAttribute("value").replace("/models/tmp/", "");
        Logger.getLogger(ServerTestThread.class.getName()).log(Level.OFF, "Temp Model created with client");
        return fullId;*/
    }

    /**
     * 
     * @param nodeType
     * @param fullModelId
     * @param position
     * @param name Name des Knoten
     * @return
     * @throws Exception
     */
    private String addNodeToModel(String nodeType, String fullModelId, Point position, Dimension dim, String name) throws Exception {
        Document xmlDoc = builder.newDocument();
        Element node = xmlDoc.createElement("node");
        Element prop1 = xmlDoc.createElement("property");
        Element prop2 = xmlDoc.createElement("property");
        prop1.setAttribute("name", "#type");
        prop1.setAttribute("value", nodeType);
        prop2.setAttribute("name", "text");
        prop2.setAttribute("value", name);
        node.appendChild(prop1);
        node.appendChild(prop2);
        setPosition(position, node, xmlDoc);
        if (dim != null) {
            setDimension(dim, node, xmlDoc);
        }
        String postUrl = addNodeUrl.replace("#fullId#", "" + fullModelId);
        xmlDoc.appendChild(node);
        this.setChanged();
        this.notifyObservers("node");
        return executePutMethod(postUrl, xmlDoc);
    }

    private void addEdgeToModel(String fullModelId, String source, String target) {
        Document xmlDoc = builder.newDocument();
        Element edges = xmlDoc.createElement("edges");
        Element edge = xmlDoc.createElement("edge");
        Element prop1 = xmlDoc.createElement("property");
        Element prop2 = xmlDoc.createElement("property");
        prop1.setAttribute("name", "#sourceNode");
        prop1.setAttribute("value", source);
        prop2.setAttribute("name", "#targetNode");
        prop2.setAttribute("value", target);
        edge.appendChild(prop1);
        edge.appendChild(prop2);
        edges.appendChild(edge);
        String postUrl = addEdgeUrl.replace("#fullId#", "" + fullModelId);
        xmlDoc.appendChild(edge);
        this.setChanged();
        this.notifyObservers("node");
        executePutMethod(postUrl, xmlDoc);
    }

    private void setPosition(Point position, Element node, Document xmlDoc) {
        Element propX = xmlDoc.createElement("property");
        propX.setAttribute("name", "x");
        propX.setAttribute("value", "" + position.x);
        Element propY = xmlDoc.createElement("property");
        propY.setAttribute("name", "y");
        propY.setAttribute("value", "" + position.y);
        node.appendChild(propX);
        node.appendChild(propY);
    }

    private void setDimension(Dimension dim, Element node, Document xmlDoc) {
        Element propX = xmlDoc.createElement("property");
        propX.setAttribute("name", "width");
        propX.setAttribute("value", "" + dim.width);
        Element propY = xmlDoc.createElement("property");
        propY.setAttribute("name", "height");
        propY.setAttribute("value", "" + dim.height);
        node.appendChild(propX);
        node.appendChild(propY);
    }

    /**
     * @param fullModelId
     * @param parentNodeId
     * @param nodeId
     */
    private void setParentCluster(String fullModelId, String parentNodeId, String nodeId) {
        //<update type='cluster'><new value='17440602'/></update>
        if (nodeId != null) {
            Document xmlDoc = builder.newDocument();
            Element update = xmlDoc.createElement("update");
            update.setAttribute("type", "cluster");
            Element new_ = xmlDoc.createElement("new");
            new_.setAttribute("value", parentNodeId);
            update.appendChild(new_);
            xmlDoc.appendChild(update);
            String postURL = updateNodeUrl.replaceAll("#fullId#", fullModelId);
            postURL = postURL.replaceAll("#nodeId#", nodeId);
            executePostMethod(postURL, xmlDoc);
        }
    }

    /**
     * Executes a Put Method and return the id of the new Object
     * @param postUrl
     * @param xmlElement
     * @return
     */
    private String executePutMethod(String putUrl, Document xmlDoc) {
        try {
            URI putURI = new URI(putUrl);
            XmlHttpRequest newReq = new XmlHttpRequest(putURI);
            newReq.addCredentials(LoadTestConfiguration.getCredentials());
            newReq.setRequestProperty("Content-type", "text/xml; charset=ISO-8859-1");
            Document responseXML = newReq.executePutRequest(xmlDoc);
            if (responseXML != null) {
                Element node = (Element) responseXML.getElementsByTagName("node").item(0);
                if (node != null) {
                    return node.getAttribute("newId");
                }
            }
            int retries = newReq.getCurrentRetries();
            if (retryMap.get("" + retries) != null) {
                int temp = retryMap.get("" + retries);
                temp++;
                retryMap.put("" + retries, temp);
            } else {
                retryMap.put("" + retries, 1);
            }
            request++;
            executionMap.put("" + request, (int) newReq.getExecutionTime());
        } catch (Exception ex) {
            ex.printStackTrace();
            //Logger.getLogger(ServerTestThread.class.getName()).log(Level.OFF, ex.getMessage());
        }
        return null;
    }

    /**
     * Executes a Put Method and return the id of the new Object
     * @param postUrl
     * @param xmlElement
     * @return
     */
    private String executePostMethod(String putUrl, Document xmlDoc) {
        try {
            URI putURI = new URI(putUrl);
            XmlHttpRequest newReq = new XmlHttpRequest(putURI);
            newReq.addCredentials(LoadTestConfiguration.getCredentials());
            newReq.setRequestProperty("Content-type", "text/xml; charset=ISO-8859-1");
            Document responseXML = newReq.executePostRequest(xmlDoc);
            if (responseXML != null) {
                Element node = (Element) responseXML.getElementsByTagName("node").item(0);
                if (node != null) {
                    return node.getAttribute("newId");
                }
            }
            int retries = newReq.getCurrentRetries();
            if (retryMap.get("" + retries) != null) {
                int temp = retryMap.get("" + retries);
                retryMap.put("" + retries, temp++);
            } else {
                retryMap.put("" + retries, 1);
            }
            request++;
            executionMap.put("" + request, (int) newReq.getExecutionTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Server creates image
     * @param modelId
     * @param nodeId
     */
    private void getImage(String modelId, String nodeId) {
        String getUrl = getImageUrl.replaceAll("#fullId#", modelId);
        if (nodeId != null) {
            try {
                getUrl = getUrl.replaceAll("#nodeId#", nodeId);
                URI imageURI = new URI(getUrl);
                XmlHttpRequest newReq = new XmlHttpRequest(imageURI);
                newReq.executeGetRequest();
                request++;
                executionMap.put("" + request, (int) newReq.getExecutionTime());
                //System.out.println("Get Image tried to connect " + newReq.getCurrentRetries());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
