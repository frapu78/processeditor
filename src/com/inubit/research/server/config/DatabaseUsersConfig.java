/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseSchema;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.GroupProxy;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.SingleUserProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fel
 */
public class DatabaseUsersConfig implements UsersConfig {

    private DatabaseConnector db;

    private PreparedStatement singleUserSelect;

    private Map<String, SingleUserProxy> users = new HashMap<String, SingleUserProxy>();
    private Map<String, GroupProxy> groups = new HashMap<String, GroupProxy>();

    public DatabaseUsersConfig() {
        this( ProcessEditorServerHelper.getDatabaseConnector() );
    }

    public DatabaseUsersConfig( DatabaseConnector dc ) {
        this.db = dc;
        loadAllUserProxys();
        checkForExistingUsers();
    }

    public Set<String> getUserNames() {
        loadAllUserProxys();
        return this.users.keySet();
    }

    public Set<String> getGroupNames() {
        this.groups = new HashMap<String, GroupProxy>();
        Set<Object> groupNames = db.selectSingleAttribute(DatabaseSchema.Attribute.GROUP_NAME, DatabaseConnector.EntityType.GROUP);
        groupNames.addAll(db.selectSingleAttribute(DatabaseSchema.Attribute.SUBGROUP_NAME, DatabaseConnector.EntityType.SUBGROUP));
        for ( Object group : groupNames )
            groups.put( (String) group, new GroupProxy((String) group, db) );

        return groups.keySet();
    }

    public Group getGroup(String name) {
        if ( !this.groups.containsKey(name) )
          this.getGroupNames();

        return this.groups.get(name);
    }

    public SingleUser getUser(String name) {
        if ( this.users.containsKey(name) )
            return this.users.get(name);

        //if user is not listed, try fetching it from the database
        try {
            this.singleUserSelect.setString(1, name);
            ResultSet result = this.singleUserSelect.executeQuery();
            if ( result.next() ) {
                SingleUserProxy newUser = new SingleUserProxy(name, db);
                this.users.put( name, newUser );
                return newUser;
            } else return null;
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return null;
        }

    }

    public boolean addUser(String name, String pwd) {
        pwd = ProcessEditorServerUtils.getMD5Hash(pwd);

        SingleUser su = new SingleUser(name, pwd);

        if ( db.addUser(su) ) {
            users.put(name, new SingleUserProxy(name, db));
            return true;
        } else {
            return false;
        }
    }

    public boolean addGroup(String name) {
        if ( !this.groups.containsKey(name) ) {
            this.groups.put( name, new GroupProxy(name, db));
            return true;
        }

        return false;
    }

    public void setMail(String userName, String mail, boolean deferWrite) {
        SingleUser su = this.getUser(userName);
        if ( su != null ) su.setMail(mail);
    }

    public void setPictureId(String userName, String id, boolean deferWrite) {
        SingleUser su = this.getUser(userName);
        if ( su != null ) su.setPictureId(id);
    }

    public void setRealName(String userName, String name, boolean deferWrite) {
        SingleUser su = this.getUser(userName);
        if ( su != null ) su.setRealName(name);
    }

    public void setAdmin(String userName, boolean isAdmin) {
        SingleUser su = this.getUser(userName);
        if ( su != null ) su.setIsAdmin(isAdmin);
    }

    public void setGroupMembers(String name, Set<String> members) {
        this.getGroup(name).setMembers(members);
    }

    public void setSubgroups(String name, Set<String> subgroups) {
        this.getGroup(name).setSubGroups(subgroups);
    }

    public boolean addISConnection(ISLocation ism, SingleUser user) {
        if (ism.checkConnection()) {
            ism.setOwner(null, user, null);
            user.addISConnection(ism);
            return true;
        }

        return false;
    }

    public void removeISConnection(ISLocation ism, SingleUser user) {
        user.removeISConnection(ism);
    }

    private void checkForExistingUsers() {
        if ( this.getUserNames().isEmpty() ) {
            this.addUser("root", "inubit");
            users.get("root").setIsAdmin(true);
        }
    }

    private void loadAllUserProxys() {
        this.users = new HashMap<String, SingleUserProxy>();
        Set<Object> userNames = db.selectSingleAttribute( DatabaseSchema.Attribute.USER_NAME, DatabaseConnector.EntityType.USER);

        for ( Object userName : userNames ) {
            users.put( (String) userName, new SingleUserProxy( (String) userName, db));
        }
    }

}
