package com.dw.merchant.weixin;

import android.util.Log;
import android.util.Xml;

import com.dw.merchant.Constants;
import com.dw.merchant.util.DateTimeUtils;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.NameValuePair;

/**
 * @Author: lvyongxu
 * @Date: 15:27 2015/12/15
 */
public class WeiXinUtils {

    private static final String TAG = WeiXinUtils.class.getSimpleName();

    /**
     * 生成一个随机字数符串（32位）
     *
     * @return
     */
    public static String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
                .getBytes());
    }

    /**
     * 生成商家自己的订单号（20位）
     *
     * @param length 随机字符串的长度 (10)
     * @return 订单号（10位随机数加10位时间戳）
     */
    public static String genOutTradeNo(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString() + DateTimeUtils.getSecond();
    }

    /**
     * 根据微信支付接口要求,生成签名字符串
     *
     * @param params
     * @return
     */
    public static String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);
        String signString = MD5.getMessageDigest(sb.toString().getBytes())
                .toUpperCase(Locale.CHINA);
        Log.e(TAG, "signString===" + signString);
        return signString;
    }

    /**
     * 将List集合中的NameValuePair参数拼接成xml格式的字符串
     *
     * @param params
     * @return
     */
    public static String listToXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        int size = params.size();
        for (int i = 0; i < size; i++) {
            sb.append("<" + params.get(i).getName() + ">");
            sb.append(params.get(i).getValue());
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 将服务器返回的xml格式数据的字符串转换成Map集合
     *
     * @param result
     * @return
     */
    public static Map<String, String> decodeXmlToMap(String result) {
        Map<String, String> xml = new HashMap<String, String>();
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(result));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("xml".equals(nodeName) == false) {
                            // 实例化对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
