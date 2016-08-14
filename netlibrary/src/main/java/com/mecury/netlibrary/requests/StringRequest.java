package com.mecury.netlibrary.requests;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;

/**
 * Created by 海飞 on 2016/8/6.
 */
public class StringRequest extends Request<String> {

    public StringRequest(HttpMethod method, String url, RequestListener<String> listener){
        super(method, url, listener);
    }

    @Override
    public String parseResponse(Response response) {
        return new String(response.getRawData());
    }
}
