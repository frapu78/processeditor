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
 *
 * @author Uwe
 */
public interface SecurableSetable extends Securable {

    public void setSecurityDescriptor(SecurityDescriptor securityDescriptor);
}
