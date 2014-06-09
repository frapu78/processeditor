/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author fel
 */
public class ExtArrayStore extends ExtFormSimpleComponent {
    private static final String XTYPE = "arraystore";

    private JSONArray dataEntries = new JSONArray();
    private JSONArray fields = new JSONArray();

    ExtArrayStore() throws JSONException {
        super();
        this.put(ExtJSProperty.AUTO_DESTROY.getName(), true);
        this.put(ExtJSProperty.ID_INDEX.getName(), 0);
        this.put("fields", fields);
        this.put("data", dataEntries);
    }

    void setFields( JSONArray fields ) throws JSONException {
        this.fields = fields;
        this.put("fields", fields);
    }

    void setDataEntries( JSONArray data ) throws JSONException {
//        dataEntries = new LinkedList<String>();
//        for (List dataLine : data) {
//            StringBuffer buffer = new StringBuffer();
//            buffer.append("[");
//            Iterator iterator = dataLine.iterator();
//            while (iterator.hasNext()) {
//                Object item = iterator.next();
//                if (item instanceof String) {
//                    buffer.append("'").append((String) item).append("'");
//                } else {
//                    buffer.append(item);
//                }
//                if (iterator.hasNext()) {
//                    buffer.append(",");
//                }
//            }
//            buffer.append("]");
//            dataEntries.add(buffer.toString());
//        }
        this.dataEntries = data;
        this.put("data", dataEntries);
    }

    public void addField( String field ) {
        this.fields.put(field);
    }

    public void addDataEntry( JSONArray entry ) {
        this.dataEntries.put(entry);
    }

//    @Override
//    public String getPropertyJSON() {
//        StringBuffer b = new StringBuffer(super.getPropertyJSON());
//
//        b.append("fields:[");
//        Iterator<String> it = this.fields.iterator();
//        while ( it.hasNext() ) {
//            b.append("\"");
//            b.append(it.next());
//            b.append("\"");
//            if ( it.hasNext() )
//                b.append(",");
//        }
//
//        b.append("],");
//
//        b.append("data: [");
//        it = this.dataEntries.iterator();
//        while ( it.hasNext() ) {
//            b.append(it.next());
//            if ( it.hasNext() )
//                b.append(",");
//        }
//        b.append("],");
//
//        return b.toString();
//    }

    @Override
    protected String getXType() {
        return XTYPE;
    }
}
