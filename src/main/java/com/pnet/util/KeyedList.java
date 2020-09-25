package com.pnet.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.ArrayList;

@RequiredArgsConstructor
public class KeyedList<K, V extends Keyed> {
    @Delegate
    private final ArrayList<V> list=new ArrayList<>();
    //private final OnGetKey<K, V> onGetKey;

    public V getOrDefault(K key, V defaultValue) {
        int i = indexOfKey(key);
        if (i >= 0) return list.get(i);
        return defaultValue;
    }

    private int indexOfKey(K key) {
        for (int i = 0; i < list.size(); i++) {
            V v = list.get(i);
            if (v.getKey() == key)
                return i;

        }
        return -1;
    }

    public void moveToFirst(K key) {
        list.add(0, list.remove(indexOfKey(key)));
    }

    public boolean containsKey(K key) {
        return indexOfKey(key)>=0;
    }

    public void put(K key, V value) {
        list.add(value);
    }
}
