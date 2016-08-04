package com.mecury.netlibrary.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 海飞 on 2016/8/4.
 * 网络请求类， 注意get和delete不能传递请求参数，因为其请求的性质所致,用户可以将参数构建到url后传递进来到Request中
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    public static enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        /**Http request type*/
        private String mHttpMethod = "";

        HttpMethod(String method) {
            mHttpMethod = method;
        }

        @Override
        public String toString() {
            return mHttpMethod;
        }
    }

    //优先级枚举
    public static enum Priority{
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    /**
     * Default encoding for POST or PUT
     */
    public static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * Default Content-type
     */
    public final static String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 请求序列号
     */
    protected int mSericalNum = 0;

    /**
     * 优先级默认为Normal
     */
    protected Priority mPriority = Priority.NORMAL;

    /**
     * 是否取消请求
     */
    protected boolean isCancel = false;

    /**
     * 该请求是否应该缓存
     */
    private boolean mShouldCache = true;

    /**
     * 请求Listener
     */
    protected RequestListener<T> mRequestListener;

    /**
     * 请求的url
     */
    private String mUrl;

    /**
     * 请求的方法
     */
    HttpMethod mHttpMethod = HttpMethod.GET;

    /**
     * 请求的header
     */
    private Map<String, String> mHeaders = new HashMap<String, String>();

    /**
     * 请求参数
     */
    private Map<String, String> mBodyParams = new HashMap<String, String>();

    /**
     *
     * @param method
     * @param url
     * @param listener
     */
    public Request(HttpMethod method, String url, RequestListener<T> listener){
        mHttpMethod = method;
        mUrl = url;
        mRequestListener = listener;
    }

    public void addHeader(String name, String value){
        mHeaders.put(name, value);
    }

    /**
     * 从原生的网络请求中解析结果
     */
    public abstract T pareseResponse(Response response);

    /**
     * 处理Response, 该方法运行在UI线程
     * @param response
     */
    public final void deliveryResponse(Response response){

    }







    /**
     * 网络请求Listener
     * @param <T> 请求的response类型
     */
    public static interface RequestListener<T> {
        
    }
}




















