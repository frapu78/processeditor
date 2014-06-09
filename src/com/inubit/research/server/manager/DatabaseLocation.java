/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.config.DirectoryConfig;
import com.inubit.research.server.meta.DatabaseMetaDataHandler;
import com.inubit.research.server.meta.MetaDataHandler;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.DatabaseServerModel;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseConnector.EntityType;
import com.inubit.research.server.persistence.DatabaseSchema;
import com.inubit.research.server.user.SingleUser;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fel
 */

public class DatabaseLocation implements Location, UserHomeable {
    private DatabaseConnector db;
    private DatabaseMetaDataHandler metaHandler;
    private Map<String, List<ServerModel>> index;

    DatabaseLocation() {
        this (ProcessEditorServerHelper.getDatabaseConnector());
    }

    public DatabaseLocation( DatabaseConnector dc ) {
        db = dc;
        metaHandler = new DatabaseMetaDataHandler(dc);
    }

    public boolean checkConnection() {
        return db.checkConnection();
    }

    public boolean removeModel(String id, SingleUser user) {
        String folder = this.metaHandler.getFolderAlias(id);
        return folder.startsWith(this.getAtticPath(user));
    }

    public String getAtticPath(SingleUser user) {
        return this.getUserHome(user) + MetaCache.ATTIC_FOLDER_NAME;
    }

    public Map<String, List<ServerModel>> getIndex(Set<String> usedIDs, boolean forceRefresh) {
        Map<String, List<ServerModel>> index = new HashMap<String, List<ServerModel>>();

        if ( checkConnection() ) {
            Set<Object> ids = db.selectSingleAttribute( DatabaseSchema.Attribute.MODEL_ID, EntityType.MODEL );
            for ( Object id : ids ) {
                index.put( String.valueOf(id), new DatabaseModelListProxy(db, String.valueOf(id)) );
            }
        }

        this.index = index;

        return index;
    }

    public MetaDataHandler getMetaDataHandler() {
        return this.metaHandler;
    }

    public LocationType getType() {
        return LocationType.DB;
    }

    public String getName() {
        return db.getConnectionName();
    }

    public Set<String> listPaths() {
        Set<String> paths = new HashSet<String>();

        Set<Object> dbresult = db.selectSingleAttribute( DatabaseSchema.Attribute.MODEL_PATH, EntityType.MODEL );
        for ( Object o : dbresult )
            paths.add((String) o);

        return paths;
    }

    public Set<String> listPaths(SingleUser user) {
       return db.getAccessiblePaths(user);
    }

    public Map<String, AccessType> getModelsForUser(SingleUser user) {
        return db.getModelsWithAccess(user);
    }

    public ServerModel createNewModel(File model, String path, String id, SingleUser user, String comment) {
        if ( db.addModel(model, path, id, user, comment) )
            return new DatabaseServerModel(db, id, 1);

        return null;

    }

    public String getUserHome(SingleUser user) {
        if (user.isAdmin())
            return "/" + DirectoryConfig.USER_HOME_ROOT_PATH;
        else
            return "/" + DirectoryConfig.USER_HOME_ROOT_PATH + "/" + user.getName();
    }

    public void createUserHome(SingleUser user) {
        //empty
    }

}
