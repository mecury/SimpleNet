package com.mecury.netlibrary.core;

import android.os.Handler;
import android.os.Looper;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;

import java.util.concurrent.Executor;

/**
 * Created by 海飞 on 2016/8/5.
 * 请求结果投递类，将请求结果投递给UI线程
 */
public class ResponseDelivery implements Executor{

    Handler mResponseHandler = new Handler(Looper.getMainLooper());

    /**
     * 处理请求结果，将其执行在UI线程
     * @param request
     * @param response
     * @return
     */
    public void deliveryResponse(final Request<?> request, final Response response){
        Runnable respRunnable = new Runnable() {
            @Override
            public void run() {
                request.deliveryResponse(response);
            }
        };

        execute(respRunnable);
    }

    @Override
    public void execute(Runnable runnable) {
        mResponseHandler.post(runnable);
    }
}
