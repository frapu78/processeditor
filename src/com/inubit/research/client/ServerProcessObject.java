/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

import org.w3c.dom.Document;

import com.inubit.research.server.HttpConstants;

/**
 *
 * @author uha
 */
public class ServerProcessObject extends ServerEntity {

    public ServerProcessObject(URI uri, UserCredentials credentials) {
        this.uri = uri;
        this.credentials = credentials;
    }


    public ProcessObject getObject() throws IOException, ParserConfigurationException, XMLHttpRequestException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        XmlHttpRequest req = getRequest();
        req.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        Document resp = req.executeGetRequest();
        ProcessNode newInstanceFromSerialization = null;
        newInstanceFromSerialization = ProcessNode.newInstanceFromSerialization(resp.getElementsByTagName("node").item(0));     
        return newInstanceFromSerialization;
    }

    public Document getImage() throws IOException, ParserConfigurationException, XMLHttpRequestException, ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        XmlHttpRequest req = getRequest();
        req.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, "image");
        //HttpURLConnection conn = req.getConnection();

        //ProcessNode newInstanceFromSerialization = null;
        //newInstanceFromSerialization = ProcessNode.newInstanceFromSerialization(resp.getElementsByTagName("node").item(0));
        return null;//req.executeGetRequesttest();
    }


}
