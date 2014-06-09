/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.SingleUser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author fel
 */
public class FileSystemUsersConfig implements UsersConfig {
    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private static XPath xpath = xpathFactory.newXPath();

    private Document doc;
    private File file;

    private Map<String, UserConfig> users = new HashMap<String, UserConfig>();
    private Map<String, GroupConfig> groups = new HashMap<String, GroupConfig>();

    private FileSystemUsersConfig() {}

    public Set<String> getUserNames() {
        return this.users.keySet();
    }

    public Set<String> getGroupNames() {
        return this.groups.keySet();
    }

    public Group getGroup( String name ) {
        GroupConfig gc = this.groups.get(name);

        if (gc != null)
            return gc.getGroup();
        else
            return null;
    }

    public SingleUser getUser( String name ) {
        UserConfig u = this.users.get(name);

        if ( u != null)
            return u.getUser();
        else
            return null;
    }

    public boolean addUser( String name, String pwd ) {
        if (users.containsKey(name))
            return false;

        pwd = ProcessEditorServerUtils.getMD5Hash(pwd);

        Element e = this.doc.createElement("user");
        e.setAttribute("name", name);
        Element p = this.doc.createElement("property");
        p.setAttribute("name", "pwd");
        p.setAttribute("value", pwd);
        Element m = this.doc.createElement("mail");
        Element r = this.doc.createElement("realname");
        Element i = this.doc.createElement("picture");
        e.appendChild(p);
        e.appendChild(m);
        e.appendChild(r);
        e.appendChild(i);

        this.doc.getDocumentElement().appendChild(e);
        this.users.put(name, UserConfig.forNode(e));

        this.writeConfig();

        return true;
    }

    public boolean addGroup( String name ) {
        if (groups.containsKey(name))
            return false;

        Element g = this.doc.createElement("group");
        g.setAttribute("name", name );

        this.doc.getDocumentElement().appendChild(g);
        this.groups.put(name, GroupConfig.forNode(g));

        this.writeConfig();

        return true;
    }

    public void setMail( String userName, String mail, boolean deferWrite ) {
        this.users.get(userName).setMail(mail);

        if (!deferWrite )
            this.writeConfig();
    }

    public void setPictureId( String userName, String id, boolean deferWrite ) {
        this.users.get(userName).setPictureId(id);

        if (!deferWrite )
            this.writeConfig();
    }

    public void setRealName( String userName, String name, boolean deferWrite ) {
        this.users.get(userName).setRealName(name);

        if (!deferWrite )
            this.writeConfig();
    }

    public void setAdmin( String name, boolean isAdmin ) {
        this.users.get(name).setAdmin(isAdmin);
        this.writeConfig();
    }

    public void setGroupMembers( String name, Set<String> members ) {
        GroupConfig gc = this.groups.get(name);
        gc.getGroup().setMembers(members);

        gc.updateNode(this.doc);
        this.writeConfig();
    }

    public void setSubgroups( String name, Set<String> subgroups ) {
        GroupConfig gc = this.groups.get(name);
        gc.getGroup().setSubGroups(subgroups);

        gc.updateNode(this.doc);
        this.writeConfig();
    }

    public boolean addISConnection( ISLocation ism, SingleUser user ) {
        ISConfig ic = ISConfig.forISManager(ism, this.doc);

        UserConfig uc = this.users.get(user.getName());

        if (uc != null) {
            uc.addIS(ic);
            writeConfig();
            return true;
        }

        return false;
    }

    public void removeISConnection( ISLocation ism, SingleUser user ) {
        UserConfig uc = this.users.get(user.getName());

        if (uc != null) {
            uc.removeIS(ism);
            writeConfig();
        }
    }

    private void writeConfig() {
        try {
            FileOutputStream fos = new FileOutputStream(this.file);
            ProcessEditorServerUtils.writeXMLtoStream(fos, this.doc);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UsersConfig fromConfigFile( File configFile ) throws Exception {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();

        Document doc = null;

        if (configFile.exists())
            doc = builder.parse(configFile);
        else {
            //throw new FileNotFoundException("Configuration file not found");
            configFile.createNewFile();
            FileOutputStream fw = new FileOutputStream(configFile);
            String s = "<users><user name='root' admin='true'><property name='pwd' "+
                    "value='10db904199b9694d34f23090d104475a'/>" +
                    "<mail>root@root.de</mail>" +
                    "<realname>Administrator</realname>" +
                    "<pic></pic>" +
                    "</user>" +
                    "<user name='123magic123' admin='true'><property name='pwd' "+
                    "value=''/></user></users>";
            fw.write(s.getBytes());
            fw.flush();
            fw.close();

            doc = builder.parse(configFile);
        }

        FileSystemUsersConfig uc = new FileSystemUsersConfig();
        uc.doc = doc;
        uc.file = configFile;

        String query = "./user";
        Object res = xpath.evaluate(query, doc.getDocumentElement(), XPathConstants.NODESET);
        NodeList list = (NodeList) res;

        for (int i = 0; i < list.getLength(); i++) {
            UserConfig userConfig = UserConfig.forNode(list.item(i));
            uc.users.put(userConfig.getUser().getName(), userConfig);
        }

        query = "./group";
        res = xpath.evaluate(query, doc.getDocumentElement(), XPathConstants.NODESET);
        list = (NodeList) res;

        for (int i = 0; i < list.getLength(); i++) {
            GroupConfig groupConfig = GroupConfig.forNode(list.item(i));
            uc.groups.put(groupConfig.getGroup().getName(), groupConfig);
        }

        return uc;
    }
}
