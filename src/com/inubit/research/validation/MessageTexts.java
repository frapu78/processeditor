/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author tmi
 */
public class MessageTexts {

    private Document xmlFile;

    public MessageTexts(Document messagesDocument) {
        this.xmlFile = messagesDocument;
//        InputStream temp = this.getClass().getResourceAsStream("/validation-messages.xml");
//        xmlFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(temp);
    }

    public String getLongText(String name) {
        return valueOfChildNode(getNodeForMessage(name), "fullText");
    }

    public String getShortText(String name) {
        return valueOfChildNode(getNodeForMessage(name), "shortText");
    }

    public int getLevel(String name) {
        if (getNodeForMessage(name)==null) return ValidationMessage.TYPE_INFO; // fpu
        String level = valueOfAttribute(getNodeForMessage(name),"type");
        if (level.equals("error")) return ValidationMessage.TYPE_ERROR;
        else if (level.equals("warning")) return ValidationMessage.TYPE_WARNING;
        else return ValidationMessage.TYPE_INFO;
    }

    public boolean isFatal(String name) {
        String fatalValue = valueOfAttribute(getNodeForMessage(name),"fatal");
        return !(fatalValue.equals("") || fatalValue.equals("false"));
    }
    
    private Node getNodeForMessage(String name) {
        NodeList messages = xmlFile.getDocumentElement().
                getElementsByTagName("message");
        for (int i = 0; i < messages.getLength(); ++i) {
            Node currentNode = messages.item(i);
            String currentName = currentNode.getAttributes().
                    getNamedItem("name").getNodeValue();
            if (currentName.equals(name)) {
                return currentNode;
            }
        }
        return null;
    }
    
    private String valueOfChildNode(Node messageNode, String childName) {
        if (messageNode == null) return "";
        NodeList children = messageNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            if (children.item(i).getNodeName().equals(childName)) {
                return children.item(i).getTextContent();
            }
        }
        return "";
    }

    private String valueOfAttribute(Node messageNode, String attributeName) {
        if (messageNode == null) return "";
        Node attribute = messageNode.getAttributes().getNamedItem(attributeName);
        if (attribute == null) {
            return "";
        } else {
            return attribute.getNodeValue();
        }
    }
}
