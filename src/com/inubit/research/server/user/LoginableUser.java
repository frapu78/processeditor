/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.user;

/**
 *
 * @author fel
 */
public abstract class LoginableUser extends User {
    protected String realName;
    protected String mail;

    public void setRealName(String realName) {
        this.realName  = realName;
    }

    public String getRealName() {
        return this.realName;
    }

    public void setMail( String newMail ){
        this.mail = newMail;
    }

    public String getMail() {
        return this.mail;
    }

    public boolean isAdmin() {
        return false;
    }
}
