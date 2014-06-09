/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.collections4.map.MultiKeyMap;


/**
 *
 * @author Uwe
 */
public class Relation<K,V>  extends MultiKeyMap  {

    private HashMap<K,V> view1 = new HashMap<K, V>();
    private HashMap<K,V> view2 = new HashMap<K, V>();


    public boolean containsKeys(K key1, K key2) {
        return containsKey(key1, key2);
    }

    public boolean containsKey1(K key1) {
        return view1.containsKey(key1);
    }

    public boolean containsKey2(K key2) {
        return view2.containsKey(key2);
    }


    
    public V put(K[] key, V value) {       
           view1.put(key[0], value);
           view2.put(key[1], value);

        return (V) super.put(key[0],key[1], value);
    }

    public V put(Tupel value) {
        Object[] keys = {value.getKey1(),value.getKey2()};
        K[] res;
        res = (K[]) keys;
        return put(res, (V) value);
    }

//    @Override
    public boolean remove(Object key1, Object key2) {
        view1.remove(key1);
        view2.remove(key2);        
        
        return super.removeAll(key1, key2);               
    }

    public V getWithKey1(K key1) {
        return view1.get(key1);
    }

    public V getWithKey2(K key2) {
        return view2.get(key2);
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

}
