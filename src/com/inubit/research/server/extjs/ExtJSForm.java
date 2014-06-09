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
 *
 * @author fel
 */
public class ExtJSForm extends JSONObject {

    private JSONArray items;

    public ExtJSForm () throws JSONException {
        this.items = new JSONArray();
        this.setDefaults();
    }

    public ExtJSForm ( JSONArray items ) throws JSONException {
        this.items = items;
        this.setDefaults();
    }

    public void setProperty( ExtJSProperty prop, Object value ) {
        try {
            this.put(prop.getName(), value);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
//        if (prop.getType().equals(ExtJSProperty.ExtJSPropertyType.STRING))
//            valueString = "'" + (String) value + "'";
//        else
//            valueString = value.toString();
//
//        this.properties.setProperty(prop.getName(), valueString);
    }

    public void addItem( ExtFormComponent cmp ) {
        if (cmp != null)
            this.items.put(cmp);
    }

    private void setDefaults() throws JSONException {
        this.put("xtype", "form");
        this.put("fileUpload", true);
        this.put("items", this.items);
    }
}
