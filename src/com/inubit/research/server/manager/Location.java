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
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.user.SingleUser;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for different model locations / repository parts
 * @author fel
 */
public interface Location {
    public static final String SHARED_PATH_PREFIX = "/shared";
    
    public enum LocationType {
        FILE,
        IS,
        DB
    }

    /**
     * Checks if this location is currently available.
     * @return
     */
    public boolean checkConnection();

    /**
     * Remove a certain model
     * @param id the model id
     * @param user the user that requested the deletion
     * @return <ul>
     *  <li> true, if the model can be physically deleted </li>
     *  <li> false, if the model should only be moved to some location where it resides until physical deletion </li>
     * </ul>
     */
    public boolean removeModel( String id, SingleUser user );

    /**
     * Get path to the attic directory
     * @param user the user
     * @return the path
     */
    public String getAtticPath( SingleUser user );

    /**
     * Get index of this location.
     * @return the index mapping IDs to lists of model versions
     */
    public Map<String, List<ServerModel>> getIndex(Set<String> usedIDs, boolean forceRefresh);


    /**
     * Get meta data handler for this location
     * @return the meta data handler
     */
    public MetaDataHandler getMetaDataHandler();

    /**
     * Get the type of this location
     * @return the type
     */
    public LocationType getType();

    /**
     * Get this location's name
     * @return the name
     */
    public String getName();

    /**
     * Get all paths belonging to this location
     */
    public Set<String> listPaths();

    /**
     * Get all paths belonging to a certain user within this location
     * @param user the user
     * @return the list of paths
     */
    public Set<String> listPaths( SingleUser user );

    /**
     * Get all models belonging to a certain user withing this location
     * @param user the user
     * @return a map of model IDs and access types
     */
    public Map<String, AccessType> getModelsForUser( SingleUser user );


    /**
     * Create new model
     */
    public ServerModel createNewModel( File model, String path, String id, SingleUser user, String comment );
}
