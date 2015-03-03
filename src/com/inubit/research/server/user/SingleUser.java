/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.inubit.research.server.user;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.manager.Location;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fel
 */
public class SingleUser extends LoginableUser {
    private String pwd;
    protected boolean admin;

    protected String picId;

    Set<ISLocation> isConnections = new HashSet<ISLocation>();

    public SingleUser ( String name, String pwd ) {
        this.name = name;
        this.pwd = pwd;
        this.admin = false;
    }

    public SingleUser ( String name, String pwd, boolean admin ) {
        this.name = name;
        this.pwd = pwd;
        this.admin = admin;
    }

    protected SingleUser() {}

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public boolean isAdmin() {
        return this.admin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }

    public Set<ISLocation> getISConnections() {
        return this.isConnections;
    }

    public void addISConnection( ISLocation ism ) {
        this.isConnections.add(ism);
    }

    public void removeISConnection( ISLocation ism ) {
        this.isConnections.remove(ism);
    }

    public void setPictureId(String path) {
        this.picId = path;
    }

    public String getPictureId() {
        return this.picId;
    }

    public boolean isAllowedToSaveToFileSystem() {
        return true;
    }

    public Location getHomeLocation() {
        return null;
    }

    @Override
    public boolean isSingleUser() {
        return true;
    }

    @Override
    public UserType getUserType() {
        return UserType.SINGLE_USER;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SingleUser))
            return false;

        return ((SingleUser) o).getName().equals(this.getName());
    }

}
