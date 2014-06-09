package com.inubit.research.server.user;

import com.inubit.research.security.Trustee;
import com.inubit.research.server.manager.IntegratedUserManager;
import com.inubit.research.server.manager.UserManager;
import java.util.Set;

/**
 *
 * @author fel
 */
public class Group extends User {

    private Set<String> subGroups;
    private Set<Integer> subGroupSIDs;
    private Set<String> users;
    private Set<Integer> userSIDs;

    protected Group() {
    }

    public Group(String name, Set<String> users, Set<String> groups) {
        this.name = name;
        setMembers(users);
        setSubGroups(groups);
    }

    public Set<String> getMembers() {
        return this.users;
    }

    public Set<String> getSubGroups() {
        return this.subGroups;
    }

    public boolean hasMember(String userName) {
        return users.contains(userName);
    }

    public boolean hasSubGroup(String groupName) {
        return this.subGroups.contains(groupName);
    }

    public void setMembers(Set<String> members) {
        this.users = members;
    }

    public void setSubGroups(Set<String> subgroups) {
        this.subGroups = subgroups;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public UserType getUserType() {
        return UserType.GROUP;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Group)) {
            return false;
        }

        return ((Group) o).getName().equals(this.getName());
    }

    @Override
    public boolean containsTrustee(Trustee trustee) {
        if (this.getSID().equals(trustee.getSID())) {
            return true;
        }
        if (this.userSIDs.contains(trustee.getSID())) {
            return true;
        }
        for (String subGroupName : this.getSubGroups()) {
            Group subGroup = IntegratedUserManager.getInstance().getGroupForName(subGroupName);
            if (subGroup.containsTrustee(trustee)) {
                return true;
            }
        }
        return false;
    }
}
