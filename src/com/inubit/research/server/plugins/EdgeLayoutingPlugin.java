/**
 *
 * Process Editor
 *
 * (C) 2015 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.handler.ModelRequestHandler;
import com.inubit.research.server.user.LoginableUser;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.RoutingPointLayouter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by frank on 27.03.15.
 */
public class EdgeLayoutingPlugin extends SimpleServerPlugin implements ModelScope {

    private boolean edgeLayoutingIsOn = true;

    @Override
    void performAction(ModelInformation modelInfo, RequestFacade req, ResponseFacade resp, LoginableUser u) throws IOException, JSONException {
        JSONObject jo = new JSONObject();
        jo.put("success", true);
        jo.put("action", PluginResponseType.UPDATE);

        ModelDifferenceTracker mdt = new ModelDifferenceTracker( modelInfo.getProcessModel() );
        modelInfo.getProcessModel().addListener(mdt);

        // Check whether a RoutingPointLayouter exists as Listener
        List<ProcessModelListener> listener = modelInfo.getProcessModel().getListeners();
        RoutingPointLayouter rpl = null;
        for (ProcessModelListener l: listener) {
            if (l instanceof RoutingPointLayouter) rpl = (RoutingPointLayouter)l;
        }
        // Remove listener if found, add if not
        if (rpl == null) {
            // Add new one
            modelInfo.getProcessModel().addListener(modelInfo.getProcessModel().getUtils().getRoutingPointLayouter());
            edgeLayoutingIsOn = true;
        } else {
            // Remove
            modelInfo.getProcessModel().removeListener(rpl);
            edgeLayoutingIsOn = false;
        }

        modelInfo.getProcessModel().removeListener(mdt);
        jo.put("data", mdt.toJSON( ModelRequestHandler.getAbsoluteAddressPrefix(req) ) );

        ResponseUtils.respondWithJSON(resp, jo, 200);
    }

    @Override
    protected String getItemText() {
        return "Toggle Edge Layouting";
    }

    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        return SUPPORT_ALL;
    }
}
