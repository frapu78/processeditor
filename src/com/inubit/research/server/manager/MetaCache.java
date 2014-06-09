/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.meta.MetaDataHandler;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.meta.VersionMetaData;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class for accessing meta data about models.
 * @author fel
 */
public class MetaCache {
    public final static String ATTIC_FOLDER_NAME = "/attic";
    
    private Map<String, VersionMetaData> metaData = new HashMap<String, VersionMetaData>();

    public void loadMetaData(MetaDataHandler handler, String id, String version) {
        VersionMetaData meta = handler.getVersionMetaData(id, version);

        metaData.put(id, meta);
    }

    public VersionMetaData getMetaData(String id) {
        return this.metaData.get(id);
    }

    public String getFolderAlias( String id ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null) {
            return meta.getFolder();
        }

        return "/";
    }

    public void setFolderAlias( String id, String folder , SingleUser user) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null) {
            meta.setFolder(folder);
            meta.getHandler().setFolderAlias(id, folder, user);
        }
    }

//    public void setVersionComment( String id, int version, String comment ) {
//        VersionMetaData meta = metaData.get(id);
//
//        if (meta != null) {
//            meta.getHandler().setVersionComment(id, String.valueOf(version), comment);
//            this.loadMetaData(meta.getHandler(), id, String.valueOf(version));
//        }
//    }

    public String getVersionComment( String id, int version) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null) {
            if (version == -1)
                return meta.getComment();
            return meta.getHandler().getVersionComment(id, String.valueOf(version));
        }

        return "";
    }

    public String getVersionUser ( String id, int version ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null) {
            if (version == -1)
                return meta.getUser();
            return meta.getHandler().getVersionUser(id, String.valueOf(version));
        }

        return "unknown";
    }

    public void addComment( String modelId, ProcessObjectComment comment ) {
        VersionMetaData meta = metaData.get(modelId);

        if (meta != null)
            meta.getHandler().addComment( modelId, comment );
    }

    public Set<ProcessObjectComment> getComments( String modelId, int version, String elementId ) {
        VersionMetaData meta = metaData.get(modelId);

        if (meta != null) 
            return meta.getHandler().getComments( modelId, String.valueOf(version), elementId );

        return new HashSet<ProcessObjectComment>();
    }
    
    public ProcessObjectComment updateComment( String modelId, String commentId, String newText, int validUntil ) {
        VersionMetaData meta = metaData.get(modelId);

        if (meta != null)
            return meta.getHandler().updateComment(modelId, commentId, newText, validUntil);
        
        return null;
    }
    
    @Deprecated
    public void resolveComment( String modelId, String commentId, String version ) {
        VersionMetaData meta = metaData.get(modelId);

        if (meta != null)
            meta.getHandler().resolveComment(modelId, commentId, version);
    }

    public void removeComment( String modelId, String commentId ) {
        VersionMetaData meta = metaData.get(modelId);

        if (meta != null)
            meta.getHandler().removeComment( modelId, commentId );
    }
    
    public Set<String> getPreceedingVersions ( String id, int version ) {
        VersionMetaData meta = metaData.get(id);
        if (meta != null) 
            return meta.getHandler().getPreceedingVersions(id, String.valueOf(version));
        
        return new HashSet<String>();
    }

    public Set<String> getSucceedingVersions ( String id, int version ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().getSucceedingVersions(id, String.valueOf(version));

        return new HashSet<String>();
    }

    public String getOwner( String id ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().getOwner(id);

        return null;
    }

    public Set<User> getViewers( String id ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().getViewers(id);

        return null;
    }

    public Set<User> getEditors( String id ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().getEditors(id);

        return null;
    }

    public Set<User> getAnnotators( String id ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().getAnnotators(id);

        return null;
    }

    public void grantRight( String id, AccessType at, Set<User> users ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            meta.getHandler().grantRight(id, at, users);
    }

    public void divestRight( String id, AccessType at, Set<User> users ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            meta.getHandler().divestRight(id, at, users);
    }

    public boolean setOwner ( String id, SingleUser owner, SingleUser admin ) {
        VersionMetaData meta = metaData.get(id);

        if (meta != null)
            return meta.getHandler().setOwner(id, owner, admin);

        return false;
    }
}
