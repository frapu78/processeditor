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
 * @author Felix
 */
public class ExtFieldSet extends ExtFormContainer {
    private static final String XTYPE = "fieldset";

    public ExtFieldSet() throws JSONException {
        super();
    }

    public ExtFieldSet ( JSONArray items ) throws JSONException {
        super(items);
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }
}
