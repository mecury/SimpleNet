package com.mecury.netlibrary.core;

import com.mecury.netlibrary.httpstacks.HttpStack;

/**
 * Created by 海飞 on 2016/8/5.
 */
public class SimpleNet {

    /**
     * 创建一个请求队列，NetworkExecutor数量为默认的数量
     */
    public static RequestQueue newRequestQueue(){
        return newRequestQueue(RequestQueue.DEFAULT_CORE_NUMS);
    }

    /**
     * 创建一个请求队列，NetWorkExecutor数量为coreNums
     * @param coreNums
     * @return
     */
    public static RequestQueue newRequestQueue(int coreNums){
        return newRequestQueue(coreNums, null);
    }

    /**
     * 创建一个请求队列，NetWorkExecutor数量为coreNums
     * @param coreNums
     * @param httpStack 网络执行者
     * @return
     */
    public static RequestQueue newRequestQueue(int coreNums, HttpStack httpStack){
        RequestQueue queue = new RequestQueue(Math.max(0, coreNums), httpStack);
        queue.start();
        return queue;
    }
}
