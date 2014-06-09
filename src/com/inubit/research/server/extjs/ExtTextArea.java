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
 * @author tmi
 */
public class ExtTextArea extends ExtFormSimpleComponent {

    private static final String XTYPE = "textarea";

    public ExtTextArea() throws JSONException {
        super();
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }

}
