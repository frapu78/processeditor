/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.User;
import java.net.URL;

import java.util.Date;
import net.frapu.code.visualization.ProcessModel;

import com.inubit.research.server.manager.ISLocation;
import com.inubit.research.server.model.ISServerModel;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import java.util.HashSet;
import java.util.Set;


/**
 * @author ff
 *
 */
public class ISMetaDataHandler implements MetaDataHandler {

	private URL f_url;
        private String f_usr;
	private ISLocation f_isManager;

	public ISMetaDataHandler(URL serverURL, String usr, ISLocation isManager) {
		f_url = serverURL;
                f_usr = usr;
		f_isManager = isManager;
	}

	@Override
	public String getFolderAlias(String id) {
            return getFolder(id);
	}

	private String getFolder(String id) {
		String _part = f_url.toString().substring(7);

                if (id == null) {
                    return "/is/" + f_usr + "@" + _part.substring(0,_part.indexOf("/"));
                } else {
                    String group = ((ISServerModel) f_isManager.getModel(id)).getModelGroup();
                    return "/is/" + f_usr + "@" + _part.substring(0,_part.indexOf("/")) + "/" + group;
                }
	}

	@Override
	public String getVersionComment(String id, String version) {
		return f_isManager.getModel(id).getComment();
	}
	
	public String getLastCheckinDate(String id) {
            // fpu: @todo Disabled for performance (needs to be reworked)
//		ProcessModel _model =  f_isManager.getModel(id).load().getModel();
//		if(_model != null)
//			return _model.getProperty(ProcessModel.PROP_LASTCHECKIN);
		return "";
	}

	@Override
	public VersionMetaData getVersionMetaData(String id, String version) {
		return new VersionMetaData(getFolderAlias(id),getVersionComment(id, version), "unknown", getLastCheckinDate(id),this);
	}

	@Override
	public void remove(String id) {
		//not supported
	}

	@Override
	public void setFolderAlias(String id, String alias, SingleUser user) {
            if ( this.getFolderAlias(id).equals(alias) )
                return;

            ISServerModel hm = (ISServerModel) this.f_isManager.getModel(id);

            hm.save( hm.getModel(), 0, id, "changed group", alias );
	}

	@Override
	public void setVersionComment(String id, String version, String comment) {
		f_isManager.getModel(id).getModel().setProperty(ProcessModel.PROP_COMMENT,comment);
	}

    public void addComment( String modelId, ProcessObjectComment comment) {
        //do nothing by now
    }

    public Set<ProcessObjectComment> getComments( String modelId, String version, String elementId ) {
        return new HashSet<ProcessObjectComment>();
    }

    public ProcessObjectComment updateComment ( String modelId, String commentId, String newText, int validUntil ) {
        return null;
        //do nothing by now
    }

    public void resolveComment( String modelId, String commentId, String version ) {
        //do nothing by now
    }

    public void removeComment( String modeldId, String commentId ) {
        //do nothing by now
    }

    @Override
    public void setVersionDate(String id, String version, Date date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersionDate(String id, String version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void setSucceedingVersions( String id, String version, Set<String> versions ) {
        return;
    }

    @Override
    public Set<String> getSucceedingVersions( String id, String version) {
        return new HashSet<String>();
    }

    @Override
    public void setPreceedingVersions( String id, String version, Set<String> versions ) {
        return;
    }

    @Override
    public Set<String> getPreceedingVersions( String id, String version) {
        return new HashSet<String>();
    }

    public void setVersionUser(String id, String version, String user) {
        //not supported
    }

    public String getVersionUser(String id, String version) {
        return "unknown";
    }

    public AccessType getAccessability(String id, int version, LoginableUser user) {
        if (user.getName().equals(this.f_isManager.getOwner(null)))
                return AccessType.OWNER;
            else if (user.isAdmin())
                return AccessType.ADMIN;
            else
                return AccessType.NONE;
    }

    public String getOwner(String id) {
        return this.f_isManager.getOwner(id);
    }

    public Set<User> getViewers( String id ) {
            //not supported...maybe ;-)
            return new HashSet<User>();
    }

    public Set<User> getEditors( String id ) {
        //not supported...maybe ;-)
        return new HashSet<User>();
    }

    public Set<User> getAnnotators( String id ) {
        //not supported...maybe ;-)
        return new HashSet<User>();
    }

    public boolean setOwner( String id, SingleUser owner, SingleUser admin ) {
        this.f_isManager.setOwner(id, owner, admin);
        //not supported...maybe ;-)
        return false;
    }

    @Override
    public void grantRight( String id, AccessType at, Set<User> users ) {
        //not supported...maybe ;-)
    }

    @Override
    public void divestRight( String id, AccessType at, Set<User> users ) {
        //not supported...maybe ;-)
    }

}
