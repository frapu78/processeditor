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
 * @author fel
 */
public class ExtPasswordField extends ExtFormSimpleComponent {

   private static final String XTYPE = "textfield";

    public ExtPasswordField() throws JSONException {
        super();
        this.put(ExtJSProperty.INPUT_TYPE.getName(), "password");
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }

}
