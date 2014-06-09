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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract super class for directory configurations.
 * @author felix
 */
public abstract class FileSystemConfig {
    protected String name;

    protected Map<String, FileSystemConfig> subdirectories;
    protected Map<String, ModelConfig> models;

    protected FileSystemConfig parent;
    protected Node node;

    public String getPath() {
        if (this.parent == null)
            if (!this.name.equals(""))
                return "/" + this.name;
            else
                return "";
        else
            if (!this.name.equals(""))
                return this.parent.getPath() + "/" + this.name;
            else
                return this.parent.getPath();
    }

    public Set<String> listPaths() {
        Set<String> paths = new HashSet<String>();
        String ownPath = this.getPath();

        if (!ownPath.startsWith("/"))
            ownPath = "/" + ownPath;

        paths.add(ownPath);

        for (FileSystemConfig fc : subdirectories.values()) {
            paths.addAll(fc.listPaths());
        }

        return paths;
    }

    public Set<String> getModelIDs( String path, boolean recursive ) {
        FileSystemConfig dc = this.getDirConfigForPath(path/*, this.doc*/);

        if (dc != null) {
            return dc.getModelIDs(recursive);
        }

        return new HashSet<String>();
    }

    public ModelConfig getModelConfig ( String id ) {
        FileSystemConfig fc = this.getDirConfigForModel(id);

        if (fc != null)
            return fc.models.get(id);

        return null;
    }

    public abstract boolean isReadableByUser ( User user ) ;

    public abstract boolean isReadableByUser ( User user , String id );

    public abstract boolean isWriteableByUser ( User user );

    public abstract boolean isWriteableByUser ( User user , String id );

    public abstract boolean isAnnotatableByUser ( User user );
    
    public abstract boolean isAnnotatableByUser ( User user , String id );

    public abstract boolean isOwner ( SingleUser user );


    protected FileSystemConfig getDirConfigForPath( String path /*,Document doc*/ ) {
        int index = path.indexOf("/", 1);
        if (index < 0 && path.substring(1).equals(this.name)) {
            //this is the final part of path --> reached destination
                return this;
        } else {
            String dirName;
            String replacement = "";
            if (path.startsWith("/"))
                replacement = "/";

            //remove this dir's name from path
            replacement += this.name;
            path = path.replaceFirst(replacement, "");
            index = path.indexOf("/", 1);

            if (path.equals(""))
                return this;

            if (index < 0)
                index = path.length();
            dirName = path.substring(1, index);
                //path = path.substring(index);
            

            if (this.subdirectories.containsKey(dirName)) {
                //if sub dir exists
                return this.subdirectories.get(dirName).getDirConfigForPath(path /*, doc*/);
            }
        }

        return null;
    }

    protected FileSystemConfig getDirConfigForModel( String id ) {
        if (this.models.keySet().contains(id)) {
            return this;
         }

        FileSystemConfig con = null;
        for (FileSystemConfig dc : subdirectories.values()) {
            con = dc.getDirConfigForModel(id);
            if (con != null)
                break;
        }

        return con;
    }

    protected FileSystemConfig createSubDir( String dirName , Document doc ) {
        System.out.println("Create dir: " + dirName);
        Element newSubDir = doc.createElement(DirectoryConfig.SUB_DIR_TAG_NAME);
        newSubDir.setAttribute("name", dirName);
        this.node.appendChild(newSubDir);
        try {
            FileSystemConfig newDC =  AccessibleDirectoryConfig.forNode(newSubDir, this);
            this.subdirectories.put(dirName, newDC);
            
            return newDC;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected Set<String> getModelIDs( boolean recursive ) {
        Set<String> modelIds = new HashSet<String>();

        modelIds.addAll(this.models.keySet());

        if(recursive) {
            for (FileSystemConfig dc : this.subdirectories.values())
                modelIds.addAll(dc.getModelIDs(recursive));
        }

        return modelIds;
    }

    protected FileSystemConfig createDirConfig( String path, Document doc ) {
        FileSystemConfig fc = null;

        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        if (!path.startsWith("/"))
            path = "/" + path;

        if (path.equals("/")) {
            return this;
        }

        String[] pathParts = path.split("/");
        String p = "";

        FileSystemConfig lastFc = this;
        for (int i  = 1; i < pathParts.length; i++) {
            p += "/" + pathParts[i];
            fc = this.getDirConfigForPath(p);
            if (fc == null)
                fc = lastFc.createSubDir(pathParts[i], doc);

            lastFc = fc;
        }

        return fc;
    }
}