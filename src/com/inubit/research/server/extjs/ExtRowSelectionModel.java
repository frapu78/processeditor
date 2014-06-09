/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author tmi
 */
public class ExtRowSelectionModel extends JSONObject {

    private boolean singleSelect = false;
    private JSONObject listeners = new JSONObject();

    public ExtRowSelectionModel() throws JSONException {
        super();
        this.put("singleSelect", singleSelect);
        this.put("listeners", listeners);
    }

    public void addListener(String event, String handlerCode) {
        try {
            listeners.put(event, handlerCode);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }

    public void setSingleSelect() {
        singleSelect = true;
        try {
            this.put("singleSelect", singleSelect);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }

    public void setMultipleSelect() {
        singleSelect = false;
        try {
            this.put("singleSelect", singleSelect);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }
}
