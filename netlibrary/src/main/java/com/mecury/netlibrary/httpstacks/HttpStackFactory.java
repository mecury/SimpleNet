package com.mecury.netlibrary.httpstacks;

import android.os.Build;

/**
 * Created by 海飞 on 2016/8/5.
 * 根据api版本选择HttpClient或者HttpURlConnection
 */
public final class HttpStackFactory {

    private static final int GINGERBREAD_SDK_NUM = 9;

    public static HttpStack createHttpStack(){
        int runtimeSDKApi = Build.VERSION.SDK_INT;
        if (runtimeSDKApi >= GINGERBREAD_SDK_NUM){
            return new HttpURLConnStack();
        }
        return new HttpClientStack();
    }
}
