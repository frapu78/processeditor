package com.inubit.research.server.user;

import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseSchema.Attribute;
import java.util.HashSet;
import java.util.Set;

/**
 * Proxy class for accessing group information that resides in a database
 * @author fel
 */
public class GroupProxy extends Group {
    private static final Attribute[] GROUP_ATTS = new Attribute[]{ Attribute.GROUP_NAME, Attribute.GROUP_USER };
    private static final Attribute[] SUBGROUP_ATTS = new Attribute[]{ Attribute.SUBGROUP_NAME, Attribute.SUBGROUP_SUBGROUP };
    private DatabaseConnector db;

    public GroupProxy( String name, DatabaseConnector dc ) {
        this.name = name;
        this.db = dc;
    }

    @Override
    public Set<String> getMembers() {
        Set<String> members = new HashSet<String>();
        Set<Object> names = db.selectMultiValueSingleAttribute(Attribute.GROUP_USER, DatabaseConnector.EntityType.GROUP, new Object[]{this.name});
        for ( Object name : names )
            members.add( (String) name );
        return members;
    }

    @Override
    public Set<String> getSubGroups() {
        Set<String> subgroups = new HashSet<String>();
        Set<Object> names = db.selectMultiValueSingleAttribute(Attribute.SUBGROUP_SUBGROUP, DatabaseConnector.EntityType.SUBGROUP, new Object[]{this.name});
        for ( Object name : names )
            subgroups.add( (String) name );
        return subgroups;
    }

    @Override
    public boolean hasMember(String userName) {
        return this.getMembers().contains(userName);
    }

    @Override
    public boolean hasSubGroup(String groupName) {
        return this.getSubGroups().contains(groupName);
    }

    @Override
    public void setMembers(Set<String> members) {
        //1. delete current members
        db.deleteEntity(DatabaseConnector.EntityType.GROUP, new Object[]{this.name});

        //2. insert new ones
        for ( String member : members ) 
            db.insertEntity(DatabaseConnector.EntityType.GROUP, GROUP_ATTS, new Object[] { this.name, member });
    }

    @Override
    public void setSubGroups(Set<String> subgroups) {
        //1. delete current subgroups
        db.deleteEntity(DatabaseConnector.EntityType.SUBGROUP, new Object[]{this.name});

        //2. insert new ones
        for ( String group : subgroups )
            db.insertEntity(DatabaseConnector.EntityType.SUBGROUP, SUBGROUP_ATTS, new Object[] { this.name, group });
    }    
}
