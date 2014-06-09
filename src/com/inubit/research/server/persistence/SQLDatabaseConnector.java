/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.manager.Location;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.DatabaseServerModel;
import com.inubit.research.server.persistence.PersistenceConnector.ImageType;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User;
import java.sql.PreparedStatement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fel
 */
public abstract class SQLDatabaseConnector extends PersistenceConnector implements DatabaseConnector {
    protected static final String PROP_HOST = "hostname";
    protected static final String PROP_PORT = "port";
    protected static final String PROP_USER = "user";
    protected static final String PROP_PASSWD = "password";
    protected static final String PROP_DB_NAME = "database";

    protected static final String PROP_SCHEMA_STMT_USER = "stmt.schema.users";
    protected static final String PROP_SCHEMA_STMT_USER_IMAGE = "stmt.schema.userImage";
    protected static final String PROP_SCHEMA_STMT_GROUPS = "stmt.schema.groups";
    protected static final String PROP_SCHEMA_STMT_SUBGROUPS = "stmt.schema.subgroups";
    protected static final String PROP_SCHEMA_STMT_MODELS = "stmt.schema.models";
    protected static final String PROP_SCHEMA_STMT_VERSIONS = "stmt.schema.versions";
    protected static final String PROP_SCHEMA_STMT_EVOLUTION = "stmt.schema.evolution";
    protected static final String PROP_SCHEMA_STMT_COMMENTS = "stmt.schema.comments";
    protected static final String PROP_SCHEMA_STMT_ACCESS = "stmt.schema.access";
    protected static final String PROP_SCHEMA_STMT_PATHACCESS = "stmt.schema.pathaccess";
    protected static final String PROP_SCHEMA_STMT_ID = "stmt.schema.isids";
    protected static final String PROP_SCHEMA_STMT_CONNECTIONS = "stmt.schema.isconnections";
    
    protected static final DatabaseSchema.Attribute[] MAPPING_ATTS = { DatabaseSchema.Attribute.IS_HOST, DatabaseSchema.Attribute.IS_URI, DatabaseSchema.Attribute.IS_ID };

    protected static final DatabaseSchema.Table[] tableNames = {
        DatabaseSchema.Table.USER_IMAGE, DatabaseSchema.Table.USER, DatabaseSchema.Table.MODEL,
        DatabaseSchema.Table.VERSIONS, DatabaseSchema.Table.ACCESS, DatabaseSchema.Table.COMMENTS, DatabaseSchema.Table.EVOLUTION,
        DatabaseSchema.Table.GROUPS, DatabaseSchema.Table.IS,  DatabaseSchema.Table.SUBGROUPS, DatabaseSchema.Table.PATHACCESS,
        DatabaseSchema.Table.CONNECTIONS
    };

    protected static final String[] stmtProps = {
        PROP_SCHEMA_STMT_USER_IMAGE, PROP_SCHEMA_STMT_USER, PROP_SCHEMA_STMT_MODELS,
        PROP_SCHEMA_STMT_VERSIONS, PROP_SCHEMA_STMT_ACCESS, PROP_SCHEMA_STMT_COMMENTS, PROP_SCHEMA_STMT_EVOLUTION,
        PROP_SCHEMA_STMT_GROUPS, PROP_SCHEMA_STMT_ID, 
        PROP_SCHEMA_STMT_SUBGROUPS, PROP_SCHEMA_STMT_PATHACCESS, PROP_SCHEMA_STMT_CONNECTIONS
        
    };

    protected static Connection dbconn;

    public SQLDatabaseConnector() {
        super();
        initialize();
    }

