/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.security;

/**
 * Should be implemented by every Object that should be secured by Access Rights of the AccessControlList
 * @author Uwe
 */
public interface Securable {
    
    public SecurityDescriptor getSecurityInfo();
    
}
