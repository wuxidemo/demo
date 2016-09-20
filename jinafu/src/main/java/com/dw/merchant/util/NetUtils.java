package com.dw.merchant.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @Author: lvyongxu
 * @Date: 14:45 2015/10/13
 */
public class NetUtils {

//    /**
//     * 判断网络情况
//     *
//     * @param context 上下文
//     * @return false 表示没有网络 true 表示有网络
//     */
//    public static boolean isNetworkAvalible(Context context) {
//        // 获得网络状态管理器
//        ConnectivityManager connectivityManager = (ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (connectivityManager == null) {
//            return false;
//        } else {
//            // 建立网络数组
//            NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
//
//            if (net_info != null) {
//                for (int i = 0; i < net_info.length; i++) {
//                    // 判断获得的网络状态是否是处于连接状态
//                    if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isConnected()) {
            return false;
        }
        return true;
    }

    /**
     * 通过post方法向服务器端发送数据请求，并获得服务器端返回的数据
     *
     * @param urlPath
     * @param params
     * @return
     * @throws Exception
     */
    public static String getResponseByPost(String urlPath, String params) {

        byte[] postData = params.getBytes(Charset.forName("UTF-8"));
        try {
            URL url = new URL(urlPath);
            //打开连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            //将请求参数数据向服务器端发送
            dos.write(postData);
            dos.flush();
            dos.close();
            if (conn.getResponseCode() == 200) {
                //获得服务器端输出流,并转换成string
                return inputStreamToString(conn.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将输入流转换成String
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    private static String inputStreamToString(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return new String(outputStream.toByteArray());
    }

    /**
     * 获取指定URL的响应字符串
     *
     * @param urlString
     * @return
     */
    private String getURLResponse(String urlString) {
        HttpURLConnection conn = null; //连接对象
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(urlString); //URL对象
            conn = (HttpURLConnection) url.openConnection(); //使用URL打开一个链接
            conn.setDoInput(true); //允许输入流，即允许下载
            conn.setDoOutput(true); //允许输出流，即允许上传
            conn.setUseCaches(false); //不使用缓冲
            conn.setRequestMethod("GET"); //使用get请求
            is = conn.getInputStream();   //获取输入流，此时才真正建立链接
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(isr);
            String inputLine = "";
            while ((inputLine = bufferReader.readLine()) != null) {
                resultData += inputLine + "\n";
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return resultData;
    }


}
