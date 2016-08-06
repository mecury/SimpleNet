package com.mecury.netlibrary.httpstacks;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;
import com.mecury.netlibrary.config.HttpUrlConnConfig;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by 海飞 on 2016/8/5.
 * 对于API 9 以上使用HttpURLConnection执行网络请求的HttpStack
 */
public class HttpURLConnStack implements HttpStack{

    /**
     * 配置Https
     */
    HttpUrlConnConfig mConfig = HttpUrlConnConfig.getConfig();

    /**
     * 回调方法，目测在NetWorkExecutor中被调用
     */
    @Override
    public Response performRequest(Request<?> request) {
        HttpURLConnection urlConnection = null;
        try{
            //设置请求参数
            urlConnection = createUrlConnection(request.getUrl());
            //设置headers
            setRequestHeaders(urlConnection, request);
            //设置Body参数
            setRequestParams(urlConnection, request);
            //https配置
            configHttps(request);
            return fetchResponse(urlConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return null;
    }

    /**
     * 创建URlConnection，并设置一些参数
     * @param url
     * @return
     */
    private HttpURLConnection createUrlConnection(String url) throws IOException {
        URL newURL = new URL(url);
        URLConnection urlConnection =newURL.openConnection();
        urlConnection.setConnectTimeout(mConfig.connTimeOunt);
        urlConnection.setReadTimeout(mConfig.soTimeOut);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        return (HttpURLConnection) urlConnection;
    }

    /**
     * 设置请求头部
     * @param conn
     * @param request
     * @return
     */
    private void setRequestHeaders(HttpURLConnection conn, Request<?> request){
        Set<String> headersKeys = request.getHeaders().keySet();
        for (String headerName : headersKeys){
            conn.addRequestProperty(headerName, request.getHeaders().get(headerName));
        }
    }

    /**
     * 设置body参数
     * @param connection
     * @param request
     * @return
     */
    protected void setRequestParams(HttpURLConnection connection, Request<?> request) throws IOException {
        Request.HttpMethod method = request.getHttpMethod();
        connection.setRequestMethod(method.toString());

        //add params.如果body不为空，证明有请求数据
        byte[] body = request.getBody();
        if (body != null){
            //enable output
            connection.setDoOutput(true);
            //set content type
            connection.addRequestProperty(Request.HEADER_CONTENT_TYPE, request.getBodyContentType());
            //write params data to connection
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.write(body);
            dataOutputStream.close();
        }
    }

    /**
     * 如果是Https请求，则使用用户配置的SSlSocketFactory
     * @param request
     */
    private void configHttps(Request<?> request){
        if (request.isHttps()){
            SSLSocketFactory sslFactory = mConfig.getSslSocketFactory();
            //配置Https
            if (sslFactory != null){
                HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);
                HttpsURLConnection.setDefaultHostnameVerifier(mConfig.getHostnameVerifier());
            }
        }
    }

    /**
     * 返回通过HttpURLConnection请求得到的response
     * @param connection
     * @return
     */
    private Response fetchResponse(HttpURLConnection connection) throws IOException {
        //initialize HttpResponse with data from the HttpURLConnection
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1){
            throw new IOException("Could not retrieve response code from HttpUrlConnection");
        }

        //状态行数据
        StatusLine responseStatus = new BasicStatusLine(protocolVersion, connection.getResponseCode()
                , connection.getResponseMessage());
        //构建response
        Response response = new Response(responseStatus);

        response.setEntity(entityFromURLConnection(connection));
        addHeadersToResponse(response, connection);
        return response;
    }

    /**
     * 执行HTTP请求之后获取到其数据流，即返回请求结果的流
     * @param connection
     * @return
     */
    private HttpEntity entityFromURLConnection(HttpURLConnection connection){
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            inputStream = connection.getErrorStream();
        }

        // TODO: 2016/8/5 GZIP
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());

        return entity;
    }

    /**
     * 将header添加到Response中
     * @param response
     * @param connection
     * @return
     */
    private void addHeadersToResponse(BasicHttpResponse response, HttpURLConnection connection){
        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()){
            if (header.getKey() != null){
                Header h = new BasicHeader(header.getKey(),header.getValue().get(0));
                response.addHeader(h);
            }
        }
    }
}























