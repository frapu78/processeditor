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
import com.inubit.research.server.meta.MetaDataHandler;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.meta.VersionMetaData;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 * Class for handling parsistent and cached index
 * @author fel
 */
public class PersistentModelManager {
    //manager for all persistent locations
    private final PersistentModelLocationManager locationManager;

    //model model cache
    private ModelCache modelCache = new ModelCache();

    //meta data cache
    private MetaCache metaCache = new MetaCache();

    //index of persistent models
    private Map<String, List<ServerModel>> index = new HashMap<String, List<ServerModel>>();

    /**
     * Create new PersistentModelManager with default home directory
     */
    PersistentModelManager() {
        this.locationManager = new PersistentModelLocationManager();
        this.loadIndex();
    }

    /**
     * Add a new model location / repository part
     * returns a boolean indicating whether the addition was succesful or not
     * @param location the location
     */
    public boolean addLocation( Location location ) {
        try {
            if (this.locationManager.addLocation( location, this.getUsedIds() )) {
                // Check if reachable
                if (location.checkConnection()) {
                    Map<String, List<ServerModel>> tmp = location.getIndex(this.getUsedIds(), false);
                    this.addToIndex( tmp );
                    return true;
                }
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean addISConnection( ISLocation ism, SingleUser user ) {
        //create file for IS
        try {
            boolean added = this.locationManager.addISConnection( ism, user );

            if ( added )
                this.addLocation( ism );

            return added;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean moveLocation( String sourcePath, String targetPath, SingleUser user ) {
        Location sourceLocation = this.locationManager.getLocationForPath(sourcePath);

        if (sourceLocation.getType().equals(Location.LocationType.FILE) &&
                this.locationManager.moveLocation(sourcePath, targetPath, user)) {

            
            int i = sourcePath.lastIndexOf("/");
            String folderName = sourcePath.substring(i+1);
//            String sourcePathStart = sourcePath.substring(0 , i);
            String targetPathStart = targetPath;

            if (targetPathStart.endsWith("/"))
                targetPathStart += folderName;
            else
                targetPathStart += "/" + folderName;

            Set<String> ids = ((FileSystemLocation) sourceLocation).getModelIDs(targetPathStart, true);

            for (String id : ids) {
                VersionMetaData vmd = this.metaCache.getMetaData(id);
                vmd.reloadFolder(id);
            }

            return true;
        }

        return false;
    }

    public void updateLocationAtPath( String path ) {
        Location l = this.locationManager.getLocationForPath(path);

        if ( l == null )
            return;

        Set<String> ids = l.getIndex( this.getUsedIds() , false ).keySet();
        for ( String id : ids )
            this.index.remove(id);

        this.locationManager.removeFromIndex(l);
        this.addLocation(l);
    }

    public String removeLocation( String path, SingleUser user ) {
        Location toRemove = this.locationManager.getLocationForPath(path);
        Set<String> ids = null;
        if (toRemove.getType().equals(Location.LocationType.FILE))
            ids = ((FileSystemLocation) toRemove).getModelIDs(path, true);

        String state = this.locationManager.removeLocation(toRemove, path, user);
        String userHome = this.getHomePath(user);

        String pathRest = path.replaceFirst(userHome, "");
        if (!pathRest.startsWith("/"))
            pathRest = "/" + pathRest;
        
        if (state.equals("deleted")) {

            if (toRemove.getType().equals(Location.LocationType.FILE))
                for (String id : ids)
                    this.removeModelPhysically(id);


            this.reloadIndex();
        } else if (state.equals("moved")) {
             if (toRemove.getType().equals(Location.LocationType.FILE))
                 for (String id : ids) {
                    VersionMetaData vmd = this.metaCache.getMetaData(id);
                    vmd.reloadFolder(id);
                 }
        }

        return state;
    }

    public Map<String, Location.LocationType> listLocations( SingleUser user ) {
        return this.locationManager.listLocations( user );
    }

    public Set<String> getLocationsForUser( SingleUser user ) {
        return this.locationManager.getLocationsForUser(user);
    }

    public String getHomeLocation( SingleUser user ) {
        return this.locationManager.getHomeLocation(user);
    }

    public String getHomePath( SingleUser user ) {
        return this.locationManager.getHomePath( user ) ;
    }

    public void createUserHome( SingleUser user )  {
        this.locationManager.createUserHome( user );
    }

    /**
     * Add model contained in the given file
     * @param modelFile the file containing the model
     * @return <ul>
     *  <li> null, if model could not be added to working dir
     *  <li> the model's unique id, otherwise
     * </ul>
     * @throws Exception thrown if there occured an error while parsing the given modelFile
     */
    public String addModel( File modelFile , String path, SingleUser user, String comment ) throws Exception {
        //parse model
        ProcessModel model = new ProcessEditorImporter().parseSource(modelFile).get(0);

        //create globally unique ID
        String uniqueId = this.createUniqueId(model);
        if ( uniqueId != model.getId() ) {
        	model.setId( uniqueId );
        	new ProcessEditorExporter().serialize(modelFile, model);
        }
        	

        Location l = this.locationManager.getSaveableLocation(path, user);

        String userHome = this.getHomePath(user);

        if ( path.equals("/") ) path = "";
        if (!path.equals(userHome) && !path.startsWith(userHome + "/"))
            path = userHome + path;
        
        ServerModel hm = l.createNewModel(modelFile, path, uniqueId, user, comment);

        this.locationManager.setLocationAtPath(path, l);
        this.locationManager.addIdToLocation(l, uniqueId);

        l.getMetaDataHandler().setVersionUser(uniqueId, "0", user.getName());

        this.addModel(hm, uniqueId);

        return uniqueId;
    }

    public boolean addNewModelVersion( ProcessModel newModel, String id, int version, Set<String> precVersions, String comment, String folder , SingleUser user) {
        ServerModel lm = this.getModel(id, version);

        Location source = this.locationManager.getLocationForId(id);
        Location target = this.locationManager.getSaveableLocation(folder, user);

        if (source != target)
            return false;

        int versionCount = this.getVersionCount(id);
        ServerModel newVersion = lm.save(newModel, versionCount, id, comment, folder);
        List<ServerModel> versions = this.index.get(id);

        if (versions != null) {
            versions.add(newVersion);
            this.modelCache.loadModel(newVersion, id);
            VersionMetaData metaData = this.metaCache.getMetaData(id);
            MetaDataHandler handler = metaData.getHandler();

            String folderAlias = null;
            if (!(folder.equals(this.getHomePath(user)) || folder.startsWith(this.getHomePath(user) + "/")))
                folderAlias = this.getHomePath(user) + folder;
            else
                folderAlias = folder;

            if (folderAlias.endsWith("/"))
                folderAlias = folderAlias.substring(0, folderAlias.length() - 1);

            this.locationManager.setLocationAtPath(folderAlias, target);

            //todo encapsulate meta data setting
            metaData.setComment(comment);
            metaData.setUser( user.getName() );
            metaData.setProcessName( newModel.getProcessName() );
            handler.setVersionComment(id, String.valueOf(versions.size() - 1), comment);
            handler.setVersionUser(id, String.valueOf(versions.size() - 1), user.getName());
            handler.setPreceedingVersions(id, String.valueOf(versions.size() - 1), precVersions);

            for (String v : precVersions) {
                Set<String> currSucc = handler.getSucceedingVersions(id, v);
                currSucc.add(String.valueOf(versions.size() - 1));
                handler.setSucceedingVersions(id, v, currSucc);
            }

            if (this.getOwner(id).equals(user.getName())) {
                metaData.setFolder(folderAlias);
                handler.setFolderAlias(id, folder, user);
            }
            
        }

        return true;
    }

    /**
     * Get model by ID and version
     * @param id the ID
     * @param version the version
     * @return <ul>
     *  <li> null, if ID and/or version is not present</li>
     *  <li> the model, otherwise </li>
     * </ul>
     */
    public ServerModel getModel( String id, int version ) {
               //if version is -1 return the most recent version
        if (version == -1)
            return this.modelCache.getModel(id);

        ServerModel sm = null;

        //get version list
        List<ServerModel> versions = this.index.get(id);

        if (version < versions.size()) {
            sm = versions.get(version);
        }

        return sm;
    }

    /**
     * Remove the model given by ID
     * @param id the ID
     * @return <ul>
     *  <li> "moved", if model was only moved to attic folder </li>
     *  <li> "deleted", if all references were dropped and potential artifacts removed </li>
     * </ul>
     */
    public String removeModel( String id , SingleUser user ) {
        Location l = this.locationManager.getLocationForId(id);

        if ( l.removeModel(id, user) ) {
            //remove it physically
            this.removeModelPhysically(id);
            return "deleted";
        } else {
            //move to attic
            this.metaCache.setFolderAlias(id, l.getAtticPath(user), user);
            return "moved";
        }
    }

    /**
     * Reload this manager's index (flush and recreate)
     *
     * @todo: Not really working yet!!! 
     *
     */
    public void reloadIndex() {
        this.index.clear();
        this.loadIndex();
    }

     /**
     * Returns a list of process nodes with a given type from a given model.
     * 
     * @param modelId the model id. If the model id is null all models are recognized.
     * @param nodeType the the node type.
     * @param user the user.
     * @return a list of matching process nodes.
     */
    public List<ProcessNode> getNodesOfType(final String modelId, final Class nodeType, final SingleUser user) {
        List<ProcessNode> nodeList = new ArrayList<ProcessNode>();
        if (nodeType == null) {
            return nodeList;
        }
        Set<String> modelIds = new HashSet<String>();
        for(Entry<String, AccessType> mapEntry : this.locationManager.getModelsForUser(user).entrySet()) {
            if ((!mapEntry.getValue().equals(AccessType.NONE)) && (modelId == null || modelId.equals(mapEntry.getKey()))) {
                modelIds.add(mapEntry.getKey());
            }
        }
        for (String id : modelIds) {
            ServerModel sm = getModel(id, -1);
            if(sm == null) {
                continue;
            }
            for (ProcessNode pn : sm.getModel().getNodes()) {
                if (nodeType.isAssignableFrom(pn.getClass())) {
                    nodeList.add(pn);
                }
            }

        }
        return nodeList;
    }

    public Map<String, AccessType> getRecentVersions(SingleUser user) {
//        this.addNecessaryIS(user);
        
        return this.locationManager.getModelsForUser(user);
    }

    public AccessType getAccessForModel( String id, int version, LoginableUser user ) {
        if (version == -1 && user.isTemporaryUser())
            version = this.getVersionCount(id) - 1;

        MetaDataHandler mdh = this.locationManager.getLocationForId(id).getMetaDataHandler();
        return mdh.getAccessability(id, version, user);
    }

    public ServerModel getRecentVersion(String id) {
        return this.modelCache.getModel(id);
    }

    public int getVersionCount( String id ) {
        List<ServerModel> versions = this.index.get(id);

        if (versions != null)
            return versions.size();

        return 0;
    }

    void setFolderAlias( String id, String folder, SingleUser user ) {
        String home = this.getHomePath(user);

        if (!folder.startsWith(home))
            folder = home + folder;

        this.metaCache.setFolderAlias(id, folder, user);
    }

    public VersionMetaData getRecentMetaData( String id ) {
        return this.metaCache.getMetaData(id);
    }

    void addComment ( String modelId, ProcessObjectComment comment ) {
        this.metaCache.addComment( modelId, comment );
    }
    
    ProcessObjectComment updateComment ( String modelId, String commentId, String newText, int validUntil ) {
        return this.metaCache.updateComment( modelId, commentId, newText, validUntil );
    }
    
    @Deprecated
    void resolveComment( String modelId, String commentId, String version ) {
        this.metaCache.resolveComment(modelId, commentId, version);
    }

    void deleteComment ( String modelId, String commentId ) {
        this.metaCache.removeComment( modelId, commentId );
    }
    
    Set<ProcessObjectComment> getComments( String modelId, int version, String elementId ) {

        if (version == -1)
            version = this.getVersionCount(modelId) - 1;

        return this.metaCache.getComments(modelId, version, elementId);
    }

    String getFolderAlias( String id ) {
        return this.metaCache.getFolderAlias(id);
    }

//    void setVersionComment( String id, int version, String comment ) {
//        this.metaCache.setVersionComment(id, version, comment);
//    }

    String getVersionComment( String id, int version ) {
        List<ServerModel> versions = this.index.get(id);

        if (versions == null)
            return "";

        //if latest version is requested, set to -1
        if (version == versions.size() - 1)
            version = -1;
        
        return metaCache.getVersionComment(id, version);
    }

    String getVersionUser( String id , int version ) {
        List<ServerModel> versions = this.index.get(id);

        if (versions == null)
            return "unknown";

        //if latest version is requested, set to -1
        if (version == versions.size() - 1)
            version = -1;

        return metaCache.getVersionUser(id, version);
    }

    public Set<String> getPreceedingVersions ( String id, int version ) {
        return this.metaCache.getPreceedingVersions( id, version );
    }

    public Set<String> getSucceedingVersions ( String id, int version ) {
        return this.metaCache.getSucceedingVersions( id, version );
    }

    String getOwner ( String id ) {
        return this.metaCache.getOwner( id );
    }

    Set<User> getViewers( String id ) {
        return this.metaCache.getViewers( id );
    }

    Set<User> getEditors( String id ) {
        return this.metaCache.getEditors( id );
    }

    Set<User> getAnnotators( String id ) {
        return this.metaCache.getAnnotators ( id );
    }

    void grantRight( String id, AccessType at, Set<User> users ) {
        this.metaCache.grantRight( id, at, users);
    }

    void divestRight( String id, AccessType at, Set<User> users ) {
        this.metaCache.divestRight( id, at, users);
    }

    void setOwner ( String id , SingleUser owner, SingleUser admin ) {
        if (this.metaCache.setOwner( id, owner, admin )) {
            this.metaCache.setFolderAlias(id, this.getHomePath(owner), owner);
        }
    }

    /**
     * Add model to index
     * @param model the model
     * @param key the key by which model is stored and indexed
     */
    private void addModel( ServerModel model, String key ) {
        List<ServerModel> versions = new LinkedList<ServerModel>();
        versions.add(model);

        this.index.put(key, versions);

        this.loadRecentVersion(model, key, 0);
    }

    /**
     * Load this manager's model index.
     * This is done at instantiation time
     */
    private void loadIndex() {
        for ( Location l : this.locationManager.getLocations() ) {
            if (l.checkConnection()) {
                // Only load if connection available(!)
                this.addToIndex(l.getIndex(this.getUsedIds(),true));
            }
        }
    }
    
    /**
     * Add index data to this manager's index
     * @param index the index data to be added
     */
    private void addToIndex( Map<String, List<ServerModel>> index ) {
        for (Map.Entry<String, List<ServerModel>> e : index.entrySet()) {
            //if id is already contained, create new id
//            String id = this.isIdInUse(e.getKey()) ? this.getUnusedId() : e.getKey();
            this.index.put(e.getKey(),  e.getValue());

            int latestVersion = e.getValue().size() - 1;
            //load recent version
            ServerModel recent = e.getValue().get(latestVersion);

            this.loadRecentVersion(recent, e.getKey(), latestVersion);
        }
    }

    /**
     * Create a unique ID for the given model. This ID has to be used in order to refer to
     * this model in any kind of URL.
     *
     * @param model the model
     * @return the created unique ID
     */
    private String createUniqueId( ProcessModel model ) {
        //try to get model id
        String modelId = model.getId();

        //if none exists, create new id from hash code
        if (modelId == null || modelId.isEmpty())
            modelId = String.valueOf(model.hashCode());

        //check if id is already in use
        if (this.isIdInUse(modelId)) {
            //create new random id
            modelId = getUnusedId();
        }

        return modelId;
    }

    /**
     * Get an unused unique ID
     * @return the ID
     */
    private String getUnusedId() {
        Random rand = new Random(System.currentTimeMillis());
        String modelId = "" + Math.abs(rand.nextLong());

        while (this.isIdInUse(modelId))
            modelId = "" + Math.abs(rand.nextLong());

        return modelId;
    }

    /**
     * Load recent model version into model and meta cache
     * @param hm the recent model version
     * @param id the ID
     * @param version the version number
     */
    private void loadRecentVersion( ServerModel hm, String id, int version ) {
        this.modelCache.loadModel(hm, id);

        MetaDataHandler mdh = this.locationManager.getLocationForId(id).getMetaDataHandler();

        this.metaCache.loadMetaData(mdh, id, String.valueOf(version));

        VersionMetaData vmd = this.metaCache.getMetaData(id);
        vmd.loadRequiredData( hm );
    }

    /**
     * Remove cached model and all artifacts belonging the given ID
     * @param id the ID
     */
    private void removeModelPhysically( String id ) {
        //delete loaded model
        this.deleteLoadedModel(id);

        //delete hosted models
        this.deleteHostedModels(id);

        //remove index entry
        this.index.remove(id);
    }

    private void deleteLoadedModel( String id ) {
        this.modelCache.remove(id);
    }

    private void deleteHostedModels( String id ) {
        List<ServerModel> hms = this.index.get(id);
        for(ServerModel hm : hms) {
            hm.delete();
        }
    }

//    private void addNecessaryIS( SingleUser user ) {
//        Set<ISLocation> isManagers = user.getISConnections();
//
//        for (ISLocation ism : isManagers) {
//            this.addLocation(ism);
//        }
//    }

    private boolean isIdInUse( String id ) {
        return this.getUsedIds().contains(id);

    }

    private Set<String> getUsedIds() {
        Set<String> ids = new HashSet<String>( this.index.keySet() );
        ids.addAll( ProcessEditorServerHelper.getPersistenceConnector().getAllMappedIDs() );
        return ids;
    }
}