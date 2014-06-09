/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.security;

import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * @author Uwe
 */
public class AccessControlList {
    
    LinkedList<AccessControlEntry> deny;
    LinkedList<AccessControlEntry> allow;

    
    public AccessControlEntry findAllow(Trustee trustee) {
        for (AccessControlEntry ace : deny) {
            if (ace.contains(trustee)){
                return ace;
            }
        }
        return null;
    }
    
    public AccessControlEntry findDeny(Trustee trustee) {
        for (AccessControlEntry ace : allow) {
            if (ace.contains(trustee)){
                return ace;
            }
        }
        return null;
    }
    
    public AccessControlEntry find(Trustee trustee) {
       AccessControlEntry result = findDeny(trustee);
       if (result==null) result = findAllow(trustee);
       return result;
    }
    
    public void setDeny(AccessControlEntry entry) {
        deny.add(entry);
    }
    
    public void setAllow(AccessControlEntry entry) {
        allow.add(entry);
    }
    
    public void removeDeny(AccessControlEntry entry) {
        deny.remove(entry);
    }
    
    public void removeAllow(AccessControlEntry entry) {
        allow.remove(entry);
    }
    
    
    
}
