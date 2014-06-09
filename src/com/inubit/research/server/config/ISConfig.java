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
import java.net.URI;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author fel
 */
public class ISConfig {
    private Node node;
    private ISLocation ism;

    private ISConfig () { }

    Node getNode() {
        return this.node;
    }

    ISLocation getISConnection() {
        return this.ism;
    }

    static ISConfig forISManager( ISLocation ism, Document doc ) {
        if (ism == null)
            return null;

        String url = null;

        try {
            url = ism.getURL();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        ISConfig ic = new ISConfig();

        Element iSEl = doc.createElement("is");
        Element urlProperty = doc.createElement("property");
        urlProperty.setAttribute("name", "url");
        urlProperty.setAttribute("value", url);

        Element userProperty = doc.createElement("property");
        userProperty.setAttribute("name", "user");
        userProperty.setAttribute("value", ism.getUser());

        Element pwdProperty = doc.createElement("property");
        pwdProperty.setAttribute("name", "pwd");
        pwdProperty.setAttribute("value", ism.getPwd());

        iSEl.appendChild(urlProperty);
        iSEl.appendChild(userProperty);
        iSEl.appendChild(pwdProperty);

        ic.node = iSEl;
        ic.ism = ism;

        return ic;
    }

    static ISConfig forNode( Node node ) {
        ISConfig ic = new ISConfig();

        Map<String, String> props = XMLHelper.parseProperties(node);
        String url = props.get("url");

        URI u = null;
        try {
            u = URI.create(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String user = props.get("user");
        String pwd = props.get("pwd");

        ic.node = node;
        ic.ism = new ISLocation(u, user, pwd);

        return ic;
    }
}
