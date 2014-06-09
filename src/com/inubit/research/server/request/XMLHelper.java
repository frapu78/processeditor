/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import com.inubit.research.server.HttpConstants;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class XMLHelper {
    private static final String KEY_ATTRIBUTE = HttpConstants.ATTRIBUTE_KEY_NAME;
    private static final String PROPERTY_LABEL = HttpConstants.ELEMENT_PROPERTY;
    private static final String VALUE_ATTRIBUTE = HttpConstants.ATTRIBUTE_KEY_VALUE;

    public static Document newDocument() {
        try {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(true);
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();

            return builder.newDocument();
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    public static Element addDocumentElement( Document doc , String nodeName ) {
        Element el = null;

        if ( doc != null ) {
            el = doc.createElement(nodeName);
            doc.appendChild(el);
        }

        return el;
    }

    public static Element addElement( Document doc, Element parent , String nodeName ) {
        Element el = null;
        if ( doc != null )
            el = doc.createElement(nodeName);

        if ( parent != null )
            parent.appendChild( el );

        return el;
    }

    public static Element addElementBefore( Document doc, Element sibling, String nodeName ) {
        Element el = null;
        if ( doc != null )
            el = doc.createElement(nodeName);

        if ( sibling != null )
            sibling.getParentNode().insertBefore(el, sibling);

        return el;
    }

    public static void addPropertyList( Document doc, Element parent, Map<Object, Object> properties ) {
        if ( doc == null || parent == null || properties == null )
            return;
        
        for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
            Element el = doc.createElement(PROPERTY_LABEL);
            el.setAttribute(KEY_ATTRIBUTE, entry.getKey().toString() );
            el.setAttribute(VALUE_ATTRIBUTE, entry.getValue().toString() );

            parent.appendChild(el);
        }
    }

    /**
     * Parse the childnodes of a node and look for &lt;property&gt; elements with attributes name and value.
     * Example:
     * &lt;node&gt;
     *    &lt;property name='n' value='v'/&gt;
     * &lt;node/&gt;
     * @param n the XML node
     * @return the parsed properties
     */
    public static Map<String, String> parseProperties(Node n) {
        NodeList propertyNodes = n.getChildNodes();
        Map<String, String> properties = new HashMap<String, String>();

        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node node = propertyNodes.item(i);

            if (node.getNodeName().equals("property")) {
                try {
                    String key = node.getAttributes().getNamedItem("name").getNodeValue();
                    String value = node.getAttributes().getNamedItem("value").getNodeValue();

                    properties.put(key, value);
                } catch ( NullPointerException e ) {
                    continue;
                }
            }
        }

        return properties;
    }
}
