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
 * @author cab
 */
public class ExtRadioButtonGroup extends ExtFormContainer {
    private static final String XTYPE = "radiogroup";

    public ExtRadioButtonGroup () throws JSONException {
        super();
    }
    
    public ExtRadioButtonGroup ( JSONArray items ) throws JSONException{
        super(items);
    }

    
    @Override
    protected String getXType() {
        return XTYPE;
    }

}