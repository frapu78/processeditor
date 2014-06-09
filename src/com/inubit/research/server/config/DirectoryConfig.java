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
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configuration for directories that can not be configured using access
 * configurations. On accessability checks always false is returned.
 * 
 * @author fel
 */
public class DirectoryConfig extends FileSystemConfig implements StructuralConfig {
    public static final String DEFAULT_FILE_NAME = "directory.cfg.xml";
    public static final String USER_HOME_ROOT_PATH = "home";
    private static final String USER_HOME_TAG_NAME = "userhome";
    static final String SUB_DIR_TAG_NAME = "subdir";

    //for xpath usage
    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private static XPath xpath = xpathFactory.newXPath();

    private Document doc = null;
    private File configFile;

    private Map<User, Set<String>> sharedIndex = new HashMap<User, Set<String>>();

    private DirectoryConfig() {};

    public String getPathForModel( String id ) {
       FileSystemConfig fc = this.getDirConfigForModel(id);

       if (fc != null)
           return fc.getPath();

       return null;
    }

    public void addModel(String id , String path, SingleUser user) {
        try {
            this.addModel(id, path, this.doc, user);

            this.writeConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPath(String id, String path, SingleUser user) {
        try {
            String userHome = this.getUserHome(user);
            if (! (path.equals(userHome) || path.startsWith(userHome + "/")) )
                path = userHome + path;

            FileSystemConfig sourceDC = this.getDirConfigForModel(id);
            if (! (sourceDC.getPath().equals(userHome) ||
                    sourceDC.getPath().startsWith(userHome + "/")))
                return;

            FileSystemConfig targetDC = this.getDirConfigForPath(path);

            if (targetDC == null) 
                targetDC = this.createDirConfig(path, this.doc);

            ModelConfig mc = sourceDC.models.get(id);

            sourceDC.node.removeChild(mc.getNode());
            targetDC.node.appendChild(mc.getNode());

            sourceDC.models.remove(id);
            targetDC.models.put(id, mc);

            this.writeConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setOwner( String id, SingleUser owner, SingleUser user ) {
        ModelConfig mc = this.getModelConfig(id);

        if (mc != null) {
            this.setPath(id, getUserHome(owner), user);

            Node mcNode = mc.getNode();

            NodeList children = mcNode.getChildNodes();
            Node ownerNode = null;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeName().equals("owner")) {
                   ownerNode = children.item(i);
                   break;
                }
            }

            if (ownerNode == null)
                ownerNode = doc.createElement("owner");
            
            ownerNode.setTextContent(owner.getName());

            mcNode.appendChild(ownerNode);
            mc.setOwner(owner.getName());

            this.writeConfigFile();

            return true;
        }

        return false;
    }
//
    public boolean moveDirectory(String dirPath, String targetPath, SingleUser user ) {
        try {
            FileSystemConfig sourceDC = this.getDirConfigForPath(dirPath/*, this.doc*/);
            FileSystemConfig targetDC = this.getDirConfigForPath(targetPath/*, this.doc*/);

            if (targetDC == null)
                targetDC = createDirConfig(targetPath, this.doc);

            if (targetDC == null)
                return false;

             if (!sourceDC.isOwner(user) || !targetDC.isOwner(user))
                return false;

            FileSystemConfig parentDC = sourceDC.parent;
            parentDC.subdirectories.remove(sourceDC.name);

            if (targetDC.subdirectories.containsKey(sourceDC.name)) {
                targetDC.subdirectories.get(sourceDC.name).models.putAll(sourceDC.models);
                targetDC.subdirectories.get(sourceDC.name).subdirectories.putAll(sourceDC.subdirectories);
            } else {
                targetDC.subdirectories.put(sourceDC.name, sourceDC);
                sourceDC.parent = targetDC;
            }

            parentDC.node.removeChild(sourceDC.node);
            targetDC.node.appendChild(sourceDC.node);

            this.writeConfigFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void remove(String id) {
        try {
            FileSystemConfig dc = this.getDirConfigForModel(id);
            ModelConfig mc = dc.models.get(id);

            dc.node.removeChild(mc.getNode());
            dc.models.remove(id);

            writeConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeDirectory( String path, SingleUser user ) {
        FileSystemConfig dc = this.getDirConfigForPath(path/*, this.doc*/);

        if (!dc.isOwner(user))
            return;

        FileSystemConfig p = dc.parent;

        p.subdirectories.remove(dc.name);
        p.node.removeChild(dc.node);

        this.writeConfigFile();
    }

    public String getUserHome( SingleUser user ) {
        if (user.isAdmin())
            return "/" + USER_HOME_ROOT_PATH;
        else
            return "/" + USER_HOME_ROOT_PATH + "/" + user.getName();
    }

    public Set<String> listUserHome( SingleUser user) {
        String path = this.getUserHome(user);
        FileSystemConfig fc = this.getDirConfigForPath(path);

        if (fc == null) {
            fc = this.createUserHome(user);
        }

        return fc.listPaths();
    }

    public Set<String> listSharedPaths( User user ) {
        Set<String> ids = this.getSharedModels(user);
        Set<String> paths = new HashSet<String>();

        if (ids != null)
            for (String id : ids) {
                String path = this.getPathForModel(id);

                paths.add(path);
            }

        return paths;
    }

    public Set<String> getSharedModels( User user ) {
        return this.getSharedModels(user, new HashSet<Group>());
    }

    public DirectoryConfig createUserHome( SingleUser user ) {
        String path = this.getUserHome(user);

        DirectoryConfig existingConfig = (DirectoryConfig) this.getDirConfigForPath(path);
        if (existingConfig != null)
            return existingConfig;

        Element newUserHome = this.doc.createElement(USER_HOME_TAG_NAME);
        newUserHome.setAttribute("name", user.getName());
        
        try {
            DirectoryConfig dc = forNode(newUserHome, this);
            this.node.appendChild(newUserHome);
            this.subdirectories.put(user.getName(), dc);
            this.writeConfigFile();
            return dc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public void grantRight( String id, AccessType at, Set<User> users ) {
        ModelConfig mc = this.getModelConfig(id);
        if (mc == null)
            return;

        String right = at.toString().toLowerCase();

        Node modelNode = mc.getNode();
        try {
            String query = "./access/" + right + "/users";
            Object res = xpath.evaluate(query, modelNode, XPathConstants.NODE);
            Node usersNode = (Node) res;

            query = "./access/" + right + "/groups";
            res = xpath.evaluate(query, modelNode, XPathConstants.NODE);
            Node groupsNode = (Node) res;

            for (User user : users) {
                if (user.isGroup()) {
                    Element groupEl = this.doc.createElement("group");
                    groupEl.setAttribute("name", user.getName());

                    groupsNode.appendChild(groupEl);
                } else if (user.isSingleUser()) {
                    Element userEl = this.doc.createElement("user");
                    userEl.setAttribute("name", user.getName());

                    usersNode.appendChild(userEl);
                }

                if (this.sharedIndex.containsKey(user))
                    this.sharedIndex.get(user).add(id);
                else {
                    Set<String> ids = new HashSet<String>();
                    ids.add(id);
                    this.sharedIndex.put(user, ids);
                }
            }

            mc.grantRight(at, users);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.writeConfigFile();
    }

    public void divestRight( String id, AccessType at, Set<User> users ) {
        ModelConfig mc = this.getModelConfig(id);
        if (mc == null)
            return;

        String right = at.toString().toLowerCase();

        Node modelNode = mc.getNode();
        try {
            String query = "./access/" + right + "/users";
            Object res = xpath.evaluate(query, modelNode, XPathConstants.NODE);
            Node usersNode = (Node) res;

            query = "./access/" + right + "/groups";
            res = xpath.evaluate(query, modelNode, XPathConstants.NODE);
            Node groupsNode = (Node) res;

            for (User user : users) {
                if (user.isGroup()) {
                    query = "./group[@name='" + user.getName()+ "']";
                    res = xpath.evaluate(query, groupsNode, XPathConstants.NODE);

                    Node n = (Node) res;
                    if (n != null)
                        n.getParentNode().removeChild(n);
                } else if (user.isSingleUser()) {
                    query = "./user[@name='" + user.getName()+ "']";

                    res = xpath.evaluate(query, usersNode, XPathConstants.NODE);
                    Node n = (Node) res;
                    if (n != null)
                        n.getParentNode().removeChild(n);
                }

                this.sharedIndex.get(user).remove(id);
            }

            mc.divestRight(at, users);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.writeConfigFile();
    }

    @Override
    public boolean isReadableByUser ( User user ) {
        return false;
    }

    @Override
    public boolean isReadableByUser ( User user , String id ) {
        return false;
    }

    @Override
    public boolean isWriteableByUser ( User user ) {
        return false;
    }

    @Override
    public boolean isWriteableByUser ( User user , String id ) {
        return false;
    }

    @Override
    public boolean isAnnotatableByUser ( User user ) {
        return false;
    }

    @Override
    public boolean isAnnotatableByUser ( User user , String id ) {
        return false;
    }

    @Override
    public boolean isOwner( SingleUser user ) {
        if (this.getPath().equals("/" + USER_HOME_ROOT_PATH) && user.isAdmin())
            return true;

        if (this.name.equals(user.getName()))
            return true;

        return false;
    }

    private Set<String> getSharedModels( User user, Set<Group> visitedGroups ) {
        Set<String> ids = this.sharedIndex.get(user);
        ids = ids != null ? new HashSet<String>(ids) : new HashSet<String>();

        Set<Group> groups = ProcessEditorServerHelper.getUserManager().getGroupsForUser(user);
        for (Group g : groups) {
            if (visitedGroups.contains(g))
                continue;
            visitedGroups.add(g);
            ids.addAll(this.getSharedModels(g, visitedGroups));
        }

        //Remove ids of models were this user is already owner
        if (user.isSingleUser()) {
            SingleUser u = (SingleUser) user;
            Set<String> toRemove = new HashSet<String>();


            for ( String id : ids ) {
                if (this.getModelConfig(id).isOwner(u))
                    toRemove.add(id);
            }

            ids.removeAll(toRemove);
        }
        

        return ids;
    }

    private void addModel(String id , String path, Document doc, SingleUser user) throws Exception {
        FileSystemConfig targetConfig = null;
        if (!user.isAdmin()) {
            FileSystemConfig homeDir = this.getDirConfigForPath(this.getUserHome(user));

            if (homeDir == null) {
                homeDir = this.createUserHome(user);
            }
            String pathRest = path.replaceFirst(this.getUserHome(user), "");
            targetConfig = homeDir.createDirConfig( pathRest, this.doc );
        } else {
            targetConfig = this.createDirConfig( path, this.doc );
        }
        //FileSystemConfig dc = this.getDirConfigForPath(path/*, doc*/);
        Element newModelNode = doc.createElement("model");
        Element ownerElement = doc.createElement("owner");
        ownerElement.setTextContent(user.getName());

        newModelNode.appendChild(ownerElement);
        newModelNode.setAttribute("id", id);

        targetConfig.node.appendChild(newModelNode);
        newModelNode.appendChild(AccessConfig.createInitialConfigNode(doc));
        targetConfig.models.put(id, ModelConfig.forNode(newModelNode, targetConfig));
    }

    private void writeConfigFile() {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            ProcessEditorServerUtils.writeXMLtoStream(fos, doc);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildSharedIndex() {
        Set<String> modelIds = this.getModelIDs(true);

        for (String id : modelIds) {

            ModelConfig mc = this.getModelConfig(id);
            Set<User> users = new HashSet<User>(mc.getViewers());
            users.addAll(mc.getEditors());
            users.addAll(mc.getAnnotators());

            for (User u : users) {
                if (this.sharedIndex.containsKey(u))
                    this.sharedIndex.get(u).add(id);
                else {
                    Set<String> ids = new HashSet<String>();
                    ids.add(id);
                    this.sharedIndex.put(u, ids);
                }
            }
        }
    }

    public static DirectoryConfig forDirectory(File dir) throws Exception {
        File configFile = new File(dir, DEFAULT_FILE_NAME);
        
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        
        Document doc = null;
        
        if (configFile.exists())
            doc = builder.parse(configFile);
        else
            doc = createEmptyConfiguration();

        DirectoryConfig config = forNode(doc.getDocumentElement(), null);
        config.doc = doc;
        config.node = doc.getDocumentElement();
        config.configFile = configFile;

        config.buildSharedIndex();

        return config;
    }

    private static DirectoryConfig forNode(Node node, DirectoryConfig parent) throws Exception {
        DirectoryConfig config = new DirectoryConfig();

        //Set p and name
        config.node = node;
        config.parent = parent;
        config.name = node.getAttributes().getNamedItem("name").getNodeValue();

        //parse subdirectories

        Map<String, FileSystemConfig> subDirConfigs = new HashMap<String, FileSystemConfig>();
        String query = "./" + SUB_DIR_TAG_NAME;
        Object res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList subDirs = (NodeList) res;

        int size = subDirs.getLength();
        
        for (int i = 0; i < size; i++) {
            FileSystemConfig subDirConf = AccessibleDirectoryConfig.forNode(subDirs.item(i), config);
            subDirConfigs.put(subDirConf.name, subDirConf);
        }

        //if we are not parsing a user home, query for user homes
        if (!config.node.getNodeName().equals(USER_HOME_TAG_NAME) ) {
            query = "./" + USER_HOME_TAG_NAME;

            res = xpath.evaluate(query, node, XPathConstants.NODESET);
            NodeList userHomes = (NodeList) res;

            size = userHomes.getLength();

            for (int i = 0; i < size; i++) {
                DirectoryConfig subDirConf = forNode(userHomes.item(i), config);
                subDirConfigs.put(subDirConf.name, subDirConf);
            }
        }

        config.subdirectories = subDirConfigs;

        //parse models
        query = "./model";
        res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList models = (NodeList) res;

        size = models.getLength();
        Map<String, ModelConfig> modelConfigs = new HashMap<String, ModelConfig>(size);
        for (int i = 0; i < size; i++) {
            ModelConfig modConf = ModelConfig.forNode(models.item(i), config);
            modelConfigs.put(modConf.getId(), modConf);
        }

        config.models = modelConfigs;

        return config;
    }

    private static Document createEmptyConfiguration() throws Exception {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();

        Document doc = builder.newDocument();
        Element el = doc.createElement("directory");
        el.setAttribute("name", USER_HOME_ROOT_PATH);

        doc.appendChild(el);

        return doc;
    }
}
