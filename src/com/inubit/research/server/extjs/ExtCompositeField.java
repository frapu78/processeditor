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

/**
 *
 * @author cab
 */
public class ExtCompositeField extends ExtFormContainer{

    private static final String XTYPE = "fieldset";

    public ExtCompositeField() throws JSONException {
        super();
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }
}
