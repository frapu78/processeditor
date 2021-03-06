/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author tmi
 */
public class ExtPanel extends ExtFormSimpleComponent {

    private static final String XTYPE = "panel";

    private JSONArray items;

    public ExtPanel() throws JSONException {
        super();
        items = new JSONArray();
        this.put("items", this.items);
    }

    @Override
    protected String getXType() {
        return XTYPE;
    }

//    @Override
//    public String getPropertyJSON() {
//        StringBuffer buffer = new StringBuffer(super.getPropertyJSON());
//        buffer.append("items:").append(items).append(",");
//        return buffer.toString();
//    }

    public void addItem(ExtFormComponent item) {
        if (item != null) {
            items.put(item);
        }
    }

    public void addItems(List<? extends ExtFormComponent> items) {
        for (ExtFormComponent item : items) {
            addItem(item);
        }
    }
}
