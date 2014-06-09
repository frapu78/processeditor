/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Plugin that is represented by a simple button. Plugins of this type have no sub-URLs.
 * They receive information about the current model and the current selection within this model, and compute their result
 * based on that information.
 *
 * @author fel
 */
public abstract class SimpleServerPlugin extends ServerPlugin {

    protected SimpleServerPlugin( ) {
        super();
        this.type = ProcessEditorServerPluginType.SIMPLE;
    }

    @Override
    public void processRequest(String requestUri, RequestFacade req, ResponseFacade resp, LoginableUser u) throws IOException {
        try {    
            //it is assumed that only POST requests are valid
            if (!req.getRequestMethod().equals("POST")) {
                JSONObject err = new JSONObject();
                err.put("errormsg", "Request method not valid within this context.");
                err.put("action", PluginResponseType.ERROR.toString());
                ResponseUtils.respondWithJSON(resp, err, 400);
                return;
            }

        
            JSONObject jo = RequestUtils.getJSON(req);

            ModelInformation modelInfo = ModelInformation.forJSON(jo);

            if (modelInfo == null) {
                JSONObject err = new JSONObject();
                err.put("msg", "Model ID not specified in request JSON");
                err.put("action", PluginResponseType.ERROR.toString());
                ResponseUtils.respondWithJSON(resp, err, 400);
                return;
            }
            
            this.performAction(modelInfo, req, resp, u);

        } catch (JSONException ex) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", PluginResponseType.ERROR.toString());
            map.put("errormsg", "Error while parsing JSON from request: " + ex.getMessage());
            JSONObject jo = new JSONObject(map);
            ResponseUtils.respondWithJSON(resp, jo, 500);
        }
    }

    abstract void performAction (
            ModelInformation modelInfo,
            RequestFacade req,
            ResponseFacade resp, 
            LoginableUser u)  throws IOException, JSONException ;

}