    @Override
    public boolean checkConnection() {
        try {
            return dbconn != null && !dbconn.isClosed();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getConnectionName() {
        try {
            return dbconn.getMetaData().getURL();
        } catch ( SQLException ex ) {
            return "unknown";
        } catch ( NullPointerException ex ) {
            return "unknown";
        }
    }

    public Map<String, String> getIDMapping(String uri) {
        Map<String, String> mapping = new HashMap<String, String>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append( DatabaseSchema.Attribute.IS_URI );
        sb.append(",");
        sb.append( DatabaseSchema.Attribute.IS_ID );
        sb.append(" FROM ");
        sb.append( DatabaseSchema.Table.IS );
        sb.append(" WHERE ");
        sb.append( DatabaseSchema.Attribute.IS_HOST );
        sb.append("=");
        sb.append( DatabaseSchema.Attribute.IS_HOST.encodeValue(uri) );

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            while (result.next())
                mapping.put( result.getString(DatabaseSchema.Attribute.IS_URI.getName()), result.getString(DatabaseSchema.Attribute.IS_ID.getName()) );

            stmt.close();
        } catch ( SQLException ex) {
            ex.printStackTrace();
        }

        return mapping;
    }

    public void storeIDMapping(String uri, Map<String, String> mapping) {
        for ( Map.Entry<String, String> entry : mapping.entrySet() ) 
            this.insertEntity(EntityType.MAPPING, MAPPING_ATTS, new Object[]{uri, entry.getValue(), entry.getKey()});
    }

    public void addToIDMapping(String uri, Map<String, String> mapping) {
        this.storeIDMapping(uri, mapping);
    }

    public Set<String> getAllMappedIDs() {
        return new HashSet<String>();
    }

    public BufferedImage loadUserImage(String id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(DatabaseSchema.Attribute.USER_IMAGE_IMAGE);
        sb.append(" FROM ");
        sb.append(DatabaseSchema.Table.USER_IMAGE.getName());
        sb.append(" WHERE ");
        sb.append(createIdClause(EntityType.USER_IMAGE, new String[]{id}));
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            if ( result.next() ) {
                InputStream is = result.getBinaryStream(1);
                return ImageIO.read(is);
            }

        } catch ( SQLException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    public String saveUserImage(String id, ImageType imageType, byte[] pic) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(pic);
            PreparedStatement stmt;
            if ( id == null )
                stmt = dbconn.prepareStatement( this.getUserImageInsertStatement(id) , Statement.RETURN_GENERATED_KEYS);
            else
                stmt = dbconn.prepareStatement( this.getUserImageInsertStatement(id) );
            this.setUserImageParameter(stmt, 1, pic);
            stmt.setString(2, imageType.toString().toLowerCase());
            stmt.execute();
            if ( id == null ) {
                ResultSet result = stmt.getGeneratedKeys();
                result.next();
                id = String.valueOf( result.getInt(1));
            }

            stmt.close();
            return id;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    private void initialize() {
        if ( dbconn == null )
            dbconn = openNewDatabaseConnection();
    }

    private void resetDatabaseConnection() {
        
    }

    public Connection getConnection() {
        return dbconn;
    }

    @Override
    public Set<Object> selectSingleAttribute(DatabaseSchema.Attribute attribute, EntityType entity) {
        return this.selectSingleAttribute( entity.toSQLTableName(), attribute.getName());
    }

    @Override
    public Set<Object> selectMultiValueSingleAttribute( DatabaseSchema.Attribute attribute, EntityType entity, Object[] idValues ) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(attribute);
        sb.append(" FROM ");
        sb.append(entity.toSQLTableName());
        sb.append(" WHERE ");
        sb.append(createIdClause(entity, idValues));

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());
            Set<Object> res = new HashSet<Object>();

            while ( result.next() ) 
                res.add( result.getObject(1));

            return res;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Set<Object> selectMultiValueSingleAttributeWithMultipleIDs( DatabaseSchema.Attribute attribute, EntityType entity, Object[][] idValues ) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(attribute);
        sb.append(" FROM ");
        sb.append(entity.toSQLTableName());
        sb.append(" WHERE ");

        for( int i = 0; i < idValues.length; i++ ) {
            sb.append("(");
            sb.append(createIdClause(entity, idValues[i]));
            sb.append(")");
            if ( i + 1 < idValues.length )
                sb.append(" OR ");
        }
        
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());
            Set<Object> res = new HashSet<Object>();

            while ( result.next() )
                res.add( result.getObject(1));

