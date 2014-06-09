/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import org.json.JSONException;
import org.json.JSONObject;

import com.inubit.research.server.extjs.JavaScriptFunction;
import com.inubit.research.server.request.RequestFacade;

public class RootPageLinkPlugin {
	
	private String link;
	private String text;
	private String iconPath = null;
	
	public RootPageLinkPlugin( String text, String link ) {
		this.text = text;
		this.link = link;
	}
	
	public RootPageLinkPlugin( String text, String link, String iconPath ) {
		this( text, link );
		this.iconPath = iconPath;
	}
	
	
	private String getIconUri( RequestFacade req ) {
        String iconUri = null;
        if (this.iconPath != null) {
            iconUri = this.iconPath;
            if ( req != null && !req.getContext().equals("/") )
                iconUri = req.getContext() + iconUri;
        }
        return iconUri;
	}
	
	private String getLinkPath( RequestFacade req ) {
		String uri = this.link;
		if ( req != null && !req.getContext().equals("/") )
            uri = req.getContext() + uri;
		return uri;
	}
	
	
	public JSONObject getJSONConfig( RequestFacade req ) throws JSONException {
		JSONObject jo = new JSONObject();
		
		jo.put("text", this.text);
		jo.put("icon", getIconUri(req));
		jo.put("handler", getHandlerFunction(req));
		
		return jo;
	}
	
	private JavaScriptFunction getHandlerFunction( RequestFacade req ) {
		return new JavaScriptFunction("function(){window.location = '" + this.getLinkPath(req) + "';}");
	}
}
