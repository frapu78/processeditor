/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.multipart;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Container class for MultiPartItems
 * @author Felix
 */
public class MultiPartObject {
    private List<MultiPartItem> items = new LinkedList<MultiPartItem>();

    public void addItem(MultiPartItem item) {
        items.add(item);
    }

    public List<MultiPartItem> getItems() {
        return this.items;
    }

    public MultiPartItem getItemByName(String name) {
        for (MultiPartItem i : this.items) {
            if (i.getDispositionAttribute("name") != null && i.getDispositionAttribute("name").equals(name))
                return i;
        }

        return null;
    }
    
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        for ( MultiPartItem i : this.items )
            if (i.getDispositionAttribute("name") != null)
                keys.add( i.getDispositionAttribute("name") );
        return keys;
    }
}
