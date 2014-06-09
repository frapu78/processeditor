/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.User;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class AccessConfig {
    private Node node;

    private Set<User> writers = new HashSet<User>();
    private Set<User> readers = new HashSet<User>();
    private Set<User> annotators = new HashSet<User>();

    private AccessConfig(Node node) {
        this.node = node;
    }

    public boolean hasReadAccess( User user ) {
        return this.readers.contains(user);
    }

    public boolean hasWriteAccess( User user ) {
        return this.writers.contains(user);
    }

    public boolean hasCommentAccess( User user ) {
        return this.annotators.contains(user);
    }

    Set<User> getReaders() {
        return this.readers;
    }

    Set<User> getEditors() {
        return this.writers;
    }

    Set<User> getAnnotators() {
        return this.annotators;
    }

    public void grantRight( AccessType at, Set<User> users ) {
        if (at.equals(AccessType.COMMENT))
            this.annotators.addAll(users);
        else if (at.equals(AccessType.WRITE))
            this.writers.addAll(users);
        else if (at.equals(AccessType.VIEW))
            this.readers.addAll(users);
    }

    public void divestRight( AccessType at, Set<User> users ) {
        if (at.equals(AccessType.COMMENT))
            this.annotators.removeAll(users);
        else if (at.equals(AccessType.WRITE))
            this.writers.removeAll(users);
        else if (at.equals(AccessType.VIEW))
            this.readers.removeAll(users);
    }

    static Node createInitialConfigNode(Document doc) {
        Node access = doc.createElement("access");

        Element write = doc.createElement("write");
        Element usersW = doc.createElement("users");
        Element groupsW = doc.createElement("groups");

        write.appendChild(usersW);
        write.appendChild(groupsW);

        Element read = doc.createElement("view");
        Element usersR = doc.createElement("users");
        Element groupsR = doc.createElement("groups");

        read.appendChild(usersR);
        read.appendChild(groupsR);

        Element comment = doc.createElement("comment");
        Element usersC = doc.createElement("users");
        Element groupsC = doc.createElement("groups");

        comment.appendChild(usersC);
        comment.appendChild(groupsC);

        access.appendChild(comment);
        access.appendChild(write);
        access.appendChild(read);

        return access;
    }

    static AccessConfig forNode(Node node) {
        AccessConfig ac = new AccessConfig(node);

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("view"))
                parseReaders(ac, children.item(i));
            else if (children.item(i).getNodeName().equals("write"))
                parseWriters(ac, children.item(i));
            else if (children.item(i).getNodeName().equals("comment"))
                parseAnnotators(ac, children.item(i));
        }

        return ac;
    }

    private static void parseReaders( AccessConfig ac, Node node ) {
        NodeList children = node.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("users"))
                ac.readers.addAll(parseAccessorType(children.item(i), "user"));
            else if (children.item(i).getNodeName().equals("groups"))
                ac.readers.addAll(parseAccessorType(children.item(i), "group"));
        }
    }

    private static void parseWriters( AccessConfig ac, Node node ) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("users"))
                ac.writers.addAll(parseAccessorType(children.item(i), "user"));
            else if (children.item(i).getNodeName().equals("groups"))
                ac.writers.addAll(parseAccessorType(children.item(i), "group"));
        }
    }

    private static void parseAnnotators( AccessConfig ac, Node node ) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("users"))
                ac.annotators.addAll(parseAccessorType(children.item(i), "user"));
            else if (children.item(i).getNodeName().equals("groups"))
                ac.annotators.addAll(parseAccessorType(children.item(i), "group"));
        }
    }

    private static Set<User> parseAccessorType(Node node, String type) {
        Set<User> accessors = new HashSet<User>();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (!child.getNodeName().equals(type))
                continue;

            String name = child.getAttributes().getNamedItem("name").getNodeValue();
            User u = null;
            if (type.equals("user"))
                u = ProcessEditorServerHelper.getUserManager().getUserForName(name);
            else if (type.equals("group"))
                u = ProcessEditorServerHelper.getUserManager().getGroupForName(name);
            
            if (u != null)
                accessors.add(u);
        }

        return accessors;
    }
}