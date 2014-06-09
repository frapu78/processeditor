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
public class ExtTextField extends ExtFormSimpleComponent {

   private static final String XTYPE = "textfield";

   public ExtTextField() throws JSONException {
       super();
   }

    @Override
    protected String getXType() {
        return XTYPE;
    }

}
