/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.user;

import com.inubit.research.security.Trustee;

/**
 *
 * @author fel
 */
public abstract class User implements Trustee{

    public enum UserType {
        SINGLE_USER,
        LIMITED_USER,
        GROUP
    }

    protected String name;

    public boolean isSingleUser() {
        return false;
    }

    public boolean isGroup() {
        return false;
    }

    public boolean isTemporaryUser() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public Integer getSID() {
        return getName().hashCode();
    }

    @Override
    public boolean containsTrustee(Trustee trustee) {
        return getSID().equals(trustee.getSID());
    }    

    public abstract UserType getUserType();
}
