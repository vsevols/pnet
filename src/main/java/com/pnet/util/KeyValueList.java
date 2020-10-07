package com.pnet.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.AbstractList;
import java.util.ArrayList;

@JsonIgnoreProperties({"empty"})
@RequiredArgsConstructor
public class KeyValueList<K, V> extends AbstractList<V> {
    @Delegate
    private final ArrayList<V> list=new ArrayList<>();

    public V getOrDefault(K key, V defaultValue) {
        int i = indexOfKey(key);
        if (i >= 0) return list.get(i);
        return defaultValue;
    }

    private int indexOfKey(K key) {
        for (int i = 0; i < list.size(); i++) {
            if (getKey(list.get(i)).equals(key))
                return i;

        }
        return -1;
    }

    protected K getKey(V v) {
        return null;
    }

    public void moveToFirst(K key) {
        list.add(0, list.remove(indexOfKey(key)));
    }

    public boolean containsKey(K key) {
        return indexOfKey(key)>=0;
    }

    public void put(K key, V value, boolean toBeginning) {
        list.add(value);
    }

    public V getByKey(K key) {
        int i = indexOfKey(key);
        if(i<0)return null;
        return get(i);
    }
}
