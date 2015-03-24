/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.handler.ModelRequestHandler;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author fel
 */
public abstract class DialogServerPlugin extends ServerPlugin {

    protected String[] jsFiles = {};
    protected String mainClassName = "";

    public DialogServerPlugin() {
        super();
        this.type = ProcessEditorServerPluginType.DIALOG;
    }

    @Override
    public void processRequest(String requestUri, RequestFacade req, ResponseFacade resp, LoginableUser u) throws IOException {
        //requestUri = /plugins/<id>(PATH) --> omit /plugins/<id>
        Pattern p = Pattern.compile( "(.?/plugins/\\d+)(.*)"  );
        Matcher m = p.matcher(requestUri);
        String path = requestUri;
        if ( m.find() ) 
            path = m.group(2);
        
        if ( path.matches("/js(\\?.+?)?") )
            respondWithFileList(resp);
        else if ( path.startsWith("/js/") )
            ResponseUtils.respondWithServerResource(HttpConstants.CONTENT_TYPE_TEXT_JAVASCRIPT, path, resp, Boolean.FALSE);
        else if ( path.equals("/load") )
            load(req, resp, u);
        else if ( path.equals("/save") )
            save(req, resp, u);
    }

    protected void respondWithFileList( ResponseFacade resp ) throws IOException {
        JSONObject jo = new JSONObject();
        try {
            jo.put("success", true);
            jo.put("files", new JSONArray(this.getJSFileList()));
            jo.put("mainclass", mainClassName);
        }  catch (JSONException ex) {
            ex.printStackTrace();
        }
        ResponseUtils.respondWithJSON(resp, jo, 200);
    }

    protected abstract JSONObject getData( ModelInformation mi, RequestFacade req, LoginableUser u ) throws JSONException;

    protected abstract JSONObject saveData( JSONArray data, ModelInformation mi ) throws JSONException;

    protected String[] getJSFileList() {
        return this.jsFiles;
    }

    private void load( RequestFacade req, ResponseFacade resp, LoginableUser u ) throws IOException {
        try {
            JSONObject miJSON = RequestUtils.getJSON(req);
            ModelInformation mi = ModelInformation.forJSON(miJSON);
            JSONObject storeData = this.getData(mi, req, u);
            storeData.put("success", true);
            ResponseUtils.respondWithJSON(resp, storeData, 200);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            Map<String, String> map = new HashMap();
            map.put("success", "false");
            map.put("action", PluginResponseType.ERROR.toString());
            map.put("errormsg", ex.getMessage());
            JSONObject jo = new JSONObject(map);
            ResponseUtils.respondWithJSON(resp, jo, 500);
        } catch ( Exception ex ) {
            ex.printStackTrace();
            Map<String, String> map = new HashMap();
            map.put("success", "false");
            map.put("action", PluginResponseType.ERROR.toString());
            map.put("errormsg", ex.getMessage());
            JSONObject jo = new JSONObject(map);
            ResponseUtils.respondWithJSON(resp, jo, 400);
        }
    }

    private void save( RequestFacade req, ResponseFacade resp, LoginableUser u ) throws IOException {
        try {
            JSONObject reqJSON = RequestUtils.getJSON(req);
            ModelInformation mi = ModelInformation.forJSON(reqJSON.getJSONObject("mi"));
            JSONArray data = reqJSON.getJSONArray("data");
            ModelDifferenceTracker mdt = new ModelDifferenceTracker( mi.getProcessModel() );
            mi.getProcessModel().addListener(mdt);
            JSONObject response = this.saveData(data, mi);
            response.put("data", mdt.toJSON( ModelRequestHandler.getAbsoluteAddressPrefix(req) ));
            mi.getProcessModel().removeListener(mdt);
            
            ResponseUtils.respondWithJSON( resp, response, 200 );
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            Map<String, String> map = new HashMap();
            map.put("success", "false");
            map.put("action", PluginResponseType.ERROR.toString());
            map.put("errormsg", ex.getMessage());
            JSONObject jo = new JSONObject(map);
            ResponseUtils.respondWithJSON(resp, jo, 500);
        }
    }

}
