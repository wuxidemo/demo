package com.dw.merchant.zhifubao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.dw.merchant.R;
import com.dw.merchant.activity.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;



public class RefundActivity extends BaseActivity {

    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK5367p+RchoyzKBLXtzLAj/CoiQfgnEUUKRjomxvdwvZkL8TEfie6ZUY2WhVObYVELkup7l7PYUtAZRn8UkUfSDkyhuTL4LV/UDlggbOg68B/s6PnTd6Ldk7XnjY+UfyVnhpUUnHdhtXPIYuj83Hmndqj4xHXiyqC/hRYZBsE11AgMBAAECgYBAXXmX+dHg19hvL30KGDlcsErAix4UmFqqRmzhm7NBsjL174Js2r9nY6av7c8WwySC8UNwL782Ifkwg5h/8KBTCtQgvPYapF8PSyD1/cHzF6Tcb32XoZz7/uypnVNVt6kPraTGfXa0jDe+CRCGk09vmnranV/Mev8U/yJ9sIp62QJBANtYuQJYodJuZLC+qld9vZrrsGHD3FYoBKNMOq6gTm7RLX+Tihb9Rithds54AoifHCXtBJfeiupYAOp9Hu9SROMCQQDLn2MbU5koRV3Q8XxmKITIVw5Brq11+rRIMrjilws8JyDQOTOuN3Z29lqw9S4teFkciIxKIHqYpmnPgZpCzQvHAkB82LUFJtmEYp0hFIT0I3emE/xiyQ5CY6iwIZVNC6VY4eqZsKpqh2JHEsSCpEAc7yMgWxXAM0SyOcDbtrfC0/qtAkB50M2yoG2k+PKqOH3qg90EGYiu5LhjN2u5MZcH/8K55tKrnzz6wbV+b91LtjI9A52UA2CiTBHr1srAWFGYGyErAkEAhBw+z7JpGhS8fxLeoTxVrajqpeV31paHywb2fYTotT5eHPoH7qbG8YeGVcCv7YHygxjhY0P7WAcaCtpeIS9nww==";
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    String data = msg.getData().get("data").toString();
                    try {
                        JSONObject json = new JSONObject(data);
                        JSONObject content = new JSONObject(json.get("alipay_trade_precreate_response").toString());
                        String code = content.get("code").toString();

                        if (code.equals("10000")) {

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("gl", new String(data));
                    break;
            }
        }
    };
    private String scene = "bar_code";
    private String app_id = "2015110600707517";
    //接口名称
    private String method = "alipay.trade.refund";
    private String charset = "utf-8";
    //接口版本号
    private String version = "1.0";
    //    卖家支付宝用户ID,如果该值为空，则默认为商户签约账号对应的支付宝用户ID	2088102146225135
    private String seller_id = "2088012896752633";
    private String out_trade_no;
    private String now_time;
    private String notify_url = "http://ts.do-wi.cn/nsh/appapi/testalipaycallback/";
    private TextView tv_status;
    private String body = "";//订单名称

    @Override
    protected int addLayout() {
        return R.layout.activity_alipay_passive_scan_pay;
    }

    @Override
    protected void initLayout() {

        tv_status = (TextView) findViewById(R.id.tv_status);

        Intent intent = getIntent();
        String total = intent.getStringExtra("total");
        body = getIntent().getStringExtra("merchant_name");

        nowTime();

        out_trade_no = getOutTradeNo();

        Log.e("gl", "out_trade_no==" + out_trade_no);
        refund("2015113021001004720292979316", "0.01",now_time);

    }

    public void nowTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        now_time = df.format(date);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void refund(String out_trade_no, String total_amount, String timestamp) {

        // 订单
        String orderInfo = getOrderInfo(out_trade_no, total_amount, timestamp);

        Log.e("gl", "orderInfo===" + orderInfo);
        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.e("gl", "sign===" + sign);

        // 完整的符合支付宝参数规范的订单信息
        String payInfo = orderInfo + "&sign=\"" + sign;
        Log.e("gl", "url==" + payInfo);

        //请求参数键-值对
        final Map<String, String> params = new HashMap();
        params.put("app_id", app_id);
        params.put("biz_content", refundSuccess(out_trade_no, total_amount).toString());
        params.put("charset", charset);
        params.put("method", method);
        params.put("sign_type", "RSA");
        params.put("timestamp", timestamp);
        params.put("sign", sign);
        params.put("version", version);
        params.put("notify_url", notify_url + out_trade_no);


        new Thread() {
            @Override
            public void run() {
                try {
                    String urlPath = "https://openapi.alipay.com/gateway.do";
                    InputStream is = NetTool.getInputStreamByPost(urlPath, params, "UTF-8");

                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", new String(NetTool.readStream(is)));
                    message.setData(bundle);
                    message.what = 1001;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * create the order info. 创建退款
     */
    public JSONObject refundSuccess(String out_trade_no, String total_amount) {

        JSONObject json = new JSONObject();
        try {
            json.put("trade_no", out_trade_no);   // 订单总金额
            json.put("refund_amount", total_amount);  // 商户订单号

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("gl", "json===" + json);
        return json;
    }

    public String getQrCodePayInfoTest(String auth_code, String seller_id, String total_amount, String subject) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time_expire = sdf.format(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"out_trade_no\":\"" + getOutTradeNo() + "\",");
        sb.append("\"total_amount\":\"" + total_amount + "\",\"discountable_amount\":\"0.00\",");
        sb.append("\"subject\":\"" + subject + "\",\"body\":\"test\",");
        sb.append("\"goods_detail\":[{\"goods_id\":\"apple-01\",\"goods_name\":\"ipad\",\"goods_category\":\"7788230\",\"price\":\"88.00\",\"quantity\":\"1\"},{\"goods_id\":\"apple-02\",\"goods_name\":\"iphone\",\"goods_category\":\"7788231\",\"price\":\"88.00\",\"quantity\":\"1\"}],");
        sb.append("\"operator_id\":\"op001\",\"store_id\":\"pudong001\",\"terminal_id\":\"t_001\",");
        sb.append("\"time_expire\":\"" + time_expire + "\"}");
        System.out.println(sb.toString());

        return sb.toString();
    }

    /**
     * create the order info. 创建订单信息
     */
    public String getOrderInfo(String out_trade_no, String total_amount, String timestamp) {

        // 开发者的AppId
        String orderInfo = "app_id=" + app_id;
        // 接口名称
        orderInfo += "&biz_content=" + refundSuccess(out_trade_no, total_amount).toString();
        // 参数字符编码
        orderInfo += "&charset=" + charset;
        // 接口名称
        orderInfo += "&method=" + method;
        //接口异步通知url
        orderInfo += "&notify_url=" + notify_url + out_trade_no;
        // 签名类型
        orderInfo += "&sign_type=" + "RSA";
        // 时间戳
        orderInfo += "&timestamp=" + timestamp;
        //接口版本号
        orderInfo += "&version=" + version;
        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        Log.e("gl", "getOutTradeNo===" + key);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

}
