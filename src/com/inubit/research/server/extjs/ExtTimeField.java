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
public class ExtTimeField extends ExtTextField {

   private static final String XTYPE = "timefield";

   public ExtTimeField() throws JSONException {
       super();
   }

    @Override
    protected String getXType() {
        return XTYPE;
    }

}
