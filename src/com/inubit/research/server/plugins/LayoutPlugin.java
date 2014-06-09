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
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.handler.ModelRequestHandler;
import com.inubit.research.server.request.handler.util.ProcessModelUtils;
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author fel
 */
public class LayoutPlugin extends SimpleServerPlugin implements ModelScope {

    public LayoutPlugin() {
        super();
        this.scope = PluginScope.MODEL;
    }

    @Override
    void performAction( ModelInformation modelInfo, RequestFacade req, ResponseFacade resp, LoginableUser u ) throws IOException, JSONException {
        ProcessModelUtils utils = new ProcessModelUtils(modelInfo.getProcessModel());

        JSONObject jo = new JSONObject();
        jo.put("success", true);
        jo.put("action", PluginResponseType.UPDATE);

        ModelDifferenceTracker mdt = new ModelDifferenceTracker( modelInfo.getProcessModel() );
        modelInfo.getProcessModel().addListener(mdt);
        utils.layout( );
        modelInfo.getProcessModel().removeListener(mdt);
        jo.put("data", mdt.toJSON( ModelRequestHandler.getAbsoluteAddressPrefix(req) ) );

        ResponseUtils.respondWithJSON(resp, jo, 200);
    }

    @Override
    public boolean showInToolbar() {
        return true;
    }

    @Override
    protected String getItemText() {
        return "Layout";
    }

    @Override
    protected String getItemIconPath() {
        return "/pics/menu/icon_16x16_auto-ausrichten.gif";
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        return SUPPORT_ALL;
    }
}

