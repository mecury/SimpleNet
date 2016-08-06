package com.mecury.netlibrary.core;

import android.util.Log;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;
import com.mecury.netlibrary.cache.Cache;
import com.mecury.netlibrary.cache.LruMemCache;
import com.mecury.netlibrary.httpstacks.HttpStack;

import java.util.concurrent.BlockingQueue;

/**
 * Created by 海飞 on 2016/8/5.
 * 网络请求Executor, 继承自Thread， 从网络请求队列中循环读取请求并且执行
 */
public class NetworkExecutor extends Thread{

    /**
     * 网络请求队列
     */
    private BlockingQueue<Request<?>> mRequestQueue;

    /**
     * 网络请求栈
     */
    private HttpStack mHttpStack;

    /**
     * 结果分发器，将结果投递到主线程
     */
    private static ResponseDelivery mResponseDelivery = new ResponseDelivery();

    /**
     * 请求缓存
     */
    private static Cache<String, Response> mReqCache = new LruMemCache();

    /**
     * 是否停止
     */
    private boolean isStop = false;

    public NetworkExecutor(BlockingQueue<Request<?>> queue, HttpStack httpStack){
        mRequestQueue = queue;
        mHttpStack = httpStack;
    }

    @Override
    public void run() {
        try{
            while(!isStop){
                final Request<?> request = mRequestQueue.take();
                if (request.isCanceled()){
                    Log.d("### ", "### 取消执行了");
                    continue;
                }
                Response response = null;
                if (isUseCache(request)){
                    //从缓存中取
                    response = mReqCache.get(request.getUrl());
                }else{
                    //从网络上获取数据
                    response = mHttpStack.performRequest(request);
                    //如果该需求需要缓存，那么请求成功则缓存到mResponseCache中
                    if (request.shouldCache() && isSuccess(response)){
                        mReqCache.put(request.getUrl(), response);
                    }
                }

                mResponseDelivery.deliveryResponse(request, response);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isSuccess(Response response){
        return response != null && response.getStatusCode() == 200;
    }

    public boolean isUseCache(Request<?> request){
        return request.shouldCache() && mReqCache.get(request.getUrl()) != null;
    }

    public void quit(){
        isStop = true;
        interrupt();
    }
}









































