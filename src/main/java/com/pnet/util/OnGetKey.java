package com.pnet.util;

public interface OnGetKey<K, V> {
    K fun(V value);
}
