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
 * Interface for User, Groups, etc. everyone who can have access rights to Secureable objects
 * @author Uwe
 */
public interface Trustee {
    
    /**
     * 
     * @return the unique Sequrity ID identifying this User/Group etc.
     */
    Integer getSID();
    
    /**
     * 
     * @param trustee
     * @return true if the given trustee is this trustee or belongs to this trustee (group)
     */
    boolean containsTrustee(Trustee trustee);
    
    
}
