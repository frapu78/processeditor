/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.security;

import com.inubit.research.security.SecurityDescriptor.AccessRight;
import java.util.EnumSet;

/**
 *
 * @author Uwe
 */
public class AccessControlEntry {

    private final Trustee trustee;
    private EnumSet<SecurityDescriptor.AccessRight> accessMask;

    public AccessControlEntry(Trustee trustee) {
        this.trustee = trustee;
        this.accessMask = EnumSet.noneOf(SecurityDescriptor.AccessRight.class);
    }

    public AccessControlEntry(Trustee trustee, EnumSet<AccessRight> accessMask) {
        this.trustee = trustee;
        this.accessMask = accessMask;
    }   

    public final Trustee getTrustee() {
        return trustee;
    }

    public final boolean contains(Trustee trustee) {
        return this.getTrustee().containsTrustee(trustee);
    }

    public EnumSet<AccessRight> getAccessMask() {
        return accessMask;
    }
}
