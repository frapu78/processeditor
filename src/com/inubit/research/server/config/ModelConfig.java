/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class ModelConfig {
    //for xpath usage
    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private static XPath xpath = xpathFactory.newXPath();
    
    private String id;
    private Node node;
    private String owner;

    private AccessConfig access;
//    private AccessConfig writeAccess;
    private FileSystemConfig parent;

    private ModelConfig() {}

    public String getId() {
        return this.id;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOwner( SingleUser user ) {
        if (this.owner.equals(user.getName()))
            return true;
        
        return false;
    }

    public boolean isWriteableByUser( User user ) {
        if (this.access == null && this.parent != null)
            return this.parent.isWriteableByUser(user);

        if (this.access != null && this.access.hasWriteAccess(user))
            return true;

        if (this.parent != null)
            return this.parent.isWriteableByUser(user);
        else
            return false;
    }

    public boolean isReadableByUser( User user ) {
        if (this.access == null && this.parent != null)
            return this.parent.isReadableByUser(user);

        if (this.access != null && this.access.hasReadAccess(user))
            return true;

        if (this.parent != null)
            return this.parent.isReadableByUser(user);
        else
            return false;
    }

    public boolean isAnnotatableByUser( User user ) {
        if (this.access == null && this.parent != null)
            return this.parent.isAnnotatableByUser(user);

        if (this.access != null && this.access.hasCommentAccess(user))
            return true;

        if (this.parent != null)
            return this.parent.isAnnotatableByUser(user);
        else
            return false;
    }

    public Set<User> getViewers() {
        return this.access.getReaders();
    }

    public Set<User> getAnnotators() {
        return this.access.getAnnotators();
    }

    public Set<User> getEditors() {
        return this.access.getEditors();
    }

    public void grantRight( AccessType at, Set<User> users ) {
        this.access.grantRight( at, users );
    }

     public void divestRight( AccessType at, Set<User> users ) {
        this.access.divestRight( at, users );
    }

    Node getNode() {
        return this.node;
    }

    public static ModelConfig forNode(Node node, FileSystemConfig parent) throws Exception {
        ModelConfig config = new ModelConfig();

        config.id = node.getAttributes().getNamedItem("id").getNodeValue();
        config.node = node;

        NodeList children = node.getChildNodes();

        String query = "./access";
        Object res = xpath.evaluate(query, node, XPathConstants.NODE);
        Node accessNode = (Node) res;

        if (accessNode != null)
            config.access = AccessConfig.forNode(accessNode);

//        query = "./access/write";
//        res = xpath.evaluate(query, node, XPathConstants.NODE);
//        Node writeNode = (Node) res;
//
//        if (writeNode != null)
//            config.writeAccess = AccessConfig.forNode(writeNode);
        
        config.parent = parent;

        query = "./owner";
        res = xpath.evaluate(query, node, XPathConstants.NODE);
        Node ownerNode = (Node) res;

        if (ownerNode != null)
            config.owner = ownerNode.getTextContent();
        else
            config.owner = "root";

        return config;
    }
}
