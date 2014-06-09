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
import com.inubit.research.server.user.LoginableUser;
import java.io.IOException;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class aligns a selection of nodes vertically.
 * @author fpu
 */
public class VerticalAlignmentPlugin extends SimpleServerPlugin implements ModelScope {

    @Override
    void performAction(ModelInformation modelInfo, RequestFacade req, ResponseFacade resp, LoginableUser u) throws IOException, JSONException {
        //ProcessModelUtils utils = new ProcessModelUtils(modelInfo.getProcessModel());

        JSONObject jo = new JSONObject();
        jo.put("success", true);
        jo.put("action", PluginResponseType.UPDATE);

        ModelDifferenceTracker mdt = new ModelDifferenceTracker( modelInfo.getProcessModel() );
        modelInfo.getProcessModel().addListener(mdt);

        int xPos = Integer.MAX_VALUE;
        // Layout selection here
        for (String nodeId: modelInfo.getSelNodeIDs()) {
            ProcessNode node = modelInfo.getProcessModel().getNodeById(nodeId);
            if (xPos == Integer.MAX_VALUE) {
                xPos = node.getPos().x;
            } else {
                node.setPos(xPos, node.getPos().y);
            }
        }

        modelInfo.getProcessModel().removeListener(mdt);
        jo.put("data", mdt.toJSON( ModelRequestHandler.getAbsoluteAddressPrefix(req) ) );

        ResponseUtils.respondWithJSON(resp, jo, 200);
    }

    @Override
    protected String getItemText() {
        return "Align Selection Horizontally";
    }

    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        return SUPPORT_ALL;
    }

}
