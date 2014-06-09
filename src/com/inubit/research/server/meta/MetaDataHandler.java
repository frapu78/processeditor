/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.util.Date;
import java.util.Set;

/**
 * Interface for all meta data handlers
 * @author fel
 */
public interface MetaDataHandler {
    /**
     * Set commit comment for the given ID and version
     * @param id the ID
     * @param version the version
     * @param comment the commit message
     */
    public void setVersionComment(String id, String version, String comment );

    /**
     * Get commit message of given ID and version
     * @param id the ID
     * @param version the version
     * @return the commit message
     */
    public String getVersionComment(String id, String version);

    /**
     * Set folder alias for given ID
     * @param id the ID
     * @param alias the folder alias
     */
    public void setFolderAlias(String id, String alias, SingleUser user);

    /**
     * Get folder alias for given ID
     * @param id the ID
     * @return the folder alias
     */
    public String getFolderAlias(String id);


    /**
     * Set the user that commited a certain model version
     * @param id the model id
     * @param version the model version
     * @param user the commiting user
     */
    public void setVersionUser( String id, String version, String user );

    /**
     * Get the user's name who commited a certain model version
     * @param id the model id
     * @param version the model version
     * @return the user name
     */
    public String getVersionUser( String id, String version ) ;

    /**
     * Add an element comment
     *
     * @param modelId the model's id to which the annotated element belongs
     * @param comment the comment
     */
    public void addComment( String modelId, ProcessObjectComment comment);

    /**
     * Get comments for a certain model element
     * @param modelId the model id
     * @param version the model version
     * @param elementId an element id or 'model' if model comments are requested
     * 
     * @return the set of comments
     */
    public Set<ProcessObjectComment> getComments( String modelId, String version, String elementId );

    /**
     * Change a comment's text
     * @param modelId the model id
     * @param commentId the comment id
     * @param newText the new comment text
     */
    public ProcessObjectComment updateComment ( String modelId, String commentId, String newText, int validUntil );

    /**
     * Mark a certain comment as resolved
     * @param modelId the model id
     * @param commentId the comment id
     * @param version the version in which it was resolved
     */
    @Deprecated
    public void resolveComment( String modelId, String commentId, String version );

    /**
     * Remove a certain comment
     * @param modeldId the model id
     * @param commentId the comment id
     */
    public void removeComment( String modeldId, String commentId );

    /**
     * Set the creation date for the given ID and version
     * @param id the ID
     * @param version the version
     * @param date the date
     */
    public void setVersionDate(String id, String version, Date date);

    /**
     * Get the creation date for a certain model version
     * @param id the model ID
     * @param version the model version
     * @return the formatted date
     */
    public String getVersionDate(String id, String version);

    /**
     * Set the succeeding versions for a certain model version
     * @param id the model id
     * @param version the model version
     * @param versions the succeeding versions
     */
    public void setSucceedingVersions( String id, String version, Set<String> versions );

    /**
     * Get the succeeding versions for a certain model version
     * @param id the model id
     * @param version the model version
     * @return the succeeding versions
     */
    public Set<String> getSucceedingVersions( String id, String version);

    /**
     * Set the preceeding versions for a certain model version
     * @param id the model id
     * @param version the model version
     * @param versions the preceeding versions
     */
    public void setPreceedingVersions( String id, String version, Set<String> versions );

    /**
     * Get the preceeding versions for a certain model version
     * @param id the model id
     * @param version the model version
     * @return the preceeding versions
     */
    public Set<String> getPreceedingVersions( String id, String version);

    /**
     * Get meta data object for the given ID and given
     * @param id the ID
     * @param version the version
     * @return the meta data object
     */
    public VersionMetaData getVersionMetaData(String id, String version);

    /**
     * Remove the meta data objects for the given model
     * @param id the model id
     */
    public void remove(String id);

    /**
     * Determine the access for a given user to a certain model and version
     * @param id the model id
     * @param version the model version
     * @param user the user
     * @return the maximum access of the given user for this model
     */
    public AccessType getAccessability(String id, int version, LoginableUser user);

    /**
     * Get a model's owner
     * @param id the model id
     * @return the owner's name
     */
    public String getOwner( String id );

    /**
     * Set a model's owner
     * @param id the model id
     * @param owner the new owner
     * @param admin the user that is allowed to set the owner
     * @return
     * <ul>
     *  <li> true, if setting owner was successful </li>
     *  <li> false, otherwise </li>
     * </ul>
     */
    public boolean setOwner( String id, SingleUser owner, SingleUser admin );

    /**
     * Get all users that are allowed to view this model
     * @param id the model id
     * @return the set of users
     */
    public Set<User> getViewers( String id );

    /**
     * Get all users that are allowed to edit this model
     * @param id the model id
     * @return the set of users
     */
    public Set<User> getEditors( String id );

    /**
     * Get all users that are allowed to comment on this model
     * @param id the model id
     * @return the set of users
     */
    public Set<User> getAnnotators( String id );

    /**
     * Grant a specific right to a number of users
     * @param id the model's ID
     * @param at the kind of access that is granted
     * @param users the users that will receive the right
     */
    public void grantRight( String id, AccessType at, Set<User> users );

    /**
     * Deny a specific right for a number of users
     * @param id the model's ID
     * @param at the kind of access that is denied
     * @param users the users that lose this right
     */
    public void divestRight( String id, AccessType at, Set<User> users );

}
