package com.mecury.netlibrary.httpstacks;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;

/**
 * Created by 海飞 on 2016/8/5.
 * 执行网络的请求接口
 */
public interface HttpStack {

    /**
     * 执行网络请求的接口
     * @param request 待执行的请求
     * @return
     */
    public Response performRequest(Request<?> request);
}
