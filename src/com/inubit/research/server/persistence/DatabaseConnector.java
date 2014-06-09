/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;

/**
 * This interface is intended to provide a SQL-indepent interface to databases. Thereby,
 * different database types (e.g. XML-based, document-based) can hopefully be plugged in.
 * @author fel
 */
public interface DatabaseConnector {

    public enum EntityType {
        USER,
        USER_IMAGE,
        MODEL,
        VERSION,
        ACCESS_MODEL,
        EVOLUTION_PRE,
        EVOLUTION_SUCC,
        COMMENT,
        CONNECTION,
        MAPPING,
        GROUP,
        SUBGROUP;

        private static final DatabaseSchema.Attribute[] USER_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.USER_NAME};
        private static final DatabaseSchema.Attribute[] USER_IMAGE_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.USER_IMAGE_ID};
        private static final DatabaseSchema.Attribute[] MODEL_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.MODEL_ID};
        private static final DatabaseSchema.Attribute[] VERSION_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.VERSION_ID, DatabaseSchema.Attribute.VERSION_VERSION};
        private static final DatabaseSchema.Attribute[] ACCESS_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.ACCESS_ID, DatabaseSchema.Attribute.ACCESS_NAME, DatabaseSchema.Attribute.ACCESS_TYPE};
        private static final DatabaseSchema.Attribute[] EVOLUTION_PRE_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.EVOLUTION_ID, DatabaseSchema.Attribute.EVOLUTION_SUCCESSOR};
        private static final DatabaseSchema.Attribute[] EVOLUTION_SUCC_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.EVOLUTION_ID, DatabaseSchema.Attribute.EVOLUTION_VERSION};
        private static final DatabaseSchema.Attribute[] COMMENT_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.COMMENT_ID};
        private static final DatabaseSchema.Attribute[] CONNECTION_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.CONNECTIONS_USER, DatabaseSchema.Attribute.CONNECTIONS_URL, DatabaseSchema.Attribute.CONNECTIONS_ISUSER, DatabaseSchema.Attribute.CONNECTIONS_PWD};
        private static final DatabaseSchema.Attribute[] GROUP_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.GROUP_NAME};
        private static final DatabaseSchema.Attribute[] SUBGROUP_ID = new DatabaseSchema.Attribute[]{DatabaseSchema.Attribute.SUBGROUP_NAME};

        public String toSQLTableName() {
            switch(this) {
                case USER:
                    return DatabaseSchema.Table.USER.getName();
                case USER_IMAGE:
                    return DatabaseSchema.Table.USER_IMAGE.getName();
                case MODEL:
                    return DatabaseSchema.Table.MODEL.getName();
                case VERSION:
                    return DatabaseSchema.Table.VERSIONS.getName();
                case ACCESS_MODEL:
                    return DatabaseSchema.Table.ACCESS.getName();
                case EVOLUTION_PRE:
                    return DatabaseSchema.Table.EVOLUTION.getName();
                case EVOLUTION_SUCC:
                    return DatabaseSchema.Table.EVOLUTION.getName();
                case COMMENT:
                    return DatabaseSchema.Table.COMMENTS.getName();
                case CONNECTION:
                    return DatabaseSchema.Table.CONNECTIONS.getName();
                case MAPPING:
                    return DatabaseSchema.Table.IS.getName();
                case GROUP:
                    return DatabaseSchema.Table.GROUPS.getName();
                case SUBGROUP:
                    return DatabaseSchema.Table.SUBGROUPS.getName();
            }

            return null;
        }

        public DatabaseSchema.Attribute[] getSQLIdColumn() {
            switch(this) {
                case USER:
                    return USER_ID;
                case USER_IMAGE:
                    return USER_IMAGE_ID;
                case MODEL:
                    return MODEL_ID;
                case VERSION:
                    return VERSION_ID;
                case ACCESS_MODEL:
                    return ACCESS_ID;
                case EVOLUTION_PRE:
                    return EVOLUTION_PRE_ID;
                case EVOLUTION_SUCC:
                    return EVOLUTION_SUCC_ID;
                case COMMENT:
                    return COMMENT_ID;
                case CONNECTION:
                    return CONNECTION_ID;
                case GROUP:
                    return GROUP_ID;
                case SUBGROUP:
                    return SUBGROUP_ID;
            }

            return null;
        }
    }

    /**
     * Get the connection's name
     * @return the name
     */
    public String getConnectionName();

    /**
     * Check if the connection is alive
     * @return true, if connection is alive. false, otherwise.
     */
    public boolean checkConnection();

    /*
     * GENERIC FUNCTIONS
     */
    
    /**
     * Select ALL values for an attribute of the given entity type. <br>
     * This is equals to "SELECT <attr> FROM <table>" in SQL.
     *
     * @param attribute the attribute
     * @param entity the entity type
     * @return The set of returned objects
     */
    public Set<Object> selectSingleAttribute( DatabaseSchema.Attribute attribute, EntityType entity );

    /**
     * Select a single attribute value of an object specified by the given entity type and entity id values.
     * Which attributes are used for determining the id is specified by the given entity type.
     * 
     * @param attribute the attribute
     * @param entity the entity type
     * @param idValues the values of the id attributes
     * @return the single attribute value
     */
    public Object selectSingleAttribute( DatabaseSchema.Attribute attribute, EntityType entity, Object[] idValues );

    /**
     * Select ALL values for an attribute of an object specified by the given entity type and entity id values.
     * Which attributes are used for determining the id is specified by the given entity type.
     * 
     * @param attribute the attribute
     * @param entity the entity type
     * @param idValues the values of the id attributes
     * @return The set of returned object ( = attribute values )
     */
    public Set<Object> selectMultiValueSingleAttribute( DatabaseSchema.Attribute attribute, EntityType entity, Object[] idValues );

    /**
     * Select ALL values for an attribute of multiple object specified by the given enitity type and entity ids.
     * Which attributes are used for determining the id is specified by the given entity type.
     *
     * @param attribute the attribute
     * @param entity the entity type
     * @param idValues the value sets of the id attributes
     * @return The set of returned object ( = attribute values )
     */
    public Set<Object> selectMultiValueSingleAttributeWithMultipleIDs( DatabaseSchema.Attribute attribute, EntityType entity, Object[][] idValues );

    /**
     * Update an attribute value of an object specified by the gievn entity type and entity id values.
     * Which attributes are used for determining the id is specified by the given entity type.
     * 
     * @param attribute the attribute
     * @param value the new value
     * @param entity the enitity type
     * @param idValues the values of the id attributes
     * @return true, if the update has been successful. false, otherwise.
     */
    public boolean updateAttribute( DatabaseSchema.Attribute attribute, Object value, EntityType entity, Object[] idValues );

    /**
     * Select a set of attributes of an object specified by the gievn entity type and entity id values.
     * @param attributes the attributes that have to be selected
     * @param entity the entity type
     * @param idValues the values of the id attributes
     * @return the attribute values in the order the attributes have been given
     */
    public Object[] selectAttributes( DatabaseSchema.Attribute[] attributes, EntityType entity, Object[] idValues );

    /**
     * Delete enitities specified by the given entity type and entity id values.
     * @param entity the entity type
     * @param idValues the values of the id attributes
     */
    public void deleteEntity( EntityType entity, Object[] idValues );

    /**
     * Create an entity specified by the given entity type, attributes, and attribute values.
     * @param entity the entity type
     * @param attributes the attributes
     * @param values the attribute values
     */
    public void insertEntity( EntityType entity, DatabaseSchema.Attribute[] attributes, Object[] values );

    /*
     * MODEL FUNCTIONS
     */

    /**
     * Retrieve a specific model version
     * @param id the model id
     * @param version the version number. Be aware that due to the SQL AUTO_INCREMENT construct versions are assumed to start with number 1, NOT 0
     * @return the model
     */
    public ProcessModel getModel( String id, int version );

    /**
     * Retrieve a list of models and the respective access for a given user
     * @param user the user
     * @return a map of model ids to access types
     */
    public Map<String, AccessType> getModelsWithAccess( SingleUser user );

    /**
     * Get paths that are accessible by the given user
     * @param user the user
     * @return the paths
     */
    public Set<String> getAccessiblePaths( SingleUser user );

    /**
     * Add a model to the database
     * @param file the file containing the model
     * @param path the path of the model
     * @param id the model id
     * @param user the user that commited the model
     * @param comment the commit message
     * @return true, if model was successfully saved. false, otherwise
     */
    public boolean addModel( File file, String path, String id, SingleUser user, String comment );

    /**
     * 
     * @param f the file containing the new model version
     * @param id the model id
     * @param user the commiting user
     * @param comment the commit message
     * @param date the creation date
     * @return the model version number
     * @throws SQLException if an error occured while saving to the database
     * @throws IOException if an error occured while reading the file
     */
    public int addModelVersion(File f, String id, SingleUser user, String comment, String date) throws SQLException, IOException;

    /**
     * Retrieve the number of model versions from the database
     * @param modelId the model id
     * @return the version count
     */
    public int getModelVersionCount( String modelId );

    /**
     * Delete a model (and all its versions) from the database
     * @param modelId the model id
     */
    public void deleteModel( String modelId );

    /*
     * ACCESS FUNCTIONS
     */

    /**
     * Allow access for a specific user to a specific model
     * @param id the model id
     * @param access the access type
     * @param u the user
     */
    public void grantRight( String id, AccessType access, User u );

    /**
     * Deny access for a specific user to s specific model
     * @param id the model od
     * @param access the access type
     * @param u the user
     */
    public void divestRight( String id, AccessType access, User u );

    /**
     * Get a set of users that have a specific type of access to the given model
     * @param id the model id
     * @param access the access type
     * @return the set of users
     */
    public Set<User> getAccessors( String id, AccessType access );

    /*
     * USER FUNCTIONS
     */
    /**
     * Store the given user in the database
     * @param user the user
     * @return true, if storing the user has been sucessful
     */
    public boolean addUser( SingleUser user );

    /**
     * Retrieve the ISConnections for the given user
     * @param user the user
     * @return the set of ISConnections
     */
    public Set<ISLocation> getISConnections( SingleUser user );

    /*
     * COMMENT FUNCTIONS
     */

    /**
     * Get all comments for a specific model or modelelement
     * @param modelId the model id
     * @param version the version number
     * @param elementId the element id. Use 'model' to look up model comments.
     * @return the set of comments
     */
    public Set<ProcessObjectComment> getComments(String modelId, String version, String elementId);
}