            return res;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Object selectSingleAttribute(DatabaseSchema.Attribute attribute, EntityType entity, Object[] idValues) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(attribute);
        sb.append(" FROM ");
        sb.append(entity.toSQLTableName());
        sb.append(" WHERE ");
        sb.append(createIdClause(entity, idValues));
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            if ( result.next() ) {
                return result.getObject(1);
            }
            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public Object[] selectAttributes(DatabaseSchema.Attribute[] attributes, EntityType entity, Object[] entityId) {
        StringBuffer sb = new StringBuffer();
        Object[] result = new Object[attributes.length];

        sb.append("SELECT ");
        for ( int i = 0; i < attributes.length; i++ ) {
            sb.append( attributes[i] );
            if ( i + 1 < attributes.length )
                sb.append(",");
        }

        sb.append(" FROM ");
        sb.append( entity.toSQLTableName() );
        sb.append(" WHERE ");

        sb.append( createIdClause(entity, entityId) );

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sb.toString());
            if ( resultSet.next() ) 
                for ( int i = 0; i < attributes.length; i++ ) 
                    result[i] = resultSet.getObject(attributes[i].getName());
               

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        
        return result;
    }

    @Override
    public void deleteEntity(EntityType entity, Object[] entityId) {
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM ");
        sb.append(entity.toSQLTableName());
        sb.append(" WHERE ");
        sb.append(createIdClause(entity, entityId));

        try {
            Statement stmt = dbconn.createStatement();
            stmt.execute(sb.toString());
            stmt.close();
        } catch( SQLException ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    public void insertEntity(EntityType entity, DatabaseSchema.Attribute[] attributes, Object[] values ) {
        try {
            PreparedStatement stmt = getInsertStatement(entity.toSQLTableName(), attributes, values);
            stmt.execute();
            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean updateAttribute(DatabaseSchema.Attribute attribute, Object value, EntityType entity, Object[] entityId) {
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE ");
        sb.append(entity.toSQLTableName());
        sb.append(" SET ");
        sb.append(attribute);
        sb.append("= ? WHERE ");
        sb.append(createIdClause(entity, entityId));
        try {
            PreparedStatement stmt = dbconn.prepareStatement(sb.toString());
            stmt.setObject(1, value);
            stmt.execute();
            stmt.close();
            return true;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean addUser(SingleUser user) {
        Vector<DatabaseSchema.Attribute> attributes = new Vector<DatabaseSchema.Attribute>();
        Vector<Object> values = new Vector<Object>();

        attributes.add( DatabaseSchema.Attribute.USER_NAME );
        values.add( user.getName() );
        attributes.add( DatabaseSchema.Attribute.USER_PWD );
        values.add( user.getPwd() );
        attributes.add( DatabaseSchema.Attribute.USER_ADMIN);
        values.add( user.isAdmin() );

        if ( user.getRealName() != null ) {
            attributes.add( DatabaseSchema.Attribute.USER_REALNAME );
            values.add( user.getRealName() );
        }

        if ( user.getMail() != null ) {
            attributes.add( DatabaseSchema.Attribute.USER_MAIL );
            values.add( user.getMail() );
        }

        if ( user.getPictureId() != null ) {
            attributes.add( DatabaseSchema.Attribute.USER_IMAGE );
            values.add( user.getPictureId() );
        }

        try {
            PreparedStatement stmt = getInsertStatement(DatabaseSchema.Table.USER.getName(), attributes.toArray(new DatabaseSchema.Attribute[0]), values.toArray(new Object[0]));
            stmt.execute();
            stmt.close();
            return true;
        } catch ( SQLException ex ) {
            ex.printStackTrace();
            return false;
        }
    }

    public Set<ISLocation> getISConnections( SingleUser user ) {
        Set<ISLocation> conns = new HashSet<ISLocation>();
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT * FROM ");
        sb.append(DatabaseSchema.Table.CONNECTIONS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.CONNECTIONS_USER);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.CONNECTIONS_USER.encodeValue( user.getName() ));

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery( sb.toString() );

            while( result.next() ) {
                ISLocation isl = new ISLocation(new URI( result.getString(DatabaseSchema.Attribute.CONNECTIONS_URL.getName())), result.getString(DatabaseSchema.Attribute.CONNECTIONS_ISUSER.getName()), result.getString(DatabaseSchema.Attribute.CONNECTIONS_PWD.getName()));
                isl.setOwner(null, user, null);
                conns.add(isl);
            }

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }


        return conns;
    }

    public boolean addModel(File file, String path, String id, SingleUser user, String comment) {
         ProcessModel pm = null;
        try {
            pm = new ProcessEditorImporter().parseSource(file).get(0);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        if ( pm == null )
            return false;
         
        DatabaseSchema.Attribute[] attributes = {
            DatabaseSchema.Attribute.MODEL_ID,
            DatabaseSchema.Attribute.MODEL_OWNER,
            DatabaseSchema.Attribute.MODEL_PATH,
            DatabaseSchema.Attribute.MODEL_NAME,
            DatabaseSchema.Attribute.MODEL_AUTHOR,
            DatabaseSchema.Attribute.MODEL_COMMENT,
            DatabaseSchema.Attribute.MODEL_CREATION_DATE
        };

        Date currentDate = Calendar.getInstance().getTime();

        SimpleDateFormat pmDate = new SimpleDateFormat("d. MMMMM yyyy HH:mm:ss zzz", Locale.GERMAN);
        Date date = currentDate;

        if ( (pm.getProperty(ProcessModel.PROP_CREATE_DATE) != null && !pm.getProperty(ProcessModel.PROP_CREATE_DATE).isEmpty() ) )
            try {
                date = pmDate.parse( pm.getProperty(ProcessModel.PROP_CREATE_DATE) );
            } catch ( ParseException ex ) {

            }
        Object[] values = {
            id,
            user.getName(),
            path,
            (pm.getProcessName() != null && !pm.getProcessName().isEmpty() ? pm.getProcessName() : "model" + id ),
            (pm.getProperty(ProcessModel.PROP_AUTHOR) != null ? pm.getProperty(ProcessModel.PROP_AUTHOR) : user.getName() ),
            (pm.getProperty(ProcessModel.PROP_COMMENT) != null ? pm.getProperty(ProcessModel.PROP_COMMENT) : comment ),
            date
        };
        try {
            PreparedStatement stmt = getInsertStatement(DatabaseSchema.Table.MODEL.getName(), attributes, values);
            stmt.execute();
            stmt.close();

            this.addModelVersion(file, id, user, comment, DatabaseServerModel.SDF.format(currentDate));

            return true;

        } catch ( SQLException ex ) {
            ex.printStackTrace();
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        return false;
    }

    public ProcessModel getModel( String id, int version ) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(DatabaseSchema.Attribute.VERSION_MODEL);
        sb.append(" FROM ");
        sb.append(DatabaseSchema.Table.VERSIONS.getName());
        sb.append(" WHERE ");
        sb.append(createIdClause(EntityType.VERSION, new String[] { id, String.valueOf(version) }));

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            if ( result.next() ) {
                InputStream is = result.getBinaryStream(1);
                return ProcessUtils.parseProcessModelSerialization(is);
            }

        } catch ( SQLException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    public Map<String, AccessType> getModelsWithAccess(SingleUser user) {
        Map<String, AccessType> result = new HashMap<String, AccessType>();
        
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(getStringCastParameter( DatabaseSchema.Attribute.MODEL_ID.getName() ) );
        sb.append(",");
        sb.append(getStringCastParameter( DatabaseSchema.Attribute.ACCESS_ACCESS.getName() ) );
        sb.append(" FROM ( SELECT ");
        sb.append(DatabaseSchema.Attribute.MODEL_ID);
        sb.append(",");
        sb.append(getEnumQueryParameter(AccessType.OWNER));
        sb.append(" AS accesstype FROM ");
        sb.append(DatabaseSchema.Table.MODEL);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.MODEL_OWNER);
        sb.append(" = ");
        sb.append(DatabaseSchema.Attribute.MODEL_OWNER.encodeValue(user.getName()));
        sb.append(" UNION SELECT m.");
        sb.append(DatabaseSchema.Attribute.MODEL_ID);
        sb.append(" AS id, ");
        sb.append(DatabaseSchema.Attribute.ACCESS_ACCESS);
        sb.append(" FROM ");
        sb.append(DatabaseSchema.Table.MODEL);
        sb.append(" m JOIN ");
        sb.append(DatabaseSchema.Table.ACCESS);
        sb.append(" a ON m.");
        sb.append(DatabaseSchema.Attribute.MODEL_ID);
        sb.append(" = a.");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID);
        sb.append(" WHERE a.");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME);
        sb.append(" IN (");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME.encodeValue(user.getName()));
        for( Group g : ProcessEditorServerHelper.getUserManager().getRecursiveGroupsForUser(user) ) {
            sb.append(",");
            sb.append( DatabaseSchema.Attribute.ACCESS_NAME.encodeValue(g.getName()) );
        }
        sb.append(")) as modelaccess");


        try {
            Statement stmt = dbconn.createStatement();
            ResultSet queryResult = stmt.executeQuery( sb.toString() );

            while( queryResult.next() )
                result.put( queryResult.getString(1), AccessType.valueOf(queryResult.getString(2)) );

            stmt.close();

        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
        

        return result;
    }

    @Override
    public Set<String> getAccessiblePaths(SingleUser user) {
        Set<String> paths = new HashSet<String>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append( DatabaseSchema.Attribute.MODEL_PATH );
        sb.append(",");
        sb.append(getStringCastParameter( DatabaseSchema.Attribute.ACCESS_ACCESS.getName() ) );
        sb.append(" FROM ( SELECT ");
        sb.append(DatabaseSchema.Attribute.MODEL_PATH);
        sb.append(",");
        sb.append(getEnumQueryParameter(AccessType.OWNER));
        sb.append(" AS accesstype FROM ");
        sb.append(DatabaseSchema.Table.MODEL);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.MODEL_OWNER);
        sb.append(" = ");
        sb.append(DatabaseSchema.Attribute.MODEL_OWNER.encodeValue(user.getName()));
        sb.append(" UNION SELECT ");
        sb.append(getStringConcatenation(new String[]{"'" + Location.SHARED_PATH_PREFIX + "'", "m." + DatabaseSchema.Attribute.MODEL_PATH}));
        sb.append(" AS ");
        sb.append(DatabaseSchema.Attribute.MODEL_PATH);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.ACCESS_ACCESS);
        sb.append(" FROM ");
        sb.append(DatabaseSchema.Table.MODEL);
        sb.append(" m JOIN ");
        sb.append(DatabaseSchema.Table.ACCESS);
        sb.append(" a ON m.");
        sb.append(DatabaseSchema.Attribute.MODEL_ID);
        sb.append(" = a.");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID);
        sb.append(" WHERE a.");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME);
        sb.append(" IN (");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME.encodeValue(user.getName()));
        for( Group g : ProcessEditorServerHelper.getUserManager().getRecursiveGroupsForUser(user) ) {
            sb.append(",");
            sb.append( DatabaseSchema.Attribute.ACCESS_NAME.encodeValue(g.getName()) );
        }
        sb.append(")) as modelaccess");

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery( sb.toString() );
            while ( result.next() )
                paths.add( result.getString( DatabaseSchema.Attribute.MODEL_PATH.toString() ));

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return paths;
    }

    public int getModelVersionCount(String modelId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT COUNT(");
        sb.append(DatabaseSchema.Attribute.VERSION_VERSION);
        sb.append(") FROM ");
        sb.append(DatabaseSchema.Table.VERSIONS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.VERSION_ID);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.VERSION_ID.encodeValue(modelId));

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery( sb.toString() );
            if ( result.next() ) {
                int retValue = result.getInt(1);
                stmt.close();
                return retValue;
            }
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void deleteModel( String modelId ) {
        
    }

    public void divestRight(String id, AccessType access, User u) {
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM ");
        sb.append(DatabaseSchema.Table.ACCESS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID.encodeValue(id));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME.encodeValue( u.getName() ));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.ACCESS_TYPE);
        sb.append("=");
        sb.append(getEnumQueryParameter(u.getUserType()));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.ACCESS_ACCESS);
        sb.append("=");
        sb.append(getEnumQueryParameter(access));

        try {
            this.execute(sb.toString());
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }

    public void grantRight(String id, AccessType access, User u) {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO ");
        sb.append(DatabaseSchema.Table.ACCESS);
        sb.append(" VALUES ('");
        sb.append(id);
        sb.append("','");
        sb.append(u.getName());
        sb.append("',");
        sb.append(getEnumQueryParameter(u.getUserType()));
        sb.append(",");
        sb.append(getEnumQueryParameter(access));
        sb.append(")");

        try {
            this.execute(sb.toString());
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }

    public Set<User> getAccessors( String id, AccessType access ) {
        Set<User> users = new HashSet<User>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(DatabaseSchema.Attribute.ACCESS_NAME);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.ACCESS_TYPE);
        sb.append(" FROM ");
        sb.append(DatabaseSchema.Table.ACCESS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.ACCESS_ID.encodeValue(id));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.ACCESS_ACCESS);
        sb.append("=");
        sb.append(getEnumQueryParameter(access));

        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            while( result.next() ) {
                User u = null;
                User.UserType type = User.UserType.valueOf(result.getString(2));
                if ( type.equals(User.UserType.GROUP) )
                    u = ProcessEditorServerHelper.getUserManager().getGroupForName(result.getString(1));
                else
                    u = ProcessEditorServerHelper.getUserManager().getUserForName(result.getString(1));

                if ( u != null ) users.add(u);
            }
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return users;
    }

    public Set<ProcessObjectComment> getComments(String modelId, String version, String elementId) {
        Set<ProcessObjectComment> comments = new HashSet<ProcessObjectComment>();

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ");
        sb.append(DatabaseSchema.Table.COMMENTS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.COMMENT_MODEL);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.COMMENT_MODEL.encodeValue(modelId));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.COMMENT_ELEMENT);
        sb.append("=");
        sb.append(DatabaseSchema.Attribute.COMMENT_ELEMENT.encodeValue(elementId));
        sb.append(" AND ");
        sb.append(DatabaseSchema.Attribute.COMMENT_VALIDFROM);
        sb.append("<=");
        sb.append(version);
        sb.append(" AND ");
        sb.append(version);
        sb.append("<=");
        sb.append(DatabaseSchema.Attribute.COMMENT_VALIDUNTIL);
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(sb.toString());

            while ( result.next() ) {
                String commentId = result.getString(DatabaseSchema.Attribute.COMMENT_ID.getName());
                int validFrom = result.getInt(DatabaseSchema.Attribute.COMMENT_VALIDFROM.getName());
                int validUntil = result.getInt(DatabaseSchema.Attribute.COMMENT_VALIDUNTIL.getName());
                String user = result.getString(DatabaseSchema.Attribute.COMMENT_USER.getName());
                String text = result.getString(DatabaseSchema.Attribute.COMMENT_TEXT.getName());
                Date d = result.getTimestamp(DatabaseSchema.Attribute.COMMENT_CREATED.getName());
                comments.add( ProcessObjectComment.forAttributes(commentId, elementId, d, user, validFrom, validUntil, text));
            }

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return comments;
    }
    
    /**
     * Opens a new database connection and returns it. This does not affect the return value of
     * getConnection().
     *
     * @return <ul>
     *  <li> if successful, the created connection </li>
     *  <li> else, null </li>
     * </ul>
     */
    public Connection openNewDatabaseConnection() {
        try {
            Properties dbprops = FileSystemConnector.loadDatabaseProperties();
            Connection conn = openConnection(dbprops);

            if ( conn != null )
                createTablesIfNotExist(conn, dbprops);
            return conn;
        } catch ( IOException ex ) {
            ex.printStackTrace();
            return null;
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    protected void createTablesIfNotExist( Connection conn, Properties props ) {
        try {
            Statement stmt = conn.createStatement();

            for ( int i = 0; i < tableNames.length; i++ ) {
                if ( props.keySet().contains( stmtProps[i] ) && !props.getProperty( stmtProps[i] ).isEmpty() ) {
                    stmt.executeUpdate( getTableCreationStatement( tableNames[i].getName(), props.getProperty( stmtProps[i] )));
                } //else LOGGER.warn( "No "+ tableNames[i] + " table creation statement found --> No " + tableNames[i] + " table created" );
            }

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }
    }

    private Set<Object> selectSingleAttribute( String table, String column ) {
        Set<Object> values = new HashSet<Object>();
        String q = "SELECT " + column + " FROM " + table + ";";
        try {
            Statement stmt = dbconn.createStatement();
            ResultSet result = stmt.executeQuery(q);

            while ( result.next() ) {
                values.add( result.getObject(1) );
            }
            result.close();
            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return values;
    }

    protected Set<String> selectSingleColumn( Connection conn, String table, String column ) {
        Set<String> values = new HashSet<String>();
        String q = "SELECT " + column + " FROM " + table + ";";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(q);

            while ( result.next() ) {
                values.add( result.getString(1) );
            }
            result.close();
            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
        }

        return values;
    }

    protected String getUserImageInsertStatement( String id ) {
        StringBuffer sb = new StringBuffer();

        if ( id == null ) {
            sb.append("INSERT INTO ");
            sb.append(DatabaseSchema.Table.USER_IMAGE);
            sb.append("(");
            sb.append(DatabaseSchema.Attribute.USER_IMAGE_IMAGE);
            sb.append(",");
            sb.append(DatabaseSchema.Attribute.USER_IMAGE_TYPE);
            sb.append(") VALUES(?,?)");
        }  else {
            sb.append("UPDATE ");
            sb.append(DatabaseSchema.Table.USER_IMAGE);
            sb.append(" SET ");
            sb.append(DatabaseSchema.Attribute.USER_IMAGE_IMAGE);
            sb.append("= ?,");
            sb.append(DatabaseSchema.Attribute.USER_IMAGE_TYPE);
            sb.append("= ? WHERE ");
            sb.append(DatabaseSchema.Attribute.USER_IMAGE_ID);
            sb.append("=");
            sb.append(id);
        }

        return sb.toString();
    }

    public void execute( String update ) throws SQLException {
       Statement stmt = dbconn.createStatement();
       stmt.execute( update );
       stmt.close();
    }

    private PreparedStatement getInsertStatement( String table, DatabaseSchema.Attribute[] attributes, Object[] values ) throws SQLException {
        StringBuffer sb = new StringBuffer();

        int min = Math.min( attributes.length, values.length );
        sb.append( "INSERT INTO ");
        sb.append( table );
        sb.append( "(");
        for ( int i = 0; i < min; i++ ) {
            sb.append(attributes[i]);
            if ( i + 1 < min )
                sb.append(",");
        }
        sb.append(") VALUES (");
        for ( int i = 0; i < min; i++ ) {
            sb.append("?");
            if ( i + 1 < min )
                sb.append(",");
        }
        sb.append(");");

        PreparedStatement stmt = dbconn.prepareStatement(sb.toString());

        for ( int i = 0; i < min; i++ ) {
            if ( values[i] instanceof java.util.Date )
                stmt.setObject(i + 1, values[i], Types.TIMESTAMP);
            else
                stmt.setObject(i + 1, values[i]);
        }

        return stmt;
    }

    private String encodeBooelan( boolean b ) {
        return b ? "TRUE" : "FALSE";
    }

    private String createIdClause( EntityType entity, Object[] idParts ) {
        StringBuffer sb = new StringBuffer();
        DatabaseSchema.Attribute[] idAtts = entity.getSQLIdColumn();
        int min = Math.min( idParts.length, idAtts.length );

        for (int i = 0; i < min; i++ ) {
            sb.append(idAtts[i]);
            sb.append("=");
            sb.append(idAtts[i].encodeValue(idParts[i]));
            if ( i + 1 < min )
                sb.append(" AND ");
        }

        return sb.toString();
    }

    protected abstract Connection openConnection( Properties dbprops ) throws Exception;

    protected abstract String getTableCreationStatement( String tableName, String schemaPart );

    protected abstract void setUserImageParameter( PreparedStatement stmt, int parIndex, byte[] image ) throws SQLException;

    protected abstract String getEnumQueryParameter( Enum e );

    protected abstract String getStringCastParameter( String attribute );

    protected abstract String getStringConcatenation( String[] strings );
}
