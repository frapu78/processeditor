/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.DatabaseServerModel;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseSchema;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fel
 */
public class DatabaseMetaDataHandler implements MetaDataHandler {
    private static final DatabaseSchema.Attribute[] EVOLUTION_ATTRIBUTES = { DatabaseSchema.Attribute.EVOLUTION_ID, DatabaseSchema.Attribute.EVOLUTION_VERSION, DatabaseSchema.Attribute.EVOLUTION_SUCCESSOR };
    private static final DatabaseSchema.Attribute[] NEW_COMMENT_ATTRIBUTES = {
        DatabaseSchema.Attribute.COMMENT_MODEL, DatabaseSchema.Attribute.COMMENT_ELEMENT, DatabaseSchema.Attribute.COMMENT_VALIDFROM,
        DatabaseSchema.Attribute.COMMENT_VALIDUNTIL, DatabaseSchema.Attribute.COMMENT_USER, DatabaseSchema.Attribute.COMMENT_TEXT,
        DatabaseSchema.Attribute.COMMENT_CREATED
    };
    private DatabaseConnector db;

    public DatabaseMetaDataHandler( DatabaseConnector dc ) {
        this.db = dc;
    }

    public void setVersionComment(String id, String version, String comment) {
        db.updateAttribute(DatabaseSchema.Attribute.VERSION_COMMENT, comment, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public String getVersionComment(String id, String version) {
        return (String) db.selectSingleAttribute(DatabaseSchema.Attribute.VERSION_COMMENT, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public void setFolderAlias(String id, String alias, SingleUser user) {
        db.updateAttribute(DatabaseSchema.Attribute.MODEL_PATH, alias, DatabaseConnector.EntityType.MODEL, new String[] {id});
    }

    public String getFolderAlias(String id) {
        return (String) db.selectSingleAttribute(DatabaseSchema.Attribute.MODEL_PATH, DatabaseConnector.EntityType.MODEL, new String[] {id});
    }

    public void setVersionUser(String id, String version, String user) {
        db.updateAttribute( DatabaseSchema.Attribute.VERSION_USER, user, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public String getVersionUser(String id, String version) {
        return (String) db.selectSingleAttribute(DatabaseSchema.Attribute.VERSION_USER, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public void addComment(String modelId, ProcessObjectComment comment) {
        db.insertEntity(DatabaseConnector.EntityType.COMMENT, NEW_COMMENT_ATTRIBUTES, new Object[]{
            modelId,
            comment.getElementId(),
            comment.getValidFrom(),
            comment.getValidUntil(),
            comment.getUser(),
            comment.getText(),
            comment.getTimeStamp()
        });
    }

    public Set<ProcessObjectComment> getComments(String modelId, String version, String elementId) {
        return db.getComments(modelId, version, elementId);
    }

    public ProcessObjectComment updateComment(String modelId, String commentId, String newText, int validUntil) {
        Object[] idAtts = new Object[]{commentId};
        db.updateAttribute(DatabaseSchema.Attribute.COMMENT_TEXT, newText, DatabaseConnector.EntityType.COMMENT, idAtts);
        db.updateAttribute(DatabaseSchema.Attribute.COMMENT_VALIDUNTIL, validUntil, DatabaseConnector.EntityType.COMMENT, idAtts);
        
        Object[] commentAtts = db.selectAttributes( NEW_COMMENT_ATTRIBUTES, DatabaseConnector.EntityType.COMMENT, idAtts);
        return ProcessObjectComment.forAttributes(commentId, (String) commentAtts[1], (Date) commentAtts[6], (String) commentAtts[4], (Integer) commentAtts[2], (Integer) commentAtts[3], (String) commentAtts[5]);
    }
    
    @Deprecated
    public void resolveComment(String modelId, String commentId, String version) {
        db.updateAttribute(DatabaseSchema.Attribute.COMMENT_VALIDUNTIL, Integer.valueOf(version), DatabaseConnector.EntityType.COMMENT, new Object[]{commentId});
    }

    public void removeComment(String modeldId, String commentId) {
        db.deleteEntity(DatabaseConnector.EntityType.COMMENT, new String[]{commentId});
    }

    public void setVersionDate(String id, String version, Date date) {
        db.updateAttribute(DatabaseSchema.Attribute.VERSION_CREATED, date, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public String getVersionDate(String id, String version) {
        return (String) db.selectSingleAttribute(DatabaseSchema.Attribute.VERSION_CREATED, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});
    }

    public void setSucceedingVersions(String id, String version, Set<String> versions) {
        db.deleteEntity(DatabaseConnector.EntityType.EVOLUTION_SUCC, new Object[] {id, Integer.valueOf(version)});
        for ( String v : versions )
            db.insertEntity(DatabaseConnector.EntityType.EVOLUTION_SUCC, EVOLUTION_ATTRIBUTES, new Object[]{id, Integer.valueOf(version), Integer.valueOf(v)});
    }

    public Set<String> getSucceedingVersions(String id, String version) {
        Set<String> versions = new HashSet<String>();
        Set<Object> objects = db.selectMultiValueSingleAttribute(DatabaseSchema.Attribute.EVOLUTION_SUCCESSOR, DatabaseConnector.EntityType.EVOLUTION_SUCC, new Object[] {id, Integer.valueOf(version)});

        for ( Object o : objects )
            versions.add( String.valueOf(o) );

        return versions;
    }

    public void setPreceedingVersions(String id, String version, Set<String> versions) {
        db.deleteEntity(DatabaseConnector.EntityType.EVOLUTION_PRE, new Object[] {id, Integer.valueOf(version)});
        for ( String v : versions )
            db.insertEntity(DatabaseConnector.EntityType.EVOLUTION_PRE, EVOLUTION_ATTRIBUTES, new Object[]{id, Integer.valueOf(v), Integer.valueOf(version)});
    }

    public Set<String> getPreceedingVersions(String id, String version) {
        Set<String> versions = new HashSet<String>();
        Set<Object> objects = db.selectMultiValueSingleAttribute(DatabaseSchema.Attribute.EVOLUTION_VERSION, DatabaseConnector.EntityType.EVOLUTION_PRE, new Object[] {id, Integer.valueOf(version)});

        for ( Object o : objects )
            versions.add( String.valueOf(o) );

        return versions;
    }

    public VersionMetaData getVersionMetaData(String id, String version) {
        DatabaseSchema.Attribute[] atts = {DatabaseSchema.Attribute.VERSION_USER, DatabaseSchema.Attribute.VERSION_COMMENT, DatabaseSchema.Attribute.VERSION_CREATED };
        Object[] values = db.selectAttributes(atts, DatabaseConnector.EntityType.VERSION, new Object[]{id, Integer.valueOf(version)});

        String path = (String) db.selectSingleAttribute(DatabaseSchema.Attribute.MODEL_PATH, DatabaseConnector.EntityType.MODEL, new String[] {id});
        return new VersionMetaData(path, (String) values[1], (String) values[0], String.valueOf( values[2] ), this);
    }

    public void remove(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AccessType getAccessability(String id, int version, LoginableUser user) {
         if (user.isSingleUser() && ((SingleUser) user).isAdmin())
            return AccessType.ADMIN;

         if ( user.isSingleUser() ) {
             SingleUser su = (SingleUser) user;

             Object o = db.selectSingleAttribute(DatabaseSchema.Attribute.MODEL_OWNER, DatabaseConnector.EntityType.MODEL, new Object[] {id});
             if ( o != null && ((String) o).equals(su.getName()))
                 return AccessType.OWNER;

             o = db.selectSingleAttribute( DatabaseSchema.Attribute.ACCESS_ACCESS, DatabaseConnector.EntityType.ACCESS_MODEL, new Object[]{id, su.getName(), "SINGLE_USER"});
             if ( o != null ) {
                 return AccessType.valueOf(String.valueOf(o));
             }

             return this.getGroupBasedAcessType(su, id );
         }

         return AccessType.NONE;
    }

    public String getOwner(String id) {
        return ( String ) db.selectSingleAttribute( DatabaseSchema.Attribute.MODEL_OWNER, DatabaseConnector.EntityType.MODEL, new Object[] {id});
    }

    public boolean setOwner(String id, SingleUser owner, SingleUser admin) {
        db.updateAttribute(DatabaseSchema.Attribute.MODEL_OWNER, owner.getName(), DatabaseConnector.EntityType.MODEL,  new Object[] {id});
        return true;
    }

    public Set<User> getViewers(String id) {
        Set<User> viewers = db.getAccessors(id, AccessType.VIEW);
        return viewers;
    }

    public Set<User> getEditors(String id) {
        Set<User> editors = db.getAccessors(id, AccessType.WRITE);
        return editors;
    }

    public Set<User> getAnnotators(String id) {
        Set<User> annotators = db.getAccessors(id, AccessType.COMMENT);
        return annotators;
    }

    public void grantRight(String id, AccessType at, Set<User> users) {
        for ( User u : users )
            db.grantRight(id, at, u);
    }

    public void divestRight(String id, AccessType at, Set<User> users) {
        for ( User u : users )
            db.divestRight(id, at, u);
    }

    private AccessType getGroupBasedAcessType( SingleUser user, String id ) {
        AccessType maxAccess = AccessType.NONE;
        Set<Group> groups = ProcessEditorServerHelper.getUserManager().getRecursiveGroupsForUser(user);
        Set<Object[]> ids = new HashSet<Object[]>(groups.size());
        for ( Group g : groups ) {
            ids.add( new Object[] { id, g.getName(), "GROUP" });
        }

        Set<Object> accesstypes = db.selectMultiValueSingleAttributeWithMultipleIDs(DatabaseSchema.Attribute.ACCESS_ACCESS, DatabaseConnector.EntityType.ACCESS_MODEL, ids.toArray(new Object[0][]));
        try {
        for( Object o : accesstypes ) {
            AccessType a = AccessType.valueOf(String.valueOf(o));
            if (a.compareTo(maxAccess) > 0)
                maxAccess = a;
        }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return maxAccess;
    }

}



