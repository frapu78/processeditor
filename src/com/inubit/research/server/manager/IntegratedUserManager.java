/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.config.UsersConfig;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.handler.UserRequestHandler;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;
import com.inubit.research.server.user.User;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author fel
 */
public class IntegratedUserManager implements UserManager {

    private static IntegratedUserManager instance;
    private UsersConfig usersConfig;
    private static Map<String, LoginableUser> sessions = new HashMap<String, LoginableUser>();
    private static Map<String, Date> lastAccess = new HashMap<String, Date>();
    private static final int checkInterval = 900000;
//    private static final int checkInterval = 30000;

    static class SessionCleaner extends TimerTask {

        static Date lastCheck = new Date();

        @Override
        public void run() {
            Date currentDate = new Date();
            Set<String> removableKeys = new HashSet<String>();
            if (lastAccess != null) {
                for (Map.Entry<String, Date> e : lastAccess.entrySet()) {
                    Date lastDate = e.getValue();

                    if (lastDate.before(lastCheck)) {
                        removableKeys.add(e.getKey());
                    }
                }
            }
            System.out.println("Cleaning sessions");
            for (String key : removableKeys) {
                System.out.println("REMOVING session: " + key + " due to inactivity");
                sessions.remove(key);
                lastAccess.remove(key);
            }
            lastCheck = (Date) currentDate.clone();
        }
    }

    private IntegratedUserManager(UsersConfig usersConfig) {
        this.usersConfig = usersConfig;
    }

    @Override
    public Set<String> getUserNames() {
        return this.usersConfig.getUserNames();
    }

    @Override
    public boolean addUser(String name, String pwd) {
        return this.usersConfig.addUser(name, pwd);
    }

    @Override
    public boolean addGroup(String name) {
        return this.usersConfig.addGroup(name);
    }

    @Override
    public Set<String> getGroupNames() {
        return this.usersConfig.getGroupNames();
    }

    @Override
    public Group getGroupForName(String name) {
        return this.usersConfig.getGroup(name);
    }

    @Override
    public LoginableUser getUserForSession(String sessionID) {
        if (sessionID == null) {
            return null;
        }

        touchSession(sessionID);
        return sessions.get(sessionID);
    }

    @Override
    public SingleUser getUserForName(String name) {
        return this.usersConfig.getUser(name);
    }

    @Override
    public LoginableUser getUserForRequest(RequestFacade req) {
        String sessionID = req.getCookieByName(UserRequestHandler.SESSION_ATTRIBUTE);

        if (sessionID != null && ProcessEditorServerHelper.getUserManager().getUserForSession(sessionID) != null) {
            return this.getUserForSession(sessionID);
        }

        Map<String, String> params = RequestUtils.getQueryParameters(req);

        // Make session id as param work (e.g. for previews)
        if (params.get(UserRequestHandler.SESSION_ATTRIBUTE) != null) {
            return this.getUserForSession(params.get(UserRequestHandler.SESSION_ATTRIBUTE));
        }

        if (params.get("key") != null) {
            return this.getUserForSession(params.get("key"));
        }

        return null;
    }

    @Override
    public Set<Group> getGroupsForUser(User user) {
        Set<Group> groups = new HashSet<Group>();
        for (String g : this.usersConfig.getGroupNames()) {
            Group group = this.usersConfig.getGroup(g);

            if (group != null) {
                if (user.isSingleUser()) {
                    if (group.hasMember(user.getName())) {
                        groups.add(group);
                    }
                }

                if (user.isGroup()) {
                    if (group.hasSubGroup(user.getName())) {
                        groups.add(group);
                    }
                }
            }
        }

        return groups;
    }

    @Override
    public Set<Group> getRecursiveGroupsForUser(User user) {
        return this.getRecursiveGroupsForUser(user, new HashSet<String>());
    }

    @Override
    public Set<SingleUser> getRecursiveUsersForGroup(Group g) {
        return this.getRecursiveUsersForGroup(g, new HashSet<String>());
    }

