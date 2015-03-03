/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.user.SingleUser;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class UserConfig {

    private SingleUser user;
    private Node node;
    private Set<ISConfig> isConfigs = new HashSet<ISConfig>();

    private UserConfig() { }

    SingleUser getUser() {
        return this.user;
    }

    Node getNode() {
        return this.node;
    }

    void setAdmin( boolean isAdmin ) {
        ((Element) this.node).setAttribute("admin", String.valueOf(isAdmin));

        this.user.setIsAdmin(isAdmin);
    }

    void setPictureId( String pictureId) {
        Node picNode = this.getChildNode("picture");
        this.user.setPictureId(pictureId);

        if (picNode != null)
            picNode.setTextContent(pictureId);
    }

    void setMail( String mail ) {
        Node mailNode = this.getChildNode("mail");
        this.user.setMail(mail);

        if (mailNode != null)
            mailNode.setTextContent(mail);
    }

    void setRealName( String realName ) {
        Node realnameNode = this.getChildNode("realname");
        this.user.setRealName(realName);

        if (realnameNode != null)
            realnameNode.setTextContent(realName);

    }

    void setPwd( String password ) {
        Node pwdNode = this.getChildNode("property");
        this.user.setPwd(password);

        if (pwdNode != null)
            pwdNode.getAttributes().getNamedItem("value").setTextContent(password);
    }

    void addIS( ISConfig ic ) {
        if ( this.user.getISConnections().contains(ic.getISConnection()) )
            return;

        this.node.appendChild(ic.getNode());
        this.user.addISConnection(ic.getISConnection());
        ic.getISConnection().setOwner(null, user, null);
        this.isConfigs.add(ic);
    }

    void removeIS( ISLocation ism ) {
        ISConfig toRemove = null;
        for ( ISConfig iSConfig : this.isConfigs )
            if (iSConfig.getISConnection().equals(ism)) {
                toRemove = iSConfig;
                break;
            }

        if (toRemove != null) {
            this.node.removeChild(toRemove.getNode());
            this.user.removeISConnection(toRemove.getISConnection());
            this.isConfigs.remove(toRemove);
        }
    }

    private Node getChildNode( String name ) {
        NodeList children = this.node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
            if (children.item(i).getNodeName().equals(name))
                return children.item(i);

        return null;
    }

    public static UserConfig forNode(Node node) {
        UserConfig uc = new UserConfig();

        uc.node = node;

        Map<String, String> props  = XMLHelper.parseProperties(node);

        String name = node.getAttributes().getNamedItem("name").getNodeValue();

        Node adminAttr = node.getAttributes().getNamedItem("admin");
        
        boolean admin = false;
        
        if (adminAttr != null)
            admin = Boolean.valueOf(adminAttr.getNodeValue());
        
        uc.user = new SingleUser(name, props.get("pwd"), admin);

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("is")) {
                ISConfig ic = ISConfig.forNode(children.item(i));

                if (ic != null)
                    uc.addIS(ic);
            } else if (children.item(i).getNodeName().equals("mail"))
                uc.user.setMail(children.item(i).getTextContent());
            else if (children.item(i).getNodeName().equals("picture"))
                uc.user.setPictureId(children.item(i).getTextContent());
            else if (children.item(i).getNodeName().equals("realname"))
                uc.user.setRealName(children.item(i).getTextContent());
        }

        return uc;
    }
}
