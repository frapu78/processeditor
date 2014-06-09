/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.domainModel.DomainModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.validation.ModelValidator;
import com.inubit.research.validation.ValidationMessage;
import com.inubit.research.validation.Validator;
import com.inubit.research.validation.bpmn.BPMNValidator;
import com.inubit.research.validation.domainModel.DomainModelValidator;

/**
 *
 * @author tmi
 * @author fel
 */
public class ValidationPlugin extends InfoDialogServerPlugin implements ModelScope {

    private static final String URL_ICON_VALIDATE = "/pics/symbols/tick.png";
    private static final String URL_ICON_ERROR = "/pics/symbols/error16x16.png";
    private static final String URL_ICON_WARNING = "/pics/symbols/warning16x16.png";
    private static final String URL_ICON_INFO = "/pics/symbols/information16x16.png";
    private static Map<Class<? extends ProcessModel>, ModelValidator> supportedModels = new HashMap<Class<? extends ProcessModel>, ModelValidator>();
    private static ValidationPlugin instance;
    
    static {
        addSupportedModel(DomainModel.class, DomainModelValidator.getInstance());
        addSupportedModel(BPMNModel.class, BPMNValidator.getInstance());
    }
    
    public ValidationPlugin() {
        super();
        this.jsFiles = new String[]{"Inubit.WebModeler.plugins.ValidationInfoDialog"};
        this.mainClassName = "Inubit.WebModeler.plugins.ValidationInfoDialog";
    }
    
    @Override
    protected JSONObject getData(ModelInformation mi, RequestFacade req, LoginableUser u) throws JSONException {
        Validator validator = new Validator(mi.getProcessModel(), supportedModels);
        List<ValidationMessage> result = validator.getAllMessages();
        
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        
        int idCounter = 0;
        for (ValidationMessage message : result) {
            JSONArray a = new JSONArray();
            a.put("validationResult" + (idCounter++));
            String imageUrl;
            switch (message.getType()) {
                case ValidationMessage.TYPE_ERROR:
                    imageUrl = URL_ICON_ERROR;
                    break;
                case ValidationMessage.TYPE_WARNING:
                    imageUrl = URL_ICON_WARNING;
                    break;
                default:
                    imageUrl = URL_ICON_INFO;
                    break;
            }
            a.put("<img src=\"" + contextifyUri(imageUrl, req) + "\" alt=\""
                    + message.getTypeString() + "\" title=\""
                    + message.getTypeString() + "\"/>");
            a.put(message.getShortDescription());
            a.put(message.getDescription());
            a.put(new JSONArray(message.getRelatedNodeIDs()));
            a.put(new JSONArray(message.getRelatedEdgeIDs()));
            a.put(message.getTypeString());
            ja.put(a);
        }
        
        jo.put("data", ja);
        return jo;
    }
    
    @Override
    protected String getItemText() {
        return "Validate Model";
    }
    
    @Override
    public boolean showInToolbar() {
        return true;
    }
    
    @Override
    protected String getItemIconPath() {
        return URL_ICON_VALIDATE;
    }
    
    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        //return supportedModels;
        return supportedModels.keySet();
    }
    
    public static void addSupportedModel(Class<? extends ProcessModel> cls, ModelValidator val) {
        supportedModels.put(cls, val);
    }
    
    private String contextifyUri(String uri, RequestFacade req) {
        if (req != null && !req.getContext().equals("/")) {
            return req.getContext() + uri;
        }
        return uri;
    }
    
    public static ValidationPlugin getInstance() {
        if (instance == null) {
            instance = new ValidationPlugin();
        }
        return instance;
    }
}
