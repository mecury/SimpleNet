package com.mecury.netlibrary.cache;

/**
 * Created by 海飞 on 2016/8/6.
 */
public interface Cache<K, V> {

    public V get(K key);

    public void put(K key, V value);

    public void remove(K key);
}
