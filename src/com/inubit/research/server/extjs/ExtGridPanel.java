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
public class ExtGridPanel extends ExtFormSimpleComponent {
    private static final String XTYPE = "grid";

    private ExtArrayStore store;
    private ExtColumnModel colModel;

    ExtGridPanel( ExtArrayStore store, ExtColumnModel model ) throws JSONException {
        super();
        
        this.store = store;
        this.colModel = model;

        this.put("store", this.store);
        this.put("columns", colModel);
        this.put(ExtJSProperty.STRIPE_ROWS.getName(), true);
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }

//    @Override
//    public String getPropertyJSON() {
//        StringBuffer b = new StringBuffer(super.getPropertyJSON());
//
//        b.append("store:");
//        b.append(store.toString());
//        b.append(",columns:");
//        b.append(colModel.toString());
//        b.append(",");
//
//        return b.toString();
//    }
}
