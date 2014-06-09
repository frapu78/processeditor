/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.persistence;

import com.inubit.research.server.user.SingleUser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Properties;

/**
 *
 * @author fel
 */
public class MySQLDatabaseConnector extends SQLDatabaseConnector {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWD = "inubit";
    private static final String DEFAULT_DB_NAME = "webmodeler";

    public MySQLDatabaseConnector() {
        super();
    }

    @Override
    protected Connection openConnection(Properties dbprops) throws Exception {
        String host = ( dbprops.keySet().contains( PROP_HOST ) ? dbprops.getProperty(PROP_HOST) : DEFAULT_HOST );
        String port = ( dbprops.keySet().contains( PROP_PORT) ? dbprops.getProperty(PROP_PORT) : DEFAULT_PORT );
        String db = ( dbprops.keySet().contains( PROP_DB_NAME) ? dbprops.getProperty(PROP_DB_NAME) : DEFAULT_DB_NAME );
        String user = ( dbprops.keySet().contains( PROP_USER) ? dbprops.getProperty(PROP_USER) : DEFAULT_USER );
        String pw = ( dbprops.keySet().contains( PROP_PASSWD) ? dbprops.getProperty(PROP_PASSWD) : DEFAULT_PASSWD );

        String connURL = "jdbc:mysql://" + host + ":" + port + "/" + db;

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn =  DriverManager.getConnection(connURL, user, pw);

        return conn;
    }

    @Override
    protected String getTableCreationStatement( String tableName, String schemaPart ) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append( tableName );
        sb.append( schemaPart );

        return sb.toString();
    }

    protected void setUserImageParameter( PreparedStatement stmt, int parIndex, byte[] image ) throws SQLException {
        stmt.setBinaryStream(parIndex, new ByteArrayInputStream(image));
    }

    public int addModelVersion(File f, String id, SingleUser user, String comment, String date) throws SQLException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO ");
        sb.append(DatabaseSchema.Table.VERSIONS);
        sb.append("(");
        sb.append(DatabaseSchema.Attribute.VERSION_ID);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.VERSION_MODEL);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.VERSION_COMMENT);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.VERSION_CREATED);
        sb.append(",");
        sb.append(DatabaseSchema.Attribute.VERSION_USER);
        sb.append(") VALUES");
        sb.append("(?,?,?,?,?)");

        PreparedStatement prepStmt = dbconn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS);
        prepStmt.setString(1, id);
        prepStmt.setBinaryStream(2, new FileInputStream(f));
        prepStmt.setString(3, comment);
        prepStmt.setTimestamp(4, Timestamp.valueOf( date ));
        if (user != null)
            prepStmt.setString(5, user.getName()) ;
        else
            prepStmt.setNull(5, Types.CHAR);

        prepStmt.execute();
        ResultSet result = prepStmt.getGeneratedKeys();
        result.next();
        int vN = result.getInt(1);
        prepStmt.close();

        return vN;
    }

    @Override
    protected String getEnumQueryParameter(Enum e) {
        return "'" + e.toString() + "'";
    }

    @Override
    protected String getStringCastParameter(String attribute) {
        return "CAST(" + attribute + " AS CHAR)";
    }

    @Override
    protected String getStringConcatenation(String[] strings) {
        StringBuffer sb = new StringBuffer();
        sb.append("CONCAT(");
        for ( int i = 0; i < strings.length; i++  ) {
            sb.append(strings[i]);
            if ( i + 1 < strings.length )
                sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

}
