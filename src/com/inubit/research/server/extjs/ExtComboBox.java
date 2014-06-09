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
public class ExtComboBox extends ExtFormSimpleComponent {

    private static final String XTYPE = "combo";

    public ExtComboBox() throws JSONException {
        super();
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }
}
