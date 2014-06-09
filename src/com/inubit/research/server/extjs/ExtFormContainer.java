/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author fel
 */
public abstract class ExtFormContainer extends ExtFormComponent {

    protected JSONArray items;

    public ExtFormContainer () throws JSONException {
        this.items = new JSONArray();
        this.setDefaults();
    }

    public ExtFormContainer( JSONArray items) throws JSONException {
        super();
        this.items = items;
        this.setDefaults();
    }

    public void addItem( ExtFormComponent cmp ) {
        if (cmp != null)
            this.items.put(cmp);
    }

    protected abstract String getXType();

    private void setDefaults() throws JSONException {
        this.put("xtype", this.getXType());
        this.put("items", this.items);
    }

//    public String toString() {
//        StringBuilder b = new StringBuilder(500);
//
//        b.append("{");
//
//        b.append(this.getPropertyJSON());
//
//        b.append("xtype:'");
//        b.append(this.getXType());
//        b.append("',");
//
//        b.append("items: ");
//
//        b.append(this.items);
//
//        b.append("}");
//
//        return b.toString();
//    }
}
