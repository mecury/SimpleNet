package com.mecury.netlibrary.httpstacks;

import android.net.http.AndroidHttpClient;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;
import com.mecury.netlibrary.config.HttpClientConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Map;

/**
 * Created by 海飞 on 2016/8/5.
 * api 9以下使用HttpClient执行网络请求
 */
public class HttpClientStack implements HttpStack{

    /**
     * 使用HttpClient执行网络请求时的Https配置
     */
    HttpClientConfig mConfig = HttpClientConfig.getConfig();

    /**
     * HttpClient
     */
    HttpClient mHttpClient = AndroidHttpClient.newInstance(mConfig.userAgent);

    /**
     * 回调方法，目测在NetWorkExecutor中被调用
     * @param 
     * @return 
     */
    @Override
    public Response performRequest(Request<?> request) {
        try{
            HttpUriRequest httpRequest = createHttpRequest(request);
            //添加连接参数
            setConnectionParams(httpRequest);
            //添加header
            addHeaders(httpRequest, request.getHeaders());
            //https设置
            configHttps(request);
            //执行请求
            HttpResponse response = mHttpClient.execute(httpRequest);
            //构建Response
            Response rawResponse = new Response(response.getStatusLine());
            //设置Entity
            rawResponse.setEntity(response.getEntity());
            return rawResponse;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 如果是Https请求，则使用用户配置的SSlSocketFactory
     * @param request
     * @return 
     */
    private void configHttps(Request<?> request){
        SSLSocketFactory sslSocketFactory = mConfig.getSslSocketFactory();
        if (request.isHttps() && sslSocketFactory != null){
            Scheme sch = new Scheme("https", sslSocketFactory, 443);
            mHttpClient.getConnectionManager().getSchemeRegistry().register(sch);
        }
    }

    /**
     * 设置连接参数，这里比较简单，一些优化设置没有写
     */
    private void setConnectionParams(HttpUriRequest httpUriRequest){
        HttpParams httpParams = httpUriRequest.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, mConfig.connTimeOunt);
        //规定的时间内一直没有读取数据就抛出异常
        HttpConnectionParams.setSoTimeout(httpParams, mConfig.soTimeOut);
    }

    /**
     * 根据请求类型创建不同的Http请求
     * @param request
     * @return
     */
    static HttpUriRequest createHttpRequest(Request<?> request){
        HttpUriRequest httpUriRequest = null;
        switch (request.getHttpMethod()){
            case GET:
                httpUriRequest = new HttpGet(request.getUrl());
                break;
            case DELETE:
                httpUriRequest = new HttpDelete(request.getUrl());
                break;
            case POST:
                httpUriRequest = new HttpPost(request.getUrl());
                //设置body
                httpUriRequest.addHeader(Request.HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody((HttpPost)httpUriRequest, request);
                break;
            case PUT:
                httpUriRequest = new HttpPut(request.getUrl());
                //设置body
                httpUriRequest.addHeader(Request.HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody((HttpPut)httpUriRequest, request);
                break;
            default:
                throw new IllegalStateException("Unknown request Method");
        }
        return httpUriRequest;
    }

    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers){
        for (String key : headers.keySet()){
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    /**
     * 将请求参数设置到HttpEntity中
     * @param httpRequest
     * @param request
     */
    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequest httpRequest, Request<?> request){
        byte[] body = request.getBody();
        if (body != null){
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }
}






















