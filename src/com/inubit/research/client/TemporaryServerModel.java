/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.gui.plugins.serverLoadTests.LoadTestConfiguration;
import static com.inubit.research.server.HttpConstants.*;
import com.inubit.research.server.request.XMLHelper;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author uha
 */
public class TemporaryServerModel extends ServerModel {

    public TemporaryServerModel(URI uri, UserCredentials credentials) throws Exception {
        super(getTemporaryModelURI(uri, credentials), credentials);
    }

    private static URI getTemporaryModelURI(URI modelURI, UserCredentials credentials) throws Exception {
        String host = getBaseDirectory(modelURI);
        String tmpDir = getTempModelsDirectory(host);
        XmlHttpRequest tmpModelReq = new XmlHttpRequest(new URI(tmpDir));
        tmpModelReq.addCredentials(LoadTestConfiguration.getCredentials());
        tmpModelReq.setRequestProperty(HEADER_KEY_CREATE_TEMP_MODEL, modelURI.toString());
        Document garbage = XMLHelper.newDocument();
        Document response = tmpModelReq.executePostRequest(garbage);
        Node cn = response.getElementsByTagName(TAG_GET_TEMP_MODEL_URI_RESPONSE).item(0);
        Node cna = cn.getAttributes().item(0);
        return new URI(cna.getTextContent());
    }

    @Override
    public void setUri(URI uri) {
        try {
            super.setUri(getTemporaryModelURI(uri, getCredentials()));
        } catch (Exception ex) {
            Logger.getLogger(TemporaryServerModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     *
     * @param type
     * @return the id of the created node
     * @throws Exception
     */
    public URI addNodeToModel(Class<? extends ProcessNode> type) throws Exception {
        Document xmlDoc = XMLHelper.newDocument();
        Element node = xmlDoc.createElement("node");
        Element prop1 = xmlDoc.createElement("property");
        Element prop2 = xmlDoc.createElement("property");
        prop1.setAttribute("name", "#type");
        //full name required
        prop1.setAttribute("value", type.getName());
        prop2.setAttribute("name", "text");
        prop2.setAttribute("value", "aName");
        node.appendChild(prop1);
        node.appendChild(prop2);

        String postUrl = getUri() + FOLDER_NODES_ALIAS;
        xmlDoc.appendChild(node);
        XmlHttpRequest newReq = new XmlHttpRequest(new URI(postUrl));
        newReq.addCredentials(getCredentials());
        //newReq.setRequestProperty("Content-type", "text/xml; charset=ISO-8859-1");
        Document responseXML = newReq.executePostRequest(xmlDoc);
        //contains newId and oldId
        Node nodeRet = responseXML.getElementsByTagName("node").item(0);
        Node id = nodeRet.getAttributes().getNamedItem("newId");
        //System.out.println(id.getNodeValue());
        return getNodeURI(id.getNodeValue());
    }

    private void addEdgeToModel(Class<? extends ProcessEdge> type) {
        /*
        Document xmlDoc = XMLHelper.newDocument();
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

        executePutMethod(postUrl, xmlDoc);*/
    }
}
