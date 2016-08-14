/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mecury.originokhttp.main.java.okhttp3;

import com.mecury.originokhttp.main.java.okhttp3.internal.NamedRunnable;
import com.mecury.originokhttp.main.java.okhttp3.internal.cache.CacheInterceptor;
import com.mecury.originokhttp.main.java.okhttp3.internal.connection.ConnectInterceptor;
import com.mecury.originokhttp.main.java.okhttp3.internal.connection.StreamAllocation;
import com.mecury.originokhttp.main.java.okhttp3.internal.http.BridgeInterceptor;
import com.mecury.originokhttp.main.java.okhttp3.internal.http.CallServerInterceptor;
import com.mecury.originokhttp.main.java.okhttp3.internal.http.RealInterceptorChain;
import com.mecury.originokhttp.main.java.okhttp3.internal.http.RetryAndFollowUpInterceptor;
import com.mecury.originokhttp.main.java.okhttp3.internal.platform.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * requst在Call中真正执行的地方
 */
final class RealCall implements Call {
    private final OkHttpClient client; //总Client
    private final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;//=======

    // Guarded by this.
    private boolean executed; //是否执行标志位

    /**
     * The application's original request unadulterated by redirects or auth headers.
     */
    //原始的请求通过重定向或者自定headers  unadulterated（8.7：就是一个request的载体）
    Request originalRequest;

    protected RealCall(OkHttpClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client);
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * 执行方法
     *
     * @return Response
     */
    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        try {
            //将请求分发执行，将call加入到 保存运行中的同步队列中
            client.dispatcher().executed(this); // TODO: 2016/8/7 同步队列？异步？
            //返回结果由拦截器返回
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } finally {
            //回调分发完成的接口
            client.dispatcher().finished(this);
        }
    }

    synchronized void setForWebSocket() {
        if (executed) throw new IllegalStateException("Already Executed");
        this.retryAndFollowUpInterceptor.setForWebSocket(true);
    }

    /**
     * 处理返回的Response，包括失败和成功的返回
     */
    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        //执行AsyncCall
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override
    public void cancel() {
        retryAndFollowUpInterceptor.cancel();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return retryAndFollowUpInterceptor.isCanceled();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    // We are a final type & this saves clearing state.
    @Override
    public RealCall clone() {
        return new RealCall(client, originalRequest);
    }

    StreamAllocation streamAllocation() {
        return retryAndFollowUpInterceptor.streamAllocation();
    }

    final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        private AsyncCall(Callback responseCallback) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    signalledCallback = true;
                    responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
                    responseCallback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                    Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
                } else {
                    responseCallback.onFailure(RealCall.this, e);
                }
            } finally {
                client.dispatcher().finished(this);
            }
        }
    }

    /**
     * Returns a string that describes this call. Doesn't include a full URL as that might contain
     * sensitive information.
     */
    private String toLoggableString() {
        String string = retryAndFollowUpInterceptor.isCanceled() ? "canceled call" : "call";
        return string + " to " + redactedUrl();
    }

    String redactedUrl() {
        return originalRequest.url().redact().toString();
    }


    /**
     * 通过各种拦截器对于Response进行处理
     */
    private Response getResponseWithInterceptorChain() throws IOException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        //
        interceptors.add(retryAndFollowUpInterceptor);
        //
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        //
        interceptors.add(new CacheInterceptor(client.internalCache()));
        //
        interceptors.add(new ConnectInterceptor(client));

        if (!retryAndFollowUpInterceptor.isForWebSocket()) {
            interceptors.addAll(client.networkInterceptors());
        }

        interceptors.add(new CallServerInterceptor(
                retryAndFollowUpInterceptor.isForWebSocket()));

        Interceptor.Chain chain = new RealInterceptorChain(
                interceptors, null, null, null, 0, originalRequest);
        return chain.proceed(originalRequest);
    }
}
