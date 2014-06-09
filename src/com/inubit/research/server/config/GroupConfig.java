/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.user.Group;
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
public class GroupConfig {
    private Group group;

    private Node node;

    Group getGroup() {
        return this.group;
    }

    void updateNode( Document doc ) {
        Element newNode = doc.createElement("group");
        newNode.setAttribute("name", this.group.getName());

        for (String u : this.group.getMembers()) {
            Element userElement = doc.createElement("user");
            userElement.setAttribute("ref", u);
            newNode.appendChild(userElement);
        }

        for (String g : this.group.getSubGroups()) {
            Element groupElement = doc.createElement("group");
            groupElement.setAttribute("ref", g);
            newNode.appendChild(groupElement);
        }

        Node parent = this.node.getParentNode();
        parent.removeChild(this.node);
        parent.appendChild(newNode);

        this.node = newNode;
    }

    public static GroupConfig forNode(Node node) {
        GroupConfig gc = new GroupConfig();

        gc.node = node;

        String name = node.getAttributes().getNamedItem("name").getNodeValue();

        NodeList children = node.getChildNodes();

        Set<String> groups = new HashSet<String>();
        Set<String> users = new HashSet<String>();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeName().equals("user")) {
                users.add(child.getAttributes().getNamedItem("ref").getNodeValue());
            } else if (child.getNodeName().equals("group")) {
                groups.add(child.getAttributes().getNamedItem("ref").getNodeValue());
            }
        }
        gc.group = new Group(name, users, groups);
        return gc;
    }
}
