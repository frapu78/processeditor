/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.config;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.SingleUser;
import java.util.Set;

/**
 * Interface for configuration classes containig all registered groups and users.
 * @author fel
 */
public interface UsersConfig {
    public Set<String> getUserNames();

    public Set<String> getGroupNames();

    public Group getGroup( String name ) ;

    public SingleUser getUser( String name );

    public boolean addUser( String name, String pwd );

    public boolean addGroup( String name );

    public void setMail( String userName, String mail, boolean deferWrite ) ;

    public void setPictureId( String userName, String id, boolean deferWrite );

    public void setRealName( String userName, String name, boolean deferWrite ) ;

    public void setAdmin( String name, boolean isAdmin );

    public void setGroupMembers( String name, Set<String> members );

    public void setSubgroups( String name, Set<String> subgroups );

    public boolean addISConnection( ISLocation ism, SingleUser user ) ;

    public void removeISConnection( ISLocation ism, SingleUser user ) ;
}