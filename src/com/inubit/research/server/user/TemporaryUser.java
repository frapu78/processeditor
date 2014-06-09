/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.user;

import java.util.Date;

/**
 * Class for users that are created only temporary to allow guests to make comments
 * on certain models.
 * @author fel
 */
public class TemporaryUser extends LoginableUser {

    private Date expiryDate;
    private String modelId;
    private int modelVersion = -1;

    public TemporaryUser( String name, String mail, String realName, Date expiryDate , String modelId , int version ) {
        this.name = name;
        this.mail = mail;
        this.realName = realName;
        this.expiryDate = expiryDate;
        this.modelId = modelId;
        this.modelVersion = version;
    }

    public UserType getUserType() {
        return UserType.LIMITED_USER;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public String getModelId() {
        return this.modelId;
    }

    public int getModelVersion() {
        return this.modelVersion;
    }

    @Override
    public boolean isTemporaryUser() {
        return true;
    }

}
