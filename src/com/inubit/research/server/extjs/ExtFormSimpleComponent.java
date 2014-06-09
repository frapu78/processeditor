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
public abstract class ExtFormSimpleComponent extends ExtFormComponent{

    public ExtFormSimpleComponent() throws JSONException {
        super();
        this.put("xtype", this.getXType());
    }

    protected abstract String getXType();

//    public String toString() {
//        StringBuilder b = new StringBuilder(300);
//        b.append("{");
//        b.append(this.getPropertyJSON());
//        b.append("xtype:'");
//        b.append(this.getXType());
//        b.append("'");
//        b.append("}");
//
//        return b.toString();
//    }
}
