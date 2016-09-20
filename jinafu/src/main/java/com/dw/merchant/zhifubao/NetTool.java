package com.dw.merchant.zhifubao;

/**
 * Created by Acer on 2015/11/9.
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

public class NetTool {

    /**
     * @param urlPath  请求路径
     * @param params   Map中key为请求参数，value为请求参数的值
     * @param encoding 编码方式
     * @return
     * @throws Exception
     */

    //通过post向服务器端发送数据，并获得服务器端输出流(alipay)
    public static InputStream getInputStreamByPost(String urlPath, Map<String, String> params, String encoding) throws Exception {
        String data = buildQuery(params, encoding);

        byte[] postData = data.getBytes(encoding);

        URL url = new URL(urlPath);
        //打开连接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);
//        conn.setRequestProperty( "charset", "utf-8");
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setUseCaches(false);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        //将请求参数数据向服务器端发送
        dos.write(postData);
        dos.flush();
        dos.close();
        if (conn.getResponseCode() == 200) {
            //获得服务器端输出流
            return conn.getInputStream();
        }
        return null;
    }

    //方法重写(weixin)
    //通过post向服务器端发送数据，并获得服务器端输出流
    public static InputStream getInputStreamByPost(String urlPath, String params, String encoding) throws Exception {

        byte[] postData = params.getBytes(Charset.forName("UTF-8"));

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
            //获得服务器端输出流
            return conn.getInputStream();
        }
        return null;
    }


    public static String readContentFromGet() throws IOException {

        String fileContent = "";

        // 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
        String getURL = "http://ts.do-wi.cn/nsh/appapi/alipay";
        URL getUrl = new URL(getURL);
        // 根据拼凑的URL，打开连接，URL.openConnection函数会根据URL的类型，
        // 返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) getUrl
                .openConnection();
        // 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
        // 服务器
        conn.connect();
        // 取得输入流，并使用Reader读取
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        System.out.println("=============================");
        System.out.println("Contents of get request");
        System.out.println("=============================");
        String lines;
        while ((lines = reader.readLine()) != null) {
            System.out.println(lines);
            fileContent += lines;
        }
        reader.close();
        // 断开连接
        conn.disconnect();
        System.out.println("=============================");
        System.out.println("Contents of get request ends");
        System.out.println("=============================");

        return fileContent;
    }


    //通过输入流获得字节数组
    public static byte[] readStream(InputStream is) throws Exception {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        is.close();
        return bos.toByteArray();
    }


    public static String buildQuery(Map<String, String> params, String charset) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Map.Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (hasParam) {
                query.append("&");
            } else {
                hasParam = true;
            }

            query.append(name).append("=").append(URLEncoder.encode(value, charset));
        }

        return query.toString();
    }

}