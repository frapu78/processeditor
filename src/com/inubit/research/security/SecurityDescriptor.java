/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.security;

import com.inubit.research.server.user.User;
import java.util.HashSet;

/**
 *
 * Grants or denies access to a Securable object by a Trustee
 * 
 * @author Uwe
 */
public class SecurityDescriptor {

    public enum AccessRight {

        READ, WRITE, CREATE, DELETE
    }
    
    private AccessControlList accessControlList;
    private HashSet<SecurityDescriptor> parents;

    public SecurityDescriptor() {
        this.accessControlList = null;
        this.parents = null;
    }

    public SecurityDescriptor(AccessControlList accessControlList) {
        this.accessControlList = accessControlList;
        this.parents = null;
    }
    
    public SecurityDescriptor(Securable parent) {
        this.parents = new HashSet<SecurityDescriptor>();
        this.parents.add(parent.getSecurityInfo());
    }

    public SecurityDescriptor(AccessControlList accessControlList, SecurityDescriptor parent) {
        this.accessControlList = accessControlList;
        this.parents = new HashSet<SecurityDescriptor>();
        this.parents.add(parent);
    }

    public void grantRight(Trustee user, AccessRight right) {
        if (getAccessControlList() == null) {
            setAccessControlList(new AccessControlList());
        }
        AccessControlEntry allow = getAccessControlList().findAllow(user);
        if (allow == null) {
            allow = new AccessControlEntry(user);
        }
        allow.getAccessMask().add(right);
        AccessControlEntry deny = getAccessControlList().findDeny(user);
        if (deny != null) {
            deny.getAccessMask().remove(right);
        }
        if (deny.getAccessMask().isEmpty()) {
            getAccessControlList().removeDeny(deny);
        }
    }

    public void denyRight(Trustee user, AccessRight right) {
        if (getAccessControlList() == null) {
            setAccessControlList(new AccessControlList());
        }
        AccessControlEntry deny = getAccessControlList().findDeny(user);
        if (deny == null) {
            deny = new AccessControlEntry(user);
        }
        deny.getAccessMask().add(right);
        AccessControlEntry allow = getAccessControlList().findAllow(user);
        if (allow != null) {
            allow.getAccessMask().remove(right);
        }
        if (allow.getAccessMask().isEmpty()) {
            getAccessControlList().removeAllow(allow);
        }
    }

    public void setAllowAll() {
        accessControlList = null;
    }

    public void setAllowNoone() {
        accessControlList = new AccessControlList();
    }

    public boolean isAccessible(User user, AccessRight right) {
        if (accessControlList == null) {
            return true;
        }
        AccessControlEntry deny = getAccessControlList().findDeny(user);
        if (deny != null && deny.getAccessMask().contains(right)) {
            return false;
        }
        AccessControlEntry allow = getAccessControlList().findAllow(user);
        if (allow != null && allow.getAccessMask().contains(right)) {
            return true;
        }
        if (parents != null && !parents.isEmpty()) {
            boolean result = true;
            for (SecurityDescriptor parent : parents) {
                result = result && parent.isAccessible(user, right);
            }
            return result; 
        }
        return false;
    }

    public AccessControlList getAccessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(AccessControlList accessControlList) {
        this.accessControlList = accessControlList;
    }
    
    public void addParent(SecurityDescriptor desc) {
        parents.add(desc);
    }

}
