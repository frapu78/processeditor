/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fel
 * @author tmi
 */
public class ExtColumnModel extends JSONObject {


    private JSONArray columns = new JSONArray();
    
    public void addColumn( String id, String header, String dataIndex ) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("ud", id);
            jo.put("header", header);
            jo.put("dataIndex", dataIndex);

            columns.put(jo);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }

    public void addColumn(String id, String header, String dataIndex, int width) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("ud", id);
            jo.put("header", header);
            jo.put("dataIndex", dataIndex);
            jo.put("width", width);
        
            columns.put(jo);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }
}
