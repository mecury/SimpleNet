package com.mecury.netlibrary.requests;

import com.mecury.netlibrary.base.Request;
import com.mecury.netlibrary.base.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 海飞 on 2016/8/5.
 */
public class JsonRequest extends Request<JSONObject> {


    /**
     * @param method
     * @param url
     * @param listener
     */
    public JsonRequest(HttpMethod method, String url, RequestListener<JSONObject> listener) {
        super(method, url, listener);
    }

    /**
     * 将Response的结果转为JSONObject
     */
    @Override
    public JSONObject parseResponse(Response response) {
        String jsonString = new String(response.getRawData());
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
