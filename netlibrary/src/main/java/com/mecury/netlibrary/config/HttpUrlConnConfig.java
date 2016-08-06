package com.mecury.netlibrary.config;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by 海飞 on 2016/8/5.
 */
public class HttpUrlConnConfig extends HttpConfig {

    private static HttpUrlConnConfig sConfig = new HttpUrlConnConfig();

    private SSLSocketFactory mSslSocketFactory = null;
    private HostnameVerifier mHostnameVerifier = null;

    private HttpUrlConnConfig(){

    }

    public static HttpUrlConnConfig getConfig(){
        return sConfig;
    }

    /**
     * 配置https请求的SSLSocketFactory与HostnameVerifier
     * @param sslSocketFactory
     * @param hostnameVerifier
     */
    public void setHttpsConfig(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier){
        mSslSocketFactory = sslSocketFactory;
        mHostnameVerifier = hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier(){
        return mHostnameVerifier;
    }

    public SSLSocketFactory getSslSocketFactory(){
        return mSslSocketFactory;
    }
}






















