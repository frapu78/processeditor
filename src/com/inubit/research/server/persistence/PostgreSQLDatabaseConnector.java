/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.User.UserType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author fel
 */
public class PostgreSQLDatabaseConnector extends SQLDatabaseConnector {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_USER = "fel";
    private static final String DEFAULT_PASSWD = "inubit";
    private static final String DEFAULT_DB_NAME = "webmodeler";

    @Override
    protected Connection openConnection(Properties dbprops) throws Exception {
        String host = ( dbprops.keySet().contains( PROP_HOST ) ? dbprops.getProperty(PROP_HOST) : DEFAULT_HOST );
        String port = ( dbprops.keySet().contains( PROP_PORT) ? dbprops.getProperty(PROP_PORT) : DEFAULT_PORT );
        String db = ( dbprops.keySet().contains( PROP_DB_NAME) ? dbprops.getProperty(PROP_DB_NAME) : DEFAULT_DB_NAME );
        String user = ( dbprops.keySet().contains( PROP_USER) ? dbprops.getProperty(PROP_USER) : DEFAULT_USER );
        String pw = ( dbprops.keySet().contains( PROP_PASSWD) ? dbprops.getProperty(PROP_PASSWD) : DEFAULT_PASSWD );

        String connURL = "jdbc:postgresql://" + host + ":" + port + "/" + db;

        Class.forName("org.postgresql.Driver").newInstance();
        Connection conn =  DriverManager.getConnection(connURL, user, pw);

        return conn;
    }

    @Override
    protected void createTablesIfNotExist( Connection conn, Properties props ) {
        try {
            Set<String> existing = getExistingTableNames( conn );
            Statement stmt = conn.createStatement();
            createEnumTypes(stmt, conn);

            for ( int i = 0; i < tableNames.length; i++ ) {
                if ( existing.contains( tableNames[i].getName() ) ) {
//                    LOGGER.info( "Table " + tableNames[i] + " already exists!");
                    continue;
                }

                if ( props.keySet().contains( stmtProps[i] ) && !props.getProperty( stmtProps[i] ).isEmpty()  ) {
                    stmt.executeUpdate( getTableCreationStatement( tableNames[i].getName(), props.getProperty( stmtProps[i] )));
                } //else LOGGER.warn( "No "+ tableNames[i] + " table creation statement found --> No " + tableNames[i] + " table created" );
            }

            stmt.close();
        } catch ( SQLException ex ) {
            ex.printStackTrace();
//            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    protected String getTableCreationStatement(String tableName, String schemaPart) {
        StringBuffer sb = new StringBuffer();

        sb.append("CREATE TABLE ");
        sb.append(tableName);
        sb.append(schemaPart);
        sb.append(";");

        return sb.toString();
    }

    public int addModelVersion(File f, String id, SingleUser user, String comment, String date) throws SQLException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO ");
        sb.append(DatabaseSchema.Table.VERSIONS);
        sb.append(" VALUES");
        sb.append("(?,(SELECT COUNT(*) FROM ");
        sb.append(DatabaseSchema.Table.VERSIONS);
        sb.append(" WHERE ");
        sb.append(DatabaseSchema.Attribute.VERSION_ID);
        sb.append("='");
        sb.append(id);
        sb.append("') + 1,?,?,?,?) RETURNING ");
        sb.append(DatabaseSchema.Attribute.VERSION_VERSION);

        PreparedStatement prepStmt = dbconn.prepareStatement(sb.toString());
        prepStmt.setString(1, id);
        setModelParameter(prepStmt, 2, f);
        prepStmt.setString(3, comment);
        prepStmt.setTimestamp(4, Timestamp.valueOf( date ));
        if (user != null)
            prepStmt.setString(5, user.getName()) ;
        else
            prepStmt.setNull(5, Types.CHAR);

        prepStmt.execute();
        ResultSet result = prepStmt.getResultSet();
        result.next();
        int vN = result.getInt(1);
        prepStmt.close();

        return vN;
    }

    private void setModelParameter(PreparedStatement prepStmt, int parIndex, File f) throws IOException, SQLException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(f);
        byte[] buf = new byte[2048];
        int len = 0;
        while ( (len = fis.read(buf)) > -1 ) {
            bos.write(buf, 0, len);
        }
        SQLXML xml = dbconn.createSQLXML();
        xml.setString( bos.toString() );
        prepStmt.setSQLXML(parIndex, xml);
    }

    protected void setUserImageParameter( PreparedStatement stmt, int parIndex, byte[] image ) throws SQLException {
        ByteArrayInputStream bis =  new ByteArrayInputStream(image);

        stmt.setBinaryStream(parIndex, bis, image.length);
    }

    private Set<String> getExistingTableNames( Connection conn ) {
        return selectSingleColumn(conn, "pg_catalog.pg_tables" , "tablename");
    }

    @Override
    protected String getUserImageInsertStatement(String id) {
        String raw = super.getUserImageInsertStatement(id);
        int index = raw.lastIndexOf("?") + 1;

        String firstPart = raw.substring(0, index);
        String secondPart = raw.substring(index);

        return firstPart + "::wm_imagetype " + secondPart;
    }

    private Set<String> getExistingTypes( Connection conn ) {
        return selectSingleColumn(conn, "pg_catalog.pg_type" , "typname");
    }

    private void createEnumTypes(Statement stmt, Connection conn) {
        StringBuffer sb = new StringBuffer();

        Set<String> types = getExistingTypes(conn);

        if ( !types.contains("wm_accesstype") )
            sb.append("CREATE TYPE wm_accesstype AS ENUM ('VIEW', 'COMMENT', 'WRITE', 'OWNER');");
        if ( !types.contains("wm_usertype"))
            sb.append("CREATE TYPE wm_usertype AS ENUM ('SINGLE_USER', 'GROUP');");
        if ( !types.contains("wm_imagetype"))
            sb.append("CREATE TYPE wm_imagetype AS ENUM ('png', 'jpg');");

        try {
            if ( sb.length() > 0 );
                stmt.execute(sb.toString());
        } catch ( SQLException ex ) {
            ex.printStackTrace();
//            LOGGER.warn("createEnumTypes:", ex);
        }
    }

    @Override
    protected String getEnumQueryParameter( Enum e ) {
        if ( e.getDeclaringClass().equals( AccessType.class ) )
            return "'" + e.toString() + "'" + "::wm_accesstype";
        else if ( e.getDeclaringClass().equals( UserType.class ) )
            return "'" + e.toString() + "'" + "::wm_usertype";
        else if ( e.getDeclaringClass().equals( ImageType.class) )
            return "'" + e.toString().toLowerCase() + "'" + "wm_imagetype";
        else
            return "'" + e.toString() + "'";
    }

    @Override
    protected String getStringCastParameter(String attribute) {
        return "CAST(" + attribute + " AS text)";
    }

    @Override
    protected String getStringConcatenation(String[] strings) {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < strings.length; i++  ) {
            sb.append(strings[i]);
            if ( i + 1 < strings.length )
                sb.append(" || ");
        }
        return sb.toString();
    }
}
