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
import com.inubit.research.server.extjs.ExtJSForm;
import com.inubit.research.server.extjs.ExtJSProperty;
import com.inubit.research.server.multipart.MultiPartObject;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 *
 * @author tmi
 */
@Deprecated
public abstract class InfoFormServerPlugin extends FormServerPlugin {

    public InfoFormServerPlugin() {
        super();
//        this.type = ProcessEditorServerPluginType.INFOFORM;
    }

    protected class Response {
        public ExtJSForm form;
        public String message;
        public ResponseType type;

        public Response(ExtJSForm form) {
            this.type = ResponseType.Form;
            this.form = form;
            this.message = "";
        }

        public Response(String message) {
            this.type = ResponseType.Message;
            this.form = null;
            this.message = message;
        }
    }

    protected enum ResponseType {
        Form,
        Message;
    }

    @Override
    void performMultipartFormAction (
            RequestFacade req,
            ResponseFacade resp,
            MultiPartObject mpo,
            ModelInformation modelInfo,
            LoginableUser u ) throws IOException {
        ResponseUtils.respondWithStatus(200, "{ success: true}",
                HttpConstants.CONTENT_TYPE_TEXT_HTML, resp, false);
    }

    @Override
    JSONObject getFormConfig(ModelInformation mi, RequestFacade req,
            LoginableUser u) throws FormGenerationException {
//        StringBuilder buffer = new StringBuilder();
//        buffer.append("{");
//        Response response = getResponse(mi, req, u);
//        buffer.append("responseType: '").append(response.type).append("',");
//        if (response.type.equals(ResponseType.Form)) {
//            response.form.setProperty(ExtJSProperty.CLOSABLE, true);
//            buffer.append("form: ").append(response.form.getJSONString()).append(", ");
//        } else if (response.type.equals(ResponseType.Message)) {
//            buffer.append("message: '").append(response.message).append("',");
//        }
//        buffer.append("title: '").append(getFormTitle()).append("'");
//        buffer.append("}");
//        return buffer.toString();
        return null;
    }

    abstract Response getResponse(ModelInformation mi, RequestFacade req,
            LoginableUser u) throws FormGenerationException;
    abstract String getFormTitle();
}