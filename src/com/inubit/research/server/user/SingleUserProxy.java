package com.inubit.research.server.user;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseSchema;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Proxy object for accessing user data that is stored remotely, i.e. in a database
 *
 * TODO think about some "caching" mechanism
 *
 * @author fel
 */
public class SingleUserProxy extends SingleUser {
    private static final int CACHE_TIME = 60000;
    private static final DatabaseSchema.Attribute[] CONN_ATTS = { DatabaseSchema.Attribute.CONNECTIONS_USER, DatabaseSchema.Attribute.CONNECTIONS_URL, DatabaseSchema.Attribute.CONNECTIONS_ISUSER, DatabaseSchema.Attribute.CONNECTIONS_PWD };

    private static Calendar cal = Calendar.getInstance();

    private DatabaseConnector db;
    private Map<DatabaseSchema.Attribute, Date> lastUpdate  = new HashMap<DatabaseSchema.Attribute, Date>();

    public SingleUserProxy( String name, DatabaseConnector dc ) {
        super();
        this.name = name;
        this.db = dc;
    }

    @Override
    public String getPwd() {
        //always fetch the current password from the database
        return (String) fetchProperty( DatabaseSchema.Attribute.USER_PWD );
    }

    @Override
    public void addISConnection(ISLocation ism) {
        try {
            db.insertEntity(DatabaseConnector.EntityType.CONNECTION, CONN_ATTS, new Object[]{ this.name, ism.getURL(), ism.getUser(), ism.getPwd() } );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<ISLocation> getISConnections() {
        return db.getISConnections(this);
    }

    @Override
    public String getPictureId() {
        if ( !isUpToDate( DatabaseSchema.Attribute.USER_IMAGE ) ) {
            String value = String.valueOf(fetchProperty( DatabaseSchema.Attribute.USER_IMAGE ));
            this.picId = value;
        }
        
        return this.picId;
    }

    @Override
    public boolean isAdmin() {
        if ( !isUpToDate( DatabaseSchema.Attribute.USER_ADMIN ) ) {
            boolean value = (Boolean) fetchProperty( DatabaseSchema.Attribute.USER_ADMIN );
            this.admin = value;
        }
        return this.admin;
    }

    @Override
    public void removeISConnection(ISLocation ism) {
        try {
            db.deleteEntity(DatabaseConnector.EntityType.CONNECTION, new Object[]{ this.name, ism.getURL(), ism.getUser(), ism.getPwd() });
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setIsAdmin(boolean isAdmin) {
        if ( this.updateProperty( DatabaseSchema.Attribute.USER_ADMIN,  Boolean.valueOf(isAdmin)) ) {
            this.lastUpdate.put( DatabaseSchema.Attribute.USER_ADMIN, cal.getTime() );
            this.admin = isAdmin;
        }
    }

    @Override
    public void setPictureId(String id) {
        if ( this.updateProperty( DatabaseSchema.Attribute.USER_IMAGE, id != null ? Integer.valueOf(id) : null ) ) {
            this.lastUpdate.put( DatabaseSchema.Attribute.USER_IMAGE, cal.getTime() );
            this.picId = id;
        }
    }

    @Override
    public String getMail() {
        if ( !isUpToDate( DatabaseSchema.Attribute.USER_MAIL ) ) {
            String value = (String) fetchProperty( DatabaseSchema.Attribute.USER_MAIL );
            this.mail = value;
        }
        return this.mail;
    }

    @Override
    public String getRealName() {
        if ( !isUpToDate( DatabaseSchema.Attribute.USER_REALNAME ) ) {
            String value = (String) fetchProperty( DatabaseSchema.Attribute.USER_REALNAME );
            this.realName = value;
        }
        return this.realName;
    }

    @Override
    public void setMail(String newMail) {
        if (this.updateProperty( DatabaseSchema.Attribute.USER_MAIL,  newMail)) {
            this.lastUpdate.put( DatabaseSchema.Attribute.USER_MAIL, cal.getTime() );
            this.mail = newMail;
        }
    }

    @Override
    public void setRealName(String realName) {
        if (this.updateProperty( DatabaseSchema.Attribute.USER_REALNAME, realName)) {
            this.lastUpdate.put( DatabaseSchema.Attribute.USER_REALNAME, cal.getTime() );
            this.realName = realName;
        }
    }

    @Override
    public void setPwd(String password) {
        if (this.updateProperty( DatabaseSchema.Attribute.USER_PWD, password)) {
            this.lastUpdate.put( DatabaseSchema.Attribute.USER_PWD, cal.getTime() );
        }
    }

    @Override
    public boolean isAllowedToSaveToFileSystem() {
        return false;
    }
    
    private boolean isUpToDate( DatabaseSchema.Attribute key ) {
        if ( lastUpdate.containsKey(key) ) {
            Date d = lastUpdate.get(key);
            long diff = cal.getTime().getTime() - d.getTime();

            return diff <= CACHE_TIME;
        }

        return false;
    }

    private Object fetchProperty( DatabaseSchema.Attribute property) {
        return db.selectSingleAttribute(property, DatabaseConnector.EntityType.USER, new String[] {this.name});
    }

    private boolean updateProperty( DatabaseSchema.Attribute property, Object value ) {
        return db.updateAttribute(property, value, DatabaseConnector.EntityType.USER, new String[] {this.name});
    }
}
