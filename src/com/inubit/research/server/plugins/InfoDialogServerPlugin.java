/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation based on deprecated class InfoFormServerPlugin, originally implemented by tmi.
 * @author fel
 */
public abstract class InfoDialogServerPlugin extends DialogServerPlugin {

    public InfoDialogServerPlugin() {
        super();
        this.type = ProcessEditorServerPluginType.INFODIALOG;
    }

    @Override
    protected JSONObject saveData(JSONArray data, ModelInformation mi) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("success", true);
        return jo;
    }
}
