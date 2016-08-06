package com.mecury.netlibrary.base;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by 海飞 on 2016/8/4.
 * 请求结果类，继承自BasicHttpResponse,将结果存储在rawData中
 */
public class Response extends BasicHttpResponse {

    public byte[] rewData = new byte[0];


    public Response(ProtocolVersion ver, int code, String reason) {
        super(ver, code, reason);
    }

    public Response(StatusLine statusline) {
        super(statusline);
    }

    @Override
    public void setEntity(HttpEntity entity) {
        super.setEntity(entity);
        rewData = entityToByte(entity);
    }

    public byte[] getRawData(){
        return rewData;
    }

    /**
     * 得到状态码
     */
    public int getStatusCode(){
        return getStatusLine().getStatusCode();
    }

    public String getMessage(){
        return getStatusLine().getReasonPhrase();
    }

    public byte[] entityToByte(HttpEntity entity){
        try {
            return EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
















