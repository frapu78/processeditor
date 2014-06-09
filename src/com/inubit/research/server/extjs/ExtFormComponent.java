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
 * @author fel
 */
public abstract class ExtFormComponent extends JSONObject {
//    protected Properties properties;

    public ExtFormComponent () {
//        this.properties = new Properties();
    }

    public void setProperty( ExtJSProperty prop, Object value ) {
        try {
            this.put(prop.getName(), value);
        } catch ( JSONException ex ) {
            ex.printStackTrace();
        }
    }
}
