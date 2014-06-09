/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.meta.VersionMetaData;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fel
 */
public class ModelManager {
    private PersistentModelManager persistentModelManager;
    private TemporaryModelManager temporaryModelManager;

    private static ModelManager instance;

    private ModelManager() {
        this.persistentModelManager = new PersistentModelManager();
        this.temporaryModelManager = new TemporaryModelManager();
    }

    public boolean addPersistentLocation( Location l ) {
        return this.persistentModelManager.addLocation(l);
    }

    public String removePersistentLocation( String path, SingleUser user ) {
        return this.persistentModelManager.removeLocation(path, user);
    }

    public boolean persistentModelExists( String id ) {
        return (this.persistentModelManager.getModel(id, -1) != null);
    }

    public boolean moveLocation(String sourcePath, String targetPath, SingleUser user) {
        return this.persistentModelManager.moveLocation(sourcePath, targetPath, user);
    }

    public void updateLocationAtPath( String path ) {
        this.persistentModelManager.updateLocationAtPath( path );
    }

    public boolean addISConnection( ISLocation ism, SingleUser user ) {
        return this.persistentModelManager.addISConnection(ism, user);
    }

    public String addPersistentModel( File f, String comment, String folder, SingleUser user ) throws Exception {
        String id = this.persistentModelManager.addModel(f, folder, user, comment);

        return id;
    }

    public ServerModel getPersistentModel( String id , int version ) {
        return this.persistentModelManager.getModel(id, version);
    }

    public String removePersistentModel( String id , SingleUser user ) {
        return this.persistentModelManager.removeModel(id, user);
    }

    public List<ProcessNode> getNodesOfType(final Class<?> nodeType, final String modelId, final SingleUser user) {
        return this.persistentModelManager.getNodesOfType(null, nodeType, user);
    }

    public String createNewModel( Class<? extends ProcessModel> c ) throws InstantiationException, IllegalAccessException {
        ProcessModel newModel = c.newInstance();
        String tmpId = this.temporaryModelManager.addModel(newModel, newModel.getId());

        return this.createTmpUri(tmpId);
    }

    public String addTemporaryModel( ProcessModel pm ) {
        String tmpId = this.temporaryModelManager.addModel(pm, pm.getId());

        return this.createTmpUri(tmpId);
    }

    public ProcessModel getTemporaryModel( String id ) {
        return this.temporaryModelManager.getModel(id);
    }

    public void removeTemporaryModel( String id ) {
        this.temporaryModelManager.removeModel(id);
    }

    public String createTemporaryModel( String id , int version ) {
        ServerModel lm = this.persistentModelManager.getModel(id, version);
        try {
        String tmpId = this.temporaryModelManager.addModel(lm.getModel().clone(), id);
        
        return this.createTmpUri(tmpId);
        } catch ( Exception ex ) {
        	ex.printStackTrace();
        	return null;
        }
    }

    public int getPersistentVersionCount( String id ) {
        return this.persistentModelManager.getVersionCount(id);
    }

    public Map<String, AccessType> getRecentVersions(SingleUser user) {
        return this.persistentModelManager.getRecentVersions(user);
    }

    public ServerModel getRecentVersion( String id ) {
        return this.persistentModelManager.getRecentVersion(id);
    }

    public int saveModel( ProcessModel newModel, String id, int version, String comment, String folder, Set<String> precVersions, SingleUser user ) {
        if (persistentModelManager.addNewModelVersion(newModel, id, version, precVersions, comment, folder, user))
            return this.persistentModelManager.getVersionCount(id);
        else
            return -1;
    }

    public void addComment( String modelId, ProcessObjectComment comment ) {
        this.persistentModelManager.addComment( modelId, comment );
    }

    public ProcessObjectComment updateComment( String modelId, String commentId, String newText, int validUntil ) {
        return this.persistentModelManager.updateComment(modelId, commentId, newText, validUntil);
    }
    
    @Deprecated
    public void resolveComment( String modelId, String commentId, String version ) {
        this.persistentModelManager.resolveComment(modelId, commentId, version);
    }
    
    public void deleteComment( String modelId, String commentId ) {
        this.persistentModelManager.deleteComment( modelId, commentId );
    }

    public Set<ProcessObjectComment> getComments( String modelId, int version, String elementId ) {
        return this.persistentModelManager.getComments( modelId, version, elementId );
    }

    public VersionMetaData getRecentMetaData( String id ) {
        return this.persistentModelManager.getRecentMetaData( id );
    }

    public String getFolderAlias( String id ) {
        return this.persistentModelManager.getFolderAlias(id);
    }

    public void setFolderAlias( String id, String folder, SingleUser user ) {
        this.persistentModelManager.setFolderAlias(id, folder, user);
    }

    public String getVersionComment( String id, int version ) {
        return this.persistentModelManager.getVersionComment(id, version);
    }

    public String getVersionUser ( String id, int version ) {
        return this.persistentModelManager.getVersionUser( id, version );
    }

    public Set<String> getPreceedingVersions ( String id, int version ) {
        return this.persistentModelManager.getPreceedingVersions( id, version );
    }

    public Set<String> getSucceedingVersions ( String id, int version ) {
        return this.persistentModelManager.getSucceedingVersions( id, version );
    }

    public Map<String, Location.LocationType> listLocations(SingleUser user) {
        return this.persistentModelManager.listLocations( user );
    }

    public Set<String> getLocationsForUser( SingleUser user ) {
        return this.persistentModelManager.getLocationsForUser( user );
    }

    public String getHomeLocation( SingleUser user ) {
        return this.persistentModelManager.getHomeLocation(user);
    }

    public String getHomePath( SingleUser user ) {
        return this.persistentModelManager.getHomePath( user );
    }

    public AccessType getAccessForModel( String id, int version, LoginableUser user ) {
        return this.persistentModelManager.getAccessForModel(id, version, user);
    }

    public String getOwner( String id ) {
        return this.persistentModelManager.getOwner(id);
    }

    public Set<User> getViewers( String id ) {
        return this.persistentModelManager.getViewers( id );
    }

    public Set<User> getEditors( String id ) {
        return this.persistentModelManager.getEditors( id );
    }

    public Set<User> getAnnotators( String id ) {
        return this.persistentModelManager.getAnnotators ( id );
    }

    public void grantRight( String id, AccessType at, Set<User> users ) {
        this.persistentModelManager.grantRight( id, at, users);
    }

    public void divestRight( String id, AccessType at, Set<User> users ) {
        this.persistentModelManager.divestRight( id, at, users);
    }

    public void setOwner( String id, SingleUser owner, SingleUser admin ) {
        this.persistentModelManager.setOwner( id, owner, admin );
    }

    private String createTmpUri( String tmpId ) {
        return TemporaryModelManager.TMP_URI_PREFIX + tmpId;
    }

    public static ModelManager getInstance() {
        if (instance == null)
            instance = new ModelManager();

        return instance;
    }

    public void reloadIndex() {
        persistentModelManager.reloadIndex();
    }
    
    
}