    @Override
    public String login(String name, String pwd) {
        SingleUser u = this.usersConfig.getUser(name);
        if (u != null) {
            if (u.getPwd().equals(ProcessEditorServerUtils.getMD5Hash(pwd))) {
                String sessionID = System.currentTimeMillis() + "_" + System.nanoTime();
                sessions.put(sessionID, u);
                touchSession(sessionID);
                return sessionID;
            }
        }

        return null;
    }

    @Override
    public String login(String key, TemporaryUser tu) {
        sessions.put(key, tu);
        touchSession(key);
        return key;
    }

    @Override
    public void setMail(String userName, String mail, boolean deferWrite) {
        this.usersConfig.setMail(userName, mail, deferWrite);
    }

    @Override
    public void setPictureId(String userName, String id, boolean deferWrite) {
        this.usersConfig.setPictureId(userName, id, deferWrite);
    }

    @Override
    public BufferedImage loadUserImage(SingleUser u) {
        String picId = u.getPictureId();

        if (picId != null && !picId.equals("")) {
            return ProcessEditorServerHelper.getPersistenceConnector().loadUserImage(picId);
        }

        return null;
    }

    @Override
    public void setRealName(String userName, String name, boolean deferWrite) {
        this.usersConfig.setRealName(userName, name, deferWrite);
    }

    @Override
    public void setPwd(String userName, String password, boolean deferWrite) {
        this.usersConfig.setPwd(userName, password, deferWrite);
    }

    @Override
    public void logout(String sessionID) {
        sessions.remove(sessionID);
    }

    public void setAdmin(String name, boolean isAdmin) {
        this.usersConfig.setAdmin(name, isAdmin);
    }

    @Override
    public void setGroupMembers(String name, Set<String> members) {
        this.usersConfig.setGroupMembers(name, members);
    }

    @Override
    public void setSubgroups(String name, Set<String> subgroups) {
        this.usersConfig.setSubgroups(name, subgroups);
    }

    @Override
    public boolean addISConnection(ISLocation ism, SingleUser user) {
        return this.usersConfig.addISConnection(ism, user);
    }

    @Override
    public void removeISConnection(ISLocation ism, SingleUser user) {
        this.usersConfig.removeISConnection(ism, user);
    }

    @Override
    public Set<ISLocation> getAllISConnections() {
        Set<ISLocation> locs = new HashSet<ISLocation>();

        Set<String> users = this.getUserNames();
        for (String u : users) {
            locs.addAll(this.getUserForName(u).getISConnections());
        }

        return locs;
    }

    private Set<SingleUser> getRecursiveUsersForGroup(Group group, Set<String> visited) {
        Set<SingleUser> users = new HashSet<SingleUser>();
        for (String u : group.getMembers()) {
            users.add(this.getUserForName(u));
        }

        for (String s : group.getSubGroups()) {
            if (!visited.contains(s)) {
                Group g = this.getGroupForName(s);
                visited.add(g.getName());
                users.addAll(this.getRecursiveUsersForGroup(g, visited));
            }
        }

        return users;
    }

    private Set<Group> getRecursiveGroupsForUser(User user, HashSet<String> visited) {
        Set<Group> groups = this.getGroupsForUser(user);
        Set<Group> groupsForUser = new HashSet<Group>();
        for (Group g : groups) {
            if (!visited.contains(g.getName())) {
                visited.add(g.getName());
                groupsForUser.add(g);
                groupsForUser.addAll(this.getRecursiveGroupsForUser(g, visited));
            }
        }

        return groupsForUser;
    }

    public static IntegratedUserManager getInstance() {
        if (instance == null) {
            try {
                instance = createInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    private static synchronized IntegratedUserManager createInstance() throws Exception {
        if (instance == null) {
            UsersConfig config = ProcessEditorServerHelper.getUsersConfig();

            instance = new IntegratedUserManager(config);

            Timer timer = new Timer();
            timer.schedule(new SessionCleaner(), new Date(), checkInterval);
        }

        return instance;
    }

    private static void touchSession(String sessionID) {
        lastAccess.put(sessionID, new Date());
    }
}