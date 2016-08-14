package com.mecury.netlibrary.requests;

import android.util.Log;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;
import com.mecury.netlibrary.entity.MultipartEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by 海飞 on 2016/8/6.
 *
 * Multipart请求 ( 只能为POST请求 ),该请求可以搭载多种类型参数,比如文本、文件等,但是文件仅限于小文件,否则会出现OOM异常.
 */
public class MultipartRequest extends Request<String>{

    MultipartEntity mMultipartEntity = new MultipartEntity();

    /**
     * @param url
     * @param listener
     */
    public MultipartRequest(String url, RequestListener<String> listener) {
        super(HttpMethod.POST, url, listener);
    }

    public MultipartEntity getMultipartEntity(){
        return mMultipartEntity;
    }

    @Override
    public String getBodyContentType() {
        return mMultipartEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            //将MultipartEntity中的参数写入到bos中
            mMultipartEntity.writeTo(bos);

        } catch (IOException e) {
            Log.e("", "IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    public String parseResponse(Response response) {
        if (response != null && response.getRawData() != null){
            return new String(response.getRawData());
        }
        return "";
    }
}
