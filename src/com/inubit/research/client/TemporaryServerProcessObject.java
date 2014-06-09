/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.request.XMLHelper;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import net.frapu.code.visualization.ProcessNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author uha
 */
public class TemporaryServerProcessObject extends ServerProcessObject {

    public TemporaryServerProcessObject(URI uri, UserCredentials credentials) {
        super(uri, credentials);
        if (!uri.toString().contains(HttpConstants.FOLDER_TEMP_ALIAS)) {
            throw new IllegalArgumentException("The given uri must be contained in the tmp directory");
        }
    }



    private Element appendUpdateNode(Document xmlDoc, String type) {
        Element node = xmlDoc.createElement(HttpConstants.ELEMENT_UPDATE);
        node.setAttribute(HttpConstants.ATTRIBUTE_KEY_UPDATE_TYPE, type);
        xmlDoc.appendChild(node);
        return node;
    }

    protected void sendUpdate(String updateMethod, HashMap<Object, Object> attributesToUpdate) throws MalformedURLException, IOException, XMLHttpRequestException {
        Document xmlDoc = XMLHelper.newDocument();
        Element updateNode = appendUpdateNode(xmlDoc, updateMethod);
        XMLHelper.addPropertyList(xmlDoc, updateNode, attributesToUpdate);
        XmlHttpRequest req = this.getRequest();
        req.executePutRequest(xmlDoc);
    }

    public void setPos(Point p) throws ParserConfigurationException, IOException, MalformedURLException, XMLHttpRequestException {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(ProcessNode.PROP_XPOS, Integer.toString(p.x));
        map.put(ProcessNode.PROP_YPOS, Integer.toString(p.y));
        sendUpdate(HttpConstants.ELEMENT_UPDATE_METHOD_POSITION, map);
    }

    public void setDimension(Dimension d, Point nodePos) throws ParserConfigurationException, IOException, MalformedURLException, XMLHttpRequestException {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(ProcessNode.PROP_XPOS, Integer.toString(nodePos.x));
        map.put(ProcessNode.PROP_YPOS, Integer.toString(nodePos.y));
        map.put(ProcessNode.PROP_WIDTH, Integer.toString(d.width));
        map.put(ProcessNode.PROP_HEIGHT, Integer.toString(d.height));
        sendUpdate(HttpConstants.ELEMENT_UPDATE_METHOD_RESIZE, map);
    }

    public void setDimension(Dimension d) throws ParserConfigurationException, IOException, MalformedURLException, XMLHttpRequestException {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(ProcessNode.PROP_WIDTH, Integer.toString(d.width));
        map.put(ProcessNode.PROP_HEIGHT, Integer.toString(d.height));
        sendUpdate(HttpConstants.ELEMENT_UPDATE_METHOD_RESIZE, map);        
    }

    public void setProperty(String key, String value) throws ParserConfigurationException, IOException, MalformedURLException, XMLHttpRequestException {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put(key, value);
        sendUpdate(HttpConstants.ELEMENT_UPDATE_METHOD_PROPERTY, map);
    }

    /**
     * create new node with new type and copy all properties from the old one
     * @param type
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws MalformedURLException
     * @throws XMLHttpRequestException
     */
    public void setType(Class type) throws ParserConfigurationException, IOException, MalformedURLException, XMLHttpRequestException {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("newtype", type.getName());
        sendUpdate(HttpConstants.ELEMENT_UPDATE_METHOD_TYPE, map);
    }


}
