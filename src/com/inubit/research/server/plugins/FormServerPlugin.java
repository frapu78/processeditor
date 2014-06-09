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
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.multipart.SimpleMultipartParser;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.user.LoginableUser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author fel
 */
public abstract class FormServerPlugin extends ServerPlugin {
    protected static final String MODEL_INFO = "_modelinfo_";

    public FormServerPlugin() {
        super();
        this.type = ProcessEditorServerPluginType.FORM;
    }

    public void processRequest(String requestUri, RequestFacade req, ResponseFacade resp, LoginableUser u) throws IOException {
        try {
            if (req.getRequestURI().endsWith("?form")) {
                this.processFormRequest(req, resp, u);
            } else {
                this.processPostRequest(req, resp, u);
            }
        } catch ( JSONException ex ) {
            ex.printStackTrace();
            throw new IOException(ex);
        }
    }

    protected void processFormRequest( RequestFacade req, ResponseFacade resp, LoginableUser u ) throws JSONException, IOException {
        JSONObject jo;
        try {
            jo = RequestUtils.getJSON(req);
        } catch (Exception ex) {
            ex.printStackTrace();
            jo = null;
        }
        
        if (jo == null) {
            ResponseUtils.respondWithStatus(500, "Error while parsing JSON.", HttpConstants.CONTENT_TYPE_TEXT_PLAIN, resp, false);
            return;
        }

        ModelInformation mi = ModelInformation.forJSON(jo);

        try {
            JSONObject form = new JSONObject();
            form.put("success", true);
            form.put("form", this.getFormConfig(mi, req, u));
            ResponseUtils.respondWithJSON(resp, form, 200);
        } catch (FormGenerationException ex) {
            ResponseUtils.respondWithStatus(500, ex.getMessage(), HttpConstants.CONTENT_TYPE_TEXT_PLAIN, resp, false);
        } 
    }

    protected void processPostRequest( RequestFacade req, ResponseFacade resp , LoginableUser u ) throws IOException {
        String modelInfo;
        MultiPartObject mpo = null;
        Map<String, String> params = null;
        
        if ( req.getHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE).contains(HttpConstants.CONTENT_TYPE_APPLICATION_WWW_FORM_URLENCODED) ) {
            params = this.parseURLEncodedParams( RequestUtils.getContent(req) );
            modelInfo = params.get(MODEL_INFO);
        } else {
            mpo = new SimpleMultipartParser().parseSource(req.getInputStream());
            modelInfo = mpo.getItemByName(MODEL_INFO).getContent();
        }

        try {
            JSONTokener jt = new JSONTokener( new InputStreamReader(new ByteArrayInputStream(modelInfo.getBytes())));
            JSONObject json = new JSONObject(jt);
            ModelInformation mInfo = ModelInformation.forJSON(json);

            if (mInfo == null) {
                JSONObject jo = new JSONObject();
                jo.put("success", false);
                jo.put("action", PluginResponseType.ERROR);
                jo.put("errormsg", "Model ID not specified in request XML");
                ResponseUtils.respondWithJSONAsText(resp, jo, 400);
                return;
            }
            
            if ( mpo != null )
                this.performMultipartFormAction(req, resp, mpo, mInfo, u);
            else
                this.performURLEncodedFormAction(req, resp, params, mInfo, u);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> map = new HashMap();
            map.put("success", "false");
            map.put("action", PluginResponseType.ERROR.toString());
            map.put("errormsg", "Error while parsing XML.\n" + e.getMessage());
            JSONObject jo = new JSONObject(map);
            ResponseUtils.respondWithJSONAsText(resp, jo, 500);
        }
    }
    
    protected Map<String, String> parseURLEncodedParams( String s ) {
        Map<String, String> params = new HashMap<String, String>();
        String[] parts = s.split("&");
        
        for ( String part : parts ) 
            params.put(part.split("=")[0], ProcessEditorServerUtils.unEscapeString(part.split("=")[1]));
        
        return params;
    }

    abstract JSONObject getFormConfig (
            ModelInformation mi,
            RequestFacade req,
            LoginableUser u ) throws FormGenerationException;

    abstract void performMultipartFormAction (
            RequestFacade req,
            ResponseFacade resp,
            MultiPartObject mpo,
            ModelInformation modelInfo,
            LoginableUser u ) throws IOException, JSONException;
    
    abstract void performURLEncodedFormAction (
            RequestFacade req,
            ResponseFacade resp,
            Map<String, String> params,
            ModelInformation modelInfo,
            LoginableUser u ) throws IOException, JSONException;
}