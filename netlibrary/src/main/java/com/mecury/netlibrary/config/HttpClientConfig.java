package com.mecury.netlibrary.config;


import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * Created by 海飞 on 2016/8/5.
 * 这是针对HttpClientStack执行请求时为Https请求配置SSLSocketFactory
 */
public class HttpClientConfig extends HttpConfig {
    private static HttpClientConfig sConfig = new HttpClientConfig();
    SSLSocketFactory mSslSocketFactory;

    private HttpClientConfig(){

    }

    public static HttpClientConfig getConfig(){
        return sConfig;
    }

    /**
     * 配置https请求的SSLSocketFactory
     */
    public void getHttpsConfig(SSLSocketFactory sslSocketFactory){
        mSslSocketFactory = sslSocketFactory;
    }

    public SSLSocketFactory getSslSocketFactory(){
        return mSslSocketFactory;
    }
}
