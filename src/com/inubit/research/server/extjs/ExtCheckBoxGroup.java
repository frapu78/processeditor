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

/**
 *
 * @author fel
 */
public class ExtCheckBoxGroup extends ExtFormContainer {
    private static final String XTYPE = "checkboxgroup";

    public ExtCheckBoxGroup () throws JSONException {
        super();
    }
    
    public ExtCheckBoxGroup ( JSONArray items ) throws JSONException {
        super(items);
    }

    
    @Override
    protected String getXType() {
        return XTYPE;
    }

}
