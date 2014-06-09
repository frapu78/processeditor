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
import java.util.Set;

/**
 * Interface for any kind of configurations that aim at representing directory structures
 * for process model repositories.
 * 
 * @author fel
 */
public interface StructuralConfig {

    /**
     * Get the path of a model
     * @param id the model's ID
     * @return the path
     */
    public String getPathForModel( String id );

    /**
     * Add a model at a certain path, for a certain user
     * @param id the model's ID
     * @param path the path of the model
     * @param user the user the model is added for
     */
    public void addModel(String id , String path, SingleUser user);

    /**
     * Set the path of a model
     * @param id the model's id
     * @param path the new path
     * @param user the user that requests the change of the path
     */
    public void setPath(String id, String path, SingleUser user);

    /**
     * Move a complete directory to another path
     * @param dirPath the path of the directory that will be moved
     * @param targetPath the path were the directory will be moved to
     * @param user the user that requests the movement of the directory
     * @return <ul>
     *  <li> true, if movement was successful </li>
     *  <li> false, otherwise </li>
     * </ul>
     */
    public boolean moveDirectory(String dirPath, String targetPath, SingleUser user );

    /**
     * Remove a certain model
     * @param id the model's ID
     */
    public void remove(String id);

    /**
     * Remove a complete directory with all its models
     * @param path the path of the directory
     * @param user the user that requests deletion
     */
    public void removeDirectory( String path, SingleUser user );

    /**
     * List the paths for which a user has shared access at least to one model
     *
     * @param user the user
     * @return the set of all accessable paths
     */
    public Set<String> listSharedPaths( User user );

    /**
     * Get all models a user has shared access. These are all the models accessable by the
     * given user except of those he is owner of
     * @param user the user
     * @return the set of model IDs
     */
    public Set<String> getSharedModels( User user );

    /**
     * Create a user home
     * @param user the user
     * @return the configuration of the user home. <br>
     *         REMARK: It depends if the returned value is necessary. 
     */
    public StructuralConfig createUserHome( SingleUser user );

    /**
     * Get the path of the user's home.
     * @param user the user
     * @return the home path
     */
    public String getUserHome( SingleUser user );

    /**
     * List all paths belonging to the user's home
     * @param user the user
     * @return the list of paths
     */
    public Set<String> listUserHome( SingleUser user );

    /**
     * List all paths
     * @return the list of paths
     */
    public Set<String> listPaths();

    /**
     * Get all model IDs belonging to a specific path. It can be configured whether
     * subdirectories should be listed recursively.
     * @param path the path of the directory
     * @param recursive <ul>
     *  <li> if true, models in subdirectories will also be returned </li>
     *  <li> if false, only this directory's content will be returned </li>
     *
     * @return the set of model IDs
     */
    public Set<String> getModelIDs( String path, boolean recursive );
}
