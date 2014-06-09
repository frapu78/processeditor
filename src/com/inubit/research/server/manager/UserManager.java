/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;
import com.inubit.research.server.user.User;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 *
 * @author fel
 */
public interface UserManager {

    public boolean addUser( String name, String pwd );

    public boolean addGroup( String name );

    public Set<String> getUserNames();

    public Set<String> getGroupNames();

    public Set<Group> getGroupsForUser( User user );

    public Set<Group> getRecursiveGroupsForUser( User user );

    public SingleUser getUserForName( String name );

    public LoginableUser getUserForRequest( RequestFacade req );

    public Group getGroupForName( String name );

    public Set<SingleUser> getRecursiveUsersForGroup( Group g );

    public void setGroupMembers( String name, Set<String> members);

    public void setSubgroups( String name, Set<String> subgroups );

    public String login( String name, String pwd );

    public String login( String key, TemporaryUser tu );

    public void logout( String sessionID);

    public LoginableUser getUserForSession(String sessionID);

    public BufferedImage loadUserImage( SingleUser u );

    public void setMail( String userName, String mail, boolean deferWrite );

    public void setPictureId( String userName, String id, boolean deferWrite );

    public void setRealName( String userName, String name, boolean deferWrite );

    boolean addISConnection( ISLocation ism , SingleUser user );

    Set<ISLocation> getAllISConnections();

    void removeISConnection( ISLocation ism, SingleUser user );
}
