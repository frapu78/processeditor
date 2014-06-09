/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler.util;

import com.inubit.research.server.ProcessEditorServerUtils;
import net.frapu.code.visualization.ProcessObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author fel
 */
public class ProcessObjectUtils {
    private ProcessObject object;

    public ProcessObjectUtils(ProcessObject object) {
        this.object = object;
    }

    public void applyPropertyChange(Element el) throws IOException, DOMException, SAXException, ParserConfigurationException {
        Node propertyNode = el.getElementsByTagName("property").item(0);
        String property = propertyNode.getAttributes().getNamedItem("name").getNodeValue();
        String value = propertyNode.getAttributes().getNamedItem("value").getNodeValue();
        value = ProcessEditorServerUtils.unEscapeString(value);
        this.object.setProperty(property, value);
    }

    public Document serialize() throws Exception{
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document xmlDoc = builder.newDocument();
        // Create root element
        Element elem = this.object.getSerialization(xmlDoc);
        if (elem != null) {
            xmlDoc.appendChild(elem);
        }
        //Serialize created document
        return xmlDoc;
    }
}
