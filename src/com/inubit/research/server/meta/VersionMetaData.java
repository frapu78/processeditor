/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.model.ServerModel;

/**
 * Meta data belonging to one specific model version
 * @author fel
 */
public class VersionMetaData {
    private String folder;
    private String comment;
    private String user;
    private String date;
    private MetaDataHandler handler;
    private String name;
    private String author;
    private String creationComment;
    private String creationDate;
    private String lastModified;

    public VersionMetaData( String folder, String comment, String user, String date, MetaDataHandler handler ) {
        this.folder = folder;
        this.comment = comment;
        this.handler = handler;
        this.user = user;
        this.date = date;
    }

    public String getFolder() {
        return this.folder;
    }

    public String getComment() {
        return this.comment;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return this.date;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public MetaDataHandler getHandler() {
        return this.handler;
    }

    public void reloadFolder( String id ) {
        this.folder = this.handler.getFolderAlias(id);
    }

    public String getProcessName() {
        return this.name;
    }

    public void setProcessName( String name ) {
        this.name = name;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCreationComment() {
        return this.creationComment;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public String getLastUpdateDate() {
        return this.lastModified;
    }

    public void loadRequiredData( ServerModel lm ) {
        this.name = lm.getModelName();
        this.author = lm.getAuthor();
        this.creationComment = lm.getComment();
        this.creationDate = lm.getCreationDate();
        this.lastModified = lm.getLastUpdateDate();
    }
}
