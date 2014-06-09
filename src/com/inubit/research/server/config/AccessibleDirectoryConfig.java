/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Confgiguration for directories with configurable access.
 * 
 * @author fel
 */
public class AccessibleDirectoryConfig extends FileSystemConfig{
    //for xpath usage
    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private static XPath xpath = xpathFactory.newXPath();

    private AccessConfig access;

    private AccessibleDirectoryConfig() { }

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

    public boolean isWriteableByUser( User user, String id ) {
        FileSystemConfig dc = this.getDirConfigForModel(id);
        ModelConfig mc = dc.models.get(id);

        if (mc != null)
            return mc.isWriteableByUser(user);

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

    public boolean isReadableByUser( User user, String id ) {
        FileSystemConfig dc = this.getDirConfigForModel(id);
        ModelConfig mc = dc.models.get(id);

        if (mc != null)
            return mc.isReadableByUser(user);

        return false;
    }

    public boolean isAnnotatableByUser ( User user ) {
        if (this.access == null && this.parent != null)
            return this.parent.isAnnotatableByUser(user);

        if (this.access != null && this.access.hasCommentAccess(user))
            return true;

        if (this.parent != null)
            return this.parent.isAnnotatableByUser(user);
        else
            return false;
    }

    public boolean isAnnotatableByUser ( User user , String id ) {
        FileSystemConfig dc = this.getDirConfigForModel(id);
        ModelConfig mc = dc.models.get(id);

        if (mc != null)
            return mc.isAnnotatableByUser(user);

        return false;
    }

    @Override
    public boolean isOwner(SingleUser user) {
        if (parent != null)
            return parent.isOwner(user);
        else
            return false;
    }

    static FileSystemConfig forNode ( Node node , FileSystemConfig parent ) throws Exception {
        AccessibleDirectoryConfig config = new AccessibleDirectoryConfig();

        //Set p and name
        config.node = node;
        config.parent = parent;
        config.name = node.getAttributes().getNamedItem("name").getNodeValue();

        String query = "./access";
        Object res = xpath.evaluate(query, node, XPathConstants.NODE);
        Node accessNode = (Node) res;

        //parse access
        if (accessNode != null)
            config.access = AccessConfig.forNode(accessNode);
//        String query = "./access/read";
//        Object res = xpath.evaluate(query, node, XPathConstants.NODE);
//        Node readNode = (Node) res;
//
//        if (readNode != null)
//            config.readAccess = AccessConfig.forNode(readNode);
//
//        query = "./access/write";
//        res = xpath.evaluate(query, node, XPathConstants.NODE);
//        Node writeNode = (Node) res;
//
//        if (writeNode != null)
//            config.writeAccess = AccessConfig.forNode(writeNode);

        //parse subdirectories
        query = "./" + DirectoryConfig.SUB_DIR_TAG_NAME;
        res = xpath.evaluate(query, node, XPathConstants.NODESET);
        NodeList subDirs = (NodeList) res;

        int size = subDirs.getLength();
        Map<String, FileSystemConfig> subDirConfigs = new HashMap<String, FileSystemConfig>(size);
        for (int i = 0; i < size; i++) {
            FileSystemConfig subDirConf = forNode(subDirs.item(i), config);
            subDirConfigs.put(subDirConf.name, subDirConf);
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
}
