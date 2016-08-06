package com.mecury.netlibrary.cache;

import android.util.LruCache;

import com.mecury.netlibrary.base.Response;

/**
 * Created by 海飞 on 2016/8/6.
 * 将请求结果保存到缓存中
 */
public class LruMemCache implements Cache<String, Response> {

    /**
     * Response缓存
     */
    private LruCache<String, Response> mResponseLruCache;

    public LruMemCache(){
        //计算可使用的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);

        //取最大内存的8分之一作为缓存
        final int cacheSize = maxMemory / 8;

        mResponseLruCache = new LruCache<String, Response>(cacheSize){
            @Override
            protected int sizeOf(String key, Response value) {
                //返回缓存对象的大小
                return value.rewData.length/1024;
            }
        };
    }

    @Override
    public Response get(String key) {
        return mResponseLruCache.get(key);
    }

    @Override
    public void put(String key, Response value) {
        mResponseLruCache.put(key, value);
    }

    @Override
    public void remove(String key) {
        mResponseLruCache.remove(key);
    }
}
