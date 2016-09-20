package com.dw.merchant.util;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @Author: lvyongxu
 * @Date: 10:48 2015/10/13
 */
public class HttpClient {

    private static final String TAG = HttpClient.class.getSimpleName();

    //    private static final String BASE_URL = "http://soft.do-wi.cn/nsh/appapi/";
    private static final String BASE_URL = "http://ts.do-wi.cn/nsh/appapi/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), responseHandler);
    }

    public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.e(TAG, "Url===" + BASE_URL + relativeUrl);
        return BASE_URL + relativeUrl;
    }
}
